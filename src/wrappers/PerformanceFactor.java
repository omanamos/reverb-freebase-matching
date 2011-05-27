package wrappers;

import java.util.HashMap;
import java.util.Map;

public class PerformanceFactor {

	private Map<MatchType, Long> times;
	private long totalTime;
	private Map<MatchType, Integer> counts;
	private int totalCount;
	
	private long timer;
	
	public PerformanceFactor(){
		this.times = new HashMap<MatchType, Long>();
		this.counts = new HashMap<MatchType, Integer>();
		this.totalCount = 0;
		this.totalTime = 0;
		this.timer = 0;
	}
	
	public void start() {
		this.timer = System.nanoTime();
	}
	
	public void end(MatchType m) {
		long time = System.nanoTime() - this.timer;
		this.totalTime += time;
		
		if(!this.times.containsKey(m))
			this.times.put(m, new Long(0));
		this.times.put(m, this.times.get(m) + time);
	}

	public void match(MatchType m, int count){
		this.totalCount += count;
		
		if(!this.counts.containsKey(m))
			this.counts.put(m, 0);
		this.counts.put(m, this.counts.get(m) + count);
	}
	
	public String toString(){
		String s = "Division of Time:\n";
		long totalMS = this.totalTime / 1000000;
		for(MatchType m : this.times.keySet()){
			double ms = times.get(m) / 1000000.0;
			s += "\t" + m + " - " + ms + "ms (" + (100.0 * ms / (double)totalMS) + "%)\n";
		}
		s += "\nDivision of Matches:\n";
		for(MatchType m : this.counts.keySet()){
			int cnt = this.counts.get(m);
			s += "\t" + m + " - " + cnt + " (" + (100.0 * cnt / (double)this.totalCount) + "%)\n";
		}
		
		return s;
	}
}
