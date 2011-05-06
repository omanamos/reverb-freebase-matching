package wrappers;

public class Match {

	public final Query q;
	public final Entity e;
	public final MatchType t;
	
	public Match(Query q, Entity e, MatchType t){
		this.q = q;
		this.e = e;
		this.t = t;
	}
	
	public int hashCode(){
		return this.e.hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Match)
			return ((Match)obj).q.equals(this.q) && ((Match)obj).e.equals(this.e);
		else
			return false;
	}
}
