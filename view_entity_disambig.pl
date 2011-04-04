#!/usr/local/bin/perl

use DBI;
use Storable;

print "Content-type: text/html\n\n";

$start_time = time();
#print "Script started at time $start_time.<br>";

# Read the standard input (sent by the form):
read(STDIN, $FormData, $ENV{'CONTENT_LENGTH'});
# Get the name and value for each form input:
@pairs = split(/&/, $FormData);
# Then for each name/value pair....
foreach $pair (@pairs) {
    # Separate the name and value:
    ($name, $value) = split(/=/, $pair);
    # Convert + signs to spaces:
    $value =~ tr/+/ /;
    # Convert hex pairs (%HH) to ASCII characters:
    $value =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;
    # Store values in a hash called %FORM:
    $FORM{$name} = $value;
}

$dbh = DBI->connect('dbi:mysql:tlin_freebase;host=localhost', "tlin", "FrWMcUAEJvKcXrJX");
unless (defined $dbh) {
    print "Couldn't connect to db: " . DBI->errstr;
    exit;
}

@qsinfo=split(/\=/, $ENV{'QUERY_STRING'});

#$qsinfo[0] = "entity";
#$qsinfo[1] = "Titanic";

if ($FORM{'entity'}) {
    $entity = $FORM{'entity'};
} elsif (@qsinfo[0] eq "entity") {
    $entity = @qsinfo[1];
    $entity =~ s/\_/\ /g;
    $entity =~ s/\%92/&#146;/g;
} else {
    $entity = "Titanic";
}

if ($FORM{'maxresults'}) {
    $maxresults = $FORM{'maxresults'};
} else {
    $maxresults = 20;
}

if ($FORM{'senses'}) {
    $senses = $FORM{'senses'};
} else {
    $senses = 2;
}

$entity_in_english = $entity;

print <<EOF;
<title>Disambiguating $entity</title>

<script> 
<!--
    function setfocus() { document.f.entity.focus(); }
// -->
</script> 
<body onLoad=setfocus()> 

<form method="POST" name=f>
<center>
Argument 1: <input type=text size=20 name=entity value="$entity"> 
Max \# of senses: 
<select name=senses>
EOF

@allowable_senses = ("2", "3", "4", "5", "10", "25");

foreach $allowable (@allowable_senses) {
    if ($allowable eq $senses) {
	print "<option value='$allowable' selected>top $allowable</option>\n";
    } else {
	print "<option value='$allowable'>top $allowable</option>\n";
    }
}

print <<EOF;
</select>
Max \# of results:
<input type=text size=5 name=maxresults value="$maxresults">
<input type=submit>
</center>
</form>
<hr>
Some implementation notes on this version:<br>
<br>
<li>The returned ReVerb Tuples are only those where both arguments match on our list of people, locations, education and sports entities.
<li>At the moment the code only considers up to the top 10 source sentences for each assertion.
<li>Not optimized for speed. Specifically, it loads several very large files for information about the 2M+ entities, and these files should be preloaded to run all the queries.
<li>Right now only returning exact matches on arg1. I have some code for entity resolution when the match is not exact (e.g., "Titanic" vs "the Titanic") that we can use later when allowing looser matches on arg1.
<hr>
EOF

#-----------------------------------------------------------------------------------------------------
#
# Match to find the potential entity matches using prominence
#

@top_freebase_ids = ();
#push @top_freebase_ids, "0dr_4";
#push @top_freebase_ids, "06l72";

$time_before_er = time();

open (F1, "/homes/abstract/tlin/freebase/1-data/wikipedia/output.fbid-prominence.sorted");

search: while ($line = <F1>) {
    if ($line =~ /$entity_in_english/) {
	$hitcount++;
	#print "Found match $hitcount on $line.<br>\n";
	if ($hitcount > $senses) {
	    #print "Exceeded max # of specified hits.<br>\n";
	    last search;
	}
	chomp $line;
	@parts = split(/\t/,$line);
	$fbid = shift @parts;
	$fbid = substr($fbid, rindex($fbid, "/")+1);
	$inlinks = shift @parts;
	$title = shift @parts;

	push @top_freebase_ids, $fbid;
	$fbid_to_inlinks{$fbid} = $inlinks;
	$fbid_to_title{$fbid} = $title;
    }
}

