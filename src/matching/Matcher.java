package matching;

import java.io.FileNotFoundException;
import java.util.Scanner;

import wrappers.Result;

public class Matcher {

	public static void main(String[] args) throws FileNotFoundException, Exception{
		
		Freebase fb = Freebase.loadFreebaseEntities(true);
		
		String line = "";
		Scanner s = new Scanner(System.in);
		do{
			System.out.print("Enter Query: ");
			line = Utils.cleanString(s.nextLine().trim());
			
			long timer = System.nanoTime();
			Result res = fb.getMatches(line);
			timer = System.nanoTime() - timer;
			
			System.out.println(line + " matches:");
			Utils.printList(res, "\t");
			System.out.println();
			System.out.println("Query of \"" + line + "\" took " + timer / 1000000 + "ms");
			/*
			long total = fb.c1 + fb.c2 + fb.c3 + fb.c4 + fb.c5;
			System.out.println("Time Division:");
			System.out.println("\tTime spent computing Substring(A,B) = " + fb.c1 / 1000000 + "ms (" + (100 * fb.c1 / total) + "%).");
			System.out.println("\tTime spent computing Substring(B,A) = " + fb.c2 / 1000000 + "ms (" + (100 * fb.c2 / total) + "%).");
			System.out.println("\tTime spent computing Distance(A,B) = " + fb.c3 / 1000000 + "ms (" + (100 * fb.c3 / total) + "%).");
			System.out.println("\tTime spent computing Acronym(A,B) = " + fb.c4 / 1000000 + "ms (" + (100 * fb.c4 / total) + "%).");
			System.out.println("\tTime spent computing Acronym(B,A) = " + fb.c5 / 1000000 + "ms (" + (100 * fb.c5 / total) + "%).");
			System.out.println();
			*/
		}while(!line.isEmpty());
	}
}
