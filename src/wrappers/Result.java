package wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Result implements Iterable<Entity>{
	
	public final String query;
	private List<Entity> matches;
	private Map<String, Entity> idLookup;
	
	public Result(String query){
		this.query = query;
		this.matches = new ArrayList<Entity>();
		this.idLookup = new HashMap<String, Entity>();
	}
	
	public void add(Entity e){
		this.matches.add(e);
		this.idLookup.put(e.id, e);
	}
	
	public List<Entity> getMatches(){
		return Collections.unmodifiableList(this.matches);
	}
	
	public boolean hasMatch(String id){
		return this.idLookup.containsKey(id);
	}
	
	public Entity getMatch(String id){
		if(!this.idLookup.containsKey(id))
			throw new IllegalArgumentException("No match exists for that id.");
		return this.idLookup.get(id);
	}
	
	public int size(){
		return this.matches.size();
	}
	
	public int getDepth(String id){
		if(!this.idLookup.containsKey(id))
			throw new IllegalArgumentException("No match exists for that id.");
		int depth = 1;
		for(Entity e : matches){
			if(e.id.equals(id))
				break;
			depth++;
		}
		return depth;
	}
	
	public Entity sampleMatch(){
		if(this.matches.isEmpty())
			throw new IllegalStateException("No match available to sample.");
		return this.matches.get(0);
	}

	public Iterator<Entity> iterator() {
		return this.matches.iterator();
	}
}
