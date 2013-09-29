package blog.engine.onlinePF.absyn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ForDecisionUnit extends DecisionUnit{

	private String typeName;
	private String variableName;
	private DecisionUnit action;

	public ForDecisionUnit(String typ, String var, DecisionUnit act){
		typeName = typ;
		variableName = var;
		action = act;
	}
	
	@Override
	public String getDecision(QueryResult q){
		ArrayList<String> actualNames = q2s.get(typeName);
		String rtn = "";
		for (String v: actualNames)
			rtn += replaceVariable(action.getDecision(q), v);
		
		return rtn;
	}
	
	@Override
	public String getQueries(int t) {
		ArrayList<String> actualNames = q2s.get(variableName);
		String rtn = "";
		for (String v: actualNames)
			rtn += replaceVariable(action.getQueries(t), v);
		return rtn;
	}
	

	/** Replaces identifier variableName in given string by given time step. */
	public String replaceVariable(String template, String actual) {
		Pattern pattern = Pattern.compile("\\b"+variableName+"\\b");
		Matcher matcher = pattern.matcher(template);
		return matcher.replaceAll(actual);
	}

	@Override
	public void updateGenObjects(String s) {
		parseAvailableObjects(s);
		action.updateGenObjects(s);
	}
	
	public void parseAvailableObjects(String objGenStr){
		for (String s: objGenStr.split(";")){
			//Pattern pattern = Pattern.compile("\\s*(##[^\\[\\]]*?)\\s*(\\[([^\\,]*|[^\\,]*(\\,[^\\,]*)*)])?\\s*");
			Pattern pattern = Pattern.compile("\\s*obs\\s*\\{[^\\{\\}\\s]*\\s*([^\\{\\}]*)\\}\\s*=\\s*\\{(([^\\,]*|[^\\,]*(\\,[^\\,]*)*))\\}\\s*");
			Matcher matcher = pattern.matcher(s);
			if (matcher.matches()){
				ArrayList<String> variables = new ArrayList<String>();
				String variableName = matcher.group(1);
				for (String actualVariableName : matcher.group(2).split("\\,"))
					variables.add(actualVariableName);
				q2s.put(variableName, variables);
			}
		}
	}

	public HashMap<String, ArrayList<String>> q2s = new HashMap<String,ArrayList<String>>();
}
