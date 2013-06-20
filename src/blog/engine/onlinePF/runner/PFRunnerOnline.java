package blog.engine.onlinePF.runner;

import java.util.*;

import blog.common.Histogram;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.Util.Communicator;
import blog.engine.onlinePF.Util.FileCommunicator;
import blog.engine.onlinePF.Util.PipedCommunicator;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;


/**
 * Runners are used for running a particle filter, and handling the input of query/decision/observation
 * as well as the output of query results
 * The code mostly uses the following two classes: 
 * - ParticleFilter (inference engine)
 * - EvidenceGenerator (input/output handler)
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class PFRunnerOnline{
	protected Communicator eviCommunicator; //evidence is read from here
	protected Communicator queryResultCommunicator; //query is read from here
	public static Model model; //TODO: this is pretty hacky now to accomodate parsing in ObservabilitySignature
	public PFEngineOnline particleFilter;
	/** The evidence generator . */
	public EvidenceQueryDecisionGeneratorOnline evidenceGenerator;

	/** Properties for construction of particle filter. */
	protected Properties particleFilterProperties;

	/**the number of timesteps to run the particle filter before exiting*/
	public int numtstep;
	
	/**
	 * utility class used for setting up the input/output streams
	 */
	protected void setUpStreams(){
		eviCommunicator = new PipedCommunicator();
		queryResultCommunicator = new FileCommunicator("randomstuff//filecommunicator.log");
	}
	
	/**
	 * Constructor for PFRunner
	 * @param model the model, required for online parsing of queries/evidence/observation
	 * @param particleFilterProperties class containing information about how the particle filter should be setup
	 * @param numtstep number of timesteps to run the PFRunner
	 */
	public PFRunnerOnline(Model model, Properties particleFilterProperties, int numtstep){
		this.model = model;
		this.particleFilterProperties = particleFilterProperties;
		setUpStreams();
		this.numtstep = numtstep;
	}
	
	/**
	 * prepare the evidence generator and the particle filter for new round of query/evidence/decision
	 * This class can be overriden to perform different tasks.
	 */
	protected void beforeEvidenceAndQueries() {
		evidenceGenerator.queriesCacheInvalid = true;
		evidenceGenerator.instantiateSOSQueries();
		evidenceGenerator.getUserObservationAndQuery();
	}

	/**
	 * Phase1 refers to the phase in which 
	 * - observations(evidence) are provided and taken
	 * - queries are provided and answered
	 * Decision evidence should not be provided at this time
	 * calling the generic methods (such as particleFilter.beforeTakingEvidence()) makes
	 * it easier to use custom particleFilters without changing this code
	 */
	public void advancePhase1() {
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		if ((evidence = evidenceGenerator.getLatestObservation()) == null 
				| (queries = evidenceGenerator.getLatestQueries()) == null){
			System.err.println("Evidence/Query should not be null");
			System.exit(1);
		}
		particleFilter.beforeTakingEvidence();
		particleFilter.take(evidence);
		particleFilter.answer(queries);
		particleFilter.afterAnsweringQueries();
		
		postEvidenceAndQueryIO();
	}
	
	
	/**
	 * Phase2 refers to the phase in which 
	 * - decisions(evidence) are provided and taken
	 */
	public void advancePhase2() {
		Evidence evidence;
		evidenceGenerator.updateDecision();
		if ((evidence = evidenceGenerator.getLatestDecision()) == null) {
			System.err.println("Decision should not be null");
			System.exit(1);
		}
		particleFilter.takeDecision(evidenceGenerator.getLatestDecision());
	}



	/**
	 * Handles various tasks:
	 * - Prints the query results to output logs (only first 3 query results printed)
	 * - Prints the query to the queryResultCommunicator. This is important in many implementations
	 *   of PFRunners. For example, policy-based evidence generators obtain the query results from the
	 *   query result communicator
	 * This class can be overriden to perform different tasks.
	 */
	protected void postEvidenceAndQueryIO() {
		Collection queries = evidenceGenerator.getLatestQueries();
		particleFilter.updateQuery(queries);
		int i = 0;
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			//if (i==0)
			//	System.err.println(averageQueryResult(query));
			
			if (i==0)
				query.printResults(UBT.valueOutput.p);
			if (i==1)
				query.printResults(UBT.valueOutput2.p);
			if (i==2)
				query.printResults(UBT.valueOutput3.p);
			
			i++;
		}
		particleFilter.printResultToCommunicator(queries, queryResultCommunicator);
		
		UBT.outputRunTime();
		UBT.dataOutput.printInput("Time for timestep "+ evidenceGenerator.lastTimeStep + " is " + UBT.runTimeTimer.elapsedTime());
		UBT.runTimeTimer.startTimer();
        UBT.worldOutput.printInput("Sample world "+ Util.getFirst(particleFilter.particles).toString());		
	}


	/** Runs until shouldTerminate returns true */
	public void run() {
		//int i=0;
		while (!shouldTerminate()){
			advancePhase1();
			advancePhase2();
			//i++;
		}
	}
	
	/** 
	 * returns true if the runner should terminate
	 * override for a different condition
	 * by default checks if the timestep is equal to numtstep (provided in constructor)
	 * */
	public Boolean shouldTerminate(){
		if (evidenceGenerator.lastTimeStep == numtstep){
			System.out.println(((Particle)Util.getFirst(particleFilter.particles)).getLatestWorld().basicVarToValueMap().size());
			return true;
		}
		else
			return false;
	}

	/** utility method for finding the average value of queries whose results are numbers*/
	protected Double averageQueryResult(ArgSpecQuery q) {
		Double rtn = (double) 0;
		Histogram histogram = q.getHistogram();
		List<Histogram.Entry> entries = new ArrayList<Histogram.Entry>(histogram.entrySet());
		for (Iterator<Histogram.Entry> iter = entries.iterator(); iter.hasNext();) {
			Histogram.Entry entry = iter.next();
			double prob = entry.getWeight() / histogram.getTotalWeight();
			rtn = rtn + ((Number)entry.getElement()).doubleValue()* ((Number) entry.getWeight()).doubleValue() / ((Number) histogram.getTotalWeight()).doubleValue();
		}
		return rtn;
	}
	
}
