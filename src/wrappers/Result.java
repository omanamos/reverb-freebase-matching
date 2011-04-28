package wrappers;

import java.util.*;
import analysis.Analyze;

/**
 * Wraps a set of matches found in Freebase for a given query. Allows for quick id lookup. Also provides other functionality.
 */
public class Result implements Iterable<Entity>{

	public static final int EXACT_STRING_MATCH = 0;
	public static final int EXACT_SUBS_MATCH = 1;
	public static final int EXACT_ABBRV_MATCH = 2;

	public final String query;
	public final String dirtyQuery;
	private PriorityQueue<Entity> matches;
	private Map<String, Entity> idLookup;

	private Set<Entity> exactStringMatches;
	private Map<Entity, Integer> exactSubsMatches;
	private Set<Entity> exactAbbrvMatches;
	
	public Result(String query, String dirtyQuery){
		this.query = query;
		this.dirtyQuery = dirtyQuery;
		this.matches = new PriorityQueue<Entity>();
		this.idLookup = new HashMap<String, Entity>();

		this.exactStringMatches = new HashSet<Entity>();
		this.exactSubsMatches = new HashMap<Entity, Integer>();
		this.exactAbbrvMatches = new HashSet<Entity>();
	}
	
	/**
	 * @param e Matched Entity to add to this Result
	 */
	public void add(Entity e, int matchType){
		if(!this.idLookup.containsKey(e.id)){
			this.matches.add(e);
			this.idLookup.put(e.id, e);
		}

		switch(matchType){
		case EXACT_STRING_MATCH:
			this.exactStringMatches.add(e);
			break;
		case EXACT_SUBS_MATCH:
			if(!this.exactSubsMatches.containsKey(e))
				this.exactSubsMatches.put(e, 0);
			this.exactSubsMatches.put(e, this.exactSubsMatches.get(e) + 1);
			break;
		case EXACT_ABBRV_MATCH:
			this.exactAbbrvMatches.add(e);
			break;
		}
	}
	
	/**
	 * @param c collection of Entities to add
	 */
	public void add(Collection<Entity> c, int matchType){
		if(c != null){
			for(Entity e : c)
				this.add(e, matchType);
		}
	}
	
	/**
	 * @param id match to search for
	 * @return true if contains a match that has the given id
	 */
	public boolean hasMatch(String id){
		return id != null && this.idLookup.containsKey(id);
	}
	
	/**
	 * @param id match to search for
	 * @return Entity with the given id in this Result
	 * @throws IllegalArgumentException if no match exists in this Result that has the given id
	 */
	public Entity getMatch(String id){
		if(!this.hasMatch(id))
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
		return this.matches.peek();
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

	public String toString(){
		String s = "";
		int cnt = 0;
		String correctID = null; 
		try{
			correctID = Analyze.loadCorrectMatches().get(this.dirtyQuery);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(this.hasMatch(correctID)){
		
			for(Entity e : this){
				s += "\"" + e.contents + "\"," + e.inlinks + "," + 
					(this.exactStringMatches.contains(e) ? 1 : 0) + "," + 
					(this.exactSubsMatches.containsKey(e) ? this.exactSubsMatches.get(e) : 0) + "," + 
					(this.exactAbbrvMatches.contains(e) ? 1 : 0) + "," + 
					(e.id.equals(correctID) ? 1 : 0) + "\n";
				if(e.id.equals(correctID))
					correctID = null;
				cnt++;
				if(cnt == 5)
					break;
			}
			
			if(correctID != null){
				Entity e = this.getMatch(correctID);
				s += "\"" + e.contents + "\"," + e.inlinks + "," + 
					(this.exactStringMatches.contains(e) ? 1 : 0) + "," + 
					(this.exactSubsMatches.containsKey(e) ? this.exactSubsMatches.get(e) : 0) + "," + 
					(this.exactAbbrvMatches.contains(e) ? 1 : 0) + "," + 
					1 + "\n";
			}
		}
		
		return s;
	}
}
