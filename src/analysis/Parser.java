package analysis;

import java.io.*;
import java.util.*;

import wrappers.Entity;
import wrappers.Tuple;

import matching.Freebase;
import matching.Mapper;
import matching.Utils;


public class Parser {
	
	public static void main(String[] args) throws IOException{
		//parseActualMatches();
		getActualMatches();
	}
	
	public static void getActualMatches() throws IOException{
		File f = new File("output/matched results.txt");
		
		Scanner s = new Scanner(new FileReader(f));
		s.nextLine();
		
		Map<String, Entity> actualMatches = new HashMap<String, Entity>();
		List<Tuple> badRv = new ArrayList<Tuple>();
		Map<String, String> other = new HashMap<String, String>();
		
		Freebase fb = Mapper.loadFreebaseEntities(Mapper.FREEBASE_ENTITIES, true, true, true, true);
		
		while(s.hasNextLine()){
			Tuple t = Tuple.fromString(s.nextLine());
			
			switch(t.code){
				case -3: case -1:
					other.put(t.rvEnt, t.fbEnt);
					break;
				case -2:
					badRv.add(t);
					break;
				default:
					Entity e = find(fb, t.fbEnt);
					System.out.println("Matched rv(" + t.rvEnt + ") with fb(" + e.contents + ").");
					if(e.id == null){
						other.put(t.rvEnt, e.contents);
					}else
						actualMatches.put(t.rvEnt, e);
					break;
			}
		}
		
		List<String> rvEnts = Mapper.loadReverbEntities(Mapper.REVERB_ENTITIES);
		
		for(String rvEnt : rvEnts){
			if(!actualMatches.containsKey(rvEnt) && !other.containsKey(rvEnt)){
				Entity e = ask(fb, rvEnt);
				if(e.contents != null){
					if(e.id == null){
						other.put(rvEnt, e.contents);
					}else{
						actualMatches.put(rvEnt, e);
					}
					System.out.println("Matched rv(" + rvEnt + ") with fb(" + e.contents + ").");
				}else{
					badRv.add(new Tuple(rvEnt, "", -2));
				}
			}
		}
		
		
		File out = new File("output/actual-matches.txt");
		BufferedWriter w = new BufferedWriter(new FileWriter(out));
		
		for(String rvEnt : actualMatches.keySet()){
			w.write(rvEnt + "\t" + actualMatches.get(rvEnt).toString() + "\n");
			w.flush();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Not In Freebase:");
		for(String key : other.keySet()){
			System.out.println(key + "\t" + other.get(key));
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Bad Reverb Entries:");
		Utils.printList(badRv);
	}
	
	public static Entity find(Freebase fb, String key){
		Entity rtn = fb.search(key);
		return rtn == null ? ask(fb, key) : rtn;
	}
	
	public static Entity ask(Freebase fb, String key){
		System.out.println();
		Scanner s = new Scanner(System.in);
		
		Entity e = fb.search(key);
		String conf = null;
		if(e != null){
			System.out.print("Match " + key + " with " + e.contents + "(y/n)? ");
			conf = s.nextLine().trim();
			if(conf.equals("y"))
				return e;
		}else{
			List<Entity> ents = fb.getMatches(key, 1, 0);
			if(ents.size() != 0){
				e = ents.get(0);
				System.out.print("Match " + key + " with " + e.contents + "(y/n)? ");
				conf = s.nextLine().trim();
				if(conf.equals("y"))
					return e;
			}
		}
		
		System.out.println("Entity not found for " + key);
		Integer code = Utils.getInteger("Code for this entity (1=not in freebase, 2=bad reverb entity, 3=match found)?: ", s);
		
		String id = null;
		String contents = null;
		Integer inlinks = null;
		
		switch(code){
			case 3:
				
				while(e == null || !conf.equals("y")){
					code = Utils.getInteger("Search on (1=id, 2=contents): ", s);
					if(code == 1){
						System.out.print("What is the freebase id? ");
						id = s.nextLine().trim();
						e = fb.find(id);
					}else{
						System.out.print("What is the contents? ");
						contents = s.nextLine().trim();
						e = fb.search(contents);
					}
					
					if(e == null || code == 1 && id.isEmpty() || code == 2 && contents.isEmpty())
						return ask(fb, key);
					
					System.out.print("Match " + key + " with " + e.contents + "(y/n)? ");
					conf = s.nextLine().trim();
				}
				return e;
			case 1:
				System.out.print("What is the correct entity? ");
				contents = s.nextLine().trim();
		}
		
		return new Entity(id, contents, inlinks);
	}
	
	public static void parseActualMatches() throws FileNotFoundException{
		File f = new File("output/matched results.txt");
		
		Scanner s = new Scanner(new FileReader(f));
		s.nextLine();
		
		Map<String, List<Tuple>> ents = new HashMap<String, List<Tuple>>();
		
		List<Tuple> badRv = new ArrayList<Tuple>();
		List<Tuple> notInFB = new ArrayList<Tuple>();
		List<Tuple> notInOurFB = new ArrayList<Tuple>();
		
		while(s.hasNextLine()){
			Tuple t = Tuple.fromString(s.nextLine());
			
			switch(t.code){
				case -3:
					notInOurFB.add(t);
					break;
				case -2:
					badRv.add(t);
					break;
				case -1:
					notInFB.add(t);
					break;
				default:
					if(!ents.containsKey(t.fbEnt))
						ents.put(t.fbEnt, new ArrayList<Tuple>());
					
					ents.get(t.fbEnt).add(t);
					break;
			}
		}
		
		System.out.println("Incorrect Entities:");
		for(String fbEnt : ents.keySet()){
			System.out.println(fbEnt + "\t" + ents.get(fbEnt).get(0).code);
			for(Tuple t : ents.get(fbEnt))
				System.out.println("\t" + t.rvEnt);
		}
		System.out.println();
		System.out.println();
		
		System.out.println("Bad Reverb Entities:");
		Utils.printList(badRv);
		System.out.println();
		System.out.println();
		
		System.out.println("Entities not in Freebase:");
		Utils.printList(notInFB);
		System.out.println();
		System.out.println();
		
		System.out.println("Entities not in our Freebase dump:");
		Utils.printList(notInOurFB);
	}
}
