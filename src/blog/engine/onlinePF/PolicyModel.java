package blog.engine.onlinePF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.engine.onlinePF.parser.Parse;
import blog.engine.onlinePF.parser.Semant;

/**
 * A model that represents the policy
 * @author cheng
 *
 */
public class PolicyModel {
	public List<DecisionUnit> decisionUnits = new ArrayList<DecisionUnit>();
	FileCommunicator f = new FileCommunicator("policies.txt");  
	/**Gets the queries for latest Timestep t*/
	public String getQueries (int t){
		String rtn = "";
		for (Iterator<DecisionUnit> i = decisionUnits.iterator(); i.hasNext();){
			rtn += i.next().getQueries(t);
		}
		return rtn;
	}
	
	public String getDecisions (QueryResult q){
		String rtn = "";
		for (DecisionUnit u : decisionUnits){
			rtn += u.getDecision(q);
		}
		f.printInput(rtn);
		return rtn;
	}
	
	public static PolicyModel policyFromFile(String fileName){
		Parse parse = Parse.parseFile(fileName);
		Semant s = new Semant();
		s.transPolicy(parse.getParseResult());
		PolicyModel p = s.pm;
		return p;
	}
	
	public static PolicyModel policyFromString(String policyModel){
		Parse parse = Parse.parseString(policyModel);
		Semant s = new Semant();
		s.transPolicy(parse.getParseResult());
		PolicyModel p = s.pm;
		return p;
	}
	
	public static PolicyModel emptyPolicy(){
		return new PolicyModel();
	}
}
