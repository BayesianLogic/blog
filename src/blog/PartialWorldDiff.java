/*
 * Copyright (c) 2007, Massachusetts Institute of Technology
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package blog;

import java.util.*;

import blog.bn.BasicVar;
import blog.bn.DerivedVar;
import blog.bn.NumberVar;
import blog.common.DGraph;
import blog.common.HashMapDiff;
import blog.common.HashMultiMapDiff;
import blog.common.IndexedMultiMap;
import blog.common.IndexedMultiMapDiff;
import blog.common.MapDiff;
import blog.common.MapWithPreimagesDiff;
import blog.common.MultiMap;
import blog.common.MultiMapDiff;
import blog.common.ParentUpdateDGraph;
import blog.common.Util;

/**
 * Represents a PartialWorld as a set of differences relative to an underlying
 * "saved" PartialWorld. Note that the a PartialWorldDiff uses separate common
 * ground identifiers than its underlying world, although the asserted
 * identifiers in the underlying world are still asserted in the
 * PartialWorldDiff unless they are removed with <code>removeIdentifier</code>.
 */
public class PartialWorldDiff extends AbstractPartialWorld {
	/**
	 * Creates a new PartialWorldDiff with the given underlying world. This world
	 * uses object identifiers for the same types as the underlying world does.
	 */
	public PartialWorldDiff(PartialWorld underlying) {
		super(underlying.getIdTypes());
		basicVarToValue = new HashMapDiff(underlying.basicVarToValueMap());
		objToUsesAsValue = new HashMultiMapDiff(underlying.objToUsesAsValueMap());
		objToUsesAsArg = new HashMultiMapDiff(underlying.objToUsesAsArgMap());
		assertedIdToPOPApp = new HashMapDiff(underlying.assertedIdToPOPAppMap());
		popAppToAssertedIds = new IndexedMultiMapDiff(
				underlying.popAppToAssertedIdsMap());
		commIdToPOPApp = new HashMapDiff(underlying.assertedIdToPOPAppMap());
		popAppToCommIds = new IndexedMultiMapDiff(
				underlying.popAppToAssertedIdsMap());
		bayesNet = new ParentUpdateDGraph(underlying.getBayesNet());
		varToUninstParent = new MapWithPreimagesDiff(
				underlying.varToUninstParentMap());
		varToLogProb = new HashMapDiff(underlying.varToLogProbMap());
		derivedVarToValue = new HashMapDiff(underlying.derivedVarToValueMap());

		savedWorld = underlying;
	}

	/**
	 * Creates a new PartialWorldDiff whose underlying world is
	 * <code>underlying</code>, and whose current version is set equal to
	 * <code>toCopy</code>.
	 */
	public PartialWorldDiff(PartialWorld underlying, PartialWorld toCopy) {
		this(underlying);

		for (Iterator iter = toCopy.getAssertedIdentifiers().iterator(); iter
				.hasNext();) {
			ObjectIdentifier id = (ObjectIdentifier) iter.next();
			assertIdentifier(id, toCopy.getPOPAppSatisfied(id));
		}

		for (Iterator iter = toCopy.getInstantiatedVars().iterator(); iter
				.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			setValue(var, toCopy.getValue(var));
		}

		for (Iterator iter = toCopy.getDerivedVars().iterator(); iter.hasNext();) {
			addDerivedVar((DerivedVar) iter.next());
		}
	}

	/**
	 * Returns the saved version of this world. The returned PartialWorld object
	 * is updated as new versions are saved.
	 */
	public PartialWorld getSaved() {
		return savedWorld;
	}

	/**
	 * Changes the saved version of this world to equal the current version.
	 */
	public void save() {
		for (Iterator iter = getIdsWithChangedPOPApps().iterator(); iter.hasNext();) {
			ObjectIdentifier id = (ObjectIdentifier) iter.next();
			NumberVar newPOPApp = (NumberVar) assertedIdToPOPApp.get(id);
			if (newPOPApp == null) {
				savedWorld.removeIdentifier(id);
			} else {
				savedWorld.assertIdentifier(id, newPOPApp);
			}
		}

		for (Iterator iter = getChangedVars().iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			savedWorld.setValue(var, getValue(var));
		}

		Set derivedVars = ((MapDiff) derivedVarToValue).getChangedKeys();
		for (Iterator iter = derivedVars.iterator(); iter.hasNext();) {
			DerivedVar var = (DerivedVar) iter.next();
			if (derivedVarToValue.containsKey(var)) { // not removed
				savedWorld.addDerivedVar(var); // no effect if already there
			}
		}

		updateParentsAndProbs();
		savedWorld.updateBayesNet(bayesNet, varToUninstParent, varToLogProb,
				derivedVarToValue);

		clearChanges(); // since underlying is now updated

		for (Iterator iter = diffListeners.iterator(); iter.hasNext();) {
			WorldDiffListener listener = (WorldDiffListener) iter.next();
			listener.notifySaved();
		}
	}

