package analysis;
import java.io.*;
import java.util.*;

import wrappers.Result;

import matching.Utils;

public class CompareAllCombos {

	public static void main(String[] args) throws FileNotFoundException{
		
		for(File in : new File("output").listFiles()){
			if(in.getName().matches("^output.*")){
				List<Result> matches = Utils.parseOutputFile(in);
				
				List<String> noMatches = new ArrayList<String>();
				int totalMatches = 0;
				for(Result res : matches){
					int i = res.size();
					if(i < 5){
						noMatches.add(res.query);
					}
					totalMatches += i;
				}
				
				System.out.println("-------------------------------------");
				System.out.println("Results for " + in.getName());
				System.out.println("-------------------------------------");
				System.out.println("Total Number of Matches: " + totalMatches);
				System.out.println("Average Number of Matches Per Entity:" + (double)totalMatches / (double)matches.size());
				System.out.println("Entities with less than 5 matches:");
				Utils.printList(noMatches);
			}
		}
	}
}
