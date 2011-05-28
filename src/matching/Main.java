package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Result;

public class Main {
	public static boolean DEBUG = false;
	
	public static void main(String[] args) throws IOException{
		Options opt = new Options(args);
		if(opt.usage)
			return;
		
		if(checkFiles(opt))
			return;
		
		System.out.print("Loading ReVerb Strings...");
		Set<String> rv = new HashSet<String>();
		Scanner s = new Scanner(new File(opt.INPUT));
		while(s.hasNextLine()){
			rv.add(s.nextLine().split("\t")[1].trim());
		}
		System.out.println("Complete!");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(opt.OUTPUT)));
		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS);
		
		int cnt = 0;
		
		long timer = System.nanoTime();
		for(String rvEnt : rv){
			Result res = fb.getMatches(rvEnt, new PerformanceFactor());
			out.write(res.toString(opt.MAX_MATCHES));
			out.flush();
			
			cnt++;
			if(cnt % 50000 == 0){
				double perEntry = (System.nanoTime() - timer) / (double)cnt;
				System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second at " + cnt / (double)rv.size() + "%.");
			}
		}
		timer = System.nanoTime() - timer;
		double perEntry = timer / (double)cnt;
		System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second.");
	}
	
	private static boolean checkFiles(Options opt){
		boolean failed = false;
		File f = new File(opt.FREEBASE);
		if(!f.exists()){
			System.out.println("Could not find Freebase file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(opt.INPUT);
		if(!f.exists()){
			System.out.println("Could not find Input file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(opt.WIKI_ALIAS);
		if(!f.exists()){
			System.out.println("Could not find Wiki Alias file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(opt.FREEBASE);
		if(!f.exists()){
			System.out.println("Could not find Freebase file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(Freebase.WEIGHTS_CONFIG);
		if(!f.exists()){
			System.out.println("Could not find weights.config");
			failed = true;
		}
		return failed;
	}
}
