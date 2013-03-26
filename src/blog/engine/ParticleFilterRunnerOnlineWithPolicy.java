package blog.engine;

import java.util.*;

import blog.engine.onlinePF.OPFevidenceGeneratorWithPolicy;
import blog.engine.onlinePF.PolicyModel;
import blog.model.ArgSpecQuery;
import blog.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.ParticleFilterRunnerOnline;
import blog.engine.ParticleFilter;
import blog.model.ArgSpecQuery;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;
import blog.model.ValueEvidenceStatement;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class ParticleFilterRunnerOnlineWithPolicy extends ParticleFilterRunnerOnline {
	public ParticleFilterRunnerOnlineWithPolicy(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties, String policyFileName) {
		super(model, linkStrings, queryStrings, particleFilterProperties);
		PolicyModel pm = PolicyModel.policyFromFile(policyFileName);
		evidenceGenerator = new OPFevidenceGeneratorWithPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
	
	protected void afterEvidenceAndQueries() {
		Collection queries = evidenceGenerator.getLatestQueries();
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();

			//System.out.println("PF estimate of " + query + ":");
			queryResultCommunicator.printInputNL(printQueryString(query));
			//query.printResults(queryCommunicator.p);
			queryResultCommunicator.printInputNL("-----");
			query.printResults(System.out);//strange bug here needs fixing
		}
		queryResultCommunicator.printInput("");
		queryResultCommunicator.p.flush();
	}
	

	private static final String mazeModelString = 
			"type Action;"	
		+	"distinct Action up, down, left, right;"
					
		+	"decision Boolean applied_action (Action a, Timestep t);"
		
		+	"random Boolean succeed_action (Timestep t){"
		+	"  ~ Categorical({true -> 0.9, false -> 0.1})"
		+	"};"
			
		+	"random Integer pos (Timestep t) {"
		+	"  if (t == @0) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 1 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (right, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (left, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (right, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 5"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (left, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 6"
		+	"  else if (pos (Prev(t)) == 6 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 5"
		+	"  else"
		+	"    = pos (Prev(t))"
		+	"};"
			
		+	"random Boolean pass(Timestep t){"
		+	"  if (t==@0) then"
		+	"    = false"
		+	"  else if (pos(t)==6) then"
		+	"    = true"
		+	"  else"
		+	"    = pass(Prev(t))"
		+	"};"
			
		+	"random Real reward(Timestep t){"
		+	"  if ( (pass(t) == true) & (pass(Prev(t)) == false) ) then" 
		+	"    = 10.0"
		+	"  else"
		+	"    = 0.0"
		+	"};"
			
		+	"random Real discount(Timestep t){"
		+	"  if(t == @0) then" 
		+	"    = 1.0"
		+	"  else"
		+	"    = (discount(Prev(t)) * 0.9) /*gamma = 0.9*/"
		+	"};"
			
		+	"random Real value(Timestep t){"
		+	"  if(t == @0) then"
		+	"    =reward(@0)"
		+	"  else"
		+	"    = (value(Prev(t))) + ( reward(t) * discount(t))"
		+	"};"
		
		;
	
	public static void main(String[] args){
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list();

		Util.initRandom(false);
		Util.setVerbose(true);
		Model model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, mazeModelString);
	    ParticleFilterRunnerOnlineWithPolicy runner = new ParticleFilterRunnerOnlineWithPolicy(model, linkStrings, queryStrings, properties, "/home/saasbook/git/dblog/src/blog/engine/onlinePF/parser/test_policy");
	    runner.run();
	}
	
}
