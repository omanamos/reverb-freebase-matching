package analysis;

import java.io.IOException;
import java.util.*;

import matching.Acronym;
import matching.Mapper;
import matching.Utils;

import com.wcohen.ss.AffineGap;
import com.wcohen.ss.CharMatchScore;

public class Test {
	
	public static void main(String[] args) throws IOException{
		//testAffine();
		testAcronym();
		//testDistance();
		//buildList();
		//timingTests();
	}
	
	public static void timingTests() throws IOException{
		String opts = "341";
		String out = "output/tmp.txt";
		String[] depth = {"1", "2", "3", "5", "8", "20", "30", "50", "100", "500"};
		for(String d : depth){
			Mapper.main(new String[]{opts, out, d});
		}
	}
	
	public static void buildList(){
		Scanner in = new Scanner(System.in);
		Set<String> set = new HashSet<String>();
		do {
			System.out.println("Input String: ");
			String s1 = in.nextLine();
			if(!s1.isEmpty()){
				set.add(s1);
			}else
				break;
		} while(true);
		
		Utils.printList(set);
	}
	
	public static void testDistance(){
		Scanner in = new Scanner(System.in);
		
		do {
			System.out.println("Input String 1:");
			String s1 = in.nextLine();
			System.out.println("Input String 2:");
			String s2 = in.nextLine();
			
			System.out.println("Those string have a score of: " + Utils.stringDistance(s1, s2, 10));
			//System.out.print("Would you like to quit? ");
		} while(true);
	}
	
	public static void testAffine(){
		AffineGap comp = new AffineGap(CharMatchScore.DIST_21, -2.0, -2.0, 0.0);
		
		Scanner in = new Scanner(System.in);
		
		do {
			System.out.println("Input String 1:");
			String s1 = in.nextLine();
			System.out.println("Input String 2:");
			String s2 = in.nextLine();
			
			System.out.println("Those string have a score of: " + comp.score(s1, s2));
			//System.out.print("Would you like to quit? ");
		} while(true);
	}
	
	public static void testAcronym(){
		Scanner in = new Scanner(System.in);
		
		do {
			System.out.println("Input String 1:");
			String s1 = in.nextLine();
			boolean s1IsAcronym = Acronym.isAcronym(s1);
			System.out.println(s1IsAcronym ? "Is an acronym." : "Isn't an acronym.");
			
			System.out.println("Input String 2:");
			String s2 = in.nextLine();
			boolean s2IsAcronym = Acronym.isAcronym(s2);
			System.out.println(s2IsAcronym ? "Is an acronym." : "Isn't an acronym.");
			
			if(s1IsAcronym){
				Utils.printList(Acronym.parseAcronym(s1));
				System.out.println(s1 + " is" + (Acronym.acrMatch(s1, Acronym.computeAcronym(s2)) ? "" : " not") + " an acronym for " + s2);
			}
			
			if(s2IsAcronym){
				Utils.printList(Acronym.parseAcronym(s2));
				System.out.println(s2 + " is" + (Acronym.acrMatch(s1, Acronym.computeAcronym(s2)) ? "" : " not") + " an acronym for " + s1);
			}
			//System.out.print("Would you like to quit? ");
		} while(true);
	}
}
