package matching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import wrappers.Entity;
import wrappers.MatchType;
import wrappers.Options;
import wrappers.Result;

/**
 * This class manages and searches the set of Freebase entities.
 */
public class Freebase implements Iterable<Entity>{
	
	public static final String FREEBASE_ENTITIES = "data/output.fbid-prominence.sorted";
	public static final String WIKI_ALIASES = "data/output.wiki-aliases.sorted";
	
	private static final int ACRO_THRESHOLD = 20;
	
	private List<Entity> entities;
	private Map<String, Entity> idLookup;
	
	private Map<String, Set<Entity>> exactStringLookup;
	private Map<String, Set<Entity>> cleanedStringLookup;
	private Map<String, Set<Entity>> exactSubsLookup;
	private Map<String, Set<Entity>> exactAbbrvLookup;
	private Map<String, Set<Entity>> wikiLookup;
	
	public Freebase(){
		this.entities = new ArrayList<Entity>();
		
		this.idLookup = new HashMap<String, Entity>();
		this.exactStringLookup = new HashMap<String, Set<Entity>>();
		this.cleanedStringLookup = new HashMap<String, Set<Entity>>();
		this.exactAbbrvLookup = new HashMap<String, Set<Entity>>();
		this.exactSubsLookup = new HashMap<String, Set<Entity>>();
		this.wikiLookup = new HashMap<String, Set<Entity>>();
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
				
				String[] parts = e.cleanedContents.split("( |_|-|,)");
				if(parts.length > 1){
					for(String word : parts){
						if(word.length() > 3){
							if(!this.exactSubsLookup.containsKey(word))
								this.exactSubsLookup.put(word, new HashSet<Entity>());
							this.exactSubsLookup.get(word).add(e);
						}
					}
				}
				
				if(e.inlinks >= ACRO_THRESHOLD){
					String acronym = Acronym.computeAcronym(e.contents);
					if(acronym != null){
						if(!this.exactAbbrvLookup.containsKey(acronym))
							this.exactAbbrvLookup.put(acronym, new HashSet<Entity>());
						this.exactAbbrvLookup.get(acronym).add(e);
					}
				}
			}
			
		}
	}
	
	public Result getMatches(String query){
		Result res = new Result(Utils.cleanString(query), query);
		
		loadMatches(query, res);
		
		//if(res.size(50) < 1){
			
		//}
		
		res.sort(true);
		return res;
	}
	
	private void loadMatches(String query, Result res){
		res.add(this.exactStringLookup.get(query), MatchType.EXACT);
		
		res.add(this.cleanedStringLookup.get(query), MatchType.CLEANED);
		
		if(query.endsWith("s")){
			String stub = query.substring(0, query.length() - 1);
			res.add(this.exactStringLookup.get(stub), MatchType.EXACT);
			res.add(this.exactSubsLookup.get(stub), MatchType.SUB);
		}
		
		res.add(this.wikiLookup.get(query), MatchType.WIKI);
		
		res.add(this.exactSubsLookup.get(Utils.cleanString(query)), MatchType.SUB);
		
		String[] parts = query.split("( |_|-|,)");
		if(parts.length > 1){
			for(String word : parts){
				if(word.length() > 3)
					res.add(this.exactSubsLookup.get(word), MatchType.SUB);
			}
		}
		
		if(Acronym.isAcronym(query)){
			res.add(this.exactAbbrvLookup.get(Acronym.cleanAcronym(query)), MatchType.ABBRV);
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
	
	public static Freebase loadFreebaseEntities(Options opt, boolean loadAliases) throws FileNotFoundException{
		System.out.print("Loading Freebase...");
		
		Freebase fb = new Freebase();
		Scanner s = new Scanner(new File(FREEBASE_ENTITIES));
		int offset = 0;
		int max = 50000;
		
		while(s.hasNextLine()){
			Entity e = Entity.fromString(s.nextLine(), offset++);
			e.normInlinks = Math.min(1.0, (double)e.inlinks / (double)max);
			fb.add(e, loadAliases);
		}
		s.close();
		
		System.out.println("Complete!");
		
		if(loadAliases){
			System.out.print("Loading Wiki Aliases...");
			
			s = new Scanner(new File(WIKI_ALIASES));
			while(s.hasNextLine()){
				String[] parts = s.nextLine().split("\t");
				Entity e = fb.find(parts[3]);
				if(e != null)
					fb.add(e, parts[0]);
			}
			s.close();
			
			System.out.println("Complete!");
		}

		return fb;
	}
	
	private void add(Entity e, String alias){
		if(e != null){
			if(!this.wikiLookup.containsKey(alias))
				this.wikiLookup.put(alias, new HashSet<Entity>());
			this.wikiLookup.get(alias).add(e);
		}
	}
	
}
