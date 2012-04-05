package blog;

import java.util.*;

import blog.bn.VarWithDistrib;

/**
 * An interface for variable proposal methods specific to certain situations
 * (distributions given blanket).
 * 
 * @author Rodrigo
 */
public interface VariableImportanceSampler {
	/**
	 * Returns an (infinite) Iterator ranging over samples ({@link WeightedValue}
	 * s) of a variable in a given world.
	 */
	public Iterator sampler(VarWithDistrib var, PartialWorld world);
}
