package blog.objgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.AbstractObjectIterator;
import blog.ObjectIterator;
import blog.ObjectSet;
import blog.common.Util;
import blog.model.Term;
import blog.sample.EvalContext;


public class IntegerNode extends Node {
	void addLowerBound(Term term, boolean strict) {
		// Store weak lower bound, which is 1 greater than strict
		// lower bound.
		lowerBounds.add(new Bound(term, strict ? 1 : 0));
	}

	void addUpperBound(Term term, boolean strict) {
		// Store weak upper bound, which is 1 less than strict
		// upper bound.
		upperBounds.add(new Bound(term, strict ? -1 : 0));
	}

	boolean isConstrained() {
		return (!lowerBounds.isEmpty()) || (!upperBounds.isEmpty());
	}

	public Set getParents() {
		return Collections.EMPTY_SET;
	}

	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed) {
		if (!includeGuaranteed) {
			return (ObjectIterator) ObjectSet.EMPTY_OBJECT_SET.iterator();
		}

		if (!lowerBounds.isEmpty()) {
			Integer l = getLowerBound(context);
			if (l == null) {
				return (ObjectIterator) ObjectSet.UNDETERMINED_SET.iterator();
			}

			if (!upperBounds.isEmpty()) {
				Integer u = getUpperBound(context);
				if (u == null) {
					return (ObjectIterator) ObjectSet.UNDETERMINED_SET.iterator();
				}

				Iterator iter = Util.getIntegerRangeIterator(l.intValue(),
						u.intValue());
				return new IntNodeIterator(iter);
			}

			Iterator iter = Util.getAscendingIntegerIterator(l.intValue());
			return new IntNodeIterator(iter);
		}
		if (!upperBounds.isEmpty()) {
			Integer u = getUpperBound(context);
			if (u == null) {
				return (ObjectIterator) ObjectSet.UNDETERMINED_SET.iterator();
			}

			Iterator iter = Util.getDescendingIntegerIterator(u.intValue());
			return new IntNodeIterator(iter);
		}

		return new IntNodeIterator(Util.getIntegerIterator());
	}

	public boolean isFinite() {
		return ((!lowerBounds.isEmpty()) && (!upperBounds.isEmpty()));
	}

	public boolean dependsOnIdOrder(EvalContext context) {
		return false;
	}

	/**
	 * Method that can be overridden by subclasses so that the iterator for this
	 * node returns objects corresponding to integers, not the integers
	 * themselves.
	 */
	protected Object correspondingObj(Integer i) {
		return i;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[(");
		for (Iterator iter = lowerBounds.iterator(); iter.hasNext();) {
			buf.append(iter.next());
		}
		buf.append("), (");
		for (Iterator iter = upperBounds.iterator(); iter.hasNext();) {
			buf.append(iter.next());
		}
		buf.append(")]");
		return buf.toString();
	}

	Integer getLowerBound(EvalContext context) {
		Integer bestSoFar = null;
		for (Iterator iter = lowerBounds.iterator(); iter.hasNext();) {
			Integer value = ((IntegerNode.Bound) iter.next()).getValue(context);
			if (value == null) {
				return null;
			}
			if ((bestSoFar == null) || (value.intValue() > bestSoFar.intValue())) {
				bestSoFar = value;
			}
		}
		return bestSoFar;
	}

	Integer getUpperBound(EvalContext context) {
		Integer bestSoFar = null;
		for (Iterator iter = upperBounds.iterator(); iter.hasNext();) {
			Integer value = ((IntegerNode.Bound) iter.next()).getValue(context);
			if (value == null) {
				return null;
			}
			if ((bestSoFar == null) || (value.intValue() < bestSoFar.intValue())) {
				bestSoFar = value;
			}
		}
		return bestSoFar;
	}

	private class IntNodeIterator extends AbstractObjectIterator {
		IntNodeIterator(Iterator underlying) {
			this.underlying = underlying;
		}

		protected Object findNext() {
			if (!underlying.hasNext()) {
				return null;
			}
			return correspondingObj((Integer) underlying.next());
		}

		private Iterator underlying;
	}

	private static class Bound {
		Bound(Term term, int offset) {
			this.term = term;
			this.offset = offset;
		}

		Integer getValue(EvalContext context) {
			Number value = (Number) term.evaluate(context);
			if (value == null) {
				return null;
			}
			return new Integer(value.intValue() + offset);
		}

		public boolean equals(Object o) {
			if (o instanceof IntegerNode.Bound) {
				IntegerNode.Bound other = (IntegerNode.Bound) o;
				return (term.equals(other.term) && (offset == other.offset));
			}
			return false;
		}

		public int hashCode() {
			return (term.hashCode() + offset);
		}

		public String toString() {
			if (offset > 0) {
				return (term + "+" + offset);
			}
			if (offset > 0) {
				return (term + "-" + Math.abs(offset));
			}
			return term.toString();
		}

		private Term term;
		private int offset;
	}

	private List lowerBounds = new ArrayList(); // of Bound
	private List upperBounds = new ArrayList(); // of Bound
}