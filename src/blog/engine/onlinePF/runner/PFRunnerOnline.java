package blog.engine.onlinePF.runner;

import java.util.*;

import blog.common.Histogram;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.onlinePF.Communicator;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.PipedCommunicator;
import blog.engine.onlinePF.SampledPartitionedParticleFilter;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.evidenceGenerator.EvidenceGeneratorOnline;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.engine.onlinePF.inverseBucket.UBT;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
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
	public EvidenceGeneratorOnline evidenceGenerator;

	/** Properties for construction of particle filter. */
	protected Properties particleFilterProperties;

	protected boolean terminate = false;
	public int numtstep;
	
	protected void setUpStreams(){
		eviCommunicator = new PipedCommunicator();
		queryResultCommunicator = new FileCommunicator("randomstuff//filecommunicator.log");
	}
	public PFRunnerOnline(Model model, Properties particleFilterProperties, int numtstep){
		this.model = model;
		this.particleFilterProperties = particleFilterProperties;
		setUpStreams();
		this.numtstep = numtstep;
	}
	
	/**
	 * prepare the evidence generator and the particle filter for new round of query/evidence/decision
	 */
	protected void beforeEvidenceAndQueries() {
		evidenceGenerator.queriesCacheInvalid = true;
		evidenceGenerator.moveOn();
		evidenceGenerator.updateObservationQuery();
	}

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
		
		afterEvidenceAndQueries();
	}
	
	

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
	 * Formatting does not work well with policy, for proper version see particlefilterrunneronlinewithpolicy
	 */
	protected void afterEvidenceAndQueries() {
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


	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (!shouldTerminate()){
			advancePhase1();
			advancePhase2();
			i++;
		}
	}
	
	private Boolean shouldTerminate(){
		if (evidenceGenerator.lastTimeStep == numtstep){
			System.out.println(((Particle)Util.getFirst(particleFilter.particles)).getLatestWorld().basicVarToValueMap().size());
			return true;
		}
		else
			return false;
	}


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
