package matching;

import java.io.FileNotFoundException;
import java.util.Scanner;

import wrappers.Options;
import wrappers.Result;

public class Matcher {

	public static void main(String[] args) throws FileNotFoundException, Exception{
		Options opt;
		try {
			opt = new Options(args);
		} catch (Exception e) {
			return;
		}
		
		Freebase fb = Freebase.loadFreebaseEntities(opt);
		
		String line = "";
		Scanner s = new Scanner(System.in);
		do{
			System.out.print("Enter Query: ");
			line = s.nextLine().trim();
			Result res = fb.getMatches(line);
			System.out.println(line + " matches:");
			Utils.printList(res, "\t");
			System.out.println();
			long total = fb.c1 + fb.c2 + fb.c3 + fb.c4 + fb.c5;
			System.out.println("Query of \"" + line + "\" took " + total / 1000000 + "ms");
			System.out.println("Time Division:");
			System.out.println("\tTime spent computing Substring(A,B) = " + fb.c1 / 1000000 + "ms (" + (100 * fb.c1 / total) + "%).");
			System.out.println("\tTime spent computing Substring(B,A) = " + fb.c2 / 1000000 + "ms (" + (100 * fb.c2 / total) + "%).");
			System.out.println("\tTime spent computing Distance(A,B) = " + fb.c3 / 1000000 + "ms (" + (100 * fb.c3 / total) + "%).");
			System.out.println("\tTime spent computing Acronym(A,B) = " + fb.c4 / 1000000 + "ms (" + (100 * fb.c4 / total) + "%).");
			System.out.println("\tTime spent computing Acronym(B,A) = " + fb.c5 / 1000000 + "ms (" + (100 * fb.c5 / total) + "%).");
			System.out.println();
		}while(!line.isEmpty());
	}
}
