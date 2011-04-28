package analysis;

import java.io.*;
import java.util.*;

import matching.*;

import com.wcohen.ss.AffineGap;
import com.wcohen.ss.CharMatchScore;

import com.swabunga.spell.engine.*;

public class Test {
	
	public static void main(String[] args) throws IOException, FileNotFoundException{
		Scanner s = new Scanner(System.in);
		System.out.print("Enter code (1 = testAffine, 2 = testAcronym, 3 = testDistance, 4 = buildList, 5 = timingTests, 6 = buildJazzy, 7 = testJazzy): ");
		
		boolean cont = false;
		do{
			
			try{
				switch(Integer.parseInt(s.nextLine())){
				case 1:
					testAffine();
					break;
				case 2:
					testAcronym();
					break;
				case 3:
					testDistance();
					break;
				case 4:
					buildList();
					break;
				case 5:
					timingTests();
					break;
				case 6:
					buildJazzy();
					break;
				case 7:
					testJazzy();
					break;
				default:
					throw new Exception();
				}
			}catch(Exception e){
				cont = true;
				e.printStackTrace();
			}
		} while(cont);
	}

	public static void testJazzy() throws IOException, FileNotFoundException{
		Scanner in = new Scanner(System.in);
		System.out.print("Loading Dictionary...");
		SpellDictionaryHashMap dict = new SpellDictionaryHashMap(new BufferedReader(new FileReader(new File("data/jazzy.dict"))));
		System.out.println("Complete!");

		do {
			System.out.print("Enter word: ");
			String line = in.nextLine();
		    
			long timer = System.nanoTime();
			List<String> lst = (List<String>)dict.getSuggestions(line, 10);
			timer = System.nanoTime() - timer;
			
			System.out.println("Found " + lst.size() + " matches in " + timer / 1000000 + "ms: ");
			Utils.printList(lst);
			System.out.println();
		} while(true);
	}

	public static void buildJazzy() throws IOException{
		Scanner in = new Scanner(new File(Freebase.FREEBASE_ENTITIES));
		BufferedWriter dictOut = new BufferedWriter(new FileWriter(new File("data/jazzy.dict")));
		Set<String> dict = new HashSet<String>();

		while(in.hasNextLine()){
			String ent = in.nextLine().split("\t")[2];
			if(!dict.contains(ent)){
				dict.add(ent);
				dictOut.write(ent + "\n");
			}
				
			String[] parts = Utils.cleanString(ent).split("( |_|-|,)");
			for(String s : parts){
				if(!dict.contains(s) && s.length() > 3){
					dict.add(s);
					dictOut.write(s + "\n");
				}
			}
		}
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
