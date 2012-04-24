package blog.distrib;

import java.util.*;

import blog.*;
import blog.bn.BayesNetVar;
import blog.bn.VarWithDistrib;
import blog.model.Clause;
import blog.model.DependencyModel;
import blog.sample.DefaultEvalContext;
import blog.world.PartialWorld;

public class Util {

	public static UnivarGaussian posteriorOnVarGivenChildrenWithGaussingDistributionWithVarAsMean(
			VarWithDistrib var, PartialWorld world) {
		Collection<UnivarGaussian> gaussians = getCollectionOfPosteriorDesnsitiesOnVarIfDepModelsAreGaussiansAroundItOrNull(
				var, world);
		if (gaussians == null)
			return null;
		return UnivarGaussian.product(gaussians);
	}

	public static Collection<UnivarGaussian> getCollectionOfPosteriorDesnsitiesOnVarIfDepModelsAreGaussiansAroundItOrNull(
			BayesNetVar var, PartialWorld world) {
		LinkedList result = new LinkedList();
		Iterator childrenIt = world.getBayesNet().getChildren(var).iterator();
		while (childrenIt.hasNext()) {
			VarWithDistrib child = (VarWithDistrib) childrenIt.next();
			UnivarGaussian gaussian = getPosteriorDesnsityOnVarIfDepModelIsGaussianAroundItOrNull(
					child, var, world);
			if (gaussian == null)
				return null;
			result.add(gaussian);
		}
		return result;
	}

	public static UnivarGaussian getPosteriorDesnsityOnVarIfDepModelIsGaussianAroundItOrNull(
			VarWithDistrib child, BayesNetVar var, PartialWorld world) {
		UnivarGaussian gaussianOnChild = getGaussianIfDepModelIsGaussianWithMean(
				child.getDepModel(), world,
				((Double) world.getValue(var)).doubleValue());
		if (gaussianOnChild == null)
			return null;
		return gaussianOnChild.meanPosterior(((Double) world.getValue(child))
				.doubleValue());
	}

	public static UnivarGaussian getGaussianIfDepModelIsGaussianWithMean(
			DependencyModel depModel, PartialWorld world, double mean) {
		if (depModel.getClauseList().size() != 1)
			return null;
		Clause firstClause = (Clause) blog.common.Util
				.getFirst(depModel.getClauseList());
		DependencyModel.Distrib distrib = firstClause
				.getDistrib(new DefaultEvalContext(world));
		CondProbDistrib cpd = distrib.getCPD();
		if (!(cpd instanceof UnivarGaussian))
			return null;
		if (!distrib.getArgValues().get(0).equals(new Double(mean)))
			return null;
		UnivarGaussian gaussian = (UnivarGaussian) cpd;
		// double gaussianMean = gaussian.getMean(); // It used to be this test. But
		// for some reason the CPD was not coming with the arg values as its
		// argument values.
		// if (gaussianMean != mean)
		// return null;
		return gaussian;
	}
}
