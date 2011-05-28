package wrappers;

import matching.Acronym;
import matching.Utils;

public class Query {

	public final String orig;
	public final String q;
	public final String cleanedQ;
	public final boolean isAcronym;
	
	public Query(String query){
		this.orig = query;
		this.isAcronym = Acronym.isAcronym(query);
		this.q = query.toLowerCase();
		this.cleanedQ = Utils.cleanString(q);
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
}