	/**
	 * Changes this world to equal the saved version. Warning: WorldListener
	 * objects will not be notified of changes to the values of basic variables
	 * made by this method.
	 */
	public void revert() {
		clearChanges();
		clearCommIdChanges();

		for (Iterator iter = diffListeners.iterator(); iter.hasNext();) {
			WorldDiffListener listener = (WorldDiffListener) iter.next();
			listener.notifyReverted();
		}
	}

	/**
	 * Returns the set of variables that have different values in the current
	 * world than they do in the saved world. This includes variables that are
	 * instantiated in this world and not the saved world, or vice versa.
	 * 
	 * @return unmodifiable Set of BasicVar
	 */
	public Set getChangedVars() {
		return ((MapDiff) basicVarToValue).getChangedKeys();
	}

	/**
	 * Returns the set of objects that serve as values for a different set of
	 * basic RVs in this world than they do in the saved world. This may include
	 * objects that exist in this world but not the saved world, or vice versa.
	 */
	public Set getObjsWithChangedUsesAsValue() {
		return ((MultiMapDiff) objToUsesAsValue).getChangedKeys();
	}

	/**
	 * Returns the set of object identifiers that are asserted in either this
	 * world or the saved world, and that satisfy a different POP application in
	 * this world than in the saved world. This includes object identifiers that
	 * are asserted in this world and not the saved world, or vice versa.
	 */
	public Set getIdsWithChangedPOPApps() {
		return ((MapDiff) assertedIdToPOPApp).getChangedKeys();
	}

	/**
	 * Returns the set of POP applications whose set of asserted identifiers is
	 * different in this world from in the saved world.
	 */
	public Set getPOPAppsWithChangedIds() {
		return ((MultiMapDiff) popAppToAssertedIds).getChangedKeys();
	}

	/**
	 * Returns the Set of BayesNetVar objects V such that the probability P(V |
	 * parents(V)) is not the same in this world as in the saved world. This may
	 * be because the value of V has changed or because the values of some of V's
	 * parents have changed. The returned set also includes any DerivedVars whose
	 * value has changed.
	 * 
	 * @return unmodifiable Set of BayesNetVar
	 */
	public Set getVarsWithChangedProbs() {
		updateParentsAndProbs();

		HashSet results = new HashSet();
		results.addAll(((MapDiff) varToLogProb).getChangedKeys());
		results.addAll(((MapDiff) derivedVarToValue).getChangedKeys());
		return results;
	}

	/**
	 * Returns the set of variables that are barren in this world but either are
	 * not in the graph or are not barren in the saved world. A barren variable is
	 * one with no children.
	 * 
	 * @return unmodifiable Set of BayesNetVar
	 */
	public Set getNewlyBarrenVars() {
		updateParentsAndProbs();
		return ((ParentUpdateDGraph) bayesNet).getNewlyBarrenNodes();
	}

	/**
	 * Returns the set of identifiers that are floating in this world and not the
	 * saved world. An identifier is floating if it is used as an argument of some
	 * basic variable, but is not the value of any basic variable.
	 * 
	 * @return unmodifiable Set of ObjectIdentifier
	 */
	public Set getNewlyFloatingIds() {
		Set newlyFloating = new HashSet();

		// Scan changed and newly instantiated vars, looking for new
		// arguments and old values that are now floating.
		for (Iterator iter = getChangedVars().iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			if (getValue(var) != null) {
				for (int i = 0; i < var.args().length; ++i) {
					Object arg = var.args()[i];
					if ((arg instanceof ObjectIdentifier)
							&& getVarsWithValue(arg).isEmpty()
							&& (getSaved().getVarsWithArg(arg).isEmpty() || !getSaved()
									.getVarsWithValue(arg).isEmpty())) {
						newlyFloating.add(arg);
					}
				}
			}

			if (getSaved().getValue(var) != null) {
				Object oldValue = getSaved().getValue(var);
				if ((oldValue instanceof ObjectIdentifier)
						&& (assertedIdToPOPApp.get(oldValue) != null)
						&& getVarsWithValue(oldValue).isEmpty()
						&& !getVarsWithArg(oldValue).isEmpty()) {
					newlyFloating.add(oldValue);
				}
			}
		}

		return Collections.unmodifiableSet(newlyFloating);
	}

