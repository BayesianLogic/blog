package blog.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import blog.BLOGUtil;
import blog.DBLOGUtil;
import blog.TemporalEvidenceGenerator;
import blog.common.Util;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;

/**
 * 
 * @author Cheng
 *	
 * Defines/Redefines some methods in TemporalEvidenceGenerator to make it work for online particle filtering 
 */

public class OPFevidenceGenerator extends TemporalEvidenceGenerator {

	public OPFevidenceGenerator(Model model, Collection queryStrings, BufferedReader in) {
		super(model, Util.list(), queryStrings);
		this.in = in;
	}
	
	
	/**
	 * This method increments the lastTimeStep,  
	 * 
	 * @return a collection of queries for the next timestep.
	 */
	private Collection getQueries (int timestep) {
		Collection a = queryInstantiator.getQueries(timestep); //Cheng: changed visibility of queryInstantiator to protected

		//now link query to the model
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
	

	/**
	 * Augments and returns the current world based on the instantiation of the
	 * query templates for the time step next to the last one used, or 0 if this
	 * is the first generation.
	 */
	public void moveOn() {
		moveOn(++lastTimeStep);
	}

	/**
	 * Augments and returns the current world based on the instantiation of the
	 * query templates for the given value of t.
	 */
	public void moveOn(int t) {
		List<Query> q = (List<Query>) getQueries(lastTimeStep = t);
		getInput(new Evidence(), q);
		moveOn(q);
	}
	public void getInput (){
		Evidence ev = new Evidence();
		List<Query> q = Util.list();
		getInput (new Evidence(), Util.list());
	}
	
	private void getInput (Evidence ev, List<Query> q){
		String eviquerystr = "";
		String accstr= "";
		System.out.println("Enter evi for: "+lastTimeStep);
		while (true){
			try {
				eviquerystr = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (eviquerystr.trim().equals(""))
				break;
			else
				accstr+=eviquerystr;
		}

		parseAndTranslateEvidence(ev, q, new StringReader((String) accstr));
		
		
		
		ev.checkTypesAndScope(model);
		ev.compile();
		
		for (Query query : q){
			if (!query.checkTypesAndScope(model)){
				System.err.println("OPFevidencegenerator.getinput: error checking query");
				System.exit(1);
			}
			if (query.compile()!=0){
				System.err.println("OPFevidencegenerator.getinput: error compiling query");
				System.exit(1);
			}
		}
		
		checkEvidenceMatchesTimestep(ev);
		latestEvidence = ev;
	}
	
	
	/**
	 * Implements method used by {@link ParticleFilterRunner} to obtain evidence
	 * for current time step.
	 */
	public Evidence getEvidence() {
		return latestEvidence; 

	}
	//need to fix the error message for empty evidence string inputs
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(model, e, q, new ErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
		sem.transProg(parse.getParseResult());
		return true;
	}
	
	/**
	 * Check that the evidence provided has the correct timestep associated
	 * @param evidence
	 */
	private void checkEvidenceMatchesTimestep(Evidence evidence){
		/*do nothing*/	
	}
	
	Evidence latestEvidence;
	BufferedReader in;
}
