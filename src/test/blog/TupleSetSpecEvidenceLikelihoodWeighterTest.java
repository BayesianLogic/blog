package test.blog;

import java.util.*;

import blog.BLOGUtil;
import blog.DefaultPartialWorld;
import blog.PartialWorld;
import blog.TupleSetSpecEvidenceLikelihoodWeighter;
import blog.common.TupleIterator;
import blog.common.Util;
import blog.distrib.Gaussian;
import blog.model.Evidence;
import blog.model.Model;
import static blog.BLOGUtil.*;

import junit.framework.TestCase;

/**
 * Unit testing for {@link TupleSetSpecEvidenceLikelihoodWeighter}.
 */
public class TupleSetSpecEvidenceLikelihoodWeighterTest extends TestCase {

	private static final int numSamples = 1000;
	private static final double errorMargin = 0.05;

	public void setUp() {
		Util.initRandom(true);
	}

	public void testImplicitSetSpecImportanceSampling() {
		String evidenceDescription;
		Model model;
		DefaultPartialWorld world;
		String modelDescription;
		double expectedLikelihood;
		double variance;

		/** A basic case with a uniform distribution. */
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random NaturalNum ApparentPos(Blip b)"
				+ "if     b = b1 then ~ TabularCPD[[0.33334, 0.33333, 0.33333]]()"
				+ "elseif b = b2 then ~ TabularCPD[[0.33334, 0.33333, 0.33333]]()"
				+ "elseif b = b3 then ~ TabularCPD[[0.33334, 0.33333, 0.33333]]()"
				+ "              else = 10;";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {0, 1, 2};";
		world = new DefaultPartialWorld();
		expectedLikelihood = 2.0 / 9;
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A basic case with a deterministic distribution. */
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random NaturalNum ApparentPos(Blip b)"
				+ "if     b = b1 then ~ TabularCPD[[1, 0, 0]]()"
				+ "elseif b = b2 then ~ TabularCPD[[0, 1, 0]]()"
				+ "elseif b = b3 then ~ TabularCPD[[0, 0, 1]]()"
				+ "              else = 10;";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {0, 1, 2};";
		world = new DefaultPartialWorld();
		expectedLikelihood = 1.0;
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A basic case with a skewed distribution. */
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random NaturalNum ApparentPos(Blip b)"
				+ "if     b = b1 then ~ TabularCPD[[0.8, 0.1, 0.1]]()"
				+ "elseif b = b2 then ~ TabularCPD[[0.1, 0.8, 0.1]]()"
				+ "elseif b = b3 then ~ TabularCPD[[0.1, 0.1, 0.8]]()"
				+ "              else = 10;";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {0, 1, 2};";
		world = new DefaultPartialWorld();
		expectedLikelihood = 0.538; // calculated by hand
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A case with too many values. */
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random NaturalNum ApparentPos(Blip b)"
				+ "if     b = b1 then ~ TabularCPD[[0.8, 0.1, 0.1]]()"
				+ "elseif b = b2 then ~ TabularCPD[[0.1, 0.8, 0.1]]()"
				+ "elseif b = b3 then ~ TabularCPD[[0.1, 0.1, 0.8]]()"
				+ "              else = 10;";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {0, 1, 2, 3};";
		world = new DefaultPartialWorld();
		expectedLikelihood = 0.0; // more values than blips
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A case with some values being already determined. */
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random NaturalNum ApparentPos(Blip b)"
				+ "if     b = b1 then ~ TabularCPD[[0.8, 0.1, 0.1]]()"
				+ "elseif b = b2 then ~ TabularCPD[[0.1, 0.8, 0.1]]()"
				+ "elseif b = b3 then ~ TabularCPD[[0.1, 0.1, 0.8]]()"
				+ "              else = 10;";
		model = BLOGUtil.parseModel_NE(modelDescription);
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {0, 1, 2};";
		world = new DefaultPartialWorld();
		world.setValue(parseBasicVar_NE("ApparentPos(b1)", model), new Integer(0));
		world.setValue(parseBasicVar_NE("ApparentPos(b3)", model), new Integer(0));
		expectedLikelihood = 0.0; // more values than blips
		assertLikelihood(model, evidenceDescription, world, expectedLikelihood);

		/** A continuous values case. */
		variance = 5.0;
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random Real ApparentPos(Blip b)"
				+ "if     b = b1 then ~ Gaussian(1, " + variance + ")"
				+ "elseif b = b2 then ~ Gaussian(2, " + variance + ")"
				+ "elseif b = b3 then ~ Gaussian(3, " + variance + ")"
				+ "              else ~ Gaussian(4, " + variance + ");";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {1.0, 2.0, 3.0};";
		world = new DefaultPartialWorld();
		expectedLikelihood = gaussianObservationsLikelihood(
				Util.arrayList(1.0, 2.0, 3.0), variance, Util.arrayList(1.0, 2.0, 3.0));
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/**
		 * The same as previous, but with smaller variance and thus higher
		 * likelihood.
		 */
		variance = 1.0;
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random Real ApparentPos(Blip b)"
				+ "if     b = b1 then ~ Gaussian(1, " + variance + ")"
				+ "elseif b = b2 then ~ Gaussian(2, " + variance + ")"
				+ "elseif b = b3 then ~ Gaussian(3, " + variance + ")"
				+ "              else ~ Gaussian(4, " + variance + ");";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {1.0, 2.0, 3.0};";
		world = new DefaultPartialWorld();
		expectedLikelihood = gaussianObservationsLikelihood(
				Util.arrayList(1.0, 2.0, 3.0), variance, Util.arrayList(1.0, 2.0, 3.0));
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** The same as previous, but with only two observations. */
		variance = 1;
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random Real ApparentPos(Blip b)"
				+ "if     b = b1 then ~ Gaussian(1, " + variance + ")"
				+ "elseif b = b2 then ~ Gaussian(2, " + variance + ")"
				+ "elseif b = b3 then ~ Gaussian(3, " + variance + ")"
				+ "              else ~ Gaussian(4, " + variance + ");";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {1.0, 2.0};";
		world = new DefaultPartialWorld();
		expectedLikelihood = gaussianObservationsLikelihood(
				Util.arrayList(1.0, 2.0, 3.0), variance, Util.arrayList(1.0, 2.0));
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A boundary case, with no observations. */
		variance = 1;
		modelDescription = "type Blip;" + "guaranteed Blip b1, b2, b3;"
				+ "random Real ApparentPos(Blip b)"
				+ "if     b = b1 then ~ Gaussian(1, " + variance + ")"
				+ "elseif b = b2 then ~ Gaussian(2, " + variance + ")"
				+ "elseif b = b3 then ~ Gaussian(3, " + variance + ")"
				+ "              else ~ Gaussian(4, " + variance + ");";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {};";
		world = new DefaultPartialWorld();
		expectedLikelihood = gaussianObservationsLikelihood(
				Util.arrayList(1.0, 2.0, 3.0), variance, Util.arrayList());
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);

