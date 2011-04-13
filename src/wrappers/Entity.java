package wrappers;

public class Entity {
	
	public final String id;
	public final String contents;
	public final Integer inlinks;
	public final Integer offset;
	
	public Entity(String contents){
		this(null, contents, null, null);
	}
	
	public Entity(String id, String contents, Integer inlinks){
		this(id, contents, inlinks, null);
	}
	
    public Entity(String id, String contents, Integer inlinks, Integer offset){
		this.id = id;
		this.contents = contents;
		this.inlinks = inlinks;
		this.offset = offset;
	}
	
	public static Entity fromString(String s, Integer offset){
		String[] tmp = s.split("\t");
		return new Entity(tmp[0], tmp[2], Integer.parseInt(tmp[1]), offset);
	}
	
	public String toString(){
		return this.id + "\t" + this.contents + "\t" + inlinks;
	}
}
