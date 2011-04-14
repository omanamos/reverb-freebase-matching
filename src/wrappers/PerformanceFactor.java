package wrappers;

public class PerformanceFactor {
	public int depth;
	public long minTimer;
	public long maxTimer;
	public long totalTime;
	
	public PerformanceFactor(){
		this.depth = 0;
		this.totalTime = 0;
		this.minTimer = -1;
		this.maxTimer = -1;
	}
	
	public void updateTimer(long timer){
		this.maxTimer = Math.max(timer, this.maxTimer);
		this.minTimer = this.minTimer == -1 ? timer : Math.min(this.minTimer, timer);
		this.totalTime += timer;
	}
}