		/** A boundary case, with no Gaussians and no observations. */
		variance = 1;
		modelDescription = "type Blip;"
				+ "random Real ApparentPos(Blip b) ~ Gaussian(1, " + variance + ");";
		evidenceDescription = "obs {ApparentPos(b) for Blip b} = {null for Blip b};"; // the
																																									// 'null'
																																									// is
																																									// for
																																									// forcing
																																									// the
																																									// right
																																									// hand
																																									// side
																																									// to
																																									// be
																																									// a
																																									// tuple
																																									// set,
																																									// rather
																																									// than
																																									// an
																																									// explicit
																																									// set.
																																									// We
																																									// should
																																									// eventually
																																									// make
																																									// BLOG
																																									// represent
																																									// tuple
																																									// sets
																																									// as
																																									// regular
																																									// sets
																																									// the
																																									// elements
																																									// of
																																									// which
																																									// are
																																									// tuples
																																									// to
																																									// avoid
																																									// problems
																																									// like
																																									// that.
		world = new DefaultPartialWorld();
		expectedLikelihood = gaussianObservationsLikelihood(Util.arrayList(),
				variance, Util.arrayList());
		assertLikelihood(modelDescription, evidenceDescription, world,
				expectedLikelihood);
	}

	private void assertLikelihood(String modelDescription,
			String evidenceDescription, PartialWorld world, double expectedLikelihood) {
		Model model = BLOGUtil.parseModel_NE(modelDescription);
		assertLikelihood(model, evidenceDescription, world, expectedLikelihood);
	}

	private void assertLikelihood(Model model, String evidenceDescription,
			PartialWorld world, double expectedLikelihood) {
		Evidence evidence = parseEvidence_NE(evidenceDescription, model);
		TupleSetSpecEvidenceLikelihoodWeighter is = new TupleSetSpecEvidenceLikelihoodWeighter();
		double likelihoodEstimate = is.importanceSampling(evidence, world, 1000);
		assertEquals(expectedLikelihood, likelihoodEstimate, errorMargin);
		System.out.println("Expected likelihood: " + expectedLikelihood);
		System.out.println("Estimated likelihood: " + likelihoodEstimate);
	}

	public static double gaussianObservationsLikelihood(ArrayList means,
			double variance, ArrayList observations) {
		if (observations.size() < means.size())
			return 0;
		// See and reflect changes in (*) in {@link
		// TupleSetSpecEvidenceLikelihoodWeighter}.

		ArrayList<Gaussian> gaussians = new ArrayList<Gaussian>();
		for (Number mean : (Collection<Number>) means) {
			Gaussian g = new Gaussian(mean.doubleValue(), variance);
			gaussians.add(g);
		}
		// Goes through all associations from gaussians to values, including those
		// that do not map to all values,
		// which are skipped.
		// This is not the most efficient way of generating those.
		double sum = 0;
		List observationIndices = Util.makeIntegerSequenceList(0,
				observations.size());
		List observationIndicesList = new LinkedList();
		for (int i = 0; i != means.size(); i++)
			observationIndicesList.add(observationIndices);
		Iterator associationIt = new TupleIterator(observationIndicesList);
		while (associationIt.hasNext()) {
			List association = (List) associationIt.next();
			if (!association.containsAll(observationIndices))
				continue;
			double product = 1.0;
			for (int i = 0; i != means.size(); i++) {
				int valueIndex = ((Integer) association.get(i)).intValue();
				double factor = gaussians.get(i).getProb(
						((Number) observations.get(valueIndex)).doubleValue());
				product *= factor;
			}
			sum += product;
		}
		return sum;
	}

}
