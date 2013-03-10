/*
 * Copyright (c) 2005, Regents of the University of California
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

package blog.world;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import blog.ObjectIdentifier;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.CBN;
import blog.bn.DefaultCBN;
import blog.bn.DerivedVar;
import blog.bn.NumberVar;
import blog.bn.OriginVar;
import blog.bn.RandFuncAppVar;
import blog.bn.VarWithDistrib;
import blog.common.HashMapWithPreimages;
import blog.common.HashMultiMap;
import blog.common.IndexedHashMultiMap;
import blog.common.IndexedMultiMap;
import blog.common.IndexedSet;
import blog.common.MapWithPreimages;
import blog.common.MultiMap;
import blog.common.Util;
import blog.model.DependencyModel;
import blog.model.Model;
import blog.model.NonGuaranteedObject;
import blog.model.POP;
import blog.model.RandomFunction;
import blog.objgen.AbstractObjectSet;
import blog.objgen.ObjectIterator;
import blog.objgen.ObjectSet;
import blog.sample.ParentRecEvalContext;

/**
 * An implementation of the PartialWorld interface that just requires concrete
 * subclasses to initialize some protected variables.
 */
public abstract class AbstractPartialWorld implements PartialWorld {
	/**
	 * Creates a new partial world. Identifiers will be used to represent the
	 * user-defined types in the set idTypes.
	 * 
	 * @param idTypes
	 *          Set of Type objects
	 */
	public AbstractPartialWorld(Set idTypes) {
		this.idTypes = new HashSet(idTypes);
		this.cbn = new DefaultCBN();
	}

	public Set getInstantiatedVars() {
		return Collections.unmodifiableSet(basicVarToValue.keySet());
	}

	public boolean isInstantiated(BayesNetVar var) {
		return basicVarToValue.containsKey(var)
				|| derivedVarToValue.containsKey(var);
	}

	public Object getValue(BayesNetVar var) {
		if (var instanceof BasicVar) {
			return basicVarToValue.get(var);
		}
		if (var instanceof OriginVar) {
			return commIdToPOPApp.get(((OriginVar) var).getIdentifier());
		}
		if (var instanceof DerivedVar) {
			if (cbn.nodes().contains(var)) {
				updateParentsAndProbs();
				return derivedVarToValue.get(var);
			}

			// need to compute value
			return ((DerivedVar) var).getValue(this);
		}

		throw new IllegalArgumentException("Unrecognized BayesNetVar: " + var);
	}

