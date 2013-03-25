package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.model.Clause;

public class IfPolicyUnit {

	public static void main (String[] foo){
		IfPolicyUnit a = new IfPolicyUnit();
		pclause uni = new pclause("pos(t)", "do this(t)", IfPolicyUnit.comparator.ge, 0.5);
		a.clauses.add(uni);
		String qr = "Distribution of values for pos(@0)\n		True	1\n	False 0 \n-----\n	Distribution of values for pos(@1)\n		True	2\n	-----\n";
		if (uni.applies(qr, 0))
			System.out.println("works");
	}
	List<pclause> clauses = new ArrayList<pclause>();
	
	/**
	 * Returns the decision by choosing from the correct clause
	 */
	public String getDecisionString(String queryResults, int t) {
		for (Iterator<pclause> i = clauses.iterator(); i.hasNext();){
			pclause c = i.next();
			if (c.applies(queryResults, t))
				return c.getDecisionString(t);
		}
		System.err.print("no clauses match!");
		System.exit(1);
		return null;
	}
	public static enum comparator {
		ge, le, g, l, e
	}
	public static class pclause{ 

		String queryTemplate;
		String decisionTemplate;
		IfPolicyUnit.comparator cmp;
		Double prob;
		public pclause(String queryTemplate, String decisionTemplate, IfPolicyUnit.comparator c, Double p){
			this.queryTemplate = queryTemplate;
			this.decisionTemplate = decisionTemplate;
			this.cmp = c;
			this.prob = p;
		}
		public String getQueryString(String template, int t) {
			Pattern pattern = Pattern.compile("\\bt\\b");
			Matcher matcher = pattern.matcher(template);
			return matcher.replaceAll("@" + t);
		}
		public String getDecisionString(int t) {
			Pattern pattern = Pattern.compile("\\bt\\b");
			Matcher matcher = pattern.matcher(decisionTemplate);
			return matcher.replaceAll("@" + t);
		}
		public Double getQueryResult(String queryResults, String queryString){
			String[] qrArr = queryResults.split("-----");
			Pattern pattern = Pattern.compile(Pattern.quote(getQueryString(this.queryTemplate,0))+"\\s*(True\\s*(\\d+(\\.\\d+)?))?\\s*(False\\s*(\\d+(\\.\\d+)?))?");
			
			for (String s: qrArr){
				Matcher matcher = pattern.matcher(s);
				if (matcher.find()){
					int x = matcher.start();
					String tr = matcher.group(2);
					String fal = matcher.group(5);
					return 1.0;
					
				}
			}
			return 0.0;
		}
		public Boolean applies (String queryResults, int t){
			String queryString = getQueryString (queryTemplate, t);
			Double queryResult = getQueryResult(queryResults, queryString);
			Boolean rtn = false;
			switch (cmp){
			case ge : rtn = queryResult >= prob; break;
			case le : rtn = queryResult <= prob; break;
			case g : rtn = queryResult > prob; break;
			case l : rtn = queryResult < prob; break;
			case e : rtn = queryResult == prob; break;
			}
			return rtn;
		}
	}
	
	public static class conj{
		
	}
	public static class boolTerm{
		
	}

}
