package wrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Weights {

	public enum Attr{inlinks, exact, cleaned, substr, abbrv, wiki};
	private Map<Enum<Attr>, Double> weights; 
	
	public Weights(File f) throws FileNotFoundException{
		this.weights = new HashMap<Enum<Attr>, Double>();
		Scanner s = new Scanner(f);
		Attr[] attrs = Attr.values();
		int i = 0;
		
		while(s.hasNextLine()){
			this.weights.put(attrs[i], Double.parseDouble(s.nextLine().split("\t")[1]));
			i++;
		}
	}
	
	public Double getWeight(Attr a){
		return this.weights.get(a);
	}
}