close (F1);

$time_after_er = time();

#
#-----------------------------------------------------------------------------------------------------

foreach $sense (@top_freebase_ids) {
    $hit{$sense}++;
    #print "Adding hit on $sense<br>\n";
}

$indexfile = "/homes/abstract/tlin/freebase/4-general-context/indices.txt";
$hashfile  = "/homes/abstract/tlin/freebase/4-general-context/indices.hash";
$cachefile = "indices_cached.txt";

$fail = -1;

if (-e $cachefile) {
    open (F1, $cachefile);

    while ($line = <F1>) {
	chomp $line;
	@parts = split(/\t/,$line);
	$freebase_id = shift @parts;
	$doc_index = shift @parts;

	$freebaseid_to_docindex{$freebase_id} = $doc_index;
    }
    
    close (F1);

    foreach $sense (@top_freebase_ids) {
	if ($fail == -1) {
	    if ($freebaseid_to_docindex{$sense} > 0) {
		push @top_senses, $freebaseid_to_docindex{$sense};
		$docindex_to_freebaseid{$freebaseid_to_docindex{$sense}} = $sense;
	    } else {
		@top_senses = ();
		$fail = 1;
		#print "Cache file failed to identify sense $sense, trying full hash next.<br>\n";
	    }
	}
    }
} else {
    $fail = 1;
    #print "Cache file was not located.<br>\n";
}

$time_before_indexfile = time();

if ($fail == -1) {
    $method1 = "used cached freebaseID -> document index data";
} else {
    if (-e $hashfile) {
	%freebaseid_to_docindex = %{ retrieve($hashfile) };
	$method1 = "retrieved freebaseID -> document index hash from storage";

	#print "Trying full hash.<br>\n";
	
	open (F3, ">>$cachefile");

	foreach $sense (@top_freebase_ids) {
	    #print "Examining sense $sense.<br>\n";
	    #print "Doc Index is: ".$freebaseid_to_docindex{$sense}."<br>\n";

	    print F3 $sense."\t".$freebaseid_to_docindex{$sense}."\n";
	    push @top_senses, $freebaseid_to_docindex{$sense};
	    $docindex_to_freebaseid{$freebaseid_to_docindex{$sense}} = $sense;
	}
	close (F3);
    } else {
	# This should only be done very infrequently, so I didn't bother adding the cache lines to it
	#
	$method1 = "recalculating freebaseID -> document index hash"; 

	open (F1, $indexfile);
	
	while ($line = <F1>) {
	    chomp $line;
	    @parts = split(/\t/,$line);
	    $freebase_id = shift @parts;
	    $doc_index = shift @parts;
	    
	    if ($hit{$freebase_id}) {
		push @top_senses, $doc_index;
		$docindex_to_freebaseid{$doc_index} = $freebase_id;
	    }
	    $freebaseid_to_docindex{$freebase_id} = $doc_index;
	}
	
	close (F1);
	
	store (\%freebaseid_to_docindex, $hashfile);
    }
}    

$elapsed_indexfile = time() - $time_before_indexfile;

open (F1, ">matching_input_entities.txt");

print "Document similarity scores:<br><br>";

print "<table border=1><tr><td>Freebase ID</td><td>Name</td><td>Wiki Inlinks</td><td>Index Doc #</td></tr>\n";

foreach $sense (@top_senses) {
    $fbid = $docindex_to_freebaseid{$sense};

    print "<tr><td><a href=\"http://www.freebase.com/view/m/".$fbid."\">".$fbid."</a></td>";
    print "<td>".$fbid_to_title{$fbid}."</td>";
    print "<td>".$fbid_to_inlinks{$fbid}."</td>";
    print "<td>$sense</td></tr>";
    print F1 $sense."\n";
}

close (F1);
print "</table>";
print "<br>";

