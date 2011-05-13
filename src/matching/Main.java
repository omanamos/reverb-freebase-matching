package matching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import wrappers.Options;
import wrappers.PerformanceFactor;
import wrappers.Result;

public class Main {

	public static void main(String[] args) throws IOException{
		Options opt = new Options(args);
		if(opt.usage)
			return;
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(opt.OUTPUT)));
		List<String> rv = Utils.loadReverbEntities(opt.INPUT);
		Freebase fb = Freebase.loadFreebase(true);
		
		for(String rvEnt : rv){
			Result res = fb.getMatches(rvEnt, new PerformanceFactor());
			out.write(res.toString(opt.MAX_MATCHES));
			out.flush();
		}
	}
}
