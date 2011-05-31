package wrappers;

import matching.Utils;

public class Entity implements Comparable<Entity>{
	
	public final String id;
	public final String orig;
	public final String contents;
	public final String cleanedContents;
	public final Integer inlinks;
	public final boolean noCleaning;
	public Double normInlinks;
	/**
	 * depth in freebase
	 */
	public final Integer offset;
	public Score score;
	
	public Entity(String id){
		this(id, null, null, null);
	}
	
	public Entity(String id, String contents, Integer inlinks){
		this(id, contents, inlinks, null);
	}
	
    public Entity(String id, String contents, Integer inlinks, Integer offset){
		this.id = id;
		this.orig = contents;
		this.contents = contents.toLowerCase();
		this.inlinks = inlinks;
		this.offset = offset;
		this.cleanedContents = Utils.cleanString(this.contents);
		this.noCleaning = this.contents.equals(this.cleanedContents);
		this.score = new Score();
		this.normInlinks = 0.0;
	}
    
	public String toString(){
		return this.id + "\t" + this.orig + "\t" + this.score;
	}
	
	public String toOutputString(){
		return this.id + "\t" + this.orig + "\t" + this.inlinks + "\t" + this.score.total;
	}
	
	public int hashCode(){
		return this.id.hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Entity)
			return ((Entity)obj).id.equals(this.id);
		else
			return false;
	}

	@Override
	public int compareTo(Entity other) {
		int score = this.score.compareTo(other.score);
		return score != 0 ? -score : -this.inlinks.compareTo(other.inlinks);
	}
	
	public static Entity fromString(String s, Integer offset){
		String[] tmp = s.split("\t");
		Entity rtn = new Entity(tmp[0], tmp[2], Integer.parseInt(tmp[1]), offset);
		return rtn;
	}
	
	public static Entity fromOutputString(String s){
		String[] tmp = s.trim().split("\t");
		Entity rtn = new Entity(tmp[2], tmp[3], Integer.parseInt(tmp[4]));
		if(tmp.length > 5)
			rtn.score = new Score(Double.parseDouble(tmp[5]));
		return rtn;
	}
	
}
