package wrappers;

public class Options {
	public static final int DEFAULT_LUCENE_THRESHOLD = 40;
	public static final int DEFAULT_MATCH_DEPTH = 5;
	
	public static final String DEFAULT_OUTPUT = "top_matches.txt";
	public static final String DEFAULT_INPUT = "entities.txt";
	
	public boolean inMemory;
	public boolean monitor;
	public boolean usage;
	public String INPUT;
	public String OUTPUT;
	public String FREEBASE;
	public String WIKI_ALIAS;
	public String TESTING;
	public int MAX_MATCHES;
	public int LUCENE_THRESHOLD;
	private enum Param{ input, output, freebase, wikiAlias, max, lucene, testing, none };
	
	private Options(){
		this(true, true, false, DEFAULT_INPUT, DEFAULT_OUTPUT, Resources.DEFAULT_FREEBASE, Resources.DEFAULT_WIKI_ALIASES, null, DEFAULT_MATCH_DEPTH, DEFAULT_LUCENE_THRESHOLD);
	}
	
	public Options(boolean inMemory, boolean monitor, boolean usage, String input, String output, String freebase, String wiki_aliases, String testing, int max_matches, int lucene_threshold){
		this.inMemory = inMemory;
		this.monitor = monitor;
		this.usage = usage;
		this.INPUT = input;
		this.OUTPUT = output;
		this.FREEBASE = freebase;
		this.WIKI_ALIAS = wiki_aliases;
		this.TESTING = testing;
		this.MAX_MATCHES = max_matches;
		this.LUCENE_THRESHOLD = lucene_threshold;
	}
	
	public Options(String[] args){
		this();
		Param last = Param.none;
		for(String arg : args){
			boolean argEqualsH = arg.equals("-h");
			boolean argEqualsQ = arg.equals("-q");
			boolean argEqualsT = arg.equals("-t");
			boolean argEqualsI = arg.equals("-i");
			boolean argEqualsO = arg.equals("-o");
			boolean argEqualsF = arg.equals("-f");
			boolean argEqualsW = arg.equals("-w");
			boolean argEqualsM = arg.equals("-m");
			boolean argEqualsL = arg.equals("-l");
			boolean argEqualsD = arg.equals("-d");
			boolean isFlag = argEqualsQ || argEqualsT || argEqualsI || 
							 argEqualsO || argEqualsF || argEqualsW || 
							 argEqualsM || argEqualsL || argEqualsD ||
							 argEqualsH;
			
			if(argEqualsH && last.equals(Param.none)){
				this.usage = true;
				printUsage();
				return;
			}else if(argEqualsD && last.equals(Param.none)){
				this.inMemory = false;
			}else if(argEqualsQ && last.equals(Param.none)){
				this.monitor = false;
			}else if(argEqualsT && last.equals(Param.none)){
				last = Param.testing;
			}else if(argEqualsI && last.equals(Param.none)){
				last = Param.input;
			}else if(argEqualsO && last.equals(Param.none)){
				last = Param.output;
			}else if(argEqualsF && last.equals(Param.none)){
				last = Param.freebase;
			}else if(argEqualsW && last.equals(Param.none)){
				last = Param.wikiAlias;
			}else if(argEqualsM && last.equals(Param.none)){
				last = Param.max;
			}else if(argEqualsL && last.equals(Param.none)){
				last = Param.lucene;
			}else if(last.equals(Param.testing) && !isFlag){
				this.TESTING = arg;
				last = Param.none;
			}else if(last.equals(Param.input) && !isFlag){
				this.INPUT = arg;
				last = Param.none;
			}else if(last.equals(Param.output) && !isFlag){
				this.OUTPUT = arg;
				last = Param.none;
			}else if(last.equals(Param.freebase) && !isFlag){
				this.FREEBASE = arg;
				last = Param.none;
			}else if(last.equals(Param.wikiAlias) && !isFlag){
				this.WIKI_ALIAS = arg;
				last = Param.none;
			}else if(last.equals(Param.max) && !isFlag){
				try{
					this.MAX_MATCHES = Integer.parseInt(arg);
					last = Param.none;
				}catch(NumberFormatException e){
					this.usage = true;
					System.out.println("Error: Invalid Arguments");
					printUsage();
					return;
				}
			}else if(last.equals(Param.lucene) && !isFlag){
				try{
					this.LUCENE_THRESHOLD = Integer.parseInt(arg);
					last = Param.none;
				}catch(NumberFormatException e){
					this.usage = true;
					System.out.println("Error: Invalid Arguments");
					printUsage();
					return;
				}
			}else{
				this.usage = true;
				System.out.println("Error: Invalid Arguments!");
				printUsage();
				return;
			}
		}
	}
	
	public static void printUsage(){
		System.out.println("Usage: \"[options]\"");
		System.out.println("where options include:");
		System.out.println("  -h: Prints options available for the program.");
		System.out.println("  -d: Doesn't load the ReVerb corpus into memory. Doesn't sort or deduplicate the corpus. Assumes arg1 per line with the first character ignored.");
		System.out.println("  -q: Quits after one run, otherwise it will move the input file into the \"./processed\" \n\tdirectory and wait until the input file exists again.");
		System.out.println("  -t <path to correct matches>:<path to thresholds file>:<path to output file>: Prints out accuracies after matching. Each line in the correct matches file should be of the format:" +
								"\n\t\"<reverb string>\\t<correct freebase id>\"" +
								"\n\tThe thresholds file is to specify at which thresholds that accuracy should be calculated at. It should have one threshold per line." +
								"\n\tNote: use the -m parameter to specify the max number of matches to return as a higher number than your max threshold in this file." +
								"\n\tThe output file is of the format, with the first line as a header:" +
								"\n\t\"<depth threshold>\\t<% of time any of the top-k matches are correct>\\t<%of time all of the top-k matches are correct>\\t<average % of correct matches that are included at the current depth>\\t<average % of the top-k matches that are correct>");
		
		System.out.println("  -i <path to input file>: Defaults to \"entities.txt\"");
		System.out.println("  -o <path to output file>: Defaults to \"top_matches.txt\"");
		System.out.println("  -f <path to freebase file sorted by prominence>: Defaults to \"output.fbid-prominence.sorted\"");
		System.out.println("  -w <path to wiki aliases file>: Defaults to \"output.wiki-aliases.sorted\"");
		System.out.println("  -m <max number of matches to return>: Defaults to 5");
		System.out.println("  -l <maximum score to use Lucene on>: Defaults to 40");
	}
	
	public static Options getDefaults(){
		return new Options();
	}
}
