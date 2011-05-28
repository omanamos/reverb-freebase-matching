package wrappers;

public class Score implements Comparable<Score>{
	
	public final Double total;
	private double inlinks;
	private double factor;
	private double exact;
	private double cleaned;
	private double substr;
	private double abbrv;
	private double wiki;
	
	public Score(){
		this(0,0,0,0,0,0,0);
	}
	
	public Score(double total){
		this.total = total;
		this.inlinks = 0.0;
		this.factor = 0.0;
		this.exact = 0.0;
		this.cleaned = 0.0;
		this.substr = 0.0;
		this.abbrv = 0.0;
		this.wiki = 0.0;
	}
	
	public Score(double inlinks, double factor, double exact, double cleaned, double substr, double abbrv, double wiki){
		this.total = inlinks + factor * (exact + cleaned + substr + abbrv + wiki);
		this.inlinks = inlinks;
		this.factor = factor;
		this.exact = exact;
		this.cleaned = cleaned;
		this.substr = substr;
		this.abbrv = abbrv;
		this.wiki = wiki;
	}
	
	public String toString(){
		return total + " = " + inlinks + " + " + factor  + " * (" + exact + " + " + cleaned + " + " + substr + " + " + abbrv + " + " + wiki + ")"; 
	}

	@Override
	public int compareTo(Score other) {
		return this.total.compareTo(other.total);
	}
}
