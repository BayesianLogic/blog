package blog.engine.onlinePF.evidenceGenerator;

import java.util.Collection;
import java.util.List;

import blog.common.Util;
import blog.engine.onlinePF.CmdCommunicator;
import blog.engine.onlinePF.Util.Communicator;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;


public class EvidenceQueryDecisionGeneratorInteractive extends EvidenceQueryDecisionGeneratorOnline{
	CmdCommunicator pm = new CmdCommunicator();
	public EvidenceQueryDecisionGeneratorInteractive(Model model, Collection queryStrings,
			Communicator eviCommunicator) {
		super(model, queryStrings, eviCommunicator);
	}
	
	@Override
	public void getUserObservationAndQuery(){
		System.out.println("Enter Observation/Query for timestep "+ lastTimeStep);
		String s = pm.readInput();
		in.printInput(s);
		in.printInput("");
		super.getUserObservationAndQuery();
	}
	
	@Override
	public void updateDecision(){
		System.out.println("Enter Decision "+ lastTimeStep);
		String s = pm.readInput();
		in.printInput(s);
		in.printInput("");
		super.updateDecision();
	}

}
