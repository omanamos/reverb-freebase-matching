package wrappers;

import java.util.*;

import wrappers.Weights.Attr;
import analysis.Analyze;

/**
 * Wraps a set of matches found in Freebase for a given query. Allows for quick id lookup. Also provides other functionality.
 */
public class Result implements Iterable<Entity>{

	private static final double LUCENE_SCALING_FACTOR = 0.2;
	
	public final Weights w;
	public final Query q;
	private List<Entity> lst;
	private Map<String, Entity> idLookup;
	private Set<Match> m;
	private Double factor;
	
	private Set<Entity> exactStringMatches;
	private Set<Entity> cleanedStringMatches;
	private Map<Entity, Integer> exactSubsMatches;
	private Set<Entity> exactAbbrvMatches;
	private Set<Entity> wikiMatches;
	private Map<Entity, Double> luceneMatches;
	
	public Result(Query q, Weights w){
		this.q = q;
		this.m = new HashSet<Match>();
		this.lst = new ArrayList<Entity>();
		this.idLookup = new HashMap<String, Entity>();

		this.exactStringMatches = new HashSet<Entity>();
		this.cleanedStringMatches = new HashSet<Entity>();
		this.exactSubsMatches = new HashMap<Entity, Integer>();
		this.exactAbbrvMatches = new HashSet<Entity>();
		this.wikiMatches = new HashSet<Entity>();
		this.luceneMatches = new HashMap<Entity, Double>();
		this.factor = 1.0;
		this.w = w;
	}
	
	/**
	 * @param e Matched Entity to add to this Result
	 */
	public void add(Match m){
		if(!this.m.contains(m)){
			this.m.add(m);
			if(!this.idLookup.containsKey(m.e.id) && this.factor != 1.0)
				this.luceneMatches.put(m.e, this.factor);
			
			switch(m.t){
				case EXACT:
					this.exactStringMatches.add(m.e);
					break;
				case SUB:
					if(!this.exactSubsMatches.containsKey(m.e))
						this.exactSubsMatches.put(m.e, 0);
					this.exactSubsMatches.put(m.e, this.exactSubsMatches.get(m.e) + 1);
					break;
				case ABBRV:
					this.exactAbbrvMatches.add(m.e);
					break;
				case WIKI:
					this.wikiMatches.add(m.e);
					break;
				case CLEANED:
					this.cleanedStringMatches.add(m.e);
					break;
			}
			
			if(!this.idLookup.containsKey(m.e.id)){
				this.idLookup.put(m.e.id, m.e);
			}
		}
	}
	
	/**
	 * @param c collection of Entities to add
	 */
	public int add(Collection<Entity> c, Query q, MatchType m){
		if(c != null){
			for(Entity e : c)
				this.add(new Match(q, e, m));
			return c.size();
		}
		return 0;
	}
	
	public void sort(boolean computeScores){
		PriorityQueue<Entity> q = new PriorityQueue<Entity>();
		this.lst.clear();
		for(String key : this.idLookup.keySet()){
			Entity e = this.idLookup.get(key);
			if(computeScores)
				e.score = computeScore(e, this.luceneMatches.get(e));
			q.add(this.idLookup.get(key));
		}
		
		while(!q.isEmpty())
			this.lst.add(q.poll());
	}
	
	private Score computeScore(Entity e, Double factor){
		factor = factor == null ? 1.0 : LUCENE_SCALING_FACTOR * factor;
		return new Score(this.w.getWeight(Attr.inlinks) * e.normInlinks,
				         factor,
				         this.w.getWeight(Attr.exact) * (this.exactStringMatches.contains(e) ? 1 : 0),
				         this.w.getWeight(Attr.cleaned) * (this.cleanedStringMatches.contains(e) ? 1 : 0),
				         this.w.getWeight(Attr.substr) * (this.exactSubsMatches.containsKey(e) && !this.exactStringMatches.contains(e) && !this.cleanedStringMatches.contains(e) ? this.exactSubsMatches.get(e) : 0), 
				         this.w.getWeight(Attr.abbrv) * (this.exactAbbrvMatches.contains(e) && !this.exactSubsMatches.containsKey(e) && !this.exactStringMatches.contains(e) && !this.cleanedStringMatches.contains(e) ? 1 : 0), 
				         this.w.getWeight(Attr.wiki) * (this.wikiMatches.contains(e) ? 1 : 0));
	}
	
	public void setFactor(double factor){
		this.factor = factor;
	}
	
	/**
	 * @param id match to search for
	 * @return true if contains a match that has the given id
	 */
	public boolean hasMatch(String id){
		return id != null && this.idLookup.containsKey(id);
	}
	
	public boolean hasLuceneMatches(){
		return !this.luceneMatches.isEmpty();
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
		return this.m.size();
	}
	
	public int size(int score){
		int rtn = 0;
		
		for(Entity e : this.lst)
			if(e.score.total >= score)
				rtn++;
		
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
	
	public Iterator<Entity> iterator() {
		return this.lst.iterator();
	}
	
	/**
	 * hashes based on the this.query
	 */
	public int hashCode(){
		return this.q.hashCode();
	}
	
	/**
	 * Equality is determined based on this.query
	 */
	public boolean equals(Object other){
		if(other instanceof Result){
			return ((Result)other).q.equals(this.q);
		}else{
			return false;
		}
	}
	
	public String toString(int depth){
		String rtn = this.q.orig;
		int i = 0;
		for(Entity e : this){
			rtn += "\n\t" + e;
			i++;
			if(i == depth)
				break;
		}
		
		return rtn + "\n";
	}

	public String toString(){
		String s = "";
		int cnt = 0;
		String correctID = null; 
		try{
			correctID = Analyze.loadCorrectMatches().get(this.q.q);
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
