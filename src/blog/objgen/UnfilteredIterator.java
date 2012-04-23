/**
 * 
 */
package blog.objgen;

import java.util.BitSet;
import java.util.Set;

import blog.AbstractObjectIterator;
import blog.ObjectIterator;
import blog.sample.EvalContext;

class UnfilteredIterator extends AbstractObjectIterator {
	/**
	 * 
	 */
	private final CompiledSetSpec compiledSetSpec;
	UnfilteredIterator(CompiledSetSpec compiledSetSpec, EvalContext context, Set externallyDistinguished) {
		this.compiledSetSpec = compiledSetSpec;
		graphIters = new ObjectIterator[this.compiledSetSpec.objGenGraphs.length];
		for (int i = 0; i < this.compiledSetSpec.objGenGraphs.length; ++i) {
			graphIters[i] = this.compiledSetSpec.objGenGraphs[i].iterator(context,
					externallyDistinguished, false);
			active.set(i);
		}
	}

	protected int skipAfterNext() {
		return graphIters[nextDisjunctIndex].skipIndistinguishable();
	}

	protected Object findNext() {
		while (active.cardinality() > 0) {
			nextDisjunctIndex = (nextDisjunctIndex + 1) % graphIters.length;

			if (!graphIters[nextDisjunctIndex].canDetermineNext()) {
				canDetermineNext = false;
				return null;
			}

			if (graphIters[nextDisjunctIndex].hasNext()) {
				return graphIters[nextDisjunctIndex].next();
			} else {
				active.clear(nextDisjunctIndex);
			}
		}

		return null;
	}

	private ObjectIterator[] graphIters;
	private BitSet active = new BitSet();
	private int nextDisjunctIndex = -1;
}