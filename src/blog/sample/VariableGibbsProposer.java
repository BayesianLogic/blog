package blog.sample;

import java.util.*;

import blog.TruncatedUniformAndGaussianMCMCSampler;
import blog.WeightedValue;
import blog.bn.VarWithDistrib;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.model.Model;
import blog.world.PartialWorldDiff;
import blog.world.PartialWorld;


/**
 * Proposer that uses {@link VariableImportanceSampler}s assumed to be Gibbs and
 * uses them at random.
 * 
 * @author Rodrigo
 */
public class VariableGibbsProposer extends AbstractProposer {

	public VariableGibbsProposer(Model model, Properties properties) {
		super(model, properties);
		this.sampler = new TruncatedUniformAndGaussianMCMCSampler();
	}

	@Override
	public double proposeNextState(PartialWorldDiff proposedWorld) {
		List basicVars = new LinkedList(proposedWorld.basicVarToValueMap().keySet());
		List variables = Util.sampleWithoutReplacement(basicVars, basicVars.size());
		// System.out.println("VarGibbsProp: all vars: " +
		// proposedWorld.basicVarToValueMap());
		removeVariablesWithoutDistribution(variables);
		// System.out.println("VarGibbsProp: vars: " + variables);

		Iterator sampleIt = null;
		VarWithDistrib var = null;
		ListIterator it = variables.listIterator();
		while (it.hasNext() && sampleIt == null) {
			var = (VarWithDistrib) it.next();
			// System.out.println("VarGibbsProp: var: " + var);
			// System.out.println("VarGibbsProp: world: " + proposedWorld);
			sampleIt = sampler.sampler(var, proposedWorld);
		}

		if (sampleIt == null)
			Util.fatalError("No variable is eligible for TruncatedUniformAndGaussianMCMCSampler");

		WeightedValue weightedValue = (WeightedValue) sampleIt.next();
		proposedWorld.setValue(var, weightedValue.value);
		proposedWorld.save();
		return 1.0; // sampler is Gibbs, so proposal ratio is 1.
	}

	private void removeVariablesWithoutDistribution(List variables) {
		ListIterator it = variables.listIterator();
		while (it.hasNext())
			if (!(it.next() instanceof VarWithDistrib))
				it.remove();
	}

    public PartialWorldDiff reduceToCore(PartialWorld curWorld, BayesNetVar var) {
        return null;
    }

    public double proposeNextState(PartialWorldDiff proposedWorld, 
                            BayesNetVar var, int i) {
        return 0;
    }

    public double proposeNextState(PartialWorldDiff proposedWorld, BayesNetVar var) {
        return 0;
    }

	public VariableImportanceSampler sampler;
}
