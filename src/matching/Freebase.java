package matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import wrappers.Entity;


public class Freebase implements Iterable<Entity>{
	
	private final boolean SUB_AB;
	private final boolean SUB_BA;
	private final boolean DIST;
	private final boolean ACRO;

	public long c1 = 0;
	public long c2 = 0;
	public long c3 = 0;
	public long c4 = 0;
	
	private List<Entity> entities;
	private Map<String, Entity> idLookup;
	private Map<String, Entity> contentLookup;
	
	public Freebase(boolean SUB_AB, boolean SUB_BA, boolean DIST, boolean ACRO){
		this.SUB_AB = SUB_AB;
		this.SUB_BA = SUB_BA;
		this.DIST = DIST;
		this.ACRO = ACRO;
		
		entities = new ArrayList<Entity>();
		idLookup = new HashMap<String, Entity>();
		contentLookup = new HashMap<String, Entity>();
	}
	
	public void add(Entity e){
		entities.add(e);
		idLookup.put(e.id, e);
		if(contentLookup.containsKey(e.contents)){
			if(e.inlinks > contentLookup.get(e.contents).inlinks)
				e = contentLookup.get(e.contents);
		}
		contentLookup.put(e.contents, e);
	}
	
	public List<Entity> getMatches(String key, int maxMatches, Integer depth){
		List<Entity> rtn = new ArrayList<Entity>();
		int matches = 0;
		depth = 0;
		for(Entity ent : this){
			depth++;
			if(this.matches(ent.contents, key, ((double)depth) / this.size())){
				matches++;
				rtn.add(ent);
				if(maxMatches != -1 && matches == maxMatches)
					break;
			}
		}
		return rtn;
	}
	
	private boolean matches(String fbEnt, String rvEnt, double pt){
		boolean rtn = false;
		
		if(SUB_AB){
			long start = System.nanoTime();
			rtn = rtn || fbEnt.contains(rvEnt);
			this.c1 += System.nanoTime() - start;
		}
		
		if(SUB_BA && !rtn && fbEnt.length() > 1){
			long start = System.nanoTime();
			rtn = rtn || rvEnt.contains(fbEnt);
			this.c2 += System.nanoTime() - start;
		}
		
		if(DIST && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Utils.stringDistance(fbEnt, rvEnt, 10) < ((fbEnt.length() + rvEnt.length()) / 4) * 0.1;
			this.c3 += System.nanoTime() - start;
		}
		
		if(ACRO && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Acronym.matches(fbEnt, rvEnt) || Acronym.matches(rvEnt, fbEnt);
			this.c4 += System.nanoTime() - start;
		}
		
		return rtn;
	}
	
	public Entity find(String id){
		return this.idLookup.get(id);
	}
	
	public Entity search(String ent){
		return contentLookup.get(ent);
	}
	
	public int size(){
		return this.entities.size();
	}

	public Iterator<Entity> iterator() {
		return this.entities.iterator();
	}
}
