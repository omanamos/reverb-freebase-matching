package analysis;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import wrappers.Entity;

import matching.*;

import com.wcohen.ss.AffineGap;
import com.wcohen.ss.CharMatchScore;

import com.swabunga.spell.engine.*;

public class Test {
	
	public static void main(String[] args) throws IOException, FileNotFoundException{
		Scanner s = new Scanner(System.in);
		
		boolean cont = false;
		do{
			System.out.print("Enter code (1 = testAffine, 2 = testAcronym, 3 = testDistance, 4 = buildList, \n\t\t\t" +
										 "5 = timingTests, 6 = buildJazzy, 7 = testJazzy, 8 = createIndex, \n\t\t\t" +
										 "9 = searchIndex, 10 = spellIndex, 11 = naiveMatching, 12 = naiveSubstring): ");
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
				case 8:
					createIndex();
					break;
				case 9:
					searchIndex();
					break;
				case 10:
					spellIndex();
					break;
				case 11:
					naiveMatching();
					break;
				case 12:
					naiveSubstring();
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
	
	public static void naiveSubstring() throws IOException{
		List<Entity> fb = new ArrayList<Entity>();
		BufferedWriter w = new BufferedWriter(new FileWriter(new File("output/output_naive.txt")));
		System.out.print("Loading Freebase...");
		
		Scanner s = new Scanner(new File(Freebase.FREEBASE_ENTITIES));
		int offset = 0;
		
		while(s.hasNextLine()){
			Entity e = Entity.fromString(s.nextLine().toLowerCase(), offset++);
			fb.add(e);
		}
		s.close();
		
		System.out.println("Complete!");
		
		List<String> rv = Utils.loadReverbEntities(Mapper.REVERB_ENTITIES);
		long totalTime = 0;
		int cnt = 0;
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			rvEnt = Utils.cleanString(rvEnt).toLowerCase();
			List<Entity> matches = new ArrayList<Entity>();
			long timer = System.nanoTime();
			for(Entity e : fb){
				if(e.cleanedContents.indexOf(rvEnt) != -1){
					matches.add(e);
					if(matches.size() == 100)
						break;
					//System.out.println("\t" + e);
				}
			}
			totalTime += System.nanoTime() - timer;
			
			if(matches != null)
				for(Entity e1 : matches)
					w.write("\t" + e1.toOutputString() + "\n");
			w.flush();
			cnt++;
			System.out.println(100.0 * cnt / (double)rv.size() + "%");
		}
		System.out.println("Average time per entity: " + (double)totalTime / (double)rv.size() + "ns");
		w.close();
	}
	
	public static void naiveMatching() throws IOException{
		Map<String, List<Entity>> index = new HashMap<String, List<Entity>>();
		BufferedWriter w = new BufferedWriter(new FileWriter(new File("output/output_naive.txt")));
		System.out.print("Loading Freebase...");
		
		Scanner s = new Scanner(new File(Freebase.FREEBASE_ENTITIES));
		int offset = 0;
		
		while(s.hasNextLine()){
			Entity e = Entity.fromString(s.nextLine().toLowerCase(), offset++);
			//if(!index.containsKey(e.contents.toLowerCase()))
			//	index.put(e.contents.toLowerCase(), new ArrayList<Entity>());
			//index.get(e.contents.toLowerCase()).add(e);
			String[] parts = e.cleanedContents.split("( |_|-|,)");
			for(String word : parts){
				if(word.length() > 3){
					if(!index.containsKey(word))
						index.put(word, new ArrayList<Entity>());
					if(!index.get(word).contains(e))
						index.get(word).add(e);
				}
			}
		}
		s.close();
		
		System.out.println("Complete!");
		long totalTime = 0;
		
		List<String> rv = Utils.loadReverbEntities(Mapper.REVERB_ENTITIES);
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			long timer = System.nanoTime();
			
			String[] parts = Utils.cleanString(rvEnt).toLowerCase().split("( |_|-|,)");
			List<Entity> matches = new ArrayList<Entity>();
			for(String part : parts)
				if(part.length() > 3){
					List<Entity> tmp = index.get(part); 
					if(tmp != null)
						matches.addAll(tmp);
				}
			
			totalTime += System.nanoTime() - timer;
			if(matches != null){
				Collections.sort(matches);
				for(Entity e : matches)
					w.write("\t" + e.toOutputString() + "\n");
			}
			w.flush();
		}
		System.out.println("Average time per entity: " + (double)totalTime / (double)rv.size() + "ns");
		w.close();
	}
	
	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = new IndexWriter(new SimpleFSDirectory(new File("index")), new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31)));
		Scanner in = new Scanner(new File("data/top-300k.dict"));
		int i = 0;
		
		while(in.hasNextLine()){
			Document document = new Document();
			
			document.add(new Field("entity", in.nextLine(), Field.Store.YES, Field.Index.ANALYZED));
			
			indexWriter.addDocument(document);
			if(i > 300000)
				break;
		}
		
		indexWriter.optimize();
		indexWriter.close();
	}
	
	public static void searchIndex() throws IOException, ParseException {
		Scanner in = new Scanner(System.in);
		System.out.print("Loading Dictionary...");
		//SpellChecker dict = new SpellChecker(new RAMDirectory());
		IndexReader r = IndexReader.open(new SimpleFSDirectory(new File("index")));
		IndexSearcher s = new IndexSearcher(r);
		QueryParser p = new QueryParser(Version.LUCENE_31, "entity", new StandardAnalyzer(Version.LUCENE_31));
		//dict.indexDictionary(new LuceneDictionary(r, "entity"));
		System.out.println("Complete!");
		
		do {
			System.out.print("Enter word: ");
			String line = in.nextLine();
		    
			long timer = System.nanoTime();
			Query query = p.parse(line);
			TopDocs lst = s.search(query, 10);
			timer = System.nanoTime() - timer;
			
			System.out.println("Found " + lst.totalHits + " matches in " + timer / 1000000 + "ms: ");
			
			for(ScoreDoc d : lst.scoreDocs)
				System.out.println(s.doc(d.doc).get("entity") + " " + d.score);
			
			System.out.println();
		} while(true);
	}
	
	public static void spellIndex() throws IOException, ParseException {
		Scanner in = new Scanner(System.in);
		System.out.print("Loading Dictionary...");
		SpellChecker dict = new SpellChecker(new RAMDirectory());
		IndexReader r = IndexReader.open(new SimpleFSDirectory(new File("index")));
		dict.indexDictionary(new LuceneDictionary(r, "entity"));
		StringDistance dist = dict.getStringDistance();
		System.out.println("Complete!");
		
		do {
			System.out.print("Enter word: ");
			String line = in.nextLine();
			long timer = System.nanoTime();
			String[] similar = dict.suggestSimilar(line, 5);
			timer = System.nanoTime() - timer;
			
			System.out.println("Found " + similar.length + " matches in " + timer / 1000000 + "ms: ");
			
			for(String s : similar){
				System.out.println(s + " " + dist.getDistance(s, line));
			}
			
			System.out.println();
		} while(true);
	}

	@SuppressWarnings("unchecked")
	public static void testJazzy() throws IOException, FileNotFoundException{
		Scanner in = new Scanner(System.in);
		System.out.print("Loading Dictionary...");
		SpellDictionaryHashMap dict = new SpellDictionaryHashMap(new BufferedReader(new FileReader(new File("data/jazzy-top-300k.dict"))));
		System.out.println("Complete!");

		do {
			System.out.print("Enter word: ");
			String line = in.nextLine();
		    
			long timer = System.nanoTime();
			List<Word> lst = (List<Word>)dict.getSuggestions(line, 0);
			timer = System.nanoTime() - timer;
			
			System.out.println("Found " + lst.size() + " matches in " + timer / 1000000 + "ms: ");
			
			for(Word w : lst){
				System.out.println(w.getWord() + " " + w.getCost());
			}
			
			System.out.println();
		} while(true);
	}

	public static void buildJazzy() throws IOException{
		Scanner in = new Scanner(new File(Freebase.FREEBASE_ENTITIES));
		BufferedWriter dictOut = new BufferedWriter(new FileWriter(new File("data/jazzy-top-300k.dict")));
		Set<String> dict = new HashSet<String>();
		int cnt = 0;
		
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
			cnt++;
			if(cnt > 300000)
				break;
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
