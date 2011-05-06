package analysis;

import java.io.*;
import java.util.*;

import matching.Freebase;
import matching.Mapper;
import matching.Utils;

import wrappers.Entity;
import wrappers.Options;
import wrappers.Result;

/**
 * This script analyzes the results generated by {@link Mapper}. It outputs a file of the following format:<br>
 * [Reverb Entity] [Correct Entity Depth in Freebase] [Correct Entity Depth in Matched Freebase Entities]
 */
public class Analyze {

	public static void main(String[] args) throws IOException{
		//Ouput file to analyze
		String fileName = "output/output-full-v5.3.txt";
		
		//Load in freebase
		Freebase fb = Freebase.loadFreebaseEntities(Options.getDefaults(), false);
		
		Map<String, String> correctMatches = loadCorrectMatches();
		
		List<Result> results = Utils.parseOutputFile(new File(fileName), fb);
		Set<Result> uniqueResults = new HashSet<Result>();
		
		for(Result res : results){
			if(correctMatches.containsKey(res.q.orig) && !uniqueResults.contains(res)){
				String correctID = correctMatches.get(res.q.orig);
				if(res.hasMatch(correctID)){
					Entity e = res.getMatch(correctID);
					
					int fbDepth = e.offset + 1;
					int resDepth = res.getDepth(correctID);
					
					System.out.println(res.q.orig + "\t" + fbDepth + "\t" + resDepth);
				}else{
					System.out.println(res.q.orig + "\t" + Integer.MAX_VALUE + "\t" + Integer.MAX_VALUE);
				}
				uniqueResults.add(res);
			}
		}
	}
	
	/**
	 * @param fb Fully loaded Freebase
	 * @return Reverb Entity -> id of correct Freebase match
	 * @throws FileNotFoundException
	 */
	public static Map<String, String> loadCorrectMatches() throws FileNotFoundException{
		Map<String, String> rtn = new HashMap<String, String>();
		Scanner s = new Scanner(new FileReader(new File("data/keys/match-lookup.txt")));
		
		while(s.hasNextLine()){
			String[] parts = s.nextLine().split("\t");
			rtn.put(parts[0], parts[1]);
		}
		
		return rtn;
	}
	
	public static Set<String> loadImpossibleMatches() throws FileNotFoundException{
		Set<String> rtn = new HashSet<String>();
		
		Scanner s = new Scanner(new FileReader(new File("data/keys/bad-reverb-entities.txt")));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		
		s = new Scanner(new FileReader(new File("data/keys/not-in-freebase.txt")));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		
		return rtn;
	}
}
