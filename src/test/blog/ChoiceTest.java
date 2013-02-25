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
import blog.model.DecisionEvidenceStatement;
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
public class ChoiceTest extends TestCase {

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
	
	private static final String logisticsModelString = 
			  "type Box;  type Truck;  type City;"
			+	"distinct Box b3, b2, b1;"
			+	"distinct Truck t1, t2;"
			+	"distinct City c1, c2, c3;"

			+	"decision Boolean chosen_Load(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Unload(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Drive(City c, Truck tr, Timestep t);"
			
			+	"random Boolean applied_Load(Box b, Truck tr, Timestep t) {"
			+	"  if (exists City c (BoxIn ( b, c, t) & TruckIn (c, tr, t))) then = (chosen_Load(foo(b), tr, t) & succeed_action(t)) else = false"
			+	"};"
			
			+	"random Box foo (Box b) {if true then = b};"

			+	"random Boolean applied_Unload(Box b, Truck tr, Timestep t) {"
			+	"  if (BoxOn ( b, tr, t) == true ) then = (chosen_Unload(b, tr, t) & succeed_action(t))"
			+	"};"

			+	"random Boolean applied_Drive(City c, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Drive(c, tr, t) & succeed_action(t))"
			+ 	"};"
			
			+	"random Boolean succeed_action (Timestep t){"
			+		"~ Categorical({true -> 0.9, false -> 0.1})"
			+	"};"
			+	"random Boolean pass(Timestep t){"
			+	"  if (t==@0) then"
			+	"    = false"
			+	"  else if (BoxIn(b3, c3, t) & BoxIn(b1, c3, t) & BoxIn(b2, c3, t)) then"
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
			+	"};"



			;
	
	private static final String logisticsModelStringRandomBoxes = 
			  "type Box;  type Truck;  type City;"
			+	"#Box ~ Poisson(5);"
			+	"distinct Truck t1, t2;"
			+	"distinct City c1, c2, c3;"

			+	"decision Boolean chosen_Load(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Unload(Box b, Truck tr, Timestep t);"

			+	"decision Boolean chosen_Drive(City c, Truck tr, Timestep t);"
			+   "random Box argload (Timestep t){  ~ UniformChoice({Box b : BoxIn (b, c1, t) == true })};"
			+	"random Box argunload (Truck tr, Timestep t){  ~ UniformChoice({Box b : BoxOn (b, t1, t) == true })};"
			+	"random Boolean applied_Load(Box b, Truck tr, Timestep t) {"
			+	"  if (exists City c (BoxIn ( b, c, t) & TruckIn (c, tr, t))) then = (chosen_Load(foo(b), tr, t) & succeed_action(t)) else = false"
			+	"};"
			
			+	"random Box foo (Box b) {if true then = b};"

			+	"random Boolean applied_Unload(Box b, Truck tr, Timestep t) {"
			+	"  if (BoxOn ( b, tr, t) == true ) then = (chosen_Unload(b, tr, t) & succeed_action(t))"
			+	"};"

			+	"random Boolean applied_Drive(City c, Truck tr, Timestep t) {"
			+	"  if (true) then = (chosen_Drive(c, tr, t) & succeed_action(t))"
			+ 	"};"
			
			+	"random Boolean succeed_action (Timestep t){"
			+		"~ Categorical({true -> 0.9, false -> 0.1})"
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
			+	"};"



			;
	
	private static final String mazeModelString = 
			"type Action;"	
		+	"distinct Action up, down, left, right;"
					
		+	"decision Boolean applied_action (Action a, Timestep t);"
		
		+	"random Boolean succeed_action (Timestep t){"
		+	"  ~ Categorical({true -> 0.9, false -> 0.1})"
		+	"};"
			
