package blog.objgen;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import blog.AbstractObjectSet;
import blog.ObjectIterator;
import blog.ObjectSet;
import blog.model.Term;
import blog.sample.EvalContext;

/**
 * Node satisfied only by the denotation of the given term.
 */
public class TermNode extends Node {
	TermNode(Term term) {
		this.term = term;
	}

	public Set getParents() {
		return Collections.EMPTY_SET;
	}

	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed) {
		if (includeGuaranteed) {
			Object value = term.evaluate(context);
			if (value == null) {
				return (ObjectIterator) ObjectSet.UNDETERMINED_SET.iterator();
			}
			return (ObjectIterator) AbstractObjectSet.singleton(value).iterator();
		}
		return (ObjectIterator) ObjectSet.EMPTY_OBJECT_SET.iterator();
	}

	public boolean isFinite() {
		return true;
	}

	public boolean dependsOnIdOrder(EvalContext context) {
		return false;
	}

	public String toString() {
		return term.toString();
	}

	Term term;
}