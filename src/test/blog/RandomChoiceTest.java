package test.blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import blog.BLOGUtil;
import blog.Main;
import blog.bn.BayesNetVar;
import blog.common.Histogram;
import blog.common.Util;
import blog.engine.Particle;
import blog.engine.ParticleFilter;
import blog.engine.ParticleFilterRunnerOnline;
import blog.model.ArgSpecQuery;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.Query;
import blog.model.ValueEvidenceStatement;
import blog.msg.ErrorMsg;
import blog.parse.Parse;
import blog.semant.Semant;


import java.io.*;

import org.junit.Test;
/**
 * Unit testing for the {@link ParticleFilter}. Because sampling can potentially
 * fail no matter the error margin, tests sometimes fail. This should be rare,
 * however. If so, the user can check the indicated error to see if things look
 * ok, or run the test again.
 */
public class RandomChoiceTest extends TestCase {

	// Configuration:
	private double delta = 0.000001; // the allowed difference between
							/*									// expected and computed values
	public static void main (String[] args) throws Exception{
		System.out.println(logisticsModelString);
	}*/
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

	private static final String logisticsModelStringRandomBoxes = 
			  "type Box;  type Truck;  type City;"
			+	"#Box ~ Poisson(9);"
			+	"distinct Truck t1, t2;"
			+	"distinct City c1, c2, c3;"
			+	"distinct Box b1;"

			+	"decision Boolean chosen_Load(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Unload(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Drive(City c, Truck tr, Timestep t);"
			+   "random Box argload (Timestep t){  ~ UniformChoice({Box b : BoxIn (b, c1, t) == true })};"
			+	"random Box argunload (Truck tr, Timestep t){  ~ UniformChoice({Box b : BoxOn (b, t1, t) == true })};"
			+	"random Boolean applied_Load(Box b, Truck tr, Timestep t) {"
			+	"  if (exists City c (BoxIn ( b, c, t) & TruckIn (c, tr, t))) then = (chosen_Load(foo(b), tr, t) & succeed_action(t)) else = false"
			+	"};"
			//+	"random Boolean equals (Integer a) {if (true) then = (a==#{Box b})};"
			+	"random Box foo (Box b) {if true then = b};"

			+	"random Boolean applied_Unload(Box b, Truck tr, Timestep t) {"
			+	"  if (BoxOn ( b, tr, t) == true ) then = (chosen_Unload(b, tr, t) & succeed_action(t))"
			+	"};"

			+	"random Boolean applied_Drive(City c, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Drive(c, tr, t) & succeed_action(t))"
			+ 	"};"
			
			+	"random Boolean succeed_action (Timestep t){"
			+		"~ Categorical({true -> 0.9, false -> 0.0})"
			+	"};"
			+	"random Boolean pass(Timestep t){"
			+	"  if (t==@0) then"
			+	"    = false"
			+	"  else if (forall Box b BoxIn(b, c3, t)==true) then"
			+	"    = true"
			+	"  else"
			+	"    = pass(Prev(t))"
			+	"};"

			+	"random String actionName(Timestep t){"
			+	"  if (true & exists Box b exists Truck tr (applied_Load(b,tr,t) & true)) then"
			+	"    =\"load\""
			+	"  else if (true & exists Box b exists Truck tr (true & applied_Unload(b,tr,t))) then"
			+	"    =\"unload\""
			+	"  else if (true & exists Truck tr exists City c (true & applied_Drive(c,tr,t))) then"
			+	"    =\"drive\""
			+	"  else"
			+	"    =\"na\""
			+	"};"

			+	"random Real reward(Timestep t){"
			+	"  if (t==@0) then"
			+	"	 = 0.0"
			+	"  else if (exists Box b exists Truck tr (applied_Unload(b,tr,Prev(t)) & (TruckIn(c3,tr,Prev(t)) & BoxOn(b,tr,Prev(t))) )) then "
			+	"    = 10.0"
			+	"  else"
			+	"    = 0.0"
			+	"};"

