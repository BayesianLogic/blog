package blog.engine.onlinePF.evidenceGenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import blog.engine.onlinePF.Util.Communicator;
import blog.model.Model;

/**
 * evidence generator from file
 * @author cheng
 *
 */
public class EvidenceQueryDecisionGeneratorfFile extends EvidenceQueryDecisionGeneratorOnline{

	//br for obs/evidence
	BufferedReader obsQueryBr;
	//br for decision
	BufferedReader decBr = null;
	public EvidenceQueryDecisionGeneratorfFile(Model model, Collection queryStrings,
			Communicator eviCommunicator, String obsQueryFile) {
		super(model, queryStrings, eviCommunicator);
		try {
			obsQueryBr = new BufferedReader(new FileReader(obsQueryFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public EvidenceQueryDecisionGeneratorfFile(Model model, Collection queryStrings,
			Communicator eviCommunicator, String obsQueryFile, String decisionFile) {
		super(model, queryStrings, eviCommunicator);
		try {
			obsQueryBr = new BufferedReader(new FileReader(obsQueryFile));
			decBr = new BufferedReader(new FileReader(decisionFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	@Override
	public void getUserObservationAndQuery(){
		String s = "";
		try {
			s += obsQueryBr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		in.printInput(s);
		if (!s.equals(""))
			in.printInput("");
		super.getUserObservationAndQuery();
	}
	
	@Override
	public void updateDecision(){
		String s = "";
		if (decBr != null){
		try {
				s += decBr.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		in.printInput(s);
		if (!s.equals(""))
			in.printInput("");
		super.getUserObservationAndQuery();
	}

}
