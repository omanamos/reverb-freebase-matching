package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import wrappers.Entity;
import wrappers.PerformanceFactor;
import wrappers.Result;

public class Mapper {
	public static final String REVERB_ENTITIES = "input/entity_list.txt";

	public static void main(String[] args) throws IOException{
		
		Freebase fb = Freebase.loadFreebase(true);
		List<String> rv = Utils.loadReverbEntities(REVERB_ENTITIES);

		BufferedWriter w = new BufferedWriter(new FileWriter(new File("output/output.txt")));

		int rvCnt = 0;
		long totalTime = 0;
		int luceneMatches = 0;
		PerformanceFactor pf = new PerformanceFactor();
		
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			w.flush();
			
			long timer = System.currentTimeMillis();
			Result res = fb.getMatches(rvEnt, pf);
			totalTime += System.currentTimeMillis() - timer;
			
			for(Entity match : res){
				w.write("\t" + match.toOutputString() + "\n");
				w.flush();
			}
			
			luceneMatches += res.hasLuceneMatches() ? 1 : 0;
			rvCnt++;
			
			System.out.println(res.q.orig + " matches: ");
			System.out.println(res.toOutputString());
			System.out.println("\t" + (100 * rvCnt / (double)rv.size()) + "%");
			
		}
		
		w.close();
		System.out.println();
		double timePerEntry = (double)totalTime / (rvCnt * 1000.0);
		System.out.println("Total Time for " + rvCnt + " entries = " + totalTime + "ms");
		System.out.println("\tAverage time per entry = " + timePerEntry + " seconds");
		double entryPerSecond = 1.0 / timePerEntry;
		System.out.println("\tProcessed " + entryPerSecond + " entries per second");
		System.out.println("\tUsed Lucene " + luceneMatches + "(" + 100.0 * luceneMatches / (double)rv.size() + "%) times");
		System.out.println();
		System.out.println(pf);
		
		System.out.println();
		System.out.println();
	}
}