# Clear the context directory
$output = `rm ./context/*`;
open (F2, ">matching_input_context.txt");

# Look for and display any OCCAM data for this arg1.

$statement = 'select * from occamdata where arg1 = \''.$entity.'\' order by frequency desc';

$sth4 = $dbh->prepare($statement);
$sth4->execute();
myloop: while (@data4 = $sth4->fetchrow_array()) {
    $k++;
    if ($k > $maxresults) { last myloop; }
    open (F1, ">./context/".$k.".txt");
    print F2 "./context/".$k.".txt\n";

    # parse the extraction
    #
    $arg1 = $data4[0];
    $relation = $data4[1];
    $arg2 = $data4[2];
    $frequency = $data4[3];

    $arg2_link = $arg2;
    $arg2_link =~ s/\ /\_/g;

    $tuple_pass = $arg1."|".$relation."|".$arg2;
    $tuple_pass =~ s/\ /\_/g;

    push @tuples_in_order, $tuple_pass;

    $filename_to_source{"./context/".$k.".txt"} = $tuple_pass;
    $tuple_with_link{$tuple_pass} = "($arg1, $relation, <a href=view_entity_disambig.pl?entity=$arg2_link>$arg2</a>)";
    $evidence_link{$tuple_pass} = "<a href=view_evidence.pl?tuple=$tuple_pass>$frequency sentences</a>";

    # find the evidence count from the main database
    #
    $statement = 'select * from occamdata where arg1 LIKE \''.$arg1.'\' and relation LIKE \''.$relation.'\' and arg2 LIKE \''.$arg2.'\'';
    #print "<pre>Statement:$statement</PRE>";
    $sth = $dbh->prepare($statement);
    $sth->execute();
    while (@data = $sth->fetchrow_array()) {
        $a1 = $data[0];
        $rel = $data[1];
        $a2 = $data[2];
        $f = $data[3];

        if (($a1 eq $arg1) && ($rel eq $relation) && ($a2 eq $arg2)) {
            $frequency = $f;
            #print "Found frequency data: $frequency.<br>";
        }
    }
    $sth->finish();

    $assertion = $arg1."\t".$relation."\t".$arg2;
    $assertion =~ s/\'/\'\'/g;

    $statement = 'select source from evidence_link_500 where assertion = \''.$assertion.'\'';
    #print "<pre>Statement:$statement</PRE>";
    $sth = $dbh->prepare($statement);
    $sth->execute();
    while (@data = $sth->fetchrow_array()) {
        $source = $data[0];
        #print "Found source data: $source.<br>";
        if ($frequency > 10) {
            #print "(displaying first 10)<br>";
        }

        if ($source ne "") {
            #print "<br>Source sentences:<br><br>";
            #print "<table border=1><tr><td>#</td><td>index</td><td>source sentence</td></tr>";
            @sourceparts = split(/\,/,$source);
            foreach $sourcepart (@sourceparts) {
                $statement2 = 'select sentence from sentences where source = \''.$sourcepart.'\'';
                $sth2 = $dbh->prepare($statement2);
                $sth2->execute();
                $sent = "(not found in database)";
                while (@data2 = $sth2->fetchrow_array()) {
                    $sent = $data2[0];
                }
                $sth2->finish();

                $onlysent = $sent;
                $sent =~ s/([^\/\[ ]*)\/([^ \]]*)(\]| )/\ <b><u>$1<\/u><\/b>\/$2$3/g;

                $onlysent =~ s/([^\/\[ ]*)\/([^ \]]*)(\]| )/\~$1\~/g;
                $newsent = "";
                $recording = "off";
                while (length ($onlysent) > 0) {
                    $char1 = substr($onlysent,0,1);
                    $onlysent = substr($onlysent,1);
                    if ($char1 eq "~") {
                        if ($recording eq "off") {
                            $recording = "on";
                        } else {
                            $newsent .= " ";
                            $recording = "off";
                        }
                    } else {
                        if ($recording eq "on") {
                            $newsent .= $char1;
                        }
                    }
                }

		print F1 $newsent."\n";

                $onlysent = $newsent;
                #$onlysent =~ s/$a1/<font color="red"><b>$a1<\/b><\/font>/g;
                #$onlysent =~ s/$rel/<font color="blue"><b>$rel<\/b><\/font>/g;
                #$onlysent =~ s/$a2/<font color="green"><b>$a2<\/b><\/font>/g;

                $count++;
                #print "<tr><td>$count</td><td>$sourcepart</td><td>$onlysent</td></tr>\n";
            }
            #print "</table>";
        }
        $sth->finish();
    }
    close (F1);
}
$sth4->finish();

