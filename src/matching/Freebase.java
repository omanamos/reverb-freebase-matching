package matching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import wrappers.Document;
import wrappers.Entity;
import wrappers.MatchType;
import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Query;
import wrappers.Resources;
import wrappers.Result;
import wrappers.Tuple;
import wrappers.Weights;

/**
 * This class manages and searches the set of Freebase entities.
 */
public class Freebase implements Iterable<Entity>{
	
	private static final int ACRO_THRESHOLD = 20;
	
	private final int luceneThreshold;
	private Weights w;
	
	//Lookup and storage
	private List<Entity> entities;
	private Map<String, Entity> idLookup;
	
	//Exact Matching
	private Map<String, Set<Entity>> exactStringLookup;
	private Map<String, Set<Entity>> cleanedStringLookup;
	
	//Word Overlap Matching
	private Map<String, Set<Entity>> wordOverlapLookup;
	private Map<String, Double> wordOverlapWeights;
	private Set<String> wordOverlapBlackList;
	
	//Other Matching
	private Map<String, Set<Entity>> abbrvLookup;
	private Map<String, Set<Entity>> wikiLookup;
	
	//Partial Matching
	private SpellChecker dict;
	private StringDistance dist;
	
	public Freebase(int luceneThreshold) throws IOException{
		this.luceneThreshold = luceneThreshold;
		this.entities = new ArrayList<Entity>();
		
		this.idLookup = new HashMap<String, Entity>();
		this.exactStringLookup = new HashMap<String, Set<Entity>>();
		this.cleanedStringLookup = new HashMap<String, Set<Entity>>();
		this.abbrvLookup = new HashMap<String, Set<Entity>>();
		
		this.wordOverlapLookup = new HashMap<String, Set<Entity>>();
		this.wordOverlapWeights = new HashMap<String, Double>();
		this.wordOverlapBlackList = new HashSet<String>();
		
		this.wikiLookup = new HashMap<String, Set<Entity>>();
		
		this.dict = new SpellChecker(new RAMDirectory());
		this.dict.indexDictionary(new LuceneDictionary(IndexReader.open(new SimpleFSDirectory(new File("index"))), "entity"));
		this.dist = dict.getStringDistance();
		
		this.w = new Weights(new File(Resources.WEIGHTS_CONFIG));
	}

	public void add(Entity e){
		this.add(e, true);
	}

	/**
	 * @param e Entity to add to Freebase
	 * @param loadAliases loads aliases of the given entity to allow searching if true
	 */
	public void add(Entity e, boolean loadAliases){
		if(e != null){
			this.entities.add(e);
			
			if(!this.exactStringLookup.containsKey(e.contents))
				this.exactStringLookup.put(e.contents, new HashSet<Entity>());
			this.exactStringLookup.get(e.contents).add(e);
			
			this.idLookup.put(e.id, e);
			
			if(loadAliases){
				if(e.contents.endsWith("s")){
					String stub = e.contents.substring(0, e.contents.length() - 1);
					if(!this.exactStringLookup.containsKey(stub))
						this.exactStringLookup.put(stub, new HashSet<Entity>());
					this.exactStringLookup.get(stub).add(e);
				}
				
				if(!e.noCleaning){
					if(!this.cleanedStringLookup.containsKey(e.cleanedContents))
						this.cleanedStringLookup.put(e.cleanedContents, new HashSet<Entity>());
					this.cleanedStringLookup.get(e.cleanedContents).add(e);
					
					if(e.cleanedContents.endsWith("s")){
						String stub = e.cleanedContents.substring(0, e.cleanedContents.length() - 1);
						if(!this.cleanedStringLookup.containsKey(stub))
							this.cleanedStringLookup.put(stub, new HashSet<Entity>());
						this.cleanedStringLookup.get(stub).add(e);
					}
				}
				
				String[] parts = Utils.split(e.cleanedContents);
				if(parts.length > 1){
					for(String word : parts){
						if(!this.wordOverlapBlackList.contains(word) && word.length() > 2){
							if(!this.wordOverlapLookup.containsKey(word))
								this.wordOverlapLookup.put(word, new HashSet<Entity>());
							this.wordOverlapLookup.get(word).add(e);
						}
					}
				}
				
				if(e.inlinks >= ACRO_THRESHOLD){
					String acronym = Acronym.computeAcronym(e.contents);
					if(acronym != null){
						if(!this.abbrvLookup.containsKey(acronym))
							this.abbrvLookup.put(acronym, new HashSet<Entity>());
						this.abbrvLookup.get(acronym).add(e);
					}
				}
			}
			
		}
	}
	
