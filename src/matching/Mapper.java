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
import wrappers.Options;
import wrappers.Result;

public class Mapper {
	public static final String REVERB_ENTITIES = "input/entity_list.txt";

	public static void main(String[] args) throws IOException{
		Options opt;
		try {
			opt = new Options(args);
		} catch (Exception e) {
			return;
		}
		
		Freebase fb = Freebase.loadFreebaseEntities(opt, true);
		List<String> rv = loadReverbEntities(REVERB_ENTITIES);

		BufferedWriter w = new BufferedWriter(new FileWriter(new File(opt.OUTPUT)));
		BufferedWriter td = new BufferedWriter(new FileWriter(new File("data/ml/data.arff")));
		
		td.write("@relation thresholds\n");
		td.write("@attribute contents string\n");
		td.write("@attribute inlinks numeric\n");
		td.write("@attribute strMatches numeric\n");
		td.write("@attribute cleanMatch numeric\n");
		td.write("@attribute subMatches numeric\n");
		td.write("@attribute wikiMatch numeric\n");
		td.write("@attribute abbrMatch numeric\n");
		td.write("@attribute class numeric\n");
		td.write("@data\n");
		td.flush();

		int rvCnt = 0;
		long totalTime = 0;
		
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			w.flush();
			
			long timer = System.currentTimeMillis();
			Result res = fb.getMatches(rvEnt);
			totalTime += System.currentTimeMillis() - timer;
			
			for(Entity match : res){
				w.write("\t" + match.toOutputString() + "\n");
				w.flush();
			}
			
			rvCnt++;
			
			td.write(res.toString());
			td.flush();
			System.out.println(res.q.orig + " matches: ");
			System.out.println(res.toOutputString());
			System.out.println("\t" + (100 * rvCnt / (double)rv.size()) + "%");
			
		}
		
		System.out.println();
		double timePerEntry = (double)totalTime / (rvCnt * 1000.0);
		System.out.println("Total Time for " + rvCnt + " entries = " + totalTime + "ms");
		System.out.println("\tAverage time per entry = " + timePerEntry + " seconds");
		double entryPerSecond = 1.0 / timePerEntry;
		System.out.println("\tProcessed " + entryPerSecond + " entries per second");
		
		/*
		System.out.println();
		long total = fb.c1 + fb.c2 + fb.c3 + fb.c4 + fb.c5;
		System.out.println("Time Division:");
		System.out.println("\tTime spent computing Substring(A,B) = " + fb.c1 / 1000000000 + "s (" + (100 * fb.c1 / total) + "%).");
		System.out.println("\tTime spent computing Substring(B,A) = " + fb.c2 / 1000000000 + "s (" + (100 * fb.c2 / total) + "%).");
		System.out.println("\tTime spent computing Distance(A,B) = " + fb.c3 / 1000000000 + "s (" + (100 * fb.c3 / total) + "%).");
		System.out.println("\tTime spent computing Acronym(A,B) = " + fb.c4 / 1000000000 + "s (" + (100 * fb.c4 / total) + "%).");
		System.out.println("\tTime spent computing Acronym(B,A) = " + fb.c5 / 1000000000 + "s (" + (100 * fb.c5 / total) + "%).");
		*/
		System.out.println();
		System.out.println();
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
}
