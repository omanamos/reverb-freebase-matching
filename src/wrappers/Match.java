package wrappers;

public class Match {

	public final Query q;
	public final Entity e;
	public final MatchType t;
	public final Double weight;
	
	public Match(Query q, Entity e, MatchType t){
		this(q, e, t, 1.0);
	}
	
	public Match(Query q, Entity e, MatchType t, Double weight){
		this.q = q;
		this.e = e;
		this.t = t;
		this.weight = weight;
	}
	
	public int hashCode(){
		return this.e.hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Match)
			return ((Match)obj).q.equals(this.q) && ((Match)obj).e.equals(this.e) && ((Match)obj).t.equals(this.t);
		else
			return false;
	}
}
