package blog.engine.onlinePF.evidenceGenerator;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import blog.DBLOGUtil;
import blog.TemporalQueriesInstantiator;
import blog.common.Util;
import blog.engine.ParticleFilterRunner;
import blog.engine.onlinePF.Util.Communicator;
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

public abstract class EvidenceQueryDecisionGeneratorOnline {

	private Collection<String> hiddenQueryStrings;
	private Collection queries;
	public boolean queriesCacheInvalid = true;
	protected Model model;

	//originally stuff from TemporalPartialWorld
	public int lastTimeStep = -1;
	private Collection latestQueries;
	private Collection a;
	
	public EvidenceQueryDecisionGeneratorOnline(Model model, Collection queryStrings, Communicator in) {
		this.model = model;
		queryInstantiator = new TemporalQueriesInstantiator(model, makeQueryTemplates(queryStrings));
		this.in = in;
	}
	

	
	/**
	 * Provides the query instantiations according to current time step, for use
	 * by {@link ParticleFilterRunner}.
	 * NOTE: this crashes if called before the first call to moveOn()
	 */
	public Collection getHiddenQueriesForLatestTimestep() {
		if (queriesCacheInvalid) {
			queries = new LinkedList();
			for (Iterator it = hiddenQueryStrings.iterator(); it.hasNext();) {
				String queryString = (String) it.next();
				queries.add(getQueryForLatestTimestep(queryString));
			}
			queriesCacheInvalid = false;
		}
		return queries;
	}
	

	/**
	 * Returns the query obtained by instantiating a query string with the latest
	 * time step.
	 */
	private ArgSpecQuery getQueryForLatestTimestep(String queryString) {
		return DBLOGUtil.getQueryForTimestep(queryString, model,
				lastTimeStep);
	}
	
	/**
	 * This method increments the lastTimeStep,  
	 * 
	 * @return a collection of queries for the next timestep.
	 */
	private Collection getQueries (int timestep) {
		Collection a = queryInstantiator.getQueries(timestep); 
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
	 * Augments the current world based on the given queries.
	 */
	public void instantiateSOSQueries() {
		lastTimeStep ++;
		List<Query> queries = (List<Query>) getQueries(lastTimeStep);
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
		}
		latestQueries = queries;
	}
	
	public void getUserObservationAndQuery(){
		List<Query> q = (List<Query>) getLatestQueries();
		Evidence ev = new Evidence();
		getInput(ev, q);
		if ( !ev.getDecisionEvidence().isEmpty() ){
			System.err.println("OPFevidenceGenerator.getObservation: do not enter decisions in observation/query phase");
			System.exit(1);
		}
		latestObservation = ev;
	}
	public void updateDecision(){
		List<Query> q = Util.list();
		Evidence ev = new Evidence();
		getInput(ev, q);
		if (!q.isEmpty()){
			System.err.println("OPFevidenceGenerator.getDecision: do not enter queries in decision phase");
			System.exit(1);
		}
		if ( !ev.getValueEvidence().isEmpty() || !ev.getSymbolEvidence().isEmpty() ){
			System.err.println("OPFevidenceGenerator.getDecision: do not enter observations in decision phase");
			System.exit(1);
		}
		latestDecision = ev;
	}
	

	
	public void getInput (){
		Evidence ev = new Evidence();
		List<Query> q = Util.list();
		getInput (new Evidence(), Util.list());
	}
	
	public List<Query> getFreshQueries(){
		if (latestQueryString == null){
			System.err.println("latestQueryString uninitialized!");
			System.exit(1);
		}
		else{
			List<Query> rtn = new ArrayList<Query>();
			Evidence tmp = new Evidence();
			parseAndTranslateEvidence(tmp, rtn, new StringReader((String) latestQueryString));
			for (Query query : rtn){
				if (!query.checkTypesAndScope(model)){
					System.err.println("OPFevidencegenerator.getFreshQueries: error checking query");
					System.exit(1);
				}
				if (query.compile()!=0){
					System.err.println("OPFevidencegenerator.getFreshQueries: error compiling query");
					System.exit(1);
				}
			}
			return rtn;
		}
		System.err.println("error in opfevidencegenerator");
		System.exit(1);
		return null;//should never reach this part
		
	}
	
	private void getInput (Evidence ev, List<Query> q){
		String eviquerystr = "";
		String accstr= "";
		while (true){
			eviquerystr = in.readInput();
			if (eviquerystr.trim().equals(""))
				break;
			else
				accstr+=eviquerystr;
		}

		parseAndTranslateEvidence(ev, q, new StringReader((String) accstr));
		latestQueryString = accstr;
		
		
		ev.checkTypesAndScope(model);
		if (ev.compile()!=0)
			System.exit(1);
		
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

	}
	
	
	/**
	 * Implements method used by {@link ParticleFilterRunner} to obtain evidence
	 * for current time step.
	 */
	public Evidence getLatestObservation() {
		return latestObservation; 

	}
	public Evidence getLatestDecision() {
		return latestDecision; 

	}
	
	//need to fix the error message for empty evidence string inputs
	private boolean parseAndTranslateEvidence(Evidence e, List<Query> q, Reader reader) {
		Parse parse = new Parse(reader, null);
		Semant sem = new Semant(model, e, q, new ErrorMsg.quietErrorMsg("ParticleFilterRunnerOnGenerator.parseAndTranslateEvidence()")); //ignore this error message for now
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
	/** Provides the latest instantiated queries. */
	public Collection getLatestQueries() {
		return latestQueries;
	}
	protected static LinkedList makeQueryTemplates(Collection queryStrings) {
		LinkedList list = new LinkedList();
		for (Iterator it = queryStrings.iterator(); it.hasNext();) {
			String queryString = (String) it.next();
			String template = "query " + queryString + ";";
			list.add(template);
		}

		return list;
	}

	Evidence latestObservation;
	Evidence latestDecision;
	Communicator in;
	public String latestQueryString = null;
	protected TemporalQueriesInstantiator queryInstantiator;
}
