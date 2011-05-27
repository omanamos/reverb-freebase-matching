package wrappers;

public class Options {

	public boolean usage;
	public String INPUT;
	public String OUTPUT;
	public String FREEBASE;
	public String WIKI_ALIAS;
	public int MAX_MATCHES;
	private enum Param{ input, output, freebase, wikiAlias, max, none };
	
	private Options(){
		usage = false;
		INPUT = "entities.txt";
		OUTPUT = "top_matches.txt";
		FREEBASE = "output.fbid-prominence.sorted";
		WIKI_ALIAS = "output.wiki-aliases.sorted";
		MAX_MATCHES = 5;
	}
	
	public Options(String[] args){
		this();
		Param last = Param.none;
		for(String arg : args){
			boolean argEqualsI = arg.equals("-i");
			boolean argEqualsO = arg.equals("-o");
			boolean argEqualsF = arg.equals("-f");
			boolean argEqualsW = arg.equals("-w");
			boolean argEqualsM = arg.equals("-m");
			boolean isFlag = argEqualsI || argEqualsO || argEqualsF || argEqualsW || argEqualsM;
			
			if(argEqualsI && last.equals(Param.none)){
				last = Param.input;
			}else if(argEqualsO && last.equals(Param.none)){
				last = Param.output;
			}else if(argEqualsF && last.equals(Param.none)){
				last = Param.freebase;
			}else if(argEqualsW && last.equals(Param.none)){
				last = Param.wikiAlias;
			}else if(argEqualsM && last.equals(Param.none)){
				last = Param.max;
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
		System.out.println("\t-i <path to input file>");
		System.out.println("\t-o <path to output file>");
		System.out.println("\t-f <path to freebase file sorted by prominence>");
		System.out.println("\t-w <path to wiki aliases file>");
		System.out.println("\t-m <max number of matches to return>");
	}
	
	public static Options getDefaults(){
		return new Options();
	}
}
