
public class Entity {
	
	public final String id;
	public final String contents;
	public final Integer inlinks;
	
    public Entity(String id, String contents, Integer inlinks){
		this.id = id;
		this.contents = contents;
		this.inlinks = inlinks;
	}
	
	public static Entity fromString(String s){
		String[] tmp = s.split("\t");
		return new Entity(tmp[0], tmp[2], Integer.parseInt(tmp[1]));
	}
	
	public String toString(){
		return this.id + " " + this.contents + " " + inlinks;
	}
}
