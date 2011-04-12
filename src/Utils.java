import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.wcohen.ss.AffineGap;
import com.wcohen.ss.CharMatchScore;


public class Utils {
	
	public static <E> void printList(Iterable<E> c){
		Utils.printList(c, "");
	}
	
	public static <E> void printList(Iterable<E> c, String indent){
		for(E e : c)
			System.out.println(indent + e);
	}
	
	public static <E> void printList(E[] c){
		Utils.printList(c, "");
	}
	
	public static <E> void printList(E[] c, String indent){
		for(E e : c)
			System.out.println(indent + e);
	}
	
	public static double stringDistance(String s1, String s2){
		AffineGap comp = new AffineGap(CharMatchScore.DIST_21, -2.0, -2.0, 0.0);
		return comp.score(s1, s2) / (Math.max(s1.length(), s2.length()) * 2.0);
	}
	
	public static double stringDistance(String s1, String s2, int maxOffset){
		if (s1 == null || s1.isEmpty())
			return (s2 == null || s2.isEmpty()) ? 0 : s2.length();
		if (s2 == null || s2.isEmpty())
			return s1.length();
		
		int c = 0;
		int offset1 = 0;
		int offset2 = 0;
		int lcs = 0;
		while ((c + offset1 < s1.length()) && (c + offset2 < s2.length())){
			
			if (s1.charAt(c + offset1) == s2.charAt(c + offset2)) 
				lcs++;
			else{
				offset1 = 0;
				offset2 = 0;
				for (int i = 0; i < maxOffset; i++){
					if ((c + i < s1.length()) && (s1.charAt(c + i) == s2.charAt(c))){
						offset1 = i;
						break;
					}
					if ((c + i < s2.length()) && (s1.charAt(c) == s2.charAt(c + i))){
						offset2 = i;
						break;
					}
				}
			}
			c++;
		}
		return (s1.length() + s2.length()) / 2.0 - (double)lcs;
	}
	
	public static Map<String, List<String>> parseOutputFile(File input) throws FileNotFoundException{
		Map<String, List<String>> rtn = new HashMap<String, List<String>>();
		
		Scanner s = new Scanner(input);
		String curKey = null;
		while(s.hasNextLine()){
			String line = s.nextLine();
			if(curKey != null && line.startsWith("\t")){
				rtn.get(curKey).add(line);
			}else if(!line.isEmpty()){
				curKey = line;
				rtn.put(curKey, new ArrayList<String>());
			}
		}
		
		return rtn;
	}
}
