package wrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import matching.Utils;


public class Document implements Iterable<Tuple<String,Integer>> {
	
	private Map<String, Integer> counts;
	private List<Tuple<String, Integer>> sorted;
	private int maxFreq;
	
	public Document(File f, boolean preprocessed){
		try {
			this.sorted = new ArrayList<Tuple<String, Integer>>();
			this.counts = new HashMap<String, Integer>();
			Scanner s = new Scanner(f);
			
			while(s.hasNextLine()){
				String line = s.nextLine().trim();
				String[] parts = line.split("\t");
				
				if(preprocessed){
					counts.put(parts[0], Integer.parseInt(parts[1]));
				}else{
					for(String tup : parts){
						String[] tokens = Utils.split(tup);
						for(String t : tokens){
							t = t.trim().toLowerCase();
							if(t.length() != 0){
								if(!counts.containsKey(t))
									counts.put(t, 0);
								counts.put(t, counts.get(t) + 1);
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		this.maxFreq = -1;
		for(String key : this.counts.keySet()){
			int count = this.counts.get(key);
			this.sorted.add(new Tuple<String, Integer>(key, count));
			this.maxFreq = Math.max(this.maxFreq, count);
		}
		Collections.sort(this.sorted);
	}
	
	public int getMaxFreq(){
		return this.maxFreq;
	}
	
	public Integer getCount(String token){
		if(!this.counts.containsKey(token))
			return 0;
		else
			return this.counts.get(token);
	}
	
	public Iterator<Tuple<String, Integer>> iterator(){
		Collections.sort(this.sorted);
		return this.sorted.iterator();
	}
	
	public int size(){
		return counts.size();
	}
}