	/**
	 * Returns the set of number variables that are overloaded in this world but
	 * not the saved world. A number variable is overloaded if the number of
	 * identifiers asserted to satisfy it is greater than its value, or it is not
	 * instantiated and one or more identifiers are still asserted to satisfy it.
	 * 
	 * @return unmodifiable Set of NumberVar
	 */
	public Set getNewlyOverloadedNumberVars() {
		Set newlyOverloaded = new HashSet();

		// Iterate over variables whose values changed, look for number vars
		for (Iterator iter = getChangedVars().iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			if (var instanceof NumberVar) {
				NumberVar nv = (NumberVar) var;
				if (isOverloaded(nv) && !getSaved().isOverloaded(nv)) {
					newlyOverloaded.add(nv);
					if (Util.verbose()) {
						System.out.println("Number var " + nv + " with value "
								+ getValue(nv) + " is overloaded by "
								+ popAppToAssertedIds.get(nv));
					}
				}
			}
		}

		// Iterate over number variables with a changed set of asserted IDs
		for (Iterator iter = getPOPAppsWithChangedIds().iterator(); iter.hasNext();) {
			NumberVar nv = (NumberVar) iter.next();
			if (isOverloaded(nv) && !getSaved().isOverloaded(nv)) {
				newlyOverloaded.add(nv);
			}
		}

		return newlyOverloaded;
	}

	/**
	 * Returns the set of number variables that yield different probability
	 * multipliers in this world than they do in the saved world. These are the
	 * number variables that have different values or different numbers of
	 * asserted identifiers in this world and the saved world, and have at least
	 * one asserted identifier in this world or the saved world.
	 * 
	 * @return unmodifiable Set of NumberVar
	 */
	public Set getVarsWithChangedMultipliers() {
		Set changedMultipliers = new HashSet();

		// Iterate over variables whose values changed, look for number vars
		for (Iterator iter = getChangedVars().iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			if (var instanceof NumberVar) {
				NumberVar nv = (NumberVar) var;
				if ((getAssertedIdsForPOPApp(nv).size() > 0)
						|| (getSaved().getAssertedIdsForPOPApp(nv).size() > 0)) {
					changedMultipliers.add(nv);
				}
			}
		}

		// Iterate over number variables whose set of asserted identifiers
		// changed
		for (Iterator iter = getPOPAppsWithChangedIds().iterator(); iter.hasNext();) {
			changedMultipliers.add(iter.next());
		}

		return changedMultipliers;
	}

	/**
	 * Adds the given object to the list of listeners that will be notified when
	 * this PartialWorldDiff is saved or reverted.
	 */
	public void addDiffListener(WorldDiffListener listener) {
		if (!diffListeners.contains(listener)) {
			diffListeners.add(listener);
		}
	}

	/**
	 * Removes the given object from the list of listeners that will be notified
	 * when this PartialWorldDiff is saved or reverted.
	 */
	public void removeDiffListener(WorldDiffListener listener) {
		diffListeners.remove(listener);
	}

	private void clearChanges() {
		((MapDiff) basicVarToValue).clearChanges();
		((MultiMapDiff) objToUsesAsValue).clearChanges();
		((MultiMapDiff) objToUsesAsArg).clearChanges();
		((MapDiff) assertedIdToPOPApp).clearChanges();
		((MultiMapDiff) popAppToAssertedIds).clearChanges();
		((ParentUpdateDGraph) bayesNet).clearChanges();
		((MapDiff) varToLogProb).clearChanges();
		((MapDiff) derivedVarToValue).clearChanges();

		dirtyVars.clear();
	}

	private void clearCommIdChanges() {
		((MapDiff) commIdToPOPApp).clearChanges();
		((MultiMapDiff) popAppToCommIds).clearChanges();
	}

	private PartialWorld savedWorld;

	private List diffListeners = new ArrayList(); // of WorldDiffListener
}
