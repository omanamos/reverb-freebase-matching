package wrappers;

public class Options {

	public boolean monitor;
	public boolean test;
	public boolean usage;
	public String INPUT;
	public String OUTPUT;
	public String FREEBASE;
	public String WIKI_ALIAS;
	public int MAX_MATCHES;
	public int LUCENE_THRESHOLD;
	private enum Param{ input, output, freebase, wikiAlias, max, lucene, none };
	
	private Options(){
		monitor = true;
		test = false;
		usage = false;
		INPUT = "entities.txt";
		OUTPUT = "top_matches.txt";
		FREEBASE = "output.fbid-prominence.sorted";
		WIKI_ALIAS = "output.wiki-aliases.sorted";
		MAX_MATCHES = 5;
		LUCENE_THRESHOLD = 100;
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
			boolean isFlag = argEqualsQ || argEqualsT || argEqualsI || 
							 argEqualsO || argEqualsF || argEqualsW || 
							 argEqualsM || argEqualsL;
			
			if(argEqualsH && last.equals(Param.none)){
				this.usage = true;
				printUsage();
				return;
			}else if(argEqualsQ && last.equals(Param.none)){
				this.monitor = false;
			}else if(argEqualsT && last.equals(Param.none)){
				this.test = true;
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
		System.out.println("  -q: Quits after one run, otherwise it will move the input file into the \"./processed\" \n\tdirectory and wait until the input file exists again.");
		System.out.println("  -t: Analyzes the results after finished matching. Defaults to false. Looks for correct matches under \n\t\"data/keys/match-lookup.txt\", " +
						   "with each line of the format:\n\t\"<reverb string>\\t<correct freebase id>\\t<correct freebase entity name>\\t<correct freebase inlink count>\"");
		
		System.out.println("  -i <path to input file>: Defaults to \"entities.txt\"");
		System.out.println("  -o <path to output file>: Defaults to \"top_matches.txt\"");
		System.out.println("  -f <path to freebase file sorted by prominence>: Defaults to \"output.fbid-prominence.sorted\"");
		System.out.println("  -w <path to wiki aliases file>: Defaults to \"output.wiki-aliases.sorted\"");
		System.out.println("  -m <max number of matches to return>: Defaults to 5");
		System.out.println("  -l <maximum score to use Lucene on>: Defaults to 100");
	}
	
	public static Options getDefaults(){
		return new Options();
	}
}