close (F2);

$elapsed_beforecontext = time();

$output = `/homes/abstract/tlin/freebase/4-general-context/run-context_match.sh`;

$time_spent_in_context = time() - $elapsed_beforecontext;
$elapsed = time() - $start_time;

open (F1, "matching_output_scores.txt");

while ($line = <F1>) {
    @parts = split(/\t/,$line);
    
    $label = shift @parts;
    if ($label eq "file") {
	$filename = shift @parts;
	$keywords = shift @parts;
	$keywords =~ s/contents://g;
    } elsif ($label eq "score") {
	$entity = shift @parts;
	$score = shift @parts;
	$scoring{$filename_to_source{$filename}}{$entity} = $score;
	#print "($filename, $entity) -> $score<br>\n";
    }
}

close (F1);


# Display all the calculated results:
#

print <<EOF;
Matching details:<br>
<br>
<table border=1>
  <tr> 
    <td align=center><a href=http://reverb.cs.washington.edu>ReVerb tuples</a></td>
    <td align=center>source<br>sentences</td>
EOF

foreach $sense (@top_senses) {
    $fbid = $docindex_to_freebaseid{$sense};

    print "<td align=center>".$fbid_to_title{$fbid}."<br>($sense)</td>";
}

print "</tr>";

foreach $tuple (@tuples_in_order) {
    print "<tr><td valign=top>".$tuple_with_link{$tuple}."</td>\n";
    print "<td>".$evidence_link{$tuple}."</td>\n";

    #$docindex_to_freebaseid{$doc_index} = $freebase_id;

    $maxscore = -1;
    $bestsense = -1;
    foreach $sense (@top_senses) {
	$score = $scoring{$tuple}{$docindex_to_freebaseid{$sense}};
	if ($score > $maxscore) {
	    $maxscore = $score;
	    $bestsense = $sense;
	}
    }

    foreach $sense (@top_senses) {
	if ($sense eq $bestsense) {
	    print "<td bgcolor=#33FF66>".$scoring{$tuple}{$docindex_to_freebaseid{$sense}}."</td>";
	    if ($disambig_results{$sense}) {
		$disambig_results{$sense} .= "<br>".$tuple_with_link{$tuple};
	    } else {
		$disambig_results{$sense} = $tuple_with_link{$tuple};
	    }
	} else {
	    print "<td>".$scoring{$tuple}{$docindex_to_freebaseid{$sense}}."</td>";
	}
    }
    print "</tr>";
    $result_count++;
}

print "</table>";

print <<EOF;
<br>
Disambiguated results:<br>
<br>
EOF

print "<table border=1><tr>";
foreach $sense (@top_senses) {
    $fbid = $docindex_to_freebaseid{$sense};

    print "<td align=center>".$fbid_to_title{$fbid}." ($sense)</td>";
}
print "</tr><tr>";
foreach $sense (@top_senses) {
    print "<td valign=top>".$disambig_results{$sense}."</td>";
}
print "</table>";

print "<br><hr>";
print "Total elapsed processing time: $elapsed seconds.<br>";

$per_sentence = $time_spent_in_context / $result_count;
print "Time spent retrieving and matching entity names: ".($time_after_er - $time_before_er)." seconds.<br>";
print "Time spent processing index->document ID file: $elapsed_indexfile seconds ($method1).<br>";
print "Time spent on context matching: $time_spent_in_context seconds ($per_sentence seconds per tuple).<br>";

#print "Output was: <PRE>$output.</PRE><br>";
