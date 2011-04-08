
public class Pair implements Comparable<Pair>{

	public String s;
	public Integer i;
	
	public Pair(String s, Integer i){
		this.s = s;
		this.i = i;
	}
	
	public int compareTo(Pair other) {
		return i.compareTo(other.i);
	}

}
