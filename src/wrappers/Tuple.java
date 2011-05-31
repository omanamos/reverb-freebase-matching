package wrappers;

public class Tuple implements Comparable<Tuple> {
		
		public final String e;
		public final Integer v;
		
		public Tuple(String e, Integer v){
			this.e = e;
			this.v = v;
		}

		@Override
		public int compareTo(Tuple arg0) {
			return -this.v.compareTo(arg0.v);
		}
	}