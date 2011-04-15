package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringEscapeUtils;

import wrappers.Entity;
import wrappers.Options;
import wrappers.Result;
import wrappers.PerformanceFactor;

public class Mapper {
	public static final String FREEBASE_ENTITIES = "output.fbid-prominence.sorted";
	public static final String REVERB_ENTITIES = "input/entity_list.txt";

	public static void main(String[] args) throws IOException{
		System.out.println();
		System.out.println();
		Options opt;
		try {
			opt = new Options(args);
		} catch (Exception e) {
			return;
		}
		
		Freebase fb = loadFreebaseEntities(FREEBASE_ENTITIES, opt);
		List<String> rv = loadReverbEntities(REVERB_ENTITIES);

		BufferedWriter w = new BufferedWriter(new FileWriter(new File(opt.OUTPUT)));
		PerformanceFactor pf = new PerformanceFactor();
		
		int rvCnt = 0;
		int depthTotal = 0;
		long totalTime = 0;
		
		for(String rvEnt : rv){
			rvEnt = StringEscapeUtils.unescapeHtml(rvEnt);
			w.write(rvEnt + "\n");
			w.flush();
			
			long timer = System.currentTimeMillis();
			Result res = fb.getMatches(rvEnt, opt.MAX_MATCHES, pf);
			totalTime += System.currentTimeMillis() - timer;
			
			for(Entity match : res){
				w.write("\t" + match + "\n");
				w.flush();
			}
			
			rvCnt++;
			depthTotal += pf.depth;
			//System.out.println(rvEnt + " matches:");
			//Utils.printList(res, "\t");
			//System.out.println("\t" + (100 * rvCnt / (double)rv.size()) + "% @ depth = " + pf + " (" + ((double)pf.depth) / fb.size() + ")");
			
		}
		
		System.out.println();
		System.out.println("Average depth that the nth match was found at = " + ((double)depthTotal) / rvCnt);
		
		double timePerEntry = (double)totalTime / (rvCnt * 1000.0);
		System.out.println("Total Time for " + rvCnt + " entries = " + totalTime / 1000 + " seconds");
		System.out.println("\tAverage time per entry = " + timePerEntry + " seconds");
		double entryPerSecond = 1.0 / timePerEntry;
		System.out.println("\tProcessed " + entryPerSecond + " entries per second");
		System.out.println("\tAverage time for one match = " + pf.totalTime / depthTotal);
		System.out.println("\tMax time for one match = " + pf.maxTimer / 1000);
		System.out.println("\tMin time for one match = " + pf.minTimer / 1000);
		System.out.println();
		
		long total = fb.c1 + fb.c2 + fb.c3 + fb.c4 + fb.c5;
		System.out.println("Time Division:");
		System.out.println("\tTime spent computing Substring(A,B) = " + fb.c1 / 1000000000 + "s (" + (100 * fb.c1 / total) + "%).");
		System.out.println("\tTime spent computing Substring(B,A) = " + fb.c2 / 1000000000 + "s (" + (100 * fb.c2 / total) + "%).");
		System.out.println("\tTime spent computing Distance(A,B) = " + fb.c3 / 1000000000 + "s (" + (100 * fb.c3 / total) + "%).");
		System.out.println("\tTime spent computing Acronym(A,B) = " + fb.c4 / 1000000000 + "s (" + (100 * fb.c4 / total) + "%).");
		System.out.println("\tTime spent computing Acronym(B,A) = " + fb.c5 / 1000000000 + "s (" + (100 * fb.c5 / total) + "%).");
	}
	
	public static List<String> loadReverbEntities(String fileName) throws FileNotFoundException{
		System.out.print("Loading Reverb Entity List...");
		List<String> rtn = new ArrayList<String>();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		System.out.println("Complete!");
		return rtn;
	}
	
	public static Freebase loadFreebaseEntities(String fileName, Options opt) throws FileNotFoundException{
		System.out.print("Loading Freebase...");
		Freebase fb = new Freebase(opt);
		Scanner s = new Scanner(new File(fileName));
		int offset = 0;
		while(s.hasNextLine())
			fb.add(Entity.fromString(s.nextLine(), offset++));
		System.out.println("Complete!");
		return fb;
	}
}
