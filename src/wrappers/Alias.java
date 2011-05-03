package wrappers;

public class Alias {

	public final Entity e;
	public final MatchType m;
	
	public Alias(Entity e, MatchType m){
		this.e = e;
		this.m = m;
	}
	
	public int hashCode(){
		return this.e.hashCode();
	}
	
	public boolean equals(Object other){
		if(other instanceof Alias)
			return this.e.equals(((Alias)other).e) && this.m.equals(((Alias)other).m);
		else
			return false;
	}
}
