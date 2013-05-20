package blog.engine.onlinePF;

import java.util.Collection;
import java.util.List;

import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;


public class OPFevidenceGeneratorWithPolicy extends OPFevidenceGenerator{

	PolicyModel pm;
	Communicator resultCommunicator;
	public OPFevidenceGeneratorWithPolicy(Model model, Collection queryStrings,
			Communicator eviCommunicator, Communicator resultCommunicator, PolicyModel pm) {
		super(model, queryStrings, eviCommunicator);
		this.pm = pm;
		this.resultCommunicator = resultCommunicator;
	}
	
	@Override
	public void updateObservationQuery(){
		String s = pm.getQueries(lastTimeStep);
		in.printInput(s);
		in.printInput("");
		super.updateObservationQuery();
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
