package test.blog;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import blog.BLOGUtil;
import blog.bn.BasicVar;
import blog.common.Util;
import blog.distrib.Gaussian;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Term;
import blog.model.ValueEvidenceStatement;
import blog.sample.LikelihoodAndWeight;
import blog.sample.SymbolEvidenceLikelihoodWeighter;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;


import junit.framework.TestCase;

public class SymbolEvidenceLikelihoodWeighterTest extends TestCase {

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(SymbolEvidenceLikelihoodWeighterTest.class);
	}

	public void setUp() {
		Util.initRandom(true);
	}

	private static String modelDescription = ""
			+ "type Aircraft foo;" 							+ "\n"
			+ "guaranteed Aircraft a1, a2;" 			+ "\n" 
			+ "random Real Position(Aircraft a) ~" 		+ "\n"
			+ "	Gaussian(3.0, 1.0);" 					+ "\n" 
			+ "" 										+ "\n"
			+ "type Blip;" 								+ "\n"
			+ "guaranteed Blip b1, b2;" 				+ "\n" 
			+ "" 										+ "\n"
			+ "origin Aircraft Source(Blip);" 			+ "\n"
			+ "#Blip(Source = a) = 1;" 					+ "\n" 
			+ "" 										+ "\n"
			+ "random Real ApparentPos(Blip b) ~ "		+ "\n"
			+ "	Gaussian(1,Position(Source(b)));" 		+ "\n" 
			+ "" 										+ "\n"
			+ "random Blip B1() ~ " 					+ "\n" 
			+ "	UniformChoice({Blip b});" 				+ "\n" 
			+ "" 										+ "\n"
			+ "random Blip B2() ~ " 					+ "\n" 
			+ "	UniformChoice({Blip b});"	 			+ "\n" 
			+ "" 										+ "\n"
			+ "random Boolean Dummy() ~ Bernoulli(0.7);"	+ "\n";

	public static void testGetValueEvidenceByIdentifier() {
		Model model = Model.readFromString(modelDescription);

		Evidence evidence;
		Collection terms;
		Map evidenceByTerm;

		Term B1 = BLOGUtil.parseTerm_NE("B1()", model);
		Term B2 = BLOGUtil.parseTerm_NE("B2()", model);

		ValueEvidenceStatement st1 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1())=3;", model);
		ValueEvidenceStatement st2 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B2())=4;", model);
		ValueEvidenceStatement st3 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1())=ApparentPos(B2());", model);
		ValueEvidenceStatement st4 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1())=4;", model);
		ValueEvidenceStatement st5 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(b1)=4;", model);

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st5);
		terms = Util.list(B1, B2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1), getValueStatementSet(evidenceByTerm, B1));
		assertEquals(Util.set(st2), getValueStatementSet(evidenceByTerm, B2));
		assertEquals(Util.set(st5), getValueStatementSet(evidenceByTerm, null));

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st3);
		terms = Util.list(B1, B2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1), getValueStatementSet(evidenceByTerm, B1));
		assertEquals(Util.set(st2, st3), getValueStatementSet(evidenceByTerm, B2));
		// statements with more than one identifier get assigned to the latest of
		// them.

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st4);
		terms = Util.list(B1, B2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1, st4), getValueStatementSet(evidenceByTerm, B1));
		assertEquals(Util.set(st2), getValueStatementSet(evidenceByTerm, B2));
		assertEquals(Util.set(), getValueStatementSet(evidenceByTerm, null));
	}

	private static HashSet getValueStatementSet(Map evidenceByTerm, Term x) {
		return new HashSet(((Evidence) evidenceByTerm.get(x)).getValueEvidence());
	}

	Model model;
	Evidence evidence;
	PartialWorld world;
	SymbolEvidenceLikelihoodWeighter ses;
	LikelihoodAndWeight likelihoodAndWeight;

	public void testImportanceSampleLikelihood() {
		model = Model.readFromString(modelDescription);

		// Test fall back in cases without symbol evidence
		evidence = BLOGUtil.parseEvidence_NE("obs Position(a1)=1;", model);
		world = new DefaultPartialWorld();
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);
		assertEquals(0.053990966513188056, likelihoodAndWeight.likelihood); // density
																																				// of
																																				// Gaussian(3,1)
																																				// at 1.
		assertEquals(1.0, likelihoodAndWeight.weight);

		// Test likelihood zero when world does not correspond to number of symbols
		// in evidence.
		evidence = BLOGUtil.parseEvidence_NE("obs {Aircraft air} = {A1, A2, A3};",
				model);
		world = new DefaultPartialWorld();
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);
		assertEquals(0.0, likelihoodAndWeight.likelihood); // since model defines
																												// two aircraft.
		assertEquals(1.0, likelihoodAndWeight.weight);

		// Test probability zero for equality of continuous variables.
		model = Model.readFromString(modelDescription); // avoids symbol

		evidence = BLOGUtil.parseEvidence_NE(""
				+ "obs {Aircraft air} = {A1, A2};\n"
				+ "obs Position(A1) = Position(A2);\n", 
				model);
		world = new DefaultPartialWorld();
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);
		// fall back will sample continuous variables, with probability zero of them
		// turning out equal, and so likelihoods will be zero with weight 1.
		assertEquals(0.0, likelihoodAndWeight.likelihood);
		assertEquals(1.0, likelihoodAndWeight.weight);

		// Gaussian g3 = new Gaussian(Util.list(3, 1));
		// System.out.println("Gaussian likelihood for 1 given 3: " +
		// g3.getProb(1));
		// System.out.println("Gaussian likelihood for 6 given 3: " +
		// g3.getProb(6));
		// Gaussian g7 = new Gaussian(Util.list(7, 1));
		// System.out.println("Gaussian likelihood for 1 given 7: " +
		// g7.getProb(1));
		// System.out.println("Gaussian likelihood for 6 given 7: " +
		// g7.getProb(6));

		// The first test of a case in which association sampling actually occurs.
		model = Model.readFromString(modelDescription); // avoids symbol
																										// redefinition error.
		world = new DefaultPartialWorld();
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a1)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(""
				+ "obs {Blip x : Source(x) != null} = {Blip1, Blip2};\n"
				+ "obs ApparentPos(Blip1) = 1;\n" 
				+ "obs ApparentPos(Blip2) = 6;\n",
				model);
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);

		assertEquals(0.013064233284684921, likelihoodAndWeight.likelihood, 10E-8); // same
																																								// as
																																								// before
																																								// *
																																								// 0.7
		assertEquals(0.5000000562675874, likelihoodAndWeight.weight, 10E-8);
		// This weight actually depends on the fact that Blip1 is proposal-sampled
		// before Blip2.
		// The implementation does sample identifiers in the order they are defined.

		// Same as above but with non-symbol evidence statement as well.
		model = Model.readFromString(modelDescription); // avoids symbol
																										// redefinition error.
		world = new DefaultPartialWorld();
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a1)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(""
				+ "obs {Blip x : Source(x) != null} = {Blip1, Blip2};\n"
				+ "obs ApparentPos(Blip1) = 1;\n" 
				+ "obs ApparentPos(Blip2) = 6;\n"
				+ "obs Dummy = true;\n", 
				model);
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);
		// System.out.println("Likelihood: {" + likelihoodAndWeight.likelihood +
		// ", " + likelihoodAndWeight.weight + "}");

		assertEquals(0.009144963299279446, likelihoodAndWeight.likelihood, 10E-8); // verified
																																								// by
																																								// looking
																																								// at
																																								// trace
		assertEquals(0.5000000562675874, likelihoodAndWeight.weight, 10E-8);
		// This weight actually depends on the fact that Blip1 is proposal-sampled
		// before Blip2.
		// The implementation does sample identifiers in the order they are defined.
	}

	public void testMultipleIdentifiers() {
		// Here, blips are generated from pairs of aircraft.
		// This allows us to test situations where there are more than one
		// identifier in a value evidence statement.
		String modelDescription = ""
				+ "type Aircraft;\n"
				+ "guaranteed Aircraft a1, a2;\n"
				+ "random Real Position(Aircraft a) ~ \n"
				+ "\tGaussian(3.0, 1.0);\n"
				+ "\n"
				+ "type Blip;\n"
				+ "\n"
				+ "origin Aircraft Source1(Blip b);\n"
				+ "origin Aircraft Source2(Blip b);\n"
				+ "#Blip(Source1 = ac1, Source2 = ac2) = 1;\n"
				+ "\n"
				+ "random Real ApparentPos(Blip b) ~ \n"
				+ "Gaussian(1,Position(Source1(b)) + Position(Source2(b)));\n"
				;

		model = Model.readFromString(modelDescription); // avoids symbol
																										// redefinition error.
		world = new DefaultPartialWorld();
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a1)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(""
				+ "obs {Blip x} = {Blip1, Blip2, Blip3, Blip4};\n"
				+ "obs ApparentPos(Blip1) =  6;\n" 
				+ "obs ApparentPos(Blip2) = 10;\n"
				+ "\n"
				+ "obs ApparentPos(Blip3) = 10;\n" 
				+ "obs ApparentPos(Blip4) = 14;\n",
				model);
		ses = new SymbolEvidenceLikelihoodWeighter();
		likelihoodAndWeight = ses.likelihoodAndWeight(evidence, world);

		assertEquals(0.02533029591058445, likelihoodAndWeight.likelihood, 10E-8); // verified
																																							// by
																																							// looking
																																							// at
																																							// program's
																																							// trace
		assertEquals(0.08343120942571122, likelihoodAndWeight.weight, 10E-8); // this
																																					// makes
																																					// sense
																																					// since
																																					// two
																																					// possible
																																					// good
																																					// matches.
	}

}
