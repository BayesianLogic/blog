package blog.engine.experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class query_parser {
	public query_parser(String filename) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String s = "";
		try {
			while ((s= br.readLine())!=null){
				if (s.length()>5){
					s = s.substring(6, s.indexOf(';'));
					queries.add(s);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public ArrayList<String> queries = new ArrayList();
	
	public static void main (String[] args) throws IOException{
		query_parser a = new query_parser("//home//saasbook//git//dblog//ex_inprog//logistics//policies//forced_query");
		System.out.print(a.queries);
	}
}
