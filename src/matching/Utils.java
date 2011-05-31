package matching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import wrappers.Entity;
import wrappers.Match;
import wrappers.MatchType;
import wrappers.Query;
import wrappers.Result;
import wrappers.Weights;

public class Utils {
	
	/************************************************
	 *  STRING UTILS
	 */
	public static String cleanString(String str){
		return str.replaceAll("(,|\\.|'|&rsquo|\\(.*\\))", "").trim();
	}
	
	public static String[] split(String line){
		return line.trim().split("( |_|-|,)");
	}
	
	/*************************************************
	 * FILE I/O UTILS
	 */
	public static List<String> loadReverbEntities(String fileName) throws FileNotFoundException{
		System.out.print("Loading Reverb Entity List...");
		List<String> rtn = new ArrayList<String>();
		Scanner s = new Scanner(new File(fileName));
		while(s.hasNextLine())
			rtn.add(s.nextLine().split("\t")[0]);
		System.out.println("Complete!");
		return rtn;
	}
	
	public static List<Result> parseOutputFile(File input, Freebase fb) throws FileNotFoundException{
		System.out.print("Parsing " + input.getName() + " output...");
		List<Result> rtn = new ArrayList<Result>();
		Weights w = new Weights(new File(Freebase.WEIGHTS_CONFIG));
		
		Scanner s = new Scanner(input);
		Result curKey = null;
		while(s.hasNextLine()){
			String line = s.nextLine();
			if(curKey != null && line.startsWith("hit")){
				Entity e = Entity.fromOutputString(line);
				Entity tmp = fb.find(e.id);
				if(tmp != null){
					tmp.score = e.score;
					e = tmp;
				}
				curKey.add(new Match(curKey.q, e, MatchType.EXACT));
			}else if(!line.isEmpty()){
				if(curKey != null){
					curKey.sort(false);
					rtn.add(curKey);
				}
				curKey = new Result(new Query(line.split("\t")[1]), w);
			}
		}
		
		System.out.println("Complete!");
		return rtn;
	}
	
	public static List<Result> parseMapperOutputFile(File input) throws FileNotFoundException{
		return parseMapperOutputFile(input, null);
	}
	
	public static List<Result> parseMapperOutputFile(File input, Freebase fb) throws FileNotFoundException{
		System.out.print("Parsing " + input.getName() + " output...");
		List<Result> rtn = new ArrayList<Result>();
		Weights w = new Weights(new File(Freebase.WEIGHTS_CONFIG));
		
		Scanner s = new Scanner(input);
		Result curKey = null;
		while(s.hasNextLine()){
			String line = s.nextLine();
			if(curKey != null && line.startsWith("\t")){
				Entity e = Entity.fromOutputString(line);
				Entity tmp = fb.find(e.id);
				if(tmp != null){
					tmp.score = e.score;
					e = tmp;
				}
				curKey.add(new Match(curKey.q, e, MatchType.EXACT));
			}else if(!line.isEmpty()){
				if(curKey != null){
					curKey.sort(false);
					rtn.add(curKey);
				}
				curKey = new Result(new Query(line), w);
			}
		}
		
		System.out.println("Complete!");
		return rtn;
	}
}
