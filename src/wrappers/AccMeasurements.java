package wrappers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AccMeasurements implements Iterable<Integer>{
	
	private List<Integer> thresholds;
	public final int max;
	private Map<String, Map<Integer, Integer>> counts;
	private int totalCorrectMatches;
	private Map<Integer, Integer> totalMatchesReturned;
	private Map<Integer, List<Double>> accuracies;
	
	public AccMeasurements(String inputPath) throws IOException, NumberFormatException{
		Scanner in = new Scanner(new File(inputPath));
		this.thresholds = new ArrayList<Integer>();
		while(in.hasNextLine()){
			this.thresholds.add(Integer.parseInt(in.nextLine()));
		}
		Collections.sort(this.thresholds);
		
		this.max = this.thresholds.get(this.thresholds.size() - 1);
		
		this.counts = new HashMap<String, Map<Integer, Integer>>();
		this.accuracies = new HashMap<Integer, List<Double>>();
		this.totalCorrectMatches = 0;
		this.totalMatchesReturned = new HashMap<Integer, Integer>();
		for(int t : this.thresholds)
			this.totalMatchesReturned.put(t, 0);
	}
	
	public void add(String rvEnt, Map<Integer, Integer> counts, int totalCorrect, int totalReturned){
		this.counts.put(rvEnt, counts);
		this.totalCorrectMatches += totalCorrect;
		for(int t : this.thresholds)
			this.totalMatchesReturned.put(t, this.totalMatchesReturned.get(t) + Math.min(t, totalReturned));
	}
	
	public void compute(){
		this.accuracies = new HashMap<Integer, List<Double>>();
		Map<Integer, Integer> anyCounts = new HashMap<Integer, Integer>();
		Map<Integer, Integer> allCounts = new HashMap<Integer, Integer>();
		Map<Integer, Integer> totalCorrect = new HashMap<Integer, Integer>();
		
		for(Integer t : this.thresholds){
			anyCounts.put(t, 0);
			allCounts.put(t, 0);
			totalCorrect.put(t, 0);
		}
		
		for(String rvEnt : this.counts.keySet()){
			Map<Integer, Integer> cnt = this.counts.get(rvEnt);
			for(Integer threshold : cnt.keySet()){
				totalCorrect.put(threshold, totalCorrect.get(threshold) + cnt.get(threshold));
				
				if(cnt.get(threshold) != 0)
					anyCounts.put(threshold, anyCounts.get(threshold) + 1);
				
				if(cnt.get(threshold) == threshold)
					allCounts.put(threshold, allCounts.get(threshold) + 1);
			}
		}
		
		for(Integer t : this.thresholds){
			this.accuracies.put(t, new ArrayList<Double>());
			
			List<Double> acc = this.accuracies.get(t);
			
			acc.add(anyCounts.get(t) / (double)counts.size());
			acc.add(allCounts.get(t) / (double)counts.size());
			acc.add(totalCorrect.get(t) / (double)this.totalCorrectMatches);
			acc.add(totalCorrect.get(t) / (double)this.totalMatchesReturned.get(t));
		}
	}
	
	public String toString(){
		String rtn = "k threshold\t% of arg1s with any correct match\t% of arg1s with all correct matches\t% of all possible correct matches found\t% of top k that are correct";
		this.compute();
		List<Integer> ts = new ArrayList<Integer>(this.accuracies.keySet());
		Collections.sort(ts);
		for(Integer threshold : ts){
			rtn += "\n" + threshold;
			for(Double acc : this.accuracies.get(threshold)){
				rtn += "\t" + acc; 
			}
		}
		return rtn;
	}

	@Override
	public Iterator<Integer> iterator() {
		return this.thresholds.iterator();
	}
}
