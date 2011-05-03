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
	private List<Entity> lst;
	private Map<String, Entity> idLookup;

	private Set<Entity> exactStringMatches;
	private Set<Entity> cleanedStringMatches;
	private Map<Entity, Integer> exactSubsMatches;
	private Set<Entity> exactAbbrvMatches;
	private Set<Entity> wikiMatches;
	private Set<Entity> luceneMatches;
	
	public Result(String query, String dirtyQuery){
		this.query = query;
		this.dirtyQuery = dirtyQuery;
		this.matches = new PriorityQueue<Entity>();
		this.lst = new ArrayList<Entity>();
		this.idLookup = new HashMap<String, Entity>();

		this.exactStringMatches = new HashSet<Entity>();
		this.cleanedStringMatches = new HashSet<Entity>();
		this.exactSubsMatches = new HashMap<Entity, Integer>();
		this.exactAbbrvMatches = new HashSet<Entity>();
		this.wikiMatches = new HashSet<Entity>();
		this.luceneMatches = new HashSet<Entity>();
	}
	
	/**
	 * @param e Matched Entity to add to this Result
	 */
	public void add(Entity e, MatchType m){
		if(!this.idLookup.containsKey(e.id)){
			this.idLookup.put(e.id, e);
		}

		switch(m){
			case EXACT:
				this.exactStringMatches.add(e);
				break;
			case SUB:
				if(!this.exactSubsMatches.containsKey(e))
					this.exactSubsMatches.put(e, 0);
				this.exactSubsMatches.put(e, this.exactSubsMatches.get(e) + 1);
				break;
			case ABBRV:
				this.exactAbbrvMatches.add(e);
				break;
			case WIKI:
				this.wikiMatches.add(e);
				break;
			case CLEANED:
				this.cleanedStringMatches.add(e);
			case LUCENE:
				this.luceneMatches.add(e);
		}
	}
	
	public void sort(boolean computeScores){
		this.matches.clear();
		this.lst.clear();
		for(String key : this.idLookup.keySet()){
			Entity e = this.idLookup.get(key);
			if(computeScores)
				e.score = computeScore(e);
			this.matches.add(this.idLookup.get(key));
		}
		
		while(!this.matches.isEmpty())
			this.lst.add(matches.poll());
	}
	
	private double computeScore(Entity e){
		return 100 * e.normInlinks + 
				100 * (this.exactStringMatches.contains(e) ? 1 : 0) + 
				20 * (this.cleanedStringMatches.contains(e) ? 1 : 0) + 
				20 * (this.exactSubsMatches.containsKey(e) && !this.exactStringMatches.contains(e) && !this.cleanedStringMatches.contains(e) ? this.exactSubsMatches.get(e) : 0) + 
				80 * (this.exactAbbrvMatches.contains(e) && !this.exactSubsMatches.containsKey(e) && !this.exactStringMatches.contains(e) && !this.cleanedStringMatches.contains(e) ? 1 : 0) + 
				90 * (this.wikiMatches.contains(e) ? 1 : 0);
	}
	
	/**
	 * @param c collection of Entities to add
	 */
	public void add(Collection<Entity> c, MatchType matchType){
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
	
	public int size(int score){
		int rtn = 0;
		PriorityQueue<Entity> q = new PriorityQueue<Entity>();
		
		while(!this.matches.isEmpty()){
			Entity e = this.matches.poll();
			if(e.score > score)
				rtn++;
			q.add(e);
		}
		
		this.matches = q;
		return rtn;
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
		for(Entity e : this){
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
		return this.lst.iterator();
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
					(this.exactStringMatches.contains(e) ? 1 : 0) + "," + (this.cleanedStringMatches.contains(e) ? 1 : 0) + "," + 
					(this.exactSubsMatches.containsKey(e) ? this.exactSubsMatches.get(e) : 0) + "," + 
					(this.wikiMatches.contains(e) ? 1 : 0) + "," + (this.exactAbbrvMatches.contains(e) ? 1 : 0) + "," + 
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
					(this.wikiMatches.contains(e) ? 1 : 0) + "," + (this.exactAbbrvMatches.contains(e) ? 1 : 0) + "," + 
					1 + "\n";
			}
		}
		
		return s;
	}
	
	public String toOutputString(){
		String rtn = "";
		int cnt = 0;
		
		for(Entity e : this){
			if(cnt < 5)
				rtn += e + "\n";
			else
				break;
			cnt++;
		}
		
		return rtn;
	}
}