		+	"random Integer pos (Timestep t) {"
		+	"  if (t == @0) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 1 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (right, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 2 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 1"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (left, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 2"
		+	"  else if (pos (Prev(t)) == 3 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 3"
		+	"  else if (pos (Prev(t)) == 4 & applied_action (right, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 5"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (left, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 4"
		+	"  else if (pos (Prev(t)) == 5 & applied_action (up, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 6"
		+	"  else if (pos (Prev(t)) == 6 & applied_action (down, Prev(t)) & succeed_action(Prev(t))) then"
		+	"    = 5"
		+	"  else"
		+	"    = pos (Prev(t))"
		+	"};"
			
		+	"random Boolean pass(Timestep t){"
		+	"  if (t==@0) then"
		+	"    = false"
		+	"  else if (pos(t)==6) then"
		+	"    = true"
		+	"  else"
		+	"    = pass(Prev(t))"
		+	"};"
			
		+	"random Real reward(Timestep t){"
		+	"  if ( (pass(t) == true) & (pass(Prev(t)) == false) ) then" 
		+	"    = 10.0"
		+	"  else"
		+	"    = 0.0"
		+	"};"
			
		+	"random Real discount(Timestep t){"
		+	"  if(t == @0) then" 
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
		
		;
	
	
	private void setModel(String newModelString) throws Exception {
		model = new Model();
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.stringSetup(model, evidence, queries, newModelString);
	}
	

	@Test
	public void test_logistics() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "2000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("value(t)");

		Util.initRandom(true);
		Util.setVerbose(true);

	    setModel(logisticsModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    PrintStream out = runner.getEviOutput();
	    
	    out.println("");
	    runner.advancePhase1();
	    System.out.println(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "0.0"));
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "0.0"), 1, 0);
	    
	    out.println("decide chosen_Load(b1, t1, @0) = true;\n");
	    runner.advancePhase2();
	    
	    
	    
