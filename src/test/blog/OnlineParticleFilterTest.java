package test.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunnerOnline;
import blog.model.ArgSpecQuery;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;
import blog.model.ValueEvidenceStatement;


import java.io.*;

import org.junit.Test;
/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class OnlineParticleFilterTest extends TestCase {

	// Configuration:
	private double delta = 0.000001; // the allowed difference between
																// expected and computed values

	//public static void main(String[] args) throws Exception {
	/*
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
	    */
		//junit.textui.TestRunner.run(OnlineParticleFilterTest.class);
	    //OnlineParticleFilterTest x = new OnlineParticleFilterTest();
	    //x.testCalculation();
	    //x.test_getEvidence();
	    //x.test_getQuery1();
	//}

	/** Sets particle filter properties to default values before every test. */
	public void setUp() {
		Util.initRandom(false);
		setDefaultParticleFilterProperties();
	}

	private void setDefaultParticleFilterProperties() {
		properties = new Properties();
		properties.setProperty("numParticles", "5000");
		properties.setProperty("samplerClass", "blog.LWSampler");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
	}

	private static final String burglaryModelString =
			"type House;"
		  +	"distinct House h1, h2, h3;"
		  +	"random Boolean Burglary(House h, Timestep t) {"
		  + "if (t == @0) then"
		  + 	"~ Bernoulli(0.1)"
		  +	"else if (Burglary(h, Prev(t)) & true) then"
		  +  	"~ Bernoulli(0.001)"
		  + "else"
		  +  	"~ Bernoulli(0.12)"
		  + "};"

		  +	"random Boolean Earthquake(Timestep t) {"
		  +		"if (t == @0) then"
		  +			"~ Bernoulli(0.2)"
		  +		"else if (Earthquake(Prev(t)) & true) then"
		  + 		"~ Bernoulli(0.3)"
		  +		"else"
		  + 		"~ Bernoulli(0.1)"
		  +		"};"
		  +	"random Boolean Alarm(House h, Timestep t) {~ TabularCPD("
		  +		"{[true, true] -> ~ Bernoulli(0.95),"
		  +		"[true, false] -> ~ Bernoulli(0.94),"
		  +		"[false, true] -> ~ Bernoulli(0.29),"
		  +		"[false, false] -> ~ Bernoulli(0.001)},"
		  +		"[Burglary(h, t), Earthquake(t)])"
		  +	"};"

		  + "random Boolean JohnCalls(House h, Timestep t) {~ TabularCPD({true -> ~ Bernoulli(0.9),"
		  +		"false -> ~ Bernoulli(0.05)}, Alarm(h, t))};"

		  + "random Boolean MaryCalls(House h, Timestep t) {~ TabularCPD({true -> ~ Bernoulli(0.7),"
		  +		"false -> ~ Bernoulli(0.01)}, Alarm(h, t))};"
		  + "random House foo (Timestep t){"
		  + 	"~ UniformChoice({House h})"
		  +	"};";

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
	
	
	@Test 
	public void testCalculation() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "10000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("S(t)");
		
		Util.initRandom(false);
		Util.setVerbose(false);
		///
	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	 
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	    setModel(hmmModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(pin));
	    
	    OnlineParticleFilterTest x = new OnlineParticleFilterTest();

	    out.println("obs O(@0) = ResultC;");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "A"), 0.07123248769406526, 0.07123248769406526*delta);
	    out.println("obs O(@1) = ResultA;");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "A"), 0.8740065285267652, 0.8740065285267652*delta);
	    out.println("obs O(@2) = ResultA;");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "T"), 0.09701200417538186, 0.09701200417538186*delta);
	    out.println("obs O(@3) = ResultA;");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "G"), 0.08214118198875539, 0.08214118198875539*delta);
	    out.println("obs O(@4) = ResultG;");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "A"), 0.026816361556063043, 0.026816361556063043*delta);
	    out.println(" ");
	    runner.moveOn();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries()), model, "C"), 0.2968928775743654, 0.2968928775743654*delta);
	}

	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	
	@Test
	public void test_getEvidence() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list();

		Util.initRandom(false);

	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	 
	    /*
	    File f = new File("input.txt");
	    f.createNewFile();
	    PrintStream out = new PrintStream(new FileOutputStream("input.txt"));
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	    setModel(burglaryModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
	     
	    */
	    
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
	    setModel(burglaryModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(pin));
	    
	    OnlineParticleFilterTest x = new OnlineParticleFilterTest();

	    out.println("obs JohnCalls(h1, @0) = true;obs MaryCalls(h2, @0) = true;"); //multiple evidences case
	    Evidence e = runner.getEvidence();
	    assertEquals(e.toString(), "[JohnCalls(h1, @0) = true, MaryCalls(h2, @0) = true]");
	    
	    out.println("obs JohnCalls(h1, @0) = true;obs MaryCalls(foo(@0), @0) = true;"); //now move on, note that getEvidence "consumed" 
	    runner.moveOn();																//the previous println to out, so i must println again
	    
	    
	    out.println("obs Alarm(h2,@1) = true;"); //simple case
	    e = runner.getEvidence();
	    assertTrue(e.getValueEvidence().toArray()[0] instanceof ValueEvidenceStatement);
	    assertEquals(e.toString(), "[Alarm(h2, @1) = true]");
	    assertEquals(e.getValueEvidence().toArray()[0].toString(), "Alarm(h2, @1) = true");

	    out.println("obs Burglary(foo(@0),@1) = true;"); //derived variables case
	    e = runner.getEvidence();
	    assertEquals(e.toString(),"[/*DerivedVar*/ Burglary(foo(@0), @1) = true]");
	    ValueEvidenceStatement v = (ValueEvidenceStatement) Util.getFirst(e.getValueEvidence());
	    assertEquals(v.toString(),"/*DerivedVar*/ Burglary(foo(@0), @1) = true");
	    
	    out.println("obs foo(@2) = h3; obs Earthquake(@2) = true;"); //checking if compiled
	    e = runner.getEvidence();
	    v = (ValueEvidenceStatement) Util.getFirst(e.getValueEvidence());
	    java.lang.reflect.Field iscompiled = ValueEvidenceStatement.class.getDeclaredField("compiled");
	    iscompiled.setAccessible(true);
	    Boolean truthvalue = (Boolean) iscompiled.get(v);
	    assertTrue(truthvalue.booleanValue());
	    
	    v = (ValueEvidenceStatement) Util.getLast(e.getValueEvidence());
	    iscompiled = ValueEvidenceStatement.class.getDeclaredField("compiled");
	    iscompiled.setAccessible(true);
	    truthvalue = (Boolean) iscompiled.get(v);
	    assertTrue(truthvalue.booleanValue());
	}

	@Test
	public void test_getQuery1() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("Burglary(h1, t)");

		Util.initRandom(false);

	    PipedInputStream pin = new PipedInputStream();
	    PipedOutputStream pout = new PipedOutputStream(pin);
	 
	    PrintStream out = new PrintStream(pout);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pin));

		
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    runner.eviInputStream = pin;
	    runner.in = new BufferedReader(new InputStreamReader(pin));
	    
	    OnlineParticleFilterTest x = new OnlineParticleFilterTest();

	    //assertEquals(e.toString(), "[JohnCalls(h1, @0) = true, MaryCalls(h2, @0) = true]");
	    
	    out.println(""); 
	    runner.moveOn();															
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @0)]");
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @0)]");
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @0)]");//check that calling getQueries() multiple times works
	    out.println(""); 
	    runner.moveOn();
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @1)]");
	    out.println(""); 
	    runner.moveOn();
	    out.println(""); 
	    runner.moveOn();
	    out.println(""); 
	    runner.moveOn();
	    out.println(""); 
	    runner.moveOn();
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @5)]");
	    out.println(""); 
	    runner.moveOn();
	    assertEquals(runner.getQueries().toString(),"[Burglary(h1, @6)]");
	}

	private void assertProb(String evidenceAndQuery, String valueString,
			double expected) throws Exception {
		ModelEvidenceQueries meq = BLOGUtil.parseAndTranslateFromString(model,
				evidenceAndQuery);
		particleFilter.take(meq.evidence);
		particleFilter.answer(meq.queries);
		assertEquals(expected, BLOGUtil.getProbabilityByString(
				getQuery(meq.queries), model, valueString), delta);
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
