package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import analysis.Analyze;

import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Result;

public class Main {
	public static boolean DEBUG = true;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		Options opt = new Options(args);
		if(opt.usage)
			return;
		
		if(checkFiles(opt))
			return;
		
		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS, opt.LUCENE_THRESHOLD);
		
		if(opt.inMemory)
			process(opt, fb, DEBUG);
		else
			processLarge(opt, fb, DEBUG);
	}
	
	public static double processLarge(Options opt, Freebase fb, boolean debug) throws IOException, InterruptedException{
		double average = 0.0;
		int total = 0;
		
		File output = new File(opt.OUTPUT);
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		Scanner in = new Scanner(new File(opt.INPUT));
		do{
			if(new File(opt.INPUT).exists()){
				int cnt = 0;
				long timer = System.nanoTime();
				
				while(in.hasNextLine()){
					String rvEnt = in.nextLine().substring(1).trim();
					Result res = fb.getMatches(rvEnt, new PerformanceFactor());
					out.write(res.toString(opt.MAX_MATCHES));
					out.flush();
					
					cnt++;
					if(cnt % 50000 == 0){
						double perEntry = (System.nanoTime() - timer) / (double)cnt;
						System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second at " + cnt + ".");
					}
				}
				in.close();
				
				timer = System.nanoTime() - timer;
				double perEntry = timer / (double)cnt;
				
				if(debug)
					System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second.");
				
				if(opt.test){
					Map<String, String> correctMatches = Analyze.loadCorrectMatches();
					double acc = Analyze.analyze(fb, correctMatches, output, 5, debug);
					average += acc;
					total++;
					if(debug){
						System.out.println("Accuracy for top 5: " + acc);
						System.out.println("Accuracy for top 10: " + Analyze.analyze(fb, correctMatches, output, 10, debug));
						System.out.println("Accuracy for top 15: " + Analyze.analyze(fb, correctMatches, output, 15, debug));
						System.out.println("Accuracy for top 20: " + Analyze.analyze(fb, correctMatches, output, 20, debug));
					}
				}
				if(opt.monitor)
					moveFile(opt);
			}else if(opt.monitor){
				System.out.print(".");
				Thread.sleep(10000);
			}else{
				System.out.println("Nothing to do!");
			}
		} while(opt.monitor);
		
		return opt.test ? average / (double)total : 0.0;
	}
	
	public static double process(Options opt, Freebase fb, boolean debug) throws IOException, InterruptedException{
		double average = 0.0;
		int total = 0;
		
		File output = new File(opt.OUTPUT);
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		do{
			if(new File(opt.INPUT).exists()){
				List<String> rv = loadTuples(opt, debug);
				PerformanceFactor pf = new PerformanceFactor();
				
				if(opt.monitor)
					moveFile(opt);
				
				int cnt = 0;
				long timer = System.nanoTime();
				int luceneMatches = 0;
				
				for(String rvEnt : rv){
					Result res = fb.getMatches(rvEnt, pf);
					out.write(res.toString(opt.MAX_MATCHES));
					out.flush();
					
					luceneMatches += res.hasLuceneMatches() ? 1 : 0;
					cnt++;
					if(cnt % 50000 == 0){
						double perEntry = (System.nanoTime() - timer) / (double)cnt;
						System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second at " + cnt / (double)rv.size() + "%.");
					}
				}
				
				timer = System.nanoTime() - timer;
				double perEntry = timer / (double)cnt;
				
				if(debug){
					System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second.");
					System.out.println("\tUsed Lucene " + luceneMatches + "(" + 100.0 * luceneMatches / (double)rv.size() + "%) times");
					System.out.println();
					System.out.println(pf);
				}
				
				if(opt.test){
					Map<String, String> correctMatches = Analyze.loadCorrectMatches();
					double acc = Analyze.analyze(fb, correctMatches, output, 5, debug);
					average += acc;
					total++;
					if(debug){
						System.out.println("Accuracy for top 1: " + Analyze.analyze(fb, correctMatches, output, 1, debug));
						System.out.println("Accuracy for top 2: " + Analyze.analyze(fb, correctMatches, output, 2, debug));
						System.out.println("Accuracy for top 3: " + Analyze.analyze(fb, correctMatches, output, 3, debug));
						System.out.println("Accuracy for top 5: " + acc);
						System.out.println("Accuracy for top 10: " + Analyze.analyze(fb, correctMatches, output, 10, debug));
						System.out.println("Accuracy for top 15: " + Analyze.analyze(fb, correctMatches, output, 15, debug));
						System.out.println("Accuracy for top 20: " + Analyze.analyze(fb, correctMatches, output, 20, debug));
						System.out.println("Accuracy for top 30: " + Analyze.analyze(fb, correctMatches, output, 30, debug));
						System.out.println("Accuracy for top 50: " + Analyze.analyze(fb, correctMatches, output, 50, debug));
						System.out.println("Accuracy for top 100: " + Analyze.analyze(fb, correctMatches, output, 100, debug));
						System.out.println("Accuracy for top 500: " + Analyze.analyze(fb, correctMatches, output, 500, debug));
					}
				}
			}else if(opt.monitor){
				System.out.print(".");
				Thread.sleep(10000);
			}else{
				System.out.println("Nothing to do!");
			}
		} while(opt.monitor);
		
		return opt.test ? average / (double)total : 0.0;
	}
	
	private static void moveFile(Options opt){
		File source = new File(opt.INPUT);
		File dest = new File("processed");
		if(!dest.exists() || !dest.isDirectory())
			dest.mkdir();
		String newName = source.getName() + "." + new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
		File newFile = new File(dest, newName);
		boolean success = source.renameTo(newFile);
		if(!success){
			opt.monitor = false;
			System.out.println("ERROR: Failed to move input file into processed folder!");
		}
	}
	
	public static List<String> loadTuples(Options opt, boolean debug) throws FileNotFoundException{
		if(debug)
			System.out.print("Loading ReVerb Strings...");
		Set<String> rv = new HashSet<String>();
		File input = new File(opt.INPUT);
		Scanner s = new Scanner(input);
		while(s.hasNextLine()){
			rv.add(s.nextLine().split("\t")[1].trim());
		}
		
		List<String> rtn = new ArrayList<String>(rv);
		Collections.sort(rtn);
		s.close();
		if(debug)
			System.out.println("Complete!");
		return rtn;
	}
	
	public static boolean checkFiles(Options opt){
		boolean failed = false;
		File f = new File(opt.FREEBASE);
		if(!f.exists()){
			System.out.println("Could not find Freebase file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(opt.WIKI_ALIAS);
		if(!f.exists()){
			System.out.println("Could not find Wiki Alias file: \"" + f.getAbsolutePath() + "\"");
			failed = true;
		}
		f = new File(Freebase.WEIGHTS_CONFIG);
		if(!f.exists()){
			System.out.println("Could not find weights.config");
			failed = true;
		}
		f = new File(Freebase.STOP_WORDS);
		if(!f.exists()){
			System.out.println("Could not find stop_words.config");
			failed = true;
		}
		f = new File(Freebase.WORD_WEIGHTS);
		if(!f.exists()){
			System.out.println("Could not find word_weights.config");
			failed = true;
		}
		return failed;
	}
}
