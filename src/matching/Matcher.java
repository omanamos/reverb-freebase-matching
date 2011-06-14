package matching;

import java.io.FileNotFoundException;
import java.util.Scanner;

import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Result;

public class Matcher {

	public static void main(String[] args) throws FileNotFoundException, Exception{
		Options opt = new Options(args);
		if(opt.usage)
			return;
		
		if(Main.checkFiles(opt))
			return;
		
		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS, opt.LUCENE_THRESHOLD);
		
		String line = "";
		Scanner s = new Scanner(System.in);
		do{
			System.out.print("Enter Query: ");
			line = Utils.cleanString(s.nextLine().trim());
			
			long timer = System.nanoTime();
			Result res = fb.getMatches(line, new PerformanceFactor());
			timer = System.nanoTime() - timer;
			
			System.out.println(res.toString(10));
			System.out.println();
			System.out.println("Query of \"" + line + "\" took " + timer / 1000000 + "ms");
		}while(!line.isEmpty());
	}
}
