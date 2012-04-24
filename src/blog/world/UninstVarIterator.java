/**
 * 
 */
package blog.world;

import java.util.Iterator;

import blog.bn.VarWithDistrib;

/**
 * Inner class for iterating over the list of uninstantiated variables. It is
 * like an ordinary iterator, but has an extra method <code>setValue</code> for
 * instantiating the variable returned by the last call to <code>next</code>.
 */
public class UninstVarIterator implements Iterator<VarWithDistrib> {
	/**
	 * 
	 */
	private final WorldInProgress worldInProgress;

	/**
	 * @param worldInProgress
	 */
	UninstVarIterator(WorldInProgress worldInProgress) {
		this.worldInProgress = worldInProgress;
	}

	public boolean hasNext() {
		ensureListExtended();
		return listIter.hasNext();
	}

	/**
	 * Always returns an object of class VarWithDistrib.
	 */
	public VarWithDistrib next() {
		ensureListExtended();
		lastVar = (VarWithDistrib) listIter.next();
		return lastVar;
	}

	/**
	 * The <code>remove</code> method is not supported, because the only way to
	 * remove a variable from the list of uninstantiated variables is to
	 * instantiate it.
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				"Can't remove objects from UninstVarIterator.");
	}

	/**
	 * Instantiates the last variable returned by <code>next</code> to the given
	 * value. The variable must be supported by this partial world, or a fatal
	 * error will occur.
	 * 
	 * @throws IllegalStateException
	 *           if <code>next</code> has not yet been called, or if
	 *           <code>setValue</code> has already been called since the last
	 *           call to <code>next</code>
	 */
	public void setValue(Object value) {
		if (lastVar == null) {
			throw new IllegalStateException("No variable to instantiate.");
		}

		this.worldInProgress.setValue(lastVar, value);
		listIter.remove(); // no longer uninstantiated

		lastVar = null;
	}

	private void ensureListExtended() {
		if (!listIter.hasNext()) {
			// Try extending the list of uninstantiated vars by
			// increasing maxInt.
			this.worldInProgress.increaseMaxInt();
		}
	}

	private Iterator listIter = this.worldInProgress.uninstVars.iterator();
	VarWithDistrib lastVar = null;
}