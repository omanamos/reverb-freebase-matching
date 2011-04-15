package wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wraps a set of matches found in Freebase for a given query. Allows for quick id lookup. Also provides other functionality.
 */
public class Result implements Iterable<Entity>{
	
	public final String query;
	private List<Entity> matches;
	private Map<String, Entity> idLookup;
	
	public Result(String query){
		this.query = query;
		this.matches = new ArrayList<Entity>();
		this.idLookup = new HashMap<String, Entity>();
	}
	
	/**
	 * @param e Matched Entity to add to this Result
	 */
	public void add(Entity e){
		this.matches.add(e);
		this.idLookup.put(e.id, e);
	}
	
	/**
	 * @return an unmodifiable list of matches
	 */
	public List<Entity> getMatches(){
		return Collections.unmodifiableList(this.matches);
	}
	
	/**
	 * @param id match to search for
	 * @return true if contains a match that has the given id
	 */
	public boolean hasMatch(String id){
		return this.idLookup.containsKey(id);
	}
	
	/**
	 * @param id match to search for
	 * @return Entity with the given id in this Result
	 * @throws IllegalArgumentException if no match exists in this Result that has the given id
	 */
	public Entity getMatch(String id){
		if(!this.idLookup.containsKey(id))
			throw new IllegalArgumentException("No match exists for that id.");
		return this.idLookup.get(id);
	}
	
	/**
	 * @return number of matches in this result
	 */
	public int size(){
		return this.matches.size();
	}
	
	/**
	 * @param id match to search for
	 * @return depth of the match with the given id (matches are in order of when it was added to this result -> first match added has depth = 1)
	 * @throws IllegalArgumentException if no match exists in this Result that has the given id
	 */
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
	
	/**
	 * @return the first match added to this result
	 * @throws IllegalArgumentException if there are no matches in this result
	 */
	public Entity sampleMatch(){
		if(this.matches.isEmpty())
			throw new IllegalStateException("No match available to sample.");
		return this.matches.get(0);
	}

	public Iterator<Entity> iterator() {
		return this.matches.iterator();
	}
	
	/**
	 * hashes based on the this.query
	 */
	public int hashCode(){
		return this.query.hashCode();
	}
	
	/**
	 * Equality is determined based on this.query
	 */
	public boolean equals(Object other){
		if(other instanceof Result){
			return ((Result)other).query.equals(this.query);
		}else{
			return false;
		}
	}
}
