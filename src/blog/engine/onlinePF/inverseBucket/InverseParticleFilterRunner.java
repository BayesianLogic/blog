package blog.engine.onlinePF.inverseBucket;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import blog.DBLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunner;
import blog.engine.onlinePF.Communicator;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.OPFevidenceGenerator;
import blog.engine.onlinePF.OPFevidenceGeneratorWithPolicy;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.PipedCommunicator;
import blog.engine.onlinePF.PolicyModel;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.world.AbstractPartialWorld;
import blog.world.PartialWorld;
import blog.engine.onlinePF.ObservableRandomFunction;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class InverseParticleFilterRunner{
	protected Communicator eviCommunicator; //evidence is read from here
	protected Communicator queryResultCommunicator; //query is read from here
	/** The associated model. */
	public Model model;

	/** The associated particle filter. */
	public InverseParticleFilter particleFilter;

	
	public InverseParticleFilterRunner(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		this.model = model;
		particleFilter = new InverseParticleFilter(model, particleFilterProperties);
		this.particleFilterProperties = particleFilterProperties;
		this.queryStrings = queryStrings;

		//evidenceGenerator.afterMove = afterMoveForward; // this should always be so.
		//afterMove = monitorGeneratorWorld; // this is just a default and the user can change it

		setUpStreams();
		
		Util.setVerbose(false);
		
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}
		
		//evidenceGenerator = new OPFevidenceGenerator(model, queryStrings, eviCommunicator);
		
		evidenceGenerator = new OPFevidenceGeneratorWithPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
	
	public void setUpStreams(){
		eviCommunicator = new PipedCommunicator();
		queryResultCommunicator = new FileCommunicator("f.txt");

	}
	/*
	public void setUpStreams(InputStream pin, PrintStream pout){
		eviCommunicator = new PipedCommunicator();

		evidenceGenerator = new OPFevidenceGenerator(model, queryStrings, eviCommunicator);

	}*/
	
	public Communicator getEviCommunicator (){
		return eviCommunicator;
	}
	public Communicator getQueryCommunicator (){
		return queryResultCommunicator;
	}
	private UnaryProcedure afterMoveForward = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			afterMove.evaluate(queriesObj);
		}
	};

	/** Default afterMove event. */
	private UnaryProcedure monitorGeneratorWorld = new UnaryProcedure() {
		public void evaluate(Object queriesObj) {
			
		}
	};

	
	protected void beforeEvidenceAndQueries() {
		//evidenceGenerator.moveOn(); // move generator so that evidence can be
																// obtained.
		evidenceGenerator.moveOn();
	}

	//Cheng: overrode the moveOn in particleFilterRunner, mainly to gain access to evidenceGenerator
	public boolean advancePhase1() {
		queriesCacheInvalid = true;
		
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		evidenceGenerator.updateObservationQuery();
		if ((evidence = evidenceGenerator.getLatestObservation()) != null && (queries = evidenceGenerator.getLatestQueries()) != null) {
			//particleFilter.resample(); //resample moved here
			takeAndAnswer(evidence, queries);		
			
			afterEvidenceAndQueries();
			return true;
		}
		return false;
	}
	
	private void takeAndAnswer(Evidence evidence, Collection queries){
		particleFilter.setNextEvidence(evidence);
		particleFilter.setNextQuery((List<Query>) queries);
		particleFilter.answerQueriesWithEvidence();
			
	}
	
	//decide applied_Load(argload(@0), t1, @0)=true;
	//
	
	
	public boolean advancePhase2() {
		Evidence evidence;
		Set foo = (Set<ObservabilitySignature>)particleFilter.getPartitionSet();
		for (ObservabilitySignature os: (Set<ObservabilitySignature>)particleFilter.getPartitionSet()){
		evidenceGenerator.updateDecision();
			if ((evidence = evidenceGenerator.getLatestDecision()) != null) {
				particleFilter.takeActionWithPartition(evidence, os);
			}
			Double count = 0.0;
			for (InverseParticle ip: particleFilter.sc.IPtoState.keySet()){
				if (particleFilter.sc.IPtoState.get(ip).OStoCount.containsKey(os))
					count += particleFilter.sc.IPtoState.get(ip).OStoCount.get(os);
			}
			//System.out.println("ObservationSignature is : " + os + "with count: " + count);
			//System.out.println("Action is :" + evidence.getDecisionEvidence());
		}
		return true;
	}

	/**
	 * Provides the query instantiations according to current time step, for use
	 * by {@link ParticleFilterRunner}.
	 * NOTE: this crashes if called before the first call to moveOn()
	 */
	public Collection getQueries() {
		return getQueriesForLatestTimestep();
	}

	/**
	 * Formatting does not work well with policy, for proper version see particlefilterrunneronlinewithpolicy
	 */
	protected void afterEvidenceAndQueries() {
		Collection queries = evidenceGenerator.getLatestQueries();
		//print out the overall results
		
		int i = 0;
		//System.out.println(particleFilter.partitions.size());
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			//query.printResults(System.out);
			
			//if (i==0)
			//	System.err.println(averageQueryResult(query));
				
			i++;
		}
		//System.out.println("number of observations: " + particleFilter.sc.OStoAction.size());
		System.out.println(/*"number of states: " + */particleFilter.sc.IPtoState.size());
		//System.err.println(this.evidenceGenerator.lastTimeStep);
		/*
		if (this.evidenceGenerator.lastTimeStep==11){
			System.out.println("terminating early");
			System.exit(1);
		}
		*/
		for (ObservabilitySignature os: (Set<ObservabilitySignature>)particleFilter.getPartitionSet()){
			particleFilter.getQueryResultFromPartition(queries, os);
			Double count = 0.0;
			for (InverseParticle ip: particleFilter.sc.IPtoState.keySet()){
				if (particleFilter.sc.IPtoState.get(ip).OStoCount.containsKey(os))
					count += particleFilter.sc.IPtoState.get(ip).OStoCount.get(os);
			}
			//System.out.println("ObservationSignature is : " + os + "with count: " + count);
			//System.out.println("Action is :");
			//System.out.println("SIGNATURE: {"+ os.toString()+"} ("+((List)particleFilter.partitions.get(os)).size()+")");
			for (Iterator it = queries.iterator(); it.hasNext();) {
				ArgSpecQuery query = (ArgSpecQuery) it.next();
				//System.out.println("PF estimate of " + query + ":");
				//query.printResults(queryCommunicator.p);
				
				/*
				queryResultCommunicator.printInput(printQueryString(query));
				queryResultCommunicator.printInput("-----");
				queryResultCommunicator.p.flush();
				*/
				
				queryResultCommunicator.printInputNL(printQueryString(query));
				queryResultCommunicator.printInputNL("-----");
				
				
				//System.out.println(printQueryString(query));
				//query.printResults(queryCommunicator.p);
				//System.out.println("-----");
				//query.printResults(System.out);//strange bug here needs fixing
			}
			queryResultCommunicator.printInput("");
			queryResultCommunicator.p.flush();
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

	public Double averageQueryResult(ArgSpecQuery q) {
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
	/**
	 * Returns the collection of queries instantiated for current time step.
	 */
	public Collection getQueriesForLatestTimestep() {
		if (queriesCacheInvalid) {
			queries = new LinkedList();
			for (Iterator it = queryStrings.iterator(); it.hasNext();) {
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
				evidenceGenerator.lastTimeStep);
	}

	public PartialWorld getCurrentPartialWorld() {
		return evidenceGenerator.currentPartialWorld;
	}

	/** The evidence generator . */
	public OPFevidenceGenerator evidenceGenerator;

	/** Properties for construction of particle filter. */
	protected Properties particleFilterProperties;

	protected Collection queryStrings;
	private Collection queries;
	private boolean queriesCacheInvalid = true;

	/**
	 * An event handler called after every move, with the collection of
	 * instantiated queries as argument, with a default that prints the value of
	 * all queries on the generator current partial world, including the ones used
	 * to obtain evidence.
	 */
	public UnaryProcedure afterMove;

	

	/** Runs until there are no evidence or queries anymore. */
	public void run() {
		int i=0;
		while (advancePhase1()&&advancePhase2()){
			i++;
			//if (i>15)
			//	break;
		}
	}

	public Evidence getEvidence() {
		System.err.println("particlefilterrunneronline.getEvidence should not have been called");
		return evidenceGenerator.getEvidence();
	}
	


	
	
}
