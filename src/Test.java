import java.util.*;

import com.wcohen.ss.AffineGap;
import com.wcohen.ss.CharMatchScore;

public class Test {
	
	public static void main(String[] args){
		//testAffine();
		testAcronym();
		//testDistance();
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
				System.out.println(s1 + " is" + (Acronym.matches(s1, s2) ? "" : " not") + " an acronym for " + s2);
			}
			
			if(s2IsAcronym){
				Utils.printList(Acronym.parseAcronym(s2));
				System.out.println(s2 + " is" + (Acronym.matches(s2, s1) ? "" : " not") + " an acronym for " + s1);
			}
			//System.out.print("Would you like to quit? ");
		} while(true);
	}
}