			+	"random Real discount(Timestep t){"
			+	"  if(t==@0) then "
			+	"    = 1.0"
			+	"  else"
			+	"    = (discount(Prev(t)) * 0.9) /*gamma = 0.9*/"
			+	"};"

			+	"random Real value(Timestep t){"
			+	"  if(t == @0) then"
			+	"    =reward(@0)"
			+	"  else"
			+	"    = (value(Prev(t))) + ( reward(t) * discount(t))"
			+	"};"

			+	"random Boolean BoxIn(Box b, City c, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    if (c == c1) then "
			+	"      = true"
			+	"    else"
			+	"      = false"
			+	"  else"
			+	"    = (exists Truck tr (applied_Unload(b, tr, (Prev(t))) & TruckIn (c, tr, (Prev(t)))))"
			+	"      | (BoxIn(b, c, (Prev(t))) & !(exists Truck tr (true & applied_Load(b, tr, (Prev(t))))))"
			+	"};"

			+	"random Boolean TruckIn(City c, Truck tr, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    if (c == c1) then "
			+	"      = true"
			+	"    else"
			+	"      = false"
			+	"  else"
			+	"    = applied_Drive(c, tr, (Prev(t)))"
			+	"      | (TruckIn(c, tr, (Prev(t))) & !(exists City c2 (true & applied_Drive(c2, tr, (Prev(t))) & c2 != c)))"
			+	"};"

			+	"random Boolean BoxOn(Box b, Truck tr, Timestep t) {"
			+	"  if (t == @0) then"
			+	"    = false"
			+	"  else"
			+	"    = (exists City c (applied_Load(b, tr, (Prev(t))) & BoxIn(b, c, (Prev(t))) & TruckIn(c, tr, (Prev(t)))))"
			+	"      | (BoxOn(b, tr, (Prev(t))) & !(true & applied_Unload(b, tr, (Prev(t)))))"
			+	"};\n"



			;
	
	
	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	

	@Test
	public void test_random_logistics() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");

		Util.initRandom(true);
		Util.setVerbose(true);

	    setModel(logisticsModelStringRandomBoxes);
	    
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    PrintStream out = runner.getEviOutput();
	    
	    out.println("query #{Box b: BoxIn(b, c1, @0)==true};\nquery #{Box b};\n");
	    runner.advancePhase1();
	    int numberOfBoxes = 0;
	    Set x = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
	    for (Object o : x){
	    	numberOfBoxes = ((Integer) ((Histogram.Entry) o).getElement()).intValue();
	    }
	    for (int i=0;i<5; i++){
		    out.println(String.format("decide chosen_Load(argload(@%d), t1, @%d) = true;\n", i*4,  i*4));
		    runner.advancePhase2();
		    out.println(String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+1,i*4+1));
		    runner.advancePhase1();
		    for (Iterator iter = runner.particleFilter.particles.iterator(); iter.hasNext();){
		    	Particle p = (Particle) iter.next();
		    	String qs = String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+1,i*4+1);
				qs = "query #{Box b};" + qs;
		    	Parse parse = new Parse(new StringReader(qs), null);
				List q = Util.list();
				Semant sem = new Semant(runner.model, new Evidence(), q, new ErrorMsg("RandomChoiceTest")); //ignore this error message for now
				sem.transProg(parse.getParseResult());
				p.updateQueriesStats(q);
				
