import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class Mapper {
	public static final String FREEBASE_ENTITIES = "output.fbid-prominence.sorted";
	public static final String REVERB_ENTITIES = "input/entity_list.txt";
	public static final String OUTPUT = "output.txt";
	
	public static void main(String[] args) throws IOException{
		List<Entity> fb = loadFreebaseEntities(FREEBASE_ENTITIES);
		List<String> rv = loadReverbEntities(REVERB_ENTITIES);
		
		BufferedWriter w = new BufferedWriter(new FileWriter(new File(OUTPUT)));
		for(String rvEnt : rv){
			w.write(rvEnt + "\n");
			for(Entity ent : fb){
				if(ent.contents.contains(rvEnt)){
					w.write("\t" + ent + "\n");
				}
			}
		}
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
	
	public static <E> void printList(Collection<E> c){
		System.out.println(c.getClass());
		for(E e : c)
			System.out.println("\t" + e);
	}
}
