package wrappers;

public class Options {

	public boolean SUB_AB;
	public boolean SUB_BA;
	public boolean DIST;
	public boolean ACRO_AB;
	public boolean ACRO_BA;
	public String OUTPUT;
	public int MAX_MATCHES;
	
	private Options(){
		SUB_AB = true;
		SUB_BA = true;
		DIST = true;
		ACRO_AB = true;
		ACRO_BA = true;
		OUTPUT = "output/output.txt";
		MAX_MATCHES = -1;
	}
	
	public Options(String[] args) throws Exception{
		this();
		
		//Parse arguments
		if(args.length > 0){
			
			int p = Integer.parseInt(args[0]);
			SUB_AB = false;
			SUB_BA = false;
			DIST = false;
			ACRO_AB = false;
			ACRO_BA = false;
			
			while(p >= 1){
				
				int tmp = p % 10;
				
				switch(tmp){
					case 1:
						SUB_AB = true;
						break;
					case 2:
						SUB_BA = true;
						break;
					case 3:
						DIST = true;
						break;
					case 4:
						ACRO_AB = true;
						break;
					case 5:
						ACRO_BA = true;
						break;
					default:
						System.out.println("Invalid arguments");
						throw new Exception();
				}
				p = p / 10;
			}
			
			if(args.length > 1){
				OUTPUT = args[1];
				
				if(args.length == 3){
					MAX_MATCHES = Integer.parseInt(args[2]);
				}else if(args.length > 3){
					System.out.println("Invalid arguments");
					throw new Exception();
				}
			}
		}
	}
	
	public static Options getDefaults(){
		return new Options();
	}
}