	public void setValue(BasicVar var, Object value) {

		// if (var.toString().contains("ApparentPos") && value == Model.NULL) {
		// System.out.println("AbstractPartialWorld: " + var + " set to NULL!");
		// System.out.println();
		// }

		Object oldValue = basicVarToValue.get(var);
		if (value == null ? (oldValue == null) : value.equals(oldValue)) {
			Util.debug("Setting var: " + var + " to " + value);
			return;
		}
		Util.debug("Setting var: " + var + " to " + value + ", replacing "
				+ oldValue);
		var.ensureStable();

		if ((var instanceof NumberVar) && (oldValue != null)) {
			prepareForNumberVarChange((NumberVar) var, oldValue, value);
		}

		if (value == null) {
			basicVarToValue.remove(var);
		} else {
			// checkIdentifiers(var, value); // allow any identifiers
			basicVarToValue.put(var, value);
		}

		dirtyVars.add(var);
		updateUsageForChange(var, oldValue, value);

		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			((WorldListener) iter.next()).varChanged(var, oldValue, value);
		}
	}

	public void truncateList(RandomFunction f, Object[] initialArgs, int len) {
		Object[] args = new Object[initialArgs.length + 1];
		System.arraycopy(initialArgs, 0, args, 0, initialArgs.length);
		args[initialArgs.length] = new Integer(len);
		BasicVar var = new RandFuncAppVar(f, args);

		int i = len;
		while (getValue(var) != null) {
			setValue(var, null);
			args[initialArgs.length] = new Integer(++i);
			var = new RandFuncAppVar(f, args);
		}
	}

	public void truncateNumberList(POP pop, Object[] initialArgs, int len) {
		Object[] args = new Object[initialArgs.length + 1];
		System.arraycopy(initialArgs, 0, args, 0, initialArgs.length);
		args[initialArgs.length] = new Integer(len);
		BasicVar var = new NumberVar(pop, args);

		int i = len;
		while (getValue(var) != null) {
			setValue(var, null);
			args[initialArgs.length] = new Integer(++i);
			var = new NumberVar(pop, args);
		}
	}

	public Set getVarsWithValue(Object value) {
		return Collections.unmodifiableSet((Set) objToUsesAsValue.get(value));
	}

	public Set getVarsWithArg(Object arg) {
		return Collections.unmodifiableSet((Set) objToUsesAsArg.get(arg));
	}

	public Set getInverseTuples(RandomFunction func, Object val) {
		Set result = new HashSet();

		Set vars = (Set) objToUsesAsValue.get(val);
		for (Iterator iter = vars.iterator(); iter.hasNext();) {
			BasicVar user = (BasicVar) iter.next();
			if ((user instanceof RandFuncAppVar)
					&& (((RandFuncAppVar) user).func() == func)) {
				result.add(Arrays.asList(user.args()));
			}
		}

		return result;
	}

	public double getLogProbOfValue(BayesNetVar var) {
		if ((var instanceof VarWithDistrib) && (basicVarToValue.get(var) != null)) {
			updateParentsAndProbs();
			Double logProb = (Double) varToLogProb.get(var);
			if (logProb == null) {
				throw new IllegalArgumentException("No log prob computed for " + var);
			}
			if (logProb == PartialWorld.UNDET) {
				if (Util.verbose()) {
					BasicVar uninstParent = var.getFirstUninstParent(this);
					Util.fatalError("Can't get log prob of variable " + var
							+ " because it depends on " + uninstParent
							+ ", which is not instantiated.");
				}
			}
			return logProb.doubleValue();
		}

		return 0; // non-basic variable or not instantiated
	}

	public double getProbOfValue(BayesNetVar var) {
		return Math.exp(getLogProbOfValue(var));
	}

	public ObjectSet getSatisfiers(NumberVar popApp) {
		for (int i = 0; i < popApp.args().length; ++i) {
			Object arg = popApp.args()[i];
			if (arg == Model.NULL) {
				return ObjectSet.EMPTY_OBJECT_SET;
			}
			if (arg instanceof NonGuaranteedObject) {
				NonGuaranteedObject ngo = (NonGuaranteedObject) arg;
				NumberVar nv = ngo.getNumberVar();
				Integer nvValue = (Integer) basicVarToValue.get(nv);
				if ((nvValue != null) && (nvValue.intValue() < ngo.getNumber())) {
					// This generating object does not exist
					return ObjectSet.EMPTY_OBJECT_SET;
				}
			}
		}

		if (basicVarToValue.get(popApp) == null) {
			throw new IllegalStateException("Number variable not instantiated: "
					+ popApp);
		}

		if (idTypes.contains(popApp.pop().type())) {
			return new POPAppSatisfierSet(popApp);
		}
		return new NonGuarObjSet(popApp);
	}

	public NumberVar getPOPAppSatisfied(Object obj) {
		if (obj instanceof NonGuaranteedObject) {
			return ((NonGuaranteedObject) obj).getNumberVar();
		}

		if (obj instanceof ObjectIdentifier) {
			return (NumberVar) commIdToPOPApp.get(obj);
		}

		// Must be guaranteed object, so not generated by any POP app
		return null;
	}

	public Set getIdTypes() {
		return Collections.unmodifiableSet(idTypes);
	}

	public Set getAssertedIdentifiers() {
		return Collections.unmodifiableSet(assertedIdToPOPApp.keySet());
	}

	public IndexedSet getAssertedIdsForPOPApp(NumberVar popApp) {
		IndexedSet ids = (IndexedSet) popAppToAssertedIds.get(popApp);
		return (ids == null) ? IndexedSet.EMPTY_INDEXED_SET : ids;
	}

	public void assertIdentifier(ObjectIdentifier id, NumberVar newPOPApp) {
		if (id.getType() != newPOPApp.pop().type()) {
			throw new IllegalArgumentException("Identifier " + id
					+ " cannot satisfy POP " + newPOPApp.pop());
		}

		NumberVar oldPOPApp = (NumberVar) commIdToPOPApp.get(id);
		if (!newPOPApp.equals(oldPOPApp)) {
			Integer nvValue = (Integer) basicVarToValue.get(newPOPApp);
			if (nvValue != null) {
				trimNonAssertedIds(newPOPApp, nvValue.intValue(), 1);
			}

			if (oldPOPApp != null) {
				popAppToCommIds.remove(oldPOPApp, id);
				popAppToAssertedIds.remove(oldPOPApp, id);
			}
		}

		commIdToPOPApp.put(id, newPOPApp);
		popAppToCommIds.add(newPOPApp, id); // does nothing if already there

		assertedIdToPOPApp.put(id, newPOPApp);
		popAppToAssertedIds.add(newPOPApp, id);

		OriginVar originVar = new OriginVar(id);
		if (cbn.nodes().contains(originVar)) {
			dirtyVars.add(originVar); // so children are updated
		}

		tellListenersIdChanged(id, oldPOPApp, newPOPApp);
	}

	public void assertIdentifier(ObjectIdentifier id) {
		NumberVar popApp = (NumberVar) commIdToPOPApp.get(id);
		if (popApp == null) {
			throw new IllegalArgumentException("Identifier not in common ground: "
					+ id);
		}

		if (assertedIdToPOPApp.put(id, popApp) == null) {
			// assertion was not made already
			popAppToAssertedIds.add(popApp, id);
			tellListenersIdChanged(id, null, popApp);

			// Don't need to add OriginVar to Bayes net here because
			// it will be added automatically by
			// ParentRecEvalContext.getPOPAppSatisfied if it serves as
			// a parent for anything.
		}
	}

	public ObjectIdentifier addIdentifierForPOPApp(NumberVar popApp) {
		ObjectIdentifier id = new ObjectIdentifier(popApp.pop().type());

		Integer nvValue = (Integer) basicVarToValue.get(popApp);
		if (nvValue != null) {
			trimNonAssertedIds(popApp, nvValue.intValue(), 1);
		}

		commIdToPOPApp.put(id, popApp);
		popAppToCommIds.add(popApp, id);
		assertedIdToPOPApp.put(id, popApp);
		popAppToAssertedIds.add(popApp, id);

		tellListenersIdChanged(id, null, popApp);
		return id;
	}

	public void removeIdentifier(ObjectIdentifier id) {
		NumberVar popApp = (NumberVar) commIdToPOPApp.remove(id);
		if (popApp != null) {
			popAppToCommIds.remove(popApp, id);

			if (assertedIdToPOPApp.remove(id) != null) {
				// id was asserted before this removal
				popAppToAssertedIds.remove(popApp, id);
				uninstantiateVarsUsing(id);
				dirtyVars.add(new OriginVar(id));

				tellListenersIdChanged(id, popApp, null);
			}
		}
	}

	public boolean isOverloaded(NumberVar popApp) {
		Integer varValue = (Integer) basicVarToValue.get(popApp);
		Set ids = getAssertedIdsForPOPApp(popApp);
		return (((varValue == null) && !ids.isEmpty()) || ((varValue != null) && (ids
				.size() > varValue.intValue())));
	}

	public CBN getCBN() {
		updateParentsAndProbs();
		return cbn;
	}

	public void updateCBN(CBN newCBN, MapWithPreimages newVarToUninstParent,
			Map newVarLogProbs, Map newDerivedVarValues) {
		VarInfoUpdater updater = new CopyingInfoUpdater(newCBN,
				newVarToUninstParent, newVarLogProbs, newDerivedVarValues);
		updateParentsAndProbs(updater);
	}

	public Set getDerivedVars() {
		return Collections.unmodifiableSet(derivedVarToValue.keySet());
	}

	public boolean addDerivedVar(DerivedVar var) {
		if (cbn.addNode(var)) {
			derivedVarToValue.put(var, PartialWorld.UNDET);
			dirtyVars.add(var);
			return true;
		}
		return false;
	}

	public boolean removeDerivedVar(DerivedVar var) {
		if (cbn.removeNode(var)) {
			derivedVarToValue.remove(var);
			dirtyVars.remove(var);
			return true;
		}
		return false;
	}

	public void print(PrintStream s) {
		List vars = new ArrayList(getInstantiatedVars());
		Collections.sort(vars);
		for (Iterator iter = vars.iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			s.println(var + " = " + getValue(var));

			Set parents = getCBN().getParents((Object) var);
			for (Iterator parentIter = parents.iterator(); parentIter.hasNext();) {
				s.println("\t<- " + parentIter.next());
			}
		}
	}

	public Map basicVarToValueMap() {
		return basicVarToValue;
	}

	public MultiMap objToUsesAsValueMap() {
		return objToUsesAsValue;
	}

	public MultiMap objToUsesAsArgMap() {
		return objToUsesAsArg;
	}

	public Map assertedIdToPOPAppMap() {
		return assertedIdToPOPApp;
	}

	public IndexedMultiMap popAppToAssertedIdsMap() {
		return popAppToAssertedIds;
	}

	public MapWithPreimages varToUninstParentMap() {
		updateParentsAndProbs();
		return varToUninstParent;
	}

	public Map varToLogProbMap() {
		updateParentsAndProbs();
		return varToLogProb;
	}

	public Map derivedVarToValueMap() {
		updateParentsAndProbs();
		return derivedVarToValue;
	}

	public void addListener(WorldListener listener) {
		listeners.add(listener);
	}

	public void removeListener(WorldListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Ensure that parent sets, basic variable log probabilities, and derived
	 * variable values are all up to date. We do this in batch because recomputing
	 * these things is somewhat expensive, and it's pointless to recompute the log
	 * probability for a variable when one of its parents changes if all its other
	 * parents are also about to change.
	 */
	protected void updateParentsAndProbs() {
		updateParentsAndProbs(defaultUpdater);
	}

	/**
	 * Inner class representing the set of identifiers, and objects with no
	 * identifier, that satisfy a particular POP application. This set is backed
	 * by the partial world, so it changes when the set of objects that satisfy
	 * the POP application changes.
	 */
	private class POPAppSatisfierSet extends AbstractObjectSet {
		POPAppSatisfierSet(NumberVar popApp) {
			numberVar = popApp;
		}

		/**
		 * Returns either the number of asserted identifiers in the world that
		 * satisfy the relevant POP application, or the value of the number variable
		 * for the relevant POP application, whichever is larger. Returns null if
		 * the number variable is not instantiated.
		 */
		public Integer sizeInternal() {
			Integer varValue = (Integer) basicVarToValue.get(numberVar);
			if (varValue == null) {
				return null;
			}

			int numIds = getAssertedIdsForPOPApp(numberVar).size();
			return new Integer(Math.max(numIds, varValue.intValue()));
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			return new POPAppSatisfierIterator(externallyDistinguished);
		}

		public Boolean containsInternal(Object o) {
			return Boolean.valueOf(getAssertedIdsForPOPApp(numberVar).contains(o));
		}

		public ObjectSet getExplicitVersion() {
			if (getValue(numberVar) != null) {
				return this;
			}
			return null;
		}

		public Object sample(int n) {
			int size = size();
			if ((n < 0) || (n >= size)) {
				throw new IllegalArgumentException("Can't get element " + n
						+ " in set of size " + size);
			}

			IndexedSet ids = (IndexedSet) popAppToCommIds.get(numberVar);
			if (n < ids.size()) {
				return ids.get(n);
			}

			// Sampling an object that didn't have an identifier yet
			ObjectIdentifier id = addCommId(numberVar);
			return id;
		}

		public int indexOf(Object o) {
			IndexedSet ids = (IndexedSet) popAppToCommIds.get(numberVar);
			return ids.indexOf(o);
			// Remaining objects don't have identifiers yet, so couldn't
			// possibly equal o.
		}

		public String toString() {
			return ("IDs satisfying " + numberVar + ": " + super.toString());
		}

		private class POPAppSatisfierIterator implements ObjectIterator {
			POPAppSatisfierIterator(Set externallyDistinguished) {
				if (getValue(numberVar) == null) {
					size = -1; // so hasNext will return false
				} else {
					ids = (IndexedSet) popAppToCommIds.get(numberVar);
					size = size();
				}

				this.externallyDistinguished = externallyDistinguished;
			}

			public boolean hasNext() {
				return (nextIndex < size);
			}

			public Object next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				if (nextIndex < ids.size()) {
					latestObj = (ObjectIdentifier) ids.get(nextIndex++);
					advanceOverSkipped();
				} else {
					latestObj = addCommId(numberVar);
					++nextIndex;
					// If this this object wasn't skipped, then there
					// can't be any skipped objects beyond this index either.
				}

				return latestObj;
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"Can't remove from POPAppSatisfierSet iterator.");
			}

			public int skipIndistinguishable() {
				if (latestObj == null) {
					throw new IllegalStateException(
							"next has not been called since last call to "
									+ "skipIndistinguishable.");
				}

				int numSkipped = 0;
				if (!isDistinguished(latestObj)) {
					skipped = new boolean[ids.size()];
					Arrays.fill(skipped, false);
					for (int i = nextIndex; i < skipped.length; ++i) {
						ObjectIdentifier id = (ObjectIdentifier) ids.get(i);
						if (!isDistinguished(id)) {
							skipped[i] = true;
							++numSkipped;
						}
					}

					// All objects that don't have identifiers at this
					// moment will be skipped as well.
					numSkipped += (size - ids.size());
				}

				latestObj = null;
				return numSkipped;
			}

			public boolean canDetermineNext() {
				return (basicVarToValue.get(numberVar) != null);
			}

			private void advanceOverSkipped() {
				if (skipped != null) {
					// Skip indices for which skipped is true
					while ((nextIndex < skipped.length) && skipped[nextIndex]) {
						++nextIndex;
					}

					// All objects that didn't have identifiers when the
					// skipped array was created are automatically skipped
					if (nextIndex >= skipped.length) {
						nextIndex = size;
					}
				}
			}

			private boolean isDistinguished(ObjectIdentifier id) {
				return (objToUsesAsArg.containsKey(id)
						|| objToUsesAsValue.containsKey(id) || externallyDistinguished
							.contains(id));
			}

			private IndexedSet ids; // cached for efficiency, but may grow
			private int size; // cached for efficiency, -1 if undefined
			private int nextIndex = 0;

			private ObjectIdentifier latestObj = null;
			private Set externallyDistinguished;
			private boolean[] skipped = null;
		}

		private NumberVar numberVar;
	}

	/**
	 * Inner class representing the set of non-guaranteed objects (tuple
	 * representations) that satisfy a particular POP application. It is backed by
	 * the partial world, so it changes if the relevant number variable changes.
	 */
	private class NonGuarObjSet extends AbstractObjectSet {
		NonGuarObjSet(NumberVar popApp) {
			numberVar = popApp;
		}

		protected Integer sizeInternal() {
			return (Integer) basicVarToValueMap().get(numberVar);
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			return new NonGuarObjIterator(externallyDistinguished);
		}

		public Boolean containsInternal(Object o) {
			Integer size = (Integer) basicVarToValueMap().get(numberVar);
			if (size == null) {
				return null;
			}

			if (o instanceof NonGuaranteedObject) {
				NonGuaranteedObject obj = (NonGuaranteedObject) o;
				return Boolean.valueOf((obj.getPOP() == numberVar.pop())
						&& Arrays.equals(obj.getGenObjs(), numberVar.args())
						&& (obj.getNumber() <= size.intValue()));
			}
			return Boolean.FALSE;
		}

		public ObjectSet getExplicitVersion() {
			if (basicVarToValue.get(numberVar) == null) {
				return null;
			}
			return this;
		}

		public Object sample(int n) {
			if ((n < 0) || (n >= size())) {
				throw new IllegalArgumentException("Can't sample item " + n
						+ " in set of size " + size());
			}

			return NonGuaranteedObject.get(numberVar.pop(), numberVar.args(), n + 1);
		}

		public int indexOf(Object o) {
			if (o instanceof NonGuaranteedObject) {
				NonGuaranteedObject ngObj = (NonGuaranteedObject) o;
				if ((ngObj.getPOP() == numberVar.pop())
						&& Arrays.equals(ngObj.getGenObjs(), numberVar.args())) {
					int index = ngObj.getNumber() - 1;
					if ((index >= 0) && (index < size())) {
						return index;
					}
				}
			}
			return -1;
		}

		public String toString() {
			return ("Nonguaranteed objects satisfying " + numberVar + ": " + super
					.toString());
		}

		private class NonGuarObjIterator implements ObjectIterator {
			NonGuarObjIterator(Set externallyDistinguished) {
				Integer varValue = (Integer) basicVarToValueMap().get(numberVar);
				size = ((varValue == null) ? -1 : varValue.intValue());
			}

			public boolean hasNext() {
				return (nextIndex <= size);
			}

			public Object next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				return NonGuaranteedObject.get(numberVar.pop(), numberVar.args(),
						nextIndex++);
			}

			public void remove() {
				throw new UnsupportedOperationException(
						"Can't remove from NonGuarObjSet iterator.");
			}

			public int skipIndistinguishable() {
				// Since any non-guaranteed objects might be in use, it's
				// probably not efficient to figure out which ones are
				// indistinguishable from the last one returned.
				return 0;
			}

			public boolean canDetermineNext() {
				return (size >= 0);
			}

			private int nextIndex = 1;
			private int size; // cached for efficiency, -1 if undefined
		}

		private NumberVar numberVar;
	}

	private static interface VarInfoUpdater {
		void updateVarInfo(BayesNetVar var);
	}

	private class DefaultInfoUpdater implements VarInfoUpdater {
		public void updateVarInfo(BayesNetVar var) {
			ParentRecEvalContext context = new ParentRecEvalContext(
					AbstractPartialWorld.this, false);
			if (var instanceof VarWithDistrib) {
				DependencyModel.Distrib distrib = ((VarWithDistrib) var)
						.getDistrib(context);
				if (distrib == null) {
					varToLogProb.put(var, PartialWorld.UNDET);
				} else {
					try {
						// System.out.println("AbstractPartialWorld: var: " + var +
						// ", basicVarToValue(var): " + basicVarToValue.get(var));
						double logProb = distrib.getCPD().getLogProb(
								distrib.getArgValues(), basicVarToValue.get(var));
						/*
						 * if (Util.verbose() && (logProb == Double.NEGATIVE_INFINITY)) {
						 * System.out.println ("Got zero probability for " + var + " = " +
						 * basicVarToValue.get(var) + " with distrib " + distrib); }
						 */
						varToLogProb.put(var, new Double(logProb));
					} catch (Exception e) {
						System.err.println("Exception in getProb for variable " + var);
						Util.fatalError(e);
					}
				}
			} else if (var instanceof DerivedVar) {
				Object value = ((DerivedVar) var).getValue(context);
				derivedVarToValue
						.put(var, (value == null) ? PartialWorld.UNDET : value);
			}

			cbn.setParents(var, context.getParents());
			if (context.getLatestUninstParent() == null) {
				varToUninstParent.remove(var);
			} else {
				varToUninstParent.put(var, context.getLatestUninstParent());
			}
		}
	}

	private class CopyingInfoUpdater implements VarInfoUpdater {
		CopyingInfoUpdater(CBN givenBayesNet,
				MapWithPreimages givenVarUninstParents, Map givenVarLogProbs,
				Map givenDerivedVarValues) {
			this.givenBayesNet = givenBayesNet;
			this.givenVarUninstParents = givenVarUninstParents;
			this.givenVarLogProbs = givenVarLogProbs;
			this.givenDerivedVarValues = givenDerivedVarValues;
		}

		public void updateVarInfo(BayesNetVar var) {
			Set givenParents = givenBayesNet.getParents(var);
			if (givenParents == null) {
				throw new IllegalArgumentException("Need parents for variable " + var
						+ ", which is not in given Bayes net.");
			}
			cbn.setParents(var, givenParents);

			BasicVar uninstParent = (BasicVar) givenVarUninstParents.get(var);
			if (uninstParent == null) {
				varToUninstParent.remove(var);
			} else {
				varToUninstParent.put(var, uninstParent);
			}

			if (var instanceof VarWithDistrib) {
				Double logProb = (Double) givenVarLogProbs.get(var);
				if (logProb == null) {
					throw new IllegalArgumentException("Need log prob for variable "
							+ var + ", which is not in given map.");
				}
				varToLogProb.put(var, logProb);
			} else if (var instanceof DerivedVar) {
				Object givenValue = givenDerivedVarValues.get(var);
				if (givenValue == null) {
					throw new IllegalArgumentException("Need value for derived variable "
							+ var + ", which is not in given map.");
				}
				derivedVarToValue.put(var, givenValue);
			}
		}

		CBN givenBayesNet;
		MapWithPreimages givenVarUninstParents;
		Map givenVarLogProbs;
		Map givenDerivedVarValues;
	}

	private void updateParentsAndProbs(VarInfoUpdater updater) {
		if (dirtyVars.isEmpty()) {
			return;
		}

		// If a variable has changed, we need to update the information for
		// all its children as well. To avoid invalidating our iterator
		// over dirtyVars, we add the children to an auxiliary set, then
		// add that whole set to dirtyVars.
		Set dirtyVarChildren = new LinkedHashSet();
		for (Iterator iter = dirtyVars.iterator(); iter.hasNext();) {
			BayesNetVar dirtyVar = (BayesNetVar) iter.next();
			Set thisVarChildren = cbn.getChildren(dirtyVar);
			if (thisVarChildren != null) {
				dirtyVarChildren.addAll(thisVarChildren);
			}
			dirtyVarChildren.addAll(varToUninstParent.getPreimage(dirtyVar));
		}
		dirtyVars.addAll(dirtyVarChildren);

		for (Iterator iter = dirtyVars.iterator(); iter.hasNext();) {
			BayesNetVar var = (BayesNetVar) iter.next();
			if ((var instanceof BasicVar) && (basicVarToValue.get(var) == null)) {
				// var has been uninstantiated
				cbn.removeNode(var);
				varToLogProb.remove(var);
			} else if ((var instanceof OriginVar)
					&& (assertedIdToPOPApp.get(((OriginVar) var).getIdentifier()) == null)) {
				// identifier has been removed
				cbn.removeNode(var);
			} else {
				// node still belongs in Bayes net
				updater.updateVarInfo(var);
			}
		}

		dirtyVars = new LinkedHashSet();
	}

	private void checkIdentifiers(BasicVar var, Object value) {
		Object[] args = var.args();
		for (int i = 0; i < args.length; ++i) {
			Object arg = args[i];
			if ((arg instanceof ObjectIdentifier) && !commIdToPOPApp.containsKey(arg)) {
				throw new IllegalArgumentException("Invalid identifier: " + arg);
			}
		}

		if ((value instanceof ObjectIdentifier)
				&& !commIdToPOPApp.containsKey(value)) {
			throw new IllegalArgumentException("Invalid identifier: " + value);
		}
	}

	private ObjectIdentifier addCommId(NumberVar popApp) {
		ObjectIdentifier id = new ObjectIdentifier(popApp.pop().type());
		commIdToPOPApp.put(id, popApp);
		popAppToCommIds.add(popApp, id);
		return id;
	}

	private void updateUsageForChange(BasicVar var, Object oldValue,
			Object newValue) {
		// update usage of arguments
		Object[] args = var.args();
		if ((oldValue == null) && (newValue != null)) {
			for (int i = 0; i < args.length; ++i) {
				objToUsesAsArg.add(args[i], var);
			}
		} else if ((oldValue != null) && (newValue == null)) {
			for (int i = 0; i < args.length; ++i) {
				objToUsesAsArg.remove(args[i], var);
			}
		}

		// update usage of oldValue
		if (oldValue != null) {
			objToUsesAsValue.remove(oldValue, var);
		}

		// update usage of newValue
		if (newValue != null) {
			objToUsesAsValue.add(newValue, var);
		}
	}

	private void prepareForNumberVarChange(NumberVar nv, Object oldValue,
			Object newValue) {
		// if (nv.toString().contains("#Blip") && !
		// nv.toString().contains("Source"))
		// System.out.println("AbstractPartialWorld: " + nv + " gonna change");

		int oldNum = (oldValue == null) ? 0 : ((Integer) oldValue).intValue();
		int newNum = (newValue == null) ? 0 : ((Integer) newValue).intValue();

		if (getIdTypes().contains(nv.pop().type())) {
			trimNonAssertedIds(nv, newNum, 0);
		} else {
			// Uninstantiate variables on concrete non-guaranteed objects
			// that no longer exist.
			for (int i = newNum + 1; i <= oldNum; ++i) {
				Object toDelete = NonGuaranteedObject.get(nv, i);
				uninstantiateVarsUsing(toDelete);
			}
		}
	}

	private void trimNonAssertedIds(NumberVar popApp, int newValue,
			int numNewAsserted) {
		IndexedSet ids = (IndexedSet) popAppToCommIds.get(popApp);
		int numLeftToDelete = ids.size() + numNewAsserted - newValue;
		if (numLeftToDelete > 0) {
			List idsToDelete = new ArrayList();
			for (Iterator iter = ids.iterator(); iter.hasNext()
					&& (numLeftToDelete > 0);) {
				ObjectIdentifier id = (ObjectIdentifier) iter.next();
				if (!assertedIdToPOPApp.containsKey(id)) {
					idsToDelete.add(id);
					--numLeftToDelete;
				}
			}

			for (Iterator iter = idsToDelete.iterator(); iter.hasNext();) {
				removeIdentifier((ObjectIdentifier) iter.next());
			}
		}
	}

	private void uninstantiateVarsUsing(Object obj) {
		List users = new ArrayList((Set) objToUsesAsArg.get(obj));
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			BasicVar var = (BasicVar) iter.next();
			setValue(var, null);
		}
	}

	private void tellListenersIdChanged(ObjectIdentifier id, NumberVar oldPOPApp,
			NumberVar newPOPApp) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			((WorldListener) iter.next()).identifierChanged(id, oldPOPApp, newPOPApp);
		}
	}

	/**
	 * Replicates all fields into the fields of <code>newWorld</code>. This is
	 * meant to be invoked by clone methods of extending classes, allowing their
	 * writing without access to all, and detailed knowledge of,
	 * <code>AbstractPartialWorld</code> fields.
	 */
	public void cloneFields(AbstractPartialWorld newWorld) {
		newWorld.basicVarToValue = (Map) ((HashMap) basicVarToValue).clone();
		
		/*added by cheng*/
		newWorld.decisionInterp = (Set) ((HashSet) decisionInterp).clone();
		
		newWorld.objToUsesAsValue = (MultiMap) ((HashMultiMap) objToUsesAsValue)
				.clone();
		newWorld.objToUsesAsArg = (MultiMap) ((HashMultiMap) objToUsesAsArg)
				.clone();
		newWorld.assertedIdToPOPApp = (Map) ((HashMap) assertedIdToPOPApp).clone();
		newWorld.popAppToAssertedIds = new IndexedHashMultiMap(popAppToAssertedIds);
		newWorld.commIdToPOPApp = (Map) ((HashMap) commIdToPOPApp).clone();
		newWorld.popAppToCommIds = new IndexedHashMultiMap(popAppToCommIds);
		newWorld.cbn = (CBN) ((DefaultCBN) cbn).clone();
		newWorld.varToUninstParent = (MapWithPreimages) ((HashMapWithPreimages) varToUninstParent)
				.clone();
		newWorld.varToLogProb = (Map) ((HashMap) varToLogProb).clone();
		newWorld.derivedVarToValue = (Map) ((HashMap) derivedVarToValue).clone();

		newWorld.dirtyVars = (Set) ((LinkedHashSet) dirtyVars).clone();
		newWorld.listeners = (List) ((ArrayList) listeners).clone();
		newWorld.idTypes = new HashSet(idTypes);
	}

	public String toString() {
		return "{Basic: " + basicVarToValueMap() + ", Derived: "
				+ derivedVarToValueMap() + "}";
	}

	/**
	 * Map from instantiated basic variables to their values.
	 */
	protected Map basicVarToValue;

	/**
	 * Set of objects containing DecisionFuncAppVar objects which evaluate to true
	 */
	protected Set decisionInterp;
	/*added by cheng*/
	public Set getDecisionInterp() {
		return decisionInterp; 
	}
	/**
	 * Map from objects to the instantiated BasicVars that have them as values.
	 */
	protected MultiMap objToUsesAsValue;

	/**
	 * Map from objects to the instantiated BasicVars that use them as arguments.
	 */
	protected MultiMap objToUsesAsArg;

	/**
	 * Map from asserted ObjectIdentifiers to the NumberVars that they satisfy.
	 */
	protected Map assertedIdToPOPApp;

	/**
	 * Map from NumberVars to IndexedSets of the ObjectIdentifiers that are
	 * asserted to satify them.
	 */
	protected IndexedMultiMap popAppToAssertedIds;

	/**
	 * Map from common ground ObjectIdentifiers (including asserted ones) to the
	 * NumberVars that they satisfy.
	 */
	protected Map commIdToPOPApp;

	/**
	 * Map from NumberVars to IndexedSets of the common ground ObjectIdentifiers
	 * (including asserted ones) that satisfy them.
	 */
	protected IndexedMultiMap popAppToCommIds;

	/**
	 * CBN containing instantiated basic variables, origin variables, and
	 * those derived variables that have been explicitly added.
	 */
	protected CBN cbn;

	/**
	 * MapWithPreimages from BayesNetVars to their first uninstantiated parents. A
	 * variable is included as a key in this map only if it is not supported in
	 * this partial world, which means it has an uninstantiated parent.
	 */
	protected MapWithPreimages varToUninstParent;

	/**
	 * Map from instantiated VarWithDistrib objects to Double values representing
	 * their log probabilities given their parents. The value is null if a
	 * variable is not supported in this world.
	 */
	protected Map varToLogProb;

	/**
	 * Map from derived variables in the Bayes net to their values. Even if the
	 * values in this map are out of date, the key set is the set of derived
	 * variables in the Bayes net.
	 */
	protected Map derivedVarToValue;

	/**
	 * Variables that have been added or removed, or have had their values
	 * changed, since the last call to updateParentsAndProbs.
	 */
	protected Set dirtyVars = new LinkedHashSet();

	private VarInfoUpdater defaultUpdater = new DefaultInfoUpdater();

	protected List listeners = new ArrayList(); // of WorldListener

	protected Set idTypes;
}
