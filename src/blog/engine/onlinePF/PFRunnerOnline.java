package blog.engine.onlinePF;

import java.util.*;
import blog.common.Histogram;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.engine.Particle;
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
public abstract class PFRunnerOnline{
	protected Communicator eviCommunicator; //evidence is read from here
	protected Communicator queryResultCommunicator; //query is read from here
	/** The associated model. */
	public static Model model;
	/** The associated particle filter. */
	public SampledPartitionedParticleFilter particleFilter;
	
	public void setUpStreams(){
		eviCommunicator = new PipedCommunicator();
		queryResultCommunicator = new FileCommunicator("randomstuff//filecommunicator.log");
	}
	
	/**
	 * prepare the evidence generator and the particle filter for new round of query/evidence/decision
	 */
	protected void beforeEvidenceAndQueries() {
		//particleFilter.emptyCache();
		evidenceGenerator.queriesCacheInvalid = true;
		evidenceGenerator.updateObservationQuery();
		evidenceGenerator.moveOn();
	}

	//Cheng: overrode the moveOn in particleFilterRunner, mainly to gain access to evidenceGenerator
	public void advancePhase1() {
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		if ((evidence = evidenceGenerator.getLatestObservation()) == null 
				| (queries = evidenceGenerator.getLatestQueries()) == null){
			System.err.println("Evidence/Query should not be null");
			System.exit(1);
		}
		particleFilter.take(evidence);
		particleFilter.answer(queries);
		afterEvidenceAndQueries();
	}
	
	

	public boolean advancePhase2() {
		Evidence evidence;
	
		for (Integer osIndex : particleFilter.getPartitions().keySet()){
		evidenceGenerator.updateDecision();
			if ((evidence = evidenceGenerator.getLatestDecision()) != null) {
				//particleFilter.take(evidence);
				particleFilter.takeWithPartition(evidence, osIndex);
			}

		}
		
		return true;
		
		//return false;
	}



	/**
	 * Formatting does not work well with policy, for proper version see particlefilterrunneronlinewithpolicy
	 */
	protected void afterEvidenceAndQueries() {
		particleFilter.repartition(); 
		particleFilter.samplePartition(1);
		//particleFilter.resample();
		Collection queries = evidenceGenerator.getLatestQueries();
		//print out the overall results
		
		int i = 0;
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			
			if (i==0)
				query.printResults(UBT.valueOutput.p);
			if (i==1)
				query.printResults(UBT.valueOutput2.p);
			if (i==2)
				query.printResults(UBT.valueOutput3.p);
			
			i++;
		}
		UBT.outputRunTime();
		HashSet<AbstractPartialWorld> h = new HashSet<AbstractPartialWorld>();;
		for (TimedParticle p : particleFilter.particles){
			h.add((AbstractPartialWorld) p.curWorld);
		}
		for (Object o : h){
			int x = 1+1;
		}
		
		for (Integer osIndex: particleFilter.getPartitions().keySet()){
			particleFilter.answerWithPartition(queries, osIndex);
			for (Iterator it = queries.iterator(); it.hasNext();) {
				ArgSpecQuery query = (ArgSpecQuery) it.next();
				queryResultCommunicator.printInputNL(printQueryString(query));
				queryResultCommunicator.printInputNL("-----");
			}
			queryResultCommunicator.printInput("");
		}
		queryResultCommunicator.p.flush();
		UBT.dataOutput.printInput("Time for timestep "+ evidenceGenerator.lastTimeStep + " is " + UBT.runTimeTimer.elapsedTime());
		UBT.runTimeTimer.startTimer();
                UBT.worldOutput.printInput("Sample world "+ Util.getFirst(particleFilter.particles).toString());
		if (evidenceGenerator.lastTimeStep == numtstep){
			System.out.println(((Particle)Util.getFirst(particleFilter.particles)).getLatestWorld().basicVarToValueMap().size());
			System.exit(0);
		}
	}
	
	public String printQueryString(ArgSpecQuery q) {
		String rtn = "";
		rtn += q.getArgSpec().toString();
		Histogram histogram = q.getHistogram();
		List<Histogram.Entry> entries = new ArrayList<Histogram.Entry>(histogram.entrySet());
		for (Iterator<Histogram.Entry> iter = entries.iterator(); iter.hasNext();) {
			Histogram.Entry entry = iter.next();
			double prob = entry.getWeight() / histogram.getTotalWeight();
			rtn += ("\t[" + entry.getElement() + ":" + String.format("%.9f", prob) + "]");
		}
		return rtn;
	}



	public PartialWorld getCurrentPartialWorld() {
		return evidenceGenerator.currentPartialWorld;
	}

	/** The evidence generator . */
	public OPFevidenceGenerator evidenceGenerator;

	/** Properties for construction of particle filter. */
	protected Properties particleFilterProperties;

		

	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (!terminate){
			advancePhase1();
			advancePhase2();
			i++;
		}
	}
	

	protected boolean terminate = false;
	public int numtstep;
	
}
