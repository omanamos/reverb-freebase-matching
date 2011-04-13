package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import wrappers.Entity;

public class Mapper {
	public static final String FREEBASE_ENTITIES = "output.fbid-prominence.sorted";
	public static final String REVERB_ENTITIES = "input/entity_list.txt";

	public static void main(String[] args) throws IOException{
		boolean subAB = true;
		boolean subBA = true;
		boolean dist = true;
		boolean acro = true;
		String output = "output/output.txt";
		int maxMatches = 5;
		
		//Parse arguments
		if(args.length > 1){
			
			int p = Integer.parseInt(args[0]);
			subAB = false;
			
			while(p >= 1){
				
				int tmp = p % 10;
				
				switch(tmp){
					case 1:
						subAB = true;
						break;
					case 2:
						subBA = true;
						break;
					case 3:
						dist = true;
						break;
					case 4:
						acro = true;
						break;
					default:
						System.out.println("Invalid arguments");
						return;
				}
				p = p / 10;
			}
			
			if(args.length > 1){
				output = args[1];
				
				if(args.length == 3){
					maxMatches = Integer.parseInt(args[2]);
				}else if(args.length != 0){
					System.out.println("Invalid arguments");
					return;
				}
			}
		}
		
		System.out.print("Loading Freebase Entity List...");
		Freebase fb = loadFreebaseEntities(FREEBASE_ENTITIES, subAB, subBA, dist, acro);
		System.out.println("Complete!");

		System.out.print("Loading Reverb Entity List...");
		List<String> rv = loadReverbEntities(REVERB_ENTITIES);
		System.out.println("Complete!");

		BufferedWriter w = new BufferedWriter(new FileWriter(new File(output)));
		long start = System.currentTimeMillis();
		int cnt = 0;
		int lastEntFoundTotal = 0;
		
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			w.flush();
			
			Integer depth = 0;
			List<Entity> matches = fb.getMatches(rvEnt, maxMatches, depth);
			for(Entity match : matches){
				w.write("\t" + match + "\n");
				w.flush();
			}
			
			cnt++;
			lastEntFoundTotal += depth;
			System.out.println(rvEnt + " matches:");
			Utils.printList(matches, "\t");
			System.out.println("\t" + (100 * cnt / (double)rv.size()) + "% @ depth = " + depth + " (" + ((double)depth) / fb.size() + ")");
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Average depth that the nth match was found at = " + ((double)lastEntFoundTotal) / cnt);
		
		long totalTime = System.currentTimeMillis() - start;
		double timePerEntry = totalTime / (cnt * 1000);
		System.out.println("Total Time for " + cnt + " entries = " + totalTime / 1000 + " seconds");
		System.out.println("Average time per entry = " + timePerEntry + " seconds");
		double entryPerSecond = 1.0 / timePerEntry;
		System.out.println("Processed ~" + entryPerSecond + " entries per second");
		
		long total = fb.c1 + fb.c2 + fb.c3 + fb.c4;
		System.out.println("Time spent computing Substring(A,B) = " + fb.c1 + " (" + (100 * fb.c1 / total) + "%).");
		System.out.println("Time spent computing Substring(B,A) = " + fb.c2 + " (" + (100 * fb.c2 / total) + "%).");
		System.out.println("Time spent computing Distance(A,B) = " + fb.c3 + " (" + (100 * fb.c3 / total) + "%).");
		System.out.println("Time spent computing Acronym(A,B) = " + fb.c4 + " (" + (100 * fb.c4 / total) + "%).");
	}
	
	public static List<String> loadReverbEntities(String fileName) throws FileNotFoundException{
		List<String> rtn = new ArrayList<String>();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		return rtn;
	}
	
	public static Freebase loadFreebaseEntities(String fileName, boolean subAB, boolean subBA, boolean dist, boolean acro) throws FileNotFoundException{
		Freebase fb = new Freebase(subAB, subBA, dist, acro);
		Scanner s = new Scanner(new File(fileName));
		int offset = 0;
		while(s.hasNextLine())
			fb.add(Entity.fromString(s.nextLine(), offset++));
		return fb;
	}
}