	    out.println("query actionName(@0);\n query applied_Load(b1,t1,@0);\n obs succeed_action(@1)=true;\n "); //normal evidence
	    runner.advancePhase1();
	    Double a = BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "\"load\"");
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), 0.9, 0.09);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "0.0"), 1, 0);	    
	    out.println("decide chosen_Drive(c3, t1, @1) = true;\n ");
	    runner.advancePhase2();


	    
	    
	    
	    out.println("query actionName(@1);\n query applied_Drive(c3,t1,@1);\n obs succeed_action(@2)=true;\n");
	    runner.advancePhase1();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "\"drive\""), 1, 0);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), 1, 0.000001);
	    out.println("decide chosen_Unload(b1, t1, @2) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("query succeed_action(@2);\n query BoxIn(b1,c3,@3);\n");
	    runner.advancePhase1();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), a, a*0.111);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "0.0"), 1-a, (1-a)*0.111*1.5);

	    out.println("decide chosen_Drive(c1, t1, @3) = true;\n ");
	    runner.advancePhase2();
	    
	    out.println(" query chosen_Load(b3, t1, @4);\n");
	    runner.advancePhase1();
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "false"), 1, 0);
	    out.println("decide chosen_Load(b2, t1, @4) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("");
	    runner.advancePhase1();
	    out.println("decide chosen_Drive(c3, t1, @5) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("");
	    runner.advancePhase1();
	    out.println("decide chosen_Unload(b2, t1, @6) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("");
	    runner.advancePhase1();
	    out.println("decide chosen_Drive(c1, t1, @7) = true;\n decide chosen_Load(b3, t2, @8) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("query applied_Load(b3,t2,@8);\n");
	    runner.advancePhase1();
	    out.println("");
	    runner.advancePhase2();

	    out.println("");
	    runner.advancePhase1();
	    out.println("decide chosen_Drive(c3, t2, @9) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("query chosen_Drive(c3,t2,@9);\n query succeed_action(@9);\n query applied_Drive(c3,t2,@9);\n");
	    runner.advancePhase1();
	    out.println("decide chosen_Unload(b3, t2, @10) = true;\n");
	    runner.advancePhase2();

	    out.println("query reward(@11);\n");
	    runner.advancePhase1();
	    out.println("");
	    runner.advancePhase2();
	    boolean ltf = false;
	    
	    Set x = getQuery(runner.evidenceGenerator.getLatestQueries(), 0).getHistogram().entrySet();
	    for (Object o : x){
	    	if (((Double) ((Histogram.Entry) o).getElement()).doubleValue()>15){
	    		ltf = true;
	    		break;
	    	}
	    }
	    assertTrue(ltf);
	    //assertTrue(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "16.901")>0.1);

	}

	@Test
	public void test_maze() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("numParticles", "1000");
		properties.setProperty("useDecayedMCMC", "false");
		properties.setProperty("numMoves", "1");
		boolean randomize = true;
		Collection linkStrings = Util.list();
		Collection queryStrings = Util.list("pos(t)");

		Util.initRandom(false);
		Util.setVerbose(true);

	    setModel(mazeModelString);
	    ParticleFilterRunnerOnline runner = new ParticleFilterRunnerOnline(model, linkStrings, queryStrings, properties);
	    PrintStream out = runner.getEviOutput();
	    
	    out.println("decide applied_action(up, @0) = true;\n");
	    //runner.evidenceGenerator.getInput();
	    runner.evidenceGenerator.updateDecision();
	    Evidence e = runner.evidenceGenerator.getLatestDecision();
	    DecisionEvidenceStatement v = (DecisionEvidenceStatement) Util.getFirst(e.getChoiceEvidence());
	    assertTrue(e.getChoiceEvidence().size()==1);
	    java.lang.reflect.Field iscompiled = DecisionEvidenceStatement.class.getDeclaredField("compiled");
	    iscompiled.setAccessible(true);
	    Boolean truthvalue = (Boolean) iscompiled.get(v);
	    assertTrue(truthvalue.booleanValue());
	    assertEquals(e.toString(),"[/*DerivedVar*/ applied_action(up, @0) = true]");
	    
	    out.println("obs succeed_action(@0)=true;\n ");
	    runner.advancePhase1();
	    //basic check
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "1"), 1, 0);

	    out.println("decide applied_action(up, @0) = true;\n");
	    runner.advancePhase2();
	    
	    
	    //now test entering two evidence at the same time
	    out.println("obs succeed_action(@1)=true;\n obs succeed_action(@2)=true;\n query applied_action(up,@0); query applied_action(down,@2);\n");
	    runner.advancePhase1();
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "true"), 1, 0);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), 0, 0);
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "2"), 1, 0);

	    out.println("decide applied_action(right, @1) = true;\n decide applied_action(down, @2) = true;\n");
	    runner.advancePhase2();

	    out.println("query applied_action(up,@1); query applied_action(down,@2);\n");
	    runner.advancePhase1();
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "false"), 1, 0);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), 1, 0);
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "3"), 1, 0);

	    out.println("");
	    runner.advancePhase2();
	    
	    out.println("query applied_action(up,@1); query applied_action(down,@2);\n"); //test providing queries
	    runner.advancePhase1();
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "false"), 1, 0);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 2), model, "true"), 1, 0);
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "4"), 1, 0);

	    out.println("decide applied_action(right, @3) = true;\n");
	    runner.advancePhase2();
	    
	    out.println("obs succeed_action(@4)=true;\n query applied_action(up,@3);query  applied_action(down,@2);\n query applied_action(right,@3);\n obs succeed_action(@3)=true;\n");
	    runner.advancePhase1();
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "false"), 1, 0);
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 3), model, "false"), 0, 0);
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "5"), 1, 0);

	    out.println("decide applied_action(up, @4) = true;\n");
	    runner.advancePhase2();	    
	    
	    out.println("obs succeed_action(@5)=true;\n query applied_action(up,@4);query applied_action(down,@2);\n ");
	    runner.advancePhase1();

	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 1), model, "true"), 1, 0);
	    
	    assertEquals(BLOGUtil.getProbabilityByString(getQuery(runner.evidenceGenerator.getLatestQueries(), 0), model, "6"), 1, 0);
	    out.println("");
	    runner.advancePhase2();
	    
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
