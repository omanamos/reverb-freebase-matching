package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import wrappers.Entity;
import wrappers.Result;

public class Mapper {
	public static final String REVERB_ENTITIES = "input/entity_list.txt";

	public static void main(String[] args) throws IOException{
		
		Freebase fb = Freebase.loadFreebase(true);
		List<String> rv = Utils.loadReverbEntities(REVERB_ENTITIES);

		BufferedWriter w = new BufferedWriter(new FileWriter(new File("output/output.txt")));
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
		int luceneMatches = 0;
		
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
			
			luceneMatches += res.hasLuceneMatches() ? 1 : 0;
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
		System.out.println("\tUsed Lucene " + luceneMatches + "(" + 100.0 * luceneMatches / (double)rv.size() + "%) times");
		
		System.out.println();
		System.out.println();
	}
}
