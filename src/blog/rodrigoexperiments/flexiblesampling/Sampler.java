package blog.rodrigoexperiments.flexiblesampling;

import java.util.*;

import blog.PartialWorld;
import blog.bn.VarWithDistrib;

/**
 * 
 * A FlexibleImportanceSampler is the interface for (possibly block) importance
 * samplers. It is useful in the following situation: you have a
 * non-self-supporting partial world and would like to extend it to a
 * self-supporting partial world that instantiates a given seed random variable.
 * A FlexibleImportanceSampler will sample such extensions (one at a time) and
 * return them along with their weight, that is, the probabilities of the newly
 * sampled variables possibly divided by the proposal distribution probabilities
 * for them, if one was used.
 * 
 * It can also do the inverse: take a self-supporting partial world with a given
 * seed random variable instantiated, and return a partial world in which the
 * seed is no longer instantiated, with maybe some other random variables
 * uninstantiated as well (if it implements a block sampling method), along with
 * the weight associated with sampling the self-supporting partial world
 * <b>from</b> the returned non-self-supporting world. This is useful for
 * implementing Metropolis-Hastings transition kernels.
 * 
 * Formally, a FlexibleImportanceSampler is an object that, for a predetermined
 * {@link VarWithDistrib} <code>v</code> {@link PartialWorld} <code>w</code>
 * pair (typically provided at its construction), samples self-supporting
 * extensions <code>w'</code>of <code>w</code> in which <code>v</code> is
 * instantiated, also providing its <i>weight</i> of <code>w'</code>, that is,
 * the ratio <code>p(x)/q(x)</code> where <code>x</code> is the set of variables
 * sampled for obtaining <code>w'</code> from <code>w</code>, <code>p</code> is
 * the model's distribution, and <code>q</code> is the proposal distribution
 * used for sampling. It can also receive a self-supporting partial world
 * <code>w'</code> and seed random variable <code>v</code> and return a partial
 * world <code>w</code> that does not instantiate <code>v</code>, along with the
 * weight that <i>would</i> be computed by sampling <code>w'</code> from
 * <code>w</code>.
 * 
 * @author Rodrigo
 */
public interface Sampler {

	public interface Sample {
		public PartialWorld getPartialWorld();

		public double getWeight();
	}

	public Sample sample();

	public Sample measure();

	public Sample unsample();
}
