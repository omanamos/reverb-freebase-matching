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
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

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
										 "9 = searchIndex, 10 = spellIndex): ");
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
				default:
					throw new Exception();
				}
			}catch(Exception e){
				cont = true;
				e.printStackTrace();
			}
		} while(cont);
	}
	
	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter indexWriter = new IndexWriter(new SimpleFSDirectory(new File("index")), new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31)));
		Scanner in = new Scanner(new File("data/jazzy-top-300k.dict"));
		
		while(in.hasNextLine()){
			//String[] parts = in.nextLine().split("\t");
			Document document = new Document();
			
			document.add(new Field("entity", in.nextLine(), Field.Store.YES, Field.Index.ANALYZED));
			//document.add(new Field("inlinks", parts[1], Field.Store.YES, Field.Index.NO));
			//document.add(new Field("id", parts[0], Field.Store.YES, Field.Index.NO));
			
			indexWriter.addDocument(document);
		}
		
		indexWriter.optimize();
		indexWriter.close();
	}
	
	public static void searchIndex() throws IOException, ParseException{
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
	
	public static void spellIndex() throws IOException, ParseException{
		Scanner in = new Scanner(System.in);
		System.out.print("Loading Dictionary...");
		SpellChecker dict = new SpellChecker(new RAMDirectory());
		IndexReader r = IndexReader.open(new SimpleFSDirectory(new File("index")));
		dict.indexDictionary(new LuceneDictionary(r, "entity"));
		System.out.println("Complete!");
		
		do {
			System.out.print("Enter word: ");
			String line = in.nextLine();
			long timer = System.nanoTime();
			String[] similar = dict.suggestSimilar(line, 10);
			timer = System.nanoTime() - timer;
			
			System.out.println("Found " + similar.length + " matches in " + timer / 1000000 + "ms: ");
			
			for(String s : similar){
				System.out.println(s);
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
