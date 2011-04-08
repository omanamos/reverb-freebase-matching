import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Finder {
	public static void main(String[] args) throws FileNotFoundException{
		if(args.length != 1){
			System.out.println("Invalid Arguments");
			return;
		}
		
		File input = new File(args[0]);
		Map<String, List<String>> matches = Utils.parseOutputFile(input);
		
		Scanner in = new Scanner(System.in);
		
		do {
			System.out.print("Search for: ");
			String s1 = in.nextLine();
			
			if(matches.containsKey(s1) && matches.get(s1).size() != 0){
				System.out.println("Here are the matches for that string:");
				Utils.printList(matches.get(s1));
			}else{
				System.out.println("There were no matches for that string.");
			}
			//System.out.print("Would you like to quit? ");
		} while(true);
	}
}
