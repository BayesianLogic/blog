package blog.objgen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.ObjectIterator;
import blog.sample.EvalContext;

public abstract class Node {

	abstract Set getParents();

	Set getChildren() {
		return Collections.unmodifiableSet(children);
	}

	/**
	 * Ensures that the given node is in this node's child set.
	 */
	void addChild(Node child) {
		children.add(child);
	}

	/**
	 * Returns an iterator over the objects that satisfy this node in the given
	 * context.
	 * 
	 * <p>
	 * The obvious algorithm for enumerating objects that satisfy a node is by
	 * recursion in the object generation graph. But that algorithm would get
	 * stuck in cycles. Thus, we terminate the recursion at the parents of
	 * POPNodes. A non-root node's <code>iterator</code> method will be called
	 * repeatedly, with larger and larger lists of satisfying objects for the
	 * parents of POP nodes.
	 * 
	 * <p>
	 * Since we're calling <code>iterator</code> repeatedly, we need a way to
	 * get distinct objects each time. Thus, we specify a list of
	 * <em>desired</em> objects for each POP node parent. All the returned
	 * objects must satisfy a POP application involving at least one of these
	 * desired objects and possibly some of the additional objects specified in
	 * <code>otherPOPParentObjs</code>. The desired parent objects will always
	 * be objects that were not included in the previous calls to
	 * <code>iterator</code>, so the objects returned will be distinct from
	 * those returned previously.
	 * 
	 * <p>
	 * As an exception, if <code>includeGuaranteed</code> is true, then the
	 * returned objects may also be guaranteed objects, objects that satisfy a
	 * POP application with an empty tuple of generating objects, or the
	 * satisfiers of TermNodes.
	 * 
	 * @param desiredPOPParentObjs
	 *          Map from Node to List
	 * @param otherPOPParentObjs
	 *          Map from Node to List
	 */
	public abstract ObjectIterator iterator(EvalContext context,
			Set externallyDistinguished, boolean returnPOPApps,
			Map desiredPOPParentObjs, Map otherPOPParentObjs,
			boolean includeGuaranteed);

	/**
	 * Returns true if every call to this node's <code>iterator</code> method
	 * returns an iterator over a finite set.
	 */
	public abstract boolean isFinite();

	/**
	 * Returns true if the iteration order for satisfiers of this node depends
	 * on the iteration order for object identifiers in the given context.
	 */
	public abstract boolean dependsOnIdOrder(EvalContext context);

	Set children = new HashSet();
}