	public Result getMatches(String query, PerformanceFactor pf){
		Query q = new Query(query);
		Result res = new Result(q, w);
		
		loadMatches(q, res, pf);
		res.sort(true);
		
		if(res.size(this.luceneThreshold) < 1){
			try {
				pf.start();
				loadPartialMatches(q, res);
				pf.end(MatchType.LUCENE);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		res.sort(true);
		return res;
	}
	
	private void loadPartialMatches(Query q, Result res) throws IOException {
		String[] similar;
		try{//Need the try catch b/c some poorly extracted reverb entities have too many terms for lucene
			similar = this.dict.suggestSimilar(q.cleanedQ, 5);
		}catch(Exception e){
			return;
		}
		
		for(String query : similar){
			double dist = this.dist.getDistance(query, q.cleanedQ);
			res.setFactor(dist);
			this.loadMatches(new Query(query), res, new PerformanceFactor());
			res.setFactor(1.0);
		}
	}
	
	private void loadMatches(Query q, Result res, PerformanceFactor pf){
		pf.start();
		pf.match(MatchType.EXACT, res.add(this.exactStringLookup.get(q.q), q, MatchType.EXACT));
		pf.match(MatchType.EXACT, res.add(this.exactStringLookup.get(q.cleanedQ), q, MatchType.EXACT));
		pf.end(MatchType.EXACT);
		
		pf.start();
		pf.match(MatchType.CLEANED, res.add(this.cleanedStringLookup.get(q.cleanedQ), q, MatchType.CLEANED));
		pf.end(MatchType.CLEANED);
		
		if(q.cleanedQ.endsWith("s")){
			String stub = q.cleanedQ.substring(0, q.cleanedQ.length() - 1);
			Query s = new Query(stub);
			
			pf.start();
			pf.match(MatchType.EXACT, res.add(this.exactStringLookup.get(stub), s, MatchType.EXACT));
			pf.end(MatchType.EXACT);
			
			pf.start();
			pf.match(MatchType.SUB, res.add(this.wordOverlapLookup.get(stub), s, MatchType.SUB, this.wordOverlapWeights.get(stub)));
			pf.end(MatchType.SUB);
		}
		
		pf.start();
		pf.match(MatchType.WIKI, res.add(this.wikiLookup.get(q.cleanedQ), q, MatchType.WIKI));
		pf.end(MatchType.WIKI);
		
		pf.start();
		pf.match(MatchType.SUB, res.add(this.wordOverlapLookup.get(q.cleanedQ), q, MatchType.SUB, this.wordOverlapWeights.get(q.cleanedQ)));
		pf.end(MatchType.SUB);
		
		if(q.size() > 1){
			for(Tuple<String,Boolean> word : q){
				if(!this.wordOverlapBlackList.contains(word.e) && word.e.length() > 1){
					pf.start();
					pf.match(MatchType.SUB, res.add(this.exactStringLookup.get(word.e), new Query(word.e), MatchType.SUB, 1.5));
					pf.end(MatchType.SUB);
					
					if(word.e.endsWith("s")){
						String stub = q.cleanedQ.substring(0, q.cleanedQ.length() - 1);
						Query s = new Query(stub);
						
						pf.start();
						pf.match(MatchType.SUB, res.add(this.exactStringLookup.get(stub), s, MatchType.SUB, 1.5));
						pf.end(MatchType.SUB);
						
						pf.start();
						pf.match(MatchType.SUB, res.add(this.wordOverlapLookup.get(stub), s, MatchType.SUB, this.wordOverlapWeights.get(stub)));
						pf.end(MatchType.SUB);
					}
					if(word.v){
						pf.start();
						pf.match(MatchType.ABBRV, res.add(this.abbrvLookup.get(Acronym.cleanAcronym(word.e)), new Query(word.e), MatchType.ABBRV));
						pf.end(MatchType.ABBRV);
					}
					
					pf.start();
					pf.match(MatchType.SUB, res.add(this.wordOverlapLookup.get(word.e), new Query(word.e), MatchType.SUB, this.wordOverlapWeights.get(word.e)));
					pf.end(MatchType.SUB);
				}
			}
		}
		
		if(q.isAcronym){
			pf.start();
			pf.match(MatchType.ABBRV, res.add(this.abbrvLookup.get(Acronym.cleanAcronym(q.q)), q, MatchType.ABBRV));
			pf.end(MatchType.ABBRV);
		}
		
		res.sort(true);
	}
	
	/**
	 * @param id id of the Entity to search for
	 * @return Entity with the given id, null if none exists
	 */
	public Entity find(String id){
		return this.idLookup.get(id);
	}
	
	/**
	 * @param ent contents of the Entity to search for
	 * @return Entity with the given contents, null if none exists
	 */
	public Entity search(String ent){
		Set<Entity> s = this.exactStringLookup.get(ent);
		if(s == null) return null;
		
		for(Entity e : s)
			return e;
		return null;
	}
	
	/**
	 * @return number of entities in Freebase
	 */
	public int size(){
		return this.entities.size();
	}

	public Iterator<Entity> iterator() {
		return this.entities.iterator();
	}
	
	public void updateWeights(Weights w){
		this.w = w;
	}
	
	public static Freebase loadFreebase(boolean loadAliases) throws IOException{
		return loadFreebase(loadAliases, Resources.DEFAULT_FREEBASE, Resources.DEFAULT_WIKI_ALIASES, Options.DEFAULT_LUCENE_THRESHOLD);
	}
	
	public static Freebase loadFreebase(boolean loadAliases, String freebasePath, String wikiAliasPath, int luceneThreshold) throws IOException{
		Freebase fb = new Freebase(luceneThreshold);
		Scanner s = new Scanner(new File(freebasePath));
		
		if(loadAliases){
			System.out.print("Loading Word Weights...");
			Document d = new Document(new File(Resources.WORD_WEIGHTS), true);
			double normMax = Math.log(d.getMaxFreq());
			
			for(Tuple<String,Integer> t : d){
					String key = t.e;
					Double weight = (normMax - Math.log(t.v)) / normMax;
					fb.wordOverlapWeights.put(key, weight);
			}
			
			Scanner in = new Scanner(new File(Resources.STOP_WORDS));
			while(in.hasNextLine()){
				fb.wordOverlapBlackList.add(in.nextLine());
			}
			System.out.println("Complete!");
		}
		
		System.out.print("Loading Freebase...");
		int offset = 0;
		double max = -1.0;
		
		while(s.hasNextLine()){
			Entity e = Entity.fromString(s.nextLine(), offset++);
			
			if(max == -1.0)
				max = Math.log(e.inlinks);
			
			e.normInlinks = Math.log(e.inlinks) / max;
			fb.add(e, loadAliases);
		}
		s.close();
		System.out.println("Complete!");
		
		if(loadAliases){
			System.out.print("Loading Wiki Aliases...");
			
			s = new Scanner(new File(wikiAliasPath));
			while(s.hasNextLine()){
				String[] parts = s.nextLine().split("\t");
				Entity e = fb.find(parts[3]);
				fb.addAlias(e, Utils.cleanString(parts[0].toLowerCase()));
			}
			s.close();
			
			System.out.println("Complete!");
		}

		return fb;
	}
	
	/**
	 * Adds mapping between alias and Entity if e != null
	 * @param e Entity to map to
	 * @param alias Some alias of given Entity
	 */
	private void addAlias(Entity e, String alias){
		if(e != null){
			if(!this.wikiLookup.containsKey(alias))
				this.wikiLookup.put(alias, new HashSet<Entity>());
			this.wikiLookup.get(alias).add(e);
		}
	}
}
