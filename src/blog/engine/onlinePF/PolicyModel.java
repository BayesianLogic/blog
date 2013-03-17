package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A model that represents the policy
 * @author cheng
 *
 */
public class PolicyModel {
	public List<String> queryTemplates = new ArrayList<String>();
	public List<IfPolicyUnit> decisionMakers;
	public Communicator com;
	
	/**Gets the queries for latest Timestep t*/
	public String getQueries (int t){
		String rtn = "";
		for (Iterator<String> i = queryTemplates.iterator(); i.hasNext();){
			String qt = i.next();
			String q = getString(qt, t);
			rtn += q;
		}
		return rtn;
	}
	
	public String getDecisions (int t){
		String rtn = "";
		String queryResults = com.readInput();
		for (IfPolicyUnit u : decisionMakers){
			rtn += u.getDecisionString(queryResults, t);
		}
		return rtn;
	}
	
	/**same format as OPFevidenceGenerator*/
	private static Pattern pattern = Pattern.compile("\\bt\\b");

	/** Replaces identifier "t" in given string by given time step. */
	public static String getString(String template, int t) {
		Matcher matcher = pattern.matcher(template);
		return matcher.replaceAll("@" + t);
	}

}
