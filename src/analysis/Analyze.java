package analysis;

import java.io.*;
import java.util.*;

import matching.Freebase;
import matching.Mapper;
import matching.Utils;

import wrappers.Entity;
import wrappers.Result;

public class Analyze {

	public static void main(String[] args) throws FileNotFoundException{
		String fileName = "output/output-full-123.txt";
		
		
		Freebase fb = Mapper.loadFreebaseEntities(Mapper.FREEBASE_ENTITIES, true, true, true, true);
		System.out.println("Complete!");
		
		Map<String, String> correctMatches = loadCorrectMatches(fb);
		//Map<String, String> missingMatches = loadMissingMatches();
		
		List<Result> results = Utils.parseOutputFile(new File(fileName), fb);
		
		for(Result res : results){
			String correctID = correctMatches.get(res.query);
			if(res.hasMatch(correctID)){
				Entity e = res.getMatch(correctID);
				
				int fbDepth = e.offset + 1;
				int resDepth = res.getDepth(correctID);
				
				System.out.println(res.query + "\t" + fbDepth + "\t" + resDepth);
			}
		}
	}
	
	private static Map<String, String> loadCorrectMatches(Freebase fb) throws FileNotFoundException{
		Map<String, String> rtn = new HashMap<String, String>();
		Scanner s = new Scanner(new FileReader(new File("output/keys/match-lookup.txt")));
		
		while(s.hasNextLine()){
			String[] parts = s.nextLine().split("\t");
			rtn.put(parts[0], parts[1]);
		}
		
		return rtn;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, String> loadMissingMatches(){
		Map<String, String> rtn = new HashMap<String, String>();
		
		return rtn;
	}
}
