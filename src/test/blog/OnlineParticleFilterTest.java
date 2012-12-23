package test.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunnerOnGenerator;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;


import java.io.*;
/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class OnlineParticleFilterTest extends TestCase {

	// Configuration:
	private double delta = 0.05; // the allowed difference between
																// expected and computed values

	public static void main(String[] args) throws Exception {
	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	 
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	 
	    System.out.println("Writing to output stream...");
	    out.println("Hello World!");
	    out.flush();
	    out.println("Hello World2!");
	    out.println("Hello World3!");
	    System.out.println("Text written: " + in.readLine());
	    
	    System.out.println("Text written: " + in.readLine());
	    
	    System.out.println("Text written: " + in.readLine());
	    
		//junit.textui.TestRunner.run(OnlineParticleFilterTest.class);
	    OnlineParticleFilterTest x = new OnlineParticleFilterTest();
	    x.test1();
	}

	/** Sets particle filter properties to default values before every test. */
	public void setUp() {
		Util.initRandom(true);
		setDefaultParticleFilterProperties();
	}

	private void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "5000");
		properties.setProperty("samplerClass", "blog.LWSampler");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
	}

	private static final String hmmModelString = 
			"type State;"
		+	"distinct State A, C, G, T;"
		+	"type Output;"
		+	"distinct Output ResultA, ResultC, ResultG, ResultT;"
			
		+	"random State S(Timestep t) {"
		+	"    if t == @0 then ~ Categorical({A -> 0.3,"
		+	"                 C -> 0.2," 
		+	"                 G -> 0.1," 
		+	"                 T -> 0.4})"
		+	"    else ~ TabularCPD("
		+	"      {A -> ~ Categorical({A -> 0.1, C -> 0.3, G -> 0.3, T -> 0.3}),"
		+	"       C -> ~ Categorical({A -> 0.3, C -> 0.1, G -> 0.3, T -> 0.3}),"
		+	"       G -> ~ Categorical({A -> 0.3, C -> 0.3, G -> 0.1, T -> 0.3}),"
		+	"       T -> ~ Categorical({A -> 0.3, C -> 0.3, G -> 0.3, T -> 0.1})},"
		+	"      S(Prev(t)))"
		+	"};"
			
		+	"random Output O(Timestep t)"
		+	"   ~ TabularCPD("
		+	"     {A -> ~ Categorical({ResultA -> 0.85, ResultC -> 0.05, ResultG -> 0.05, ResultT -> 0.05}),"
		+	"      C -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.85, ResultG -> 0.05, ResultT -> 0.05}),"
		+	"      G -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.05, ResultG -> 0.85, ResultT -> 0.05}),"
		+	"      T -> ~ Categorical({ResultA -> 0.05, ResultC -> 0.05, ResultG -> 0.05, ResultT -> 0.85})},"
		+	"     S(t));";
	

	public void test1() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "5000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("S(t)");

		Util.initRandom(true);
		
		///
	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	 
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	    setModel(hmmModelString);
	    ParticleFilterRunnerOnGenerator runner = new ParticleFilterRunnerOnGenerator(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(pin));
	    
	    OnlineParticleFilterTest x = new OnlineParticleFilterTest();

	    out.println(" ");
	    out.println(" ");
	    out.println(" ");
	    out.println(" ");
	    out.println(" ");
	    out.println(" ");
	    out.flush();
	    runner.run();
		
		
	}

	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}

	private void assertProb(String evidenceAndQuery, String valueString,
			double expected) throws Exception {
		ModelEvidenceQueries meq = BLOGUtil.parseAndTranslateFromString(model,
				evidenceAndQuery);
		particleFilter.take(meq.evidence);
		particleFilter.answer(meq.queries);
		assertEquals(expected, BLOGUtil.getProbabilityByString(
				getQuery(meq.queries), model, valueString), delta);
		outputQueries(meq.queries);
	}

	private void outputQueries(Collection queries) {
		for (Iterator it = queries.iterator(); it.hasNext();) {
			ArgSpecQuery query = (ArgSpecQuery) it.next();
			for (Iterator it2 = query.getHistogram().entrySet().iterator(); it2
					.hasNext();) {
				Histogram.Entry entry = (Histogram.Entry) it2.next();
				double prob = entry.getWeight() / query.getHistogram().getTotalWeight();
				System.out.println("Prob. of " + query + " = " + entry.getElement()
						+ " is " + prob);
			}
		}
	}

	/**
	 * Helper function that gets a collection assumed to contain a single query
	 * and returns that query.
	 */
	private ArgSpecQuery getQuery(Collection singleton) {
		return (ArgSpecQuery) Util.getFirst(singleton);
	}

	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
}
