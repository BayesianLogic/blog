package test.blog;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import blog.BLOGUtil;
import blog.DefaultPartialWorld;
import blog.LikelihoodAndWeight;
import blog.PartialWorld;
import blog.SymbolEvidenceLikelihoodWeighter;
import blog.bn.BasicVar;
import blog.common.Util;
import blog.distrib.Gaussian;
import blog.model.Evidence;
import blog.model.Model;
import blog.model.Term;
import blog.model.ValueEvidenceStatement;


import junit.framework.TestCase;

public class SymbolEvidenceLikelihoodWeighterTest extends TestCase {

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(SymbolEvidenceLikelihoodWeighterTest.class);
	}

	public void setUp() {
		Util.initRandom(true);
	}

	private static String modelDescription = "type Aircraft;"
			+ "guaranteed Aircraft a, a2;" + "random Real Position(Aircraft);"
			+ "Position(ac) ~ Gaussian(3.0, 1.0);" + "" + "type Blip;"
			+ "guaranteed Blip b, b2;" + "" + "origin Aircraft Source(Blip);"
			+ "#Blip(Source = ac) = 1;" + "" + "random Real ApparentPos(Blip);"
			+ "ApparentPos(blip) ~ Gaussian[1](Position(Source(blip)));" + ""
			+ "random Blip B1;" + "B1 ~ UniformChoice({Blip b});" + ""
			+ "random Blip B2;" + "B2 ~ UniformChoice({Blip b});" + ""
			+ "random Boolean Dummy ~ Bernoulli[0.7]();";

	public static void testGetValueEvidenceByIdentifier() {
		Model model = Model.readFromString(modelDescription);

		Evidence evidence;
		Collection terms;
		Map evidenceByTerm;

		Term b1 = BLOGUtil.parseTerm_NE("B1", model);
		Term b2 = BLOGUtil.parseTerm_NE("B2", model);

		ValueEvidenceStatement st1 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1)=3;", model);
		ValueEvidenceStatement st2 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B2)=4;", model);
		ValueEvidenceStatement st3 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1)=ApparentPos(B2);", model);
		ValueEvidenceStatement st4 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(B1)=4;", model);
		ValueEvidenceStatement st5 = BLOGUtil.parseValueEvidenceStatement_NE(
				"obs ApparentPos(b)=4;", model);

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st5);
		terms = Util.list(b1, b2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1), getValueStatementSet(evidenceByTerm, b1));
		assertEquals(Util.set(st2), getValueStatementSet(evidenceByTerm, b2));
		assertEquals(Util.set(st5), getValueStatementSet(evidenceByTerm, null));

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st3);
		terms = Util.list(b1, b2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1), getValueStatementSet(evidenceByTerm, b1));
		assertEquals(Util.set(st2, st3), getValueStatementSet(evidenceByTerm, b2));
		// statements with more than one identifier get assigned to the latest of
		// them.

		// /////////////////

		evidence = new Evidence();
		evidence.addValueEvidence(st1);
		evidence.addValueEvidence(st2);
		evidence.addValueEvidence(st4);
		terms = Util.list(b1, b2);
		evidenceByTerm = SymbolEvidenceLikelihoodWeighter
				.getValueEvidenceByLastIdentifierToBeSampled(terms, evidence);

		assertEquals(Util.set(st1, st4), getValueStatementSet(evidenceByTerm, b1));
		assertEquals(Util.set(st2), getValueStatementSet(evidenceByTerm, b2));
		assertEquals(Util.set(), getValueStatementSet(evidenceByTerm, null));
	}

	private static HashSet getValueStatementSet(Map evidenceByTerm, Term b1) {
		return new HashSet(((Evidence) evidenceByTerm.get(b1)).getValueEvidence());
	}

	Model model;
	Evidence evidence;
	PartialWorld world;
	SymbolEvidenceLikelihoodWeighter ses;
	LikelihoodAndWeight likelihoodAndWeight;

	public void testImportanceSampleLikelihood() {
		model = Model.readFromString(modelDescription);

		// Test fall back in cases without symbol evidence
		evidence = BLOGUtil.parseEvidence_NE("obs Position(a)=1;", model);
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
																										// redefinition error.
		evidence = BLOGUtil.parseEvidence_NE("obs {Aircraft air} = {A1, A2};"
				+ "obs Position(A1) = Position(A2);", model);
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
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(
				"obs {Blip x : Source(x) != null} = {Blip1, Blip2};"
						+ "obs ApparentPos(Blip1) = 1;" + "obs ApparentPos(Blip2) = 6;",
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
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(
				"obs {Blip x : Source(x) != null} = {Blip1, Blip2};"
						+ "obs ApparentPos(Blip1) = 1;" + "obs ApparentPos(Blip2) = 6;"
						+ "obs Dummy = true;", model);
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
		String modelDescription = "type Aircraft;"
				+ "guaranteed Aircraft a, a2;"
				+ "random Real Position(Aircraft);"
				+ "Position(ac) ~ Gaussian(3.0, 1.0);"
				+ ""
				+ "type Blip;"
				+ ""
				+ "origin Aircraft Source1(Blip);"
				+ "origin Aircraft Source2(Blip);"
				+ "#Blip(Source1 = ac1, Source2 = ac2) = 1;"
				+ ""
				+ "random Real ApparentPos(Blip);"
				+ "ApparentPos(blip) ~ Gaussian[1](RSum(Position(Source1(blip)),Position(Source2(blip))));";

		model = Model.readFromString(modelDescription); // avoids symbol
																										// redefinition error.
		world = new DefaultPartialWorld();
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a)", model),
				new Integer(3));
		world.setValue((BasicVar) BLOGUtil.parseVariable_NE("Position(a2)", model),
				new Integer(7));
		evidence = BLOGUtil.parseEvidence_NE(
				"obs {Blip x} = {Blip1, Blip2, Blip3, Blip4};"
						+ "obs ApparentPos(Blip1) =  6;" + "obs ApparentPos(Blip2) = 10;"
						+ "obs ApparentPos(Blip3) = 10;" + "obs ApparentPos(Blip4) = 14;",
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
																																					// there
																																					// are
																																					// (only)
																																					// two
																																					// possible
																																					// good
																																					// matches.
	}

}
