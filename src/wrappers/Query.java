package wrappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import matching.Acronym;
import matching.Utils;

public class Query implements Comparable<Query>, Iterable<Tuple<String,Boolean>>{

	public final String orig;
	public final String q;
	public final String cleanedQ;
	public final boolean isAcronym;
	private List<Tuple<String, Boolean>> words;
	
	public Query(String query){
		this.orig = query;
		this.isAcronym = Acronym.isAcronym(query);
		this.q = query.toLowerCase();
		this.cleanedQ = Utils.cleanString(q);
		
		String[] tmp = Utils.split(query);
		this.words = new ArrayList<Tuple<String, Boolean>>();
		for(String s : tmp)
			this.words.add(new Tuple<String,Boolean>(Utils.cleanString(s).toLowerCase(), Acronym.isAcronym(s)));
	}
	
	public int hashCode(){
		return this.q.hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Query){
			Query other = (Query)obj;
			return this.q.equals(other.q) || this.q.equals(other.cleanedQ) || this.cleanedQ.equals(other.q) || this.cleanedQ.equals(other.cleanedQ);
		}else
			return false;
	}

	@Override
	public int compareTo(Query other) {
		return this.orig.compareTo(other.orig);
	}
	
	public int size(){
		return this.words.size();
	}

	@Override
	public Iterator<Tuple<String, Boolean>> iterator() {
		return this.words.iterator();
	}
}
