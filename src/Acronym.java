
public class Acronym {
	
	public static boolean isAcronym(String s){
		return s.matches("^([A-Z]\\.{0,1}){1,}$");
	}
	
	public static boolean matches(String acronym, String str){
		if(!isAcronym(acronym)) return false;
		
		if(isAcronym(str)) return false;
		
		str = str.trim().toUpperCase();
		
		String[] strParts = str.split("( |_|-)");
		String[] acrParts = parseAcronym(acronym);
		
		if(acrParts.length > strParts.length) return false;
		
		boolean rtn = true;
		int curChar = 0;
		for(int i = 0; i < strParts.length; i++){
			try{
				if(!strParts[i].matches("^(OF|THE)$")){
					rtn = rtn && (strParts[i].charAt(0) == acrParts[curChar].charAt(0));
					curChar++;
				}
			}catch(Exception e){
				return false;
			}
		}
		
		return rtn;
	}
	
	public static String[] parseAcronym(String s){
		return s.matches("^([A-Z]\\.){1,}$") ? s.split("(\\.)") : s.split("");
	}
}
