package wrappers;

public class Entity {
	
	public final String id;
	public final String contents;
	public final Integer inlinks;
	/**
	 * depth in freebase
	 */
	public final Integer offset;
	/**
	 * Acronym for this entity, null if none exists or isn't known
	 */
	public String acronym;
	
	public Entity(String id, String contents, Integer inlinks){
		this(id, contents, inlinks, null);
	}
	
    public Entity(String id, String contents, Integer inlinks, Integer offset){
		this.id = id;
		this.contents = contents;
		this.inlinks = inlinks;
		this.offset = offset;
		this.acronym = null;
	}
    
    public boolean hasAcronym(){
    	return this.acronym != null;
    }
	
	public static Entity fromString(String s, Integer offset){
		String[] tmp = s.split("\t");
		return new Entity(tmp[0], tmp[2], Integer.parseInt(tmp[1]), offset);
	}
	
	public static Entity fromOutputString(String s){
		String[] tmp = s.trim().split("\t");
		return new Entity(tmp[0], tmp[1], Integer.parseInt(tmp[2]));
	}
	
	public String toString(){
		return this.id + "\t" + this.contents + "\t" + inlinks;
	}
}
