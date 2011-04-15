package matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import wrappers.Entity;
import wrappers.Options;
import wrappers.Result;
import wrappers.PerformanceFactor;

/**
 * This class manages and searches the set of Freebase entities.
 */
public class Freebase implements Iterable<Entity>{
	
	/**
	 * Specifies which matching combinations to use.
	 */
	private final Options opt;

	//USED FOR PERFORMANCE BENCHMARKING
	public long c1 = 0;
	public long c2 = 0;
	public long c3 = 0;
	public long c4 = 0;
	public long c5 = 0;
	
	
	private List<Entity> entities;
	private Map<String, Entity> idLookup;
	private Map<String, Entity> contentLookup;
	
	public Freebase(Options opt){
		this.opt = opt;
		
		entities = new ArrayList<Entity>();
		idLookup = new HashMap<String, Entity>();
		contentLookup = new HashMap<String, Entity>();
	}
	
	/**
	 * @param e Entity to add to Freebase
	 */
	public void add(Entity e){
		entities.add(e);
		idLookup.put(e.id, e);
		
		//Shouldn't need a check, but in case there are duplicate Freebase entities, pick the one with the highest inlinks
		if(contentLookup.containsKey(e.contents)){
			if(e.inlinks > contentLookup.get(e.contents).inlinks)
				e = contentLookup.get(e.contents);
		}
		contentLookup.put(e.contents, e);
		
		if(this.opt.ACRO_AB)
			e.acronym = Acronym.computeAcronym(e.contents);
	}
	
	/**
	 * @param query entity to search for matches for
	 * @param maxMatches maximum number of matches to return
	 * @param pf used for performance benchmarks
	 * @return {@link Result} containing any matches found
	 */
	public Result getMatches(String query, int maxMatches, PerformanceFactor pf){
		Result rtn = new Result(query);
		int matches = 0;
		pf.depth = 0;
		long timer = 0;
		
		for(Entity ent : this){
			pf.depth++;
			timer = System.nanoTime();
			if(this.matches(ent, query, ((double)pf.depth) / this.size())){
				timer = System.nanoTime() - timer;
				pf.updateTimer(timer);
				
				matches++;
				rtn.add(ent);
				if(maxMatches != -1 && matches == maxMatches)
					break;
			}else{
				timer = System.nanoTime() - timer;
				pf.updateTimer(timer);
			}
		}
		return rtn;
	}
	
	/**
	 * @param fbEnt entity in Freebase
	 * @param rvEnt entity in Reverb
	 * @param pt current percent of Freebase entities searched
	 * @return true if the two entities match, false otherwise
	 */
	private boolean matches(Entity fbEnt, String rvEnt, double pt){
		boolean rtn = false;
		
		if(this.opt.SUB_AB){
			long start = System.nanoTime();
			rtn = rtn || fbEnt.contents.contains(rvEnt);
			this.c1 += System.nanoTime() - start;
		}
		
		if(this.opt.SUB_BA && !rtn && fbEnt.contents.length() > 1){
			long start = System.nanoTime();
			rtn = rtn || rvEnt.contains(fbEnt.contents);
			this.c2 += System.nanoTime() - start;
		}
		
		if(this.opt.DIST && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Utils.stringDistance(fbEnt.contents, rvEnt, 10) < (fbEnt.contents.length() + rvEnt.length()) / 6 + 1;
			this.c3 += System.nanoTime() - start;
		}
		
		if(this.opt.ACRO_AB && !rtn && pt < 0.2){
			long start = System.nanoTime();
			rtn = rtn || fbEnt.hasAcronym() && Acronym.acrMatch(rvEnt, fbEnt.acronym); //Acronym.cleanAcronym(rvEnt).equals(fbEnt.acronym);
			this.c4 += System.nanoTime() - start;
		}
		
		if(this.opt.ACRO_BA && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Acronym.matches(fbEnt.contents, rvEnt);
			this.c5 += System.nanoTime() - start;
		}
		
		return rtn;
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
		return contentLookup.get(ent);
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
}
