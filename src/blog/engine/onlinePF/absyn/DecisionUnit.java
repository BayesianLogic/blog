package blog.engine.onlinePF.absyn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class DecisionUnit {

	/**
	 * @paramm t the timestep for the query
	 * @return the string representing all queries potentially needed to make a decision
	 */
	public abstract String getQueries(int t);
	
	/**
	 * @param q the QueryResult object containing the results of all queries needed 
	 * @return the decision string to be passed to onlineparticlefilter
	 */
	public abstract String getDecision(QueryResult q);
	
	/**
	 * Returns the decision by choosing from the correct clause
	 */
	public static String templateToString(String template, int t) {
		Pattern pattern = Pattern.compile("\\bt\\b");
		Matcher matcher = pattern.matcher(template);
		return matcher.replaceAll("@" + t);
	}
}