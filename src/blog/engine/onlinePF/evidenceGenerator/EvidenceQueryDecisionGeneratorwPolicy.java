package blog.engine.onlinePF.evidenceGenerator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import blog.TemporalQueriesInstantiator;
import blog.common.Util;
import blog.engine.onlinePF.Util.Communicator;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.absyn.QueryResult;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;

/**
 * 
 * @author cheng
 *
 */
public class EvidenceQueryDecisionGeneratorwPolicy extends EvidenceQueryDecisionGeneratorOnline{

	PolicyModel pm;
	Communicator resultCommunicator;
	
	protected TemporalQueriesInstantiator typeGenQueryInstantiator;
	
	public EvidenceQueryDecisionGeneratorwPolicy(Model model, Collection queryStrings,
			Communicator eviCommunicator, Communicator resultCommunicator, PolicyModel pm) {
		super(model, queryStrings, eviCommunicator);
		this.pm = pm;
		this.resultCommunicator = resultCommunicator;
	}
	
	public EvidenceQueryDecisionGeneratorwPolicy(Model model, Collection queryStrings, Collection<String> typeGenQueryStrings,
			Communicator eviCommunicator, Communicator resultCommunicator, PolicyModel pm) {
		super(model, queryStrings, eviCommunicator);
		this.pm = pm;
		this.resultCommunicator = resultCommunicator;
		typeGenQueryInstantiator = new TemporalQueriesInstantiator(model, makeQueryTemplates(typeGenQueryStrings));
	}
	
	public void updateGenObj(String s){
		pm.updateGenObjects(s);
	}
	/**
	 * This method increments the lastTimeStep,  
	 * 
	 * @return a collection of queries for the next timestep.
	 */
	public Collection<Query> getTypeGenQueries (int timestep) {
		Collection<Query> a = typeGenQueryInstantiator.getQueries(timestep+1); 
		boolean correct = true;
		int errors = 0;
		for (Iterator iter = a.iterator(); iter.hasNext();) {
			Query q = (Query) iter.next();
			if (!q.checkTypesAndScope(model)) { //linked to model
				correct = false;
			}
		}

		for (Iterator iter = a.iterator(); iter.hasNext();) {
			errors += ((Query) iter.next()).compile(); //compile query
		}
		if (errors > 0) {
			System.err.println("Encountered " + errors
					+ " errors in compilation of queries in OPFevidenceGenerator.getQueries().");
			System.exit(1);
		}
		
		return a;
	}
	@Override
	public void getUserObservationAndQuery(){
		String s = pm.getQueries(lastTimeStep);
		in.printInput(s);
		in.printInput("");
		super.getUserObservationAndQuery();
	}
	
	@Override
	public void updateDecision(){
		String qr = resultCommunicator.readInput();
		QueryResult q = new QueryResult(model, qr, lastTimeStep);
		String decision = pm.getDecisions(q);
		in.printInput(decision);
		in.printInput("");
		super.updateDecision();
	}

}
