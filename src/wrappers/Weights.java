package wrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Weights implements Iterable<Attr> {

	private Map<Attr, Double> weights;
	
	public Weights(File f) throws FileNotFoundException{
		this.weights = new HashMap<Attr, Double>();
		Scanner s = new Scanner(f);
		Attr[] attrs = Attr.values();
		int i = 0;
		
		while(s.hasNextLine()){
			this.weights.put(attrs[i], Double.parseDouble(s.nextLine().split("\t")[1]));
			i++;
		}
		s.close();
	}
	
	public Weights(Weights w){
		this.weights = new HashMap<Attr, Double>();
		for(Attr a : w){
			this.weights.put(a, w.getWeight(a));
		}
	}
	
	public Double getWeight(Attr a){
		return this.weights.get(a);
	}
	
	public void setWeight(Attr a, Double weight){
		this.weights.put(a, weight);
	}

	@Override
	public Iterator<Attr> iterator() {
		return this.weights.keySet().iterator();
	}
	
	public String toString(){
		String rtn = "";
		Attr[] attrs = Attr.values();
		for(Attr a : attrs)
			rtn += a + "\t" + this.weights.get(a) + "\n";
		return rtn;
	}
}
