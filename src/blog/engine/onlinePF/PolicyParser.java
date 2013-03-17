package blog.engine.onlinePF;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class PolicyParser {
	public PolicyModel parse(String filename){
		PolicyModel m = new PolicyModel();
		FileReader f = null;
		try {
			 f = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		m.queryTemplates = queries;
		return m;
	}
	
	private List<String> queries;
	
}
