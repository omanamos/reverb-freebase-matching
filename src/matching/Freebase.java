package matching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import wrappers.Entity;
import wrappers.Options;
import wrappers.Result;

/**
 * This class manages and searches the set of Freebase entities.
 */
public class Freebase implements Iterable<Entity>{
	
	public static final String FREEBASE_ENTITIES = "output.fbid-prominence.sorted";
	public static final String WIKI_ALIASES = "output.wiki-aliases.sorted";
	
	private static final int ACRO_THRESHOLD = 20;
	
	/**
	 * Specifies which matching combinations to use.
	 */
	private final Options opt;

	private List<Entity> entities;
	private Map<String, List<Entity>> aliases;
	
	public Freebase(Options opt){
		this.opt = opt;
		this.entities = new ArrayList<Entity>();
		this.aliases = new HashMap<String, List<Entity>>();
	}
	
	
	/**
	 * @param e Entity to add to Freebase
	 */
	public void add(Entity e){
		if(e != null){
			this.entities.add(e);
			
			if(!this.aliases.containsKey(e.id))
				this.aliases.put(e.id, new ArrayList<Entity>());
			this.aliases.get(e.id).add(e);
			
			if(!this.aliases.containsKey(e.contents))
				this.aliases.put(e.contents, new ArrayList<Entity>());
			this.aliases.get(e.contents).add(e);
			
			if(e.cleanedContents.endsWith("s")){
				String stub = e.cleanedContents.substring(0, e.cleanedContents.length() - 1);
				if(!this.aliases.containsKey(stub))
					this.aliases.put(stub, new ArrayList<Entity>());
				this.aliases.get(stub).add(e);
			}
			
			if(this.opt.SUB_AB){
				String[] parts = e.contents.split("( |_|-|,)");
				if(parts.length > 1){
					for(String word : parts){
						if(word.length() > 3){
							if(!this.aliases.containsKey(word))
								this.aliases.put(word, new ArrayList<Entity>());
							this.aliases.get(word).add(e);
						}
					}
				}
			}
			
			if(this.opt.ACRO_AB && e.inlinks >= ACRO_THRESHOLD){
				String acronym = Acronym.computeAcronym(e.contents);
				if(acronym != null){
					if(!this.aliases.containsKey(acronym))
						this.aliases.put(acronym, new ArrayList<Entity>());
					this.aliases.get(acronym).add(e);
				}
			}
		}
	}
	
	public Result getMatches(String query){
		Result res = new Result(query);
		
		res.add(this.aliases.get(query));
		
		if(query.endsWith("s")){
			String stub = query.substring(0, query.length() - 1);
			res.add(this.aliases.get(stub));
		}
		
		if(this.opt.SUB_AB){
			String[] parts = query.split("( |_|-|,)");
			if(parts.length > 1){
				for(String word : parts){
					if(word.length() > 3)
						res.add(this.aliases.get(word));
				}
			}
		}
		
		if(this.opt.ACRO_AB && Acronym.isAcronym(query)){
			res.add(this.aliases.get(Acronym.cleanAcronym(query)));
		}
		
		return res;
	}
	
	/**
	 * @param id id of the Entity to search for
	 * @return Entity with the given id, null if none exists
	 */
	public Entity find(String id){
		return this.aliases.get(id) != null ? this.aliases.get(id).get(0) : null;
	}
	
	/**
	 * @param ent contents of the Entity to search for
	 * @return Entity with the given contents, null if none exists
	 */
	public Entity search(String ent){
		return this.aliases.get(ent) != null ? this.aliases.get(ent).get(0) : null;
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
	
	public static Freebase loadFreebaseEntities(Options opt) throws FileNotFoundException{
		System.out.print("Loading Freebase...");
		
		Freebase fb = new Freebase(opt);
		Scanner s = new Scanner(new File(FREEBASE_ENTITIES));
		int offset = 0;
		
		while(s.hasNextLine())
			fb.add(Entity.fromString(s.nextLine(), offset++));
		
		System.out.println("Complete!");
		
		System.out.print("Loading Wiki Aliases...");
		
		s = new Scanner(new File(WIKI_ALIASES));
		while(s.hasNextLine()){
			String[] parts = s.nextLine().split("\t");
			Entity e = fb.find(parts[3]);
			if(e != null)
				fb.add(e, parts[0]);
		}
		
		System.out.println("Complete!");
		return fb;
	}
	
	private void add(Entity e, String alias){
		if(e != null){
			if(!this.aliases.containsKey(alias))
				this.aliases.put(alias, new ArrayList<Entity>());
			this.aliases.get(alias).add(e);
		}
	}
	
}
