package blog.objgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blog.DefaultObjectIterator;
import blog.ObjectIterator;
import blog.ObjectSet;
import blog.model.BuiltInTypes;
import blog.model.Type;
import blog.sample.EvalContext;

public class GuaranteedNode extends Node {
	GuaranteedNode(Type type) {
		this.type = type;

		if (type == BuiltInTypes.BOOLEAN) {
			satisfiers = new ArrayList();
			satisfiers.add(Boolean.FALSE);
			satisfiers.add(Boolean.TRUE);
		} else if (type.isBuiltIn()) {
			satisfiers = null; // set that we won't enumerate
		} else {
			// user-defined type
			satisfiers = type.getGuaranteedObjects();
		}
	}

	public Set getParents() {
		return Collections.EMPTY_SET;
	}

	public boolean dependsOnIdOrder(EvalContext context) {
		return false;
	}

	public ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed) {
		if (includeGuaranteed) {
			if (satisfiers == null) {
				throw new UnsupportedOperationException(
						"Can't iterate over objects of type " + type);
			}

			return new DefaultObjectIterator(satisfiers.iterator());
		}
		return (ObjectIterator) ObjectSet.EMPTY_OBJECT_SET.iterator();
	}

	public boolean isFinite() {
		return (satisfiers != null);
	}

	public String toString() {
		return ("Guaranteed " + type);
	}

	List satisfiers;
	Type type;
}