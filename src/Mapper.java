import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mapper {
	public static final String FREEBASE_ENTITIES = "output.fbid-prominence.sorted";
	public static final String REVERB_ENTITIES = "input/entity_list.txt";
	private final String OUTPUT;
	private final boolean SUB_AB;
	private final boolean SUB_BA;
	private final boolean DIST;
	private final boolean ACRO;
	private final int MAX;

	private long c1 = 0;
	private long c2 = 0;
	private long c3 = 0;
	private long c4 = 0;
	
	public Mapper(boolean subAB, boolean subBA, boolean dist, boolean acro, String output, int max){
		this.SUB_AB = subAB;
		this.SUB_BA = subBA;
		this.DIST = dist;
		this.ACRO = acro;
		this.OUTPUT = output;
		this.MAX = max;
	}
	
	public static void main(String[] args) throws IOException{
		boolean subAB = true;
		boolean subBA = true;
		boolean dist = true;
		boolean acro = true;
		String output = "output/output.txt";
		int maxNumMatches = 5;
		
		//Parse arguments
		if(args.length > 1){
			
			int p = Integer.parseInt(args[0]);
			subAB = false;
			
			while(p >= 1){
				
				int tmp = p % 10;
				
				switch(tmp){
					case 1:
						subAB = true;
						break;
					case 2:
						subBA = true;
						break;
					case 3:
						dist = true;
						break;
					case 4:
						acro = true;
						break;
					default:
						System.out.println("Invalid arguments");
						return;
				}
				p = p / 10;
			}
			
			if(args.length > 1){
				output = args[1];
				
				if(args.length == 3){
					maxNumMatches = Integer.parseInt(args[2]);
				}else if(args.length != 0){
					System.out.println("Invalid arguments");
					return;
				}
			}
		}
		
		Mapper m = new Mapper(subAB, subBA, dist, acro, output, maxNumMatches); 
		
		System.out.print("Loading Freebase Entity List...");
		List<Entity> fb = loadFreebaseEntities(FREEBASE_ENTITIES);
		System.out.println("Complete!");

		System.out.print("Loading Reverb Entity List...");
		List<String> rv = loadReverbEntities(REVERB_ENTITIES);
		System.out.println("Complete!");

		BufferedWriter w = new BufferedWriter(new FileWriter(new File(m.OUTPUT)));
		long start = System.currentTimeMillis();
		int cnt = 0;
		int lastEntFoundTotal = 0;
		
		for(String rvEnt : rv){
			int curEntNum = 0;
			int matchCnt = 0;
			List<String> matches = new ArrayList<String>();
			w.write(rvEnt + "\n");
			w.flush();
			for(Entity ent : fb){
				curEntNum++;
				if(m.matches(ent.contents, rvEnt, ((double)curEntNum) / fb.size())){
					w.write("\t" + ent + "\n");
					matchCnt++;
					matches.add(ent.contents);
					if(matchCnt == m.MAX)
						break;
				}
			}
			cnt++;
			lastEntFoundTotal += curEntNum;
			System.out.println(rvEnt + " matches:");
			Utils.printList(matches, "\t");
			System.out.println("\t" + (100 * cnt / (double)rv.size()) + "% @ depth = " + curEntNum + " (" + ((double)curEntNum) / fb.size() + ")");
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Average depth that the nth match was found at = " + ((double)lastEntFoundTotal) / cnt);
		
		long totalTime = System.currentTimeMillis() - start;
		double timePerEntry = totalTime / (cnt * 1000);
		System.out.println("Total Time for " + cnt + " entries = " + totalTime / 1000 + " seconds");
		System.out.println("Average time per entry = " + timePerEntry + " seconds");
		double entryPerSecond = 1.0 / timePerEntry;
		System.out.println("Processed ~" + entryPerSecond + " entries per second");
		
		long total = m.c1 + m.c2 + m.c3 + m.c4;
		System.out.println("Time spent computing Substring(A,B) = " + m.c1 + " (" + (100 * m.c1 / total) + "%).");
		System.out.println("Time spent computing Substring(B,A) = " + m.c2 + " (" + (100 * m.c2 / total) + "%).");
		System.out.println("Time spent computing Distance(A,B) = " + m.c3 + " (" + (100 * m.c3 / total) + "%).");
		System.out.println("Time spent computing Acronym(A,B) = " + m.c4 + " (" + (100 * m.c4 / total) + "%).");
	}
	
	private boolean matches(String fbEnt, String rvEnt, double pt){
		boolean rtn = false;
		
		if(SUB_AB){
			long start = System.nanoTime();
			rtn = rtn || fbEnt.contains(rvEnt);
			this.c1 += System.nanoTime() - start;
		}
		
		if(SUB_BA && !rtn){
			long start = System.nanoTime();
			rtn = rtn || rvEnt.contains(fbEnt);
			this.c2 += System.nanoTime() - start;
		}
		
		if(DIST && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Utils.stringDistance(fbEnt, rvEnt, 10) < ((fbEnt.length() + rvEnt.length()) / 4) * 0.1;
			this.c3 += System.nanoTime() - start;
		}
		
		if(ACRO && !rtn){
			long start = System.nanoTime();
			rtn = rtn || Acronym.matches(fbEnt, rvEnt) || Acronym.matches(rvEnt, fbEnt);
			this.c4 += System.nanoTime() - start;
		}
		
		return rtn;
	}
	
	private static List<String> loadReverbEntities(String fileName) throws FileNotFoundException{
		List<String> rtn = new ArrayList<String>();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		return rtn;
	}
	
	private static List<Entity> loadFreebaseEntities(String fileName) throws FileNotFoundException{
		List<Entity> rtn = new ArrayList<Entity>();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNextLine())
			rtn.add(Entity.fromString(s.nextLine()));
		return rtn;
	}
}
