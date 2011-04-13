package wrappers;

public class Tuple{
		public final String rvEnt;
		public final String fbEnt;
		public final Integer code;
		
		public Tuple(String rvEnt, String fbEnt, Integer code){
			this.rvEnt = rvEnt;
			this.fbEnt = fbEnt;
			this.code = code;
		}
		
		public static Tuple fromString(String s){
			String[] parts = s.split("\t");
			return new Tuple(parts[0], parts[1], Integer.parseInt(parts[2]));
		}
		
		public String toString(){
			return rvEnt + "\t" + fbEnt + "\t" + code;
		}
	}