				Set a = getQuery(q, 0).getHistogram().entrySet();
			    for (Object o : a){
			    	numberOfBoxes = ((Integer) ((Histogram.Entry) o).getElement()).intValue();
			    }
			    a = getQuery(q, 1).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
			    }
			    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i));
			    }
		    }
		    Set a = getQuery(runner.evidenceGenerator.getLatestQueries(), 1).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i));
		    }


		    //assertEquals(p , 1, 0.000001);
		    //if (i*4==numberOfBoxes-1)
		    //	break;
		    out.println(String.format("decide chosen_Drive(c3, t1, @%d) = true;\n", i*4+1,i*4+1));
		    runner.advancePhase2();
		    out.println(String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+2,i*4+2));
		    runner.advancePhase1();
		    
		    for (Iterator iter = runner.particleFilter.particles.iterator(); iter.hasNext();){
		    	Particle p = (Particle) iter.next();
		    	String qs = String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+2,i*4+2);
				qs = "query #{Box b};" + qs;
		    	Parse parse = new Parse(new StringReader(qs), null);
				List q = Util.list();
				Semant sem = new Semant(runner.model, new Evidence(), q, new ErrorMsg("RandomChoiceTest")); //ignore this error message for now
				sem.transProg(parse.getParseResult());
				p.updateQueriesStats(q);
				
				a = getQuery(q, 0).getHistogram().entrySet();
			    for (Object o : a){
			    	numberOfBoxes = ((Integer) ((Histogram.Entry) o).getElement()).intValue();
			    }
			    a = getQuery(q, 1).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
			    }
			    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i));
			    }
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 1).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i));
		    }
		    
		    //if (i*4==numberOfBoxes-2)
		    //	break;
		    out.println(String.format("decide chosen_Unload(argunload(t1,@%d), t1, @%d) = true;\n", i*4+2,i*4+2));
		    runner.advancePhase2();
		    out.println(String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+3,i*4+3));
		    runner.advancePhase1();
		    for (Iterator iter = runner.particleFilter.particles.iterator(); iter.hasNext();){
		    	Particle p = (Particle) iter.next();
		    	String qs = String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",i*4+3,i*4+3);
				qs = "query #{Box b};" + qs;
		    	Parse parse = new Parse(new StringReader(qs), null);
				List q = Util.list();
				Semant sem = new Semant(runner.model, new Evidence(), q, new ErrorMsg("RandomChoiceTest")); //ignore this error message for now
				sem.transProg(parse.getParseResult());
				p.updateQueriesStats(q);
				
				a = getQuery(q, 0).getHistogram().entrySet();
			    for (Object o : a){
			    	numberOfBoxes = ((Integer) ((Histogram.Entry) o).getElement()).intValue();
			    }
			    a = getQuery(q, 1).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
			    }
			    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i+1));
			    }
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 1).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i+1));
		    }
		    
		    
		    //if (i*4==numberOfBoxes-3)
		    //	break;
		    out.println(String.format("decide chosen_Drive(c1, t1, @%d) = true;\n", i*4+3,i*4+3));
		    runner.advancePhase2();
		    out.println(String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",(i+1)*4,(i+1)*4));
		    runner.advancePhase1();
		    for (Iterator iter = runner.particleFilter.particles.iterator(); iter.hasNext();){
		    	Particle p = (Particle) iter.next();
		    	String qs = String.format("query #{Box b: BoxIn(b, c1, @%d)==true};\nquery #{Box b : BoxIn(b, c3, @%d)==true };\n",(i+1)*4,(i+1)*4);
				qs = "query #{Box b};" + qs;
		    	Parse parse = new Parse(new StringReader(qs), null);
				List q = Util.list();
				Semant sem = new Semant(runner.model, new Evidence(), q, new ErrorMsg("RandomChoiceTest")); //ignore this error message for now
				sem.transProg(parse.getParseResult());
				p.updateQueriesStats(q);
				
				a = getQuery(q, 0).getHistogram().entrySet();
			    for (Object o : a){
			    	numberOfBoxes = ((Integer) ((Histogram.Entry) o).getElement()).intValue();
			    }
			    a = getQuery(q, 1).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
			    }
			    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
			    for (Object o : a){
			    	assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i+1));
			    }
		    }
		    
		    
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 1).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (numberOfBoxes-i-1));
		    }
		    a = getQuery(runner.evidenceGenerator.getLatestQueries(), 2).getHistogram().entrySet();
		    for (Object o : a){
		    	//assertEquals(((Integer) ((Histogram.Entry) o).getElement()).intValue(), (i+1));
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
	private ArgSpecQuery getQuery(Collection singleton, int j) {
		int i =0;
		Iterator x = singleton.iterator();
		while (i!=j){
			x.next();
			i++;
		}
		return (ArgSpecQuery) x.next();
	}


	private static Properties properties;
	private static ParticleFilter particleFilter;
	private static Model model;
}
