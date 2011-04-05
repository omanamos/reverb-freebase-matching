
public class Entity {
	
	public final String id;
	public final String contents;
	
	public Entity(String id, String contents){
		this.id = id;
		this.contents = contents;
	}
	
	public static Entity fromString(String s){
		String[] tmp = s.split("\t");
		return new Entity(tmp[0], tmp[2]);
	}
	
	public String toString(){
		return this.id + " " + this.contents;
	}
}
