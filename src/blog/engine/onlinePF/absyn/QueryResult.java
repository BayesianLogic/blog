package blog.engine.onlinePF.absyn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;

public class QueryResult {
	public HashMap<String, Double> q2p = new HashMap<String,Double>();
	public int timestep;
	private Model model;
	public QueryResult (Model m, String qr, int t){
		timestep = t;
		parseQueryResult(qr);
		model = m;
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
			
		
		QueryResult q = new QueryResult(null, qr, 0);
		int x = 1;
	}
	
	public double answerQuery(String query){
		if (model != null){
			query = "query " + query + ";";
			List<Query> parsedQuery = new ArrayList<Query>();
			parseAndTranslateEvidence(new Evidence(), parsedQuery, new StringReader(query));
			String reformattedQuery = ((ArgSpecQuery)parsedQuery.get(0)).getArgSpec().toString();
			return q2p.get(reformattedQuery);
		}
		
		return q2p.get(query);
	}
	
	public void parseQueryResult(String queryResults){
		String[] qrArr = queryResults.split("-----");
		
		for (String s: qrArr){
			Pattern pattern = Pattern.compile("\\s*([^\\[\\]]*?)\\s*(\\[true:(\\d+(\\.\\d+)?)\\])?\\s*(\\[false:(\\d+(\\.\\d+)?)\\])?\\s*");
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
			else{
				pattern = Pattern.compile("\\s*([^\\[\\]]*?)\\s*(\\[false:(\\d+(\\.\\d+)?)\\])?\\s*(\\[true:(\\d+(\\.\\d+)?)\\])?\\s*");
				matcher = pattern.matcher(s);
				if (matcher.matches()){
					String q = matcher.group(1);
					String tr = matcher.group(6);
					String fal = matcher.group(3);
					
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
				else {
					//System.err.println("NOTHING MATCHESSSSSSSSSSSSSS: QueryResult.parseQueryResult, offending string is: "+s);
				}
			}
		}
	}
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(model, e, q, new ErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg2(parse.getParseResult());
		return true;
	}
	

}
