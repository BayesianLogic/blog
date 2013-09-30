package blog.engine.onlinePF.evidenceGenerator;

import java.util.Collection;
import java.util.List;

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
	public EvidenceQueryDecisionGeneratorwPolicy(Model model, Collection queryStrings,
			Communicator eviCommunicator, Communicator resultCommunicator, PolicyModel pm) {
		super(model, queryStrings, eviCommunicator);
		this.pm = pm;
		this.resultCommunicator = resultCommunicator;
	}
	public void updateGenObj(String s){
		pm.updateGenObjects(s);
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
