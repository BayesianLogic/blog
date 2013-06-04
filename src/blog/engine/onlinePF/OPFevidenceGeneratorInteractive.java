package blog.engine.onlinePF;

import java.util.Collection;
import java.util.List;

import blog.common.Util;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;


public class OPFevidenceGeneratorInteractive extends OPFevidenceGenerator{
	CmdCommunicator pm = new CmdCommunicator();
	public OPFevidenceGeneratorInteractive(Model model, Collection queryStrings,
			Communicator eviCommunicator) {
		super(model, queryStrings, eviCommunicator);
	}
	
	@Override
	public void updateObservationQuery(){
		System.out.println("Enter Observation/Query");
		String s = pm.readInput();
		in.printInput(s);
		in.printInput("");
		super.updateObservationQuery();
	}
	
	@Override
	public void updateDecision(){
		System.out.println("Enter Decision");
		String s = pm.readInput();
		in.printInput(s);
		in.printInput("");
		super.updateDecision();
	}

}
