package analysis;

import java.io.File;
import java.io.IOException;

import matching.Freebase;
import matching.Main;

import wrappers.Attr;
import wrappers.Options;
import wrappers.Weights;

public class SimulatedAnnealing {

	public static void main(String[] args) throws IOException, InterruptedException{
		Options opt = new Options(true, false, true, false, "input/news.data", "output/output.data", Freebase.FREEBASE_ENTITIES, Freebase.WIKI_ALIASES, 5, 40);
		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS, opt.LUCENE_THRESHOLD);
		
		Weights current = new Weights(new File("weights.config"));
		double currentAcc = Main.process(opt, fb, false);
		Weights max = current;
		double maxAcc = currentAcc;
		
		for(int t = 0;;t++){
			double temp = schedule(t);
			if(temp == 0.0)
				break;
			
			Weights next = getNext(current);
			fb.updateWeights(next);
			
			double nextAcc = Main.process(opt, fb, false);
			double delta1 = nextAcc - currentAcc;
			double delta2 = getDelta2(current, next);
			
			if(delta1 > 0.0 || (delta1 == 0.0 && delta2 > 0.0)){
				current = next;
				currentAcc = nextAcc;
				
				if(currentAcc > maxAcc || (currentAcc == maxAcc && getDelta2(max, current) > 0.0)){
					maxAcc = currentAcc;
					max = current;
					System.out.println("Accuracy for top 5: " + currentAcc);
					System.out.println(current);
				}
			}else if(delta1 / temp > Math.random()){
				current = next;
				currentAcc = nextAcc;
			}
		}
		System.out.println("Final accuracy for top 5: " + currentAcc);
		System.out.println(current);
		System.out.println("Max accuracy for top 5: " + maxAcc);
		System.out.println(max);
	}
	
	private static double getDelta2(Weights current, Weights next){
		double rtn = 0.0;
		for(Attr a : current){
			rtn += next.getWeight(a) - current.getWeight(a);
		}
		return rtn;
	}
	
	private static Weights getNext(Weights current){
		Weights next = new Weights(current);
		
		for(Attr a : current){
			if(Math.random() > 0.5){
				double newWeight = (int)((current.getWeight(a) + Math.random() * 10) % 100);
				next.setWeight(a, newWeight);
			}
		}
		return next;
	}
	
	private static double schedule(int time){
		return Math.log(1000000.0) - Math.log(time);
	}
}
