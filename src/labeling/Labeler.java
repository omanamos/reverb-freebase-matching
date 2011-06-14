package labeling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import wrappers.Entity;
import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Result;
import wrappers.Tuple;
import matching.Freebase;
import matching.Main;

public class Labeler {

	public static void main(String[] args) throws IOException{
		Options opt = new Options(args);
		if(opt.usage)
			return;
		
		if(Main.checkFiles(opt))
			return;
		
		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS, opt.LUCENE_THRESHOLD);
		
		List<String> rv = Main.loadTuples(opt, true);
		Set<Integer> processed = new HashSet<Integer>();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File("labels.output")));
		Scanner in = new Scanner(System.in);
		List<Tuple<Boolean,Double>> rtn = new ArrayList<Tuple<Boolean,Double>>();
		
		for(int i = 0; i < 100; i++){
			int ind = (int)(Math.random() * rv.size());
			while(processed.contains(ind))
				ind = (ind + 1) % rv.size();
			processed.add(ind);
			String rvEnt = rv.get(ind);
			
			Result r = fb.getMatches(rvEnt, new PerformanceFactor());
			System.out.println("Entity: " + rvEnt);
			int cnt = 0;
			for(Entity e : r){
				Boolean isMatch = null;
				String tmp = null;
				do{
					System.out.print("Is \"" + e + "\" a good match (y/n)? ");
					tmp = in.nextLine();
				}while(!tmp.equals("y") && !tmp.equals("n"));
				
				isMatch = tmp.equals("y") ? true : false;
				rtn.add(new Tuple<Boolean,Double>(isMatch, e.score.total));
				out.write(e.toOutputString() + "\t" + isMatch + "\n");
				out.flush();
				
				cnt++;
				if(cnt == 5)
					break;
			}
		}
		
		Collections.sort(rtn);
		for(Tuple<Boolean, Double> t : rtn){
			System.out.println(t.e + " " + t.v);
		}
	}
}
