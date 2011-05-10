package wrappers;

public class Options {

	public boolean usage;
	public String INPUT;
	public String OUTPUT;
	public int MAX_MATCHES;
	private enum Param{ input, output, max, none };
	
	private Options(){
		usage = false;
		INPUT = "entities.txt";
		OUTPUT = "top_matches.txt";
		MAX_MATCHES = 5;
	}
	
	public Options(String[] args){
		this();
		Param last = Param.none;
		for(String arg : args){
			boolean argEqualsI = arg.equals("-i");
			boolean argEqualsO = arg.equals("-o");
			boolean argEqualsM = arg.equals("-m");
			boolean isFlag = argEqualsI || argEqualsO || argEqualsM;
			
			if(argEqualsI && last.equals(Param.none)){
				last = Param.input;
			}else if(argEqualsO && last.equals(Param.none)){
				last = Param.output;
			}else if(argEqualsM && last.equals(Param.none)){
				last = Param.max;
			}else if(last.equals(Param.input) && !isFlag){
				this.INPUT = arg;
				last = Param.none;
			}else if(last.equals(Param.output) && !isFlag){
				this.OUTPUT = arg;
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
		System.out.println("\t-m <max number of matches to return>");
	}
	
	public static Options getDefaults(){
		return new Options();
	}
}
