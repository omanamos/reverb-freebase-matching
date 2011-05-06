package wrappers;

import matching.Utils;

public class Entity implements Comparable<Entity>{
	
	public final String id;
	public final String contents;
	public final String cleanedContents;
	public final Integer inlinks;
	public final boolean noCleaning;
	public Double normInlinks;
	/**
	 * depth in freebase
	 */
	public final Integer offset;
	public Double score;
	
	public Entity(String id){
		this(id, null, null, null);
	}
	
	public Entity(String id, String contents, Integer inlinks){
		this(id, contents, inlinks, null);
	}
	
    public Entity(String id, String contents, Integer inlinks, Integer offset){
		this.id = id;
		this.contents = contents;
		this.inlinks = inlinks;
		this.offset = offset;
		this.cleanedContents = Utils.cleanString(this.contents);
		this.noCleaning = this.contents.equals(this.cleanedContents);
		this.score = 0.0;
		this.normInlinks = 0.0;
	}
    
	public String toString(){
		return this.id + "\t" + this.contents + "\t" + score;
	}
	
	public String toOutputString(){
		return this.id + "\t" + this.contents + "\t" + inlinks + "\t" + score;
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
		return -this.score.compareTo(other.score);
	}
	
	public static Entity fromString(String s, Integer offset){
		String[] tmp = s.split("\t");
		Entity rtn = new Entity(tmp[0], tmp[2], Integer.parseInt(tmp[1]), offset);
		return rtn;
	}
	
	public static Entity fromOutputString(String s){
		String[] tmp = s.trim().split("\t");
		Entity rtn = new Entity(tmp[0], tmp[1], Integer.parseInt(tmp[2]));
		if(tmp.length > 3)
			rtn.score = Double.parseDouble(tmp[3]);
		return rtn;
	}
	
}
