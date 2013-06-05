package blog.engine.onlinePF.evidenceGenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import blog.engine.onlinePF.Communicator;
import blog.model.Model;


public class EvidenceGeneratorfFile extends EvidenceGeneratorOnline{

	//br for obs/evidence
	BufferedReader obsQueryBr;
	//br for decision
	BufferedReader decBr = null;
	public EvidenceGeneratorfFile(Model model, Collection queryStrings,
			Communicator eviCommunicator, String oqs) {
		super(model, queryStrings, eviCommunicator);
		try {
			obsQueryBr = new BufferedReader(new FileReader(oqs));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public EvidenceGeneratorfFile(Model model, Collection queryStrings,
			Communicator eviCommunicator, String oqs, String dqs) {
		super(model, queryStrings, eviCommunicator);
		try {
			obsQueryBr = new BufferedReader(new FileReader(oqs));
			decBr = new BufferedReader(new FileReader(dqs));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	@Override
	public void updateObservationQuery(){
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
		super.updateObservationQuery();
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
		super.updateObservationQuery();
	}

}
