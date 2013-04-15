package blog.engine.onlinePF;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import blog.DBLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.UnaryProcedure;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunner;
import blog.engine.onlinePF.Communicator;
import blog.engine.onlinePF.FileCommunicator;
import blog.engine.onlinePF.OPFevidenceGenerator;
import blog.engine.onlinePF.PipedCommunicator;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.world.PartialWorld;


/**
 * ParticleFilterRunnerOnGenerator extends {@link #ParticleFilterRunner} in
 * order to obtain evidence from an external stream input
 * @author Cheng
 * @since Jan 03 2013
 * 
 */
public class ParticleFilterRunnerOnlinePartitioned{
	protected Communicator eviCommunicator; //evidence is read from here
	protected Communicator queryResultCommunicator; //query is read from here
	/** The associated model. */
	public Model model;

	/** The associated particle filter. */
	public PartitionedParticleFilter particleFilter;

	
	public ParticleFilterRunnerOnlinePartitioned(Model model, Collection linkStrings,
			Collection queryStrings, Properties particleFilterProperties) {
		this.model = model;
		particleFilter = new PartitionedParticleFilter(model, particleFilterProperties);
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
		PolicyModel pm = PolicyModel.policyFromFile("/home/saasbook/git/dblog/src/blog/engine/onlinePF/parser/rockpaperscissors_policy");
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
			particleFilter.take(evidence);
			
			particleFilter.answer(queries);
			particleFilter.repartition(); //IMPORTANT!IMPORTANT!IMPORTANT!IMPORTANT!IMPORTANT!IMPORTANT!
			afterEvidenceAndQueries();
			return true;
		}
		return false;
	}
	
	//decide applied_Load(argload(@0), t1, @0)=true;
	//
	
	
	public boolean advancePhase2() {
		Evidence evidence;
		/*
		if(particleFilter.getPartitions().keySet().size()!=1){
			System.err.println("error in particlefilterrunneronlinepartitioned.advancephase2");
			System.exit(1);
		}
		*/
		//evidenceGenerator.updateDecision(particleFilter.getPartitions().keySet().size());
		//evidenceGenerator.updateDecision();
		for (ObservabilitySignature os: (Set<ObservabilitySignature>)particleFilter.getPartitions().keySet()){
		evidenceGenerator.updateDecision();
			if ((evidence = evidenceGenerator.getLatestDecision()) != null) {
				//particleFilter.take(evidence);
				particleFilter.takeWithPartition(evidence, os);
			}
		}
		return true;
		
		//return false;
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
		
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			query.printResults(System.out);
		}
		
		for (ObservabilitySignature os: (Set<ObservabilitySignature>)particleFilter.getPartitions().keySet()){
			particleFilter.answerWithPartition(queries, os);
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

	public static void main(String[] args) throws FileNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean verbose = false;
		boolean randomize = false;
		
		String modelFile = "ex_inprog/logistics/logistics_choice.mblog";
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)","#{Box b: BoxIn(b, c3, t)==true}","#{Box b: BoxIn(b, c1, t)==true}", "#{Box b: applied_Load(b, t1, t)==true}");

		Util.initRandom(randomize);
		Util.setVerbose(verbose);
		Model model = new Model();
		Evidence evidence = new Evidence();
		ArrayList<Query> queries = new ArrayList<Query>();
		ArrayList<Object> readersAndOrigins = new ArrayList<Object>();
		readersAndOrigins.add(new Object[] {new FileReader(modelFile), "blank"});
		


		
		Main.setup(model, evidence, queries, readersAndOrigins, new ArrayList(), verbose, false);
		ParticleFilterRunnerOnlinePartitioned a = new ParticleFilterRunnerOnlinePartitioned(model,
				linkStrings, queryStrings, properties);
		/*
		a.eviCommunicator=new BufferedReader(new InputStreamReader(System.in));
		FileInputStream evidenceIn = new FileInputStream("ex_inprog/logistics/logistics_choice.evidence");
		a.setUpStreams(evidenceIn, System.out);
		a.run();
		*/
		
	}

	

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
