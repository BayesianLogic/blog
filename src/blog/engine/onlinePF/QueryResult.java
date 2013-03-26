package blog.engine.onlinePF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResult {
	public HashMap<String, Double> q2p = new HashMap<String,Double>();
	public int timestep;
	public QueryResult (String qs, int t){
		timestep = t;
		parseQueryResult(qs);
	}
	public static void main (String[] args) throws IOException{
		FileReader f = new FileReader ("/home/saasbook/git/dblog/f.txt");
		BufferedReader b = new BufferedReader(f);
		String s = b.readLine();
		String qr = "";//"applied_action(up, @1)	[true:1.0]\n-----\napplied_action(up, @3)	[false:1.0]";
		while (s!=null){
			qr += s;
			s = b.readLine();
		}
			
		
		QueryResult q = new QueryResult(qr, 0);
		int x = 1;
	}
	
	public double answerQuery(String query){
		return q2p.get(query);
	}
	
	public void parseQueryResult(String queryResults){
		String[] qrArr = queryResults.split("-----");
		Pattern pattern = Pattern.compile("\\s*(.*?)\\s*(\\[true:(\\d+(\\.\\d+)?)\\])?\\s*(\\[false:(\\d+(\\.\\d+)?)\\])?\\s*");
		
		for (String s: qrArr){
			Matcher matcher = pattern.matcher(s);
			if (matcher.matches()){
				String q = matcher.group(1);
				String tr = matcher.group(3);
				String fal = matcher.group(6);
				
				Double pot = Double.valueOf(0);
				if (tr != null)
					pot = Double.valueOf(tr);
				else if (fal != null)
					pot = 1.0 - Double.valueOf(fal);
				else{
					System.err.println("QueryResult.java: no valid true or false probability, Query is: "+ s);
					continue;
					//System.exit(1);
				}
					
				q2p.put(q, Double.valueOf(pot));
			}
		}
	}

}
