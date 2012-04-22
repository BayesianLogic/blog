/*
 * Copyright (c) 2005, 2006 Regents of the University of California
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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import blog.ObjectIdentifier;
import blog.ObjectSet;
import blog.WorldListener;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.NumberVar;
import blog.common.DGraph;
import blog.common.IndexedMultiMap;
import blog.common.IndexedSet;
import blog.common.MapWithPreimages;
import blog.common.MultiMap;
import blog.model.POP;
import blog.model.RandomFunction;

/**
 * A partial description of a possible world in a BLOG model. A PartialWorld
 * represents an event: the set of all possible worlds that satisfy the
 * description. More specifically, a PartialWorld makes an assertion of the
 * form:
 * 
 * <blockquote> There exist distinct objects <i>obj_1</i>, ..., <i>obj_k</i>
 * such that <i>X_1</i> = <i>x_1</i>, ..., <i>X_n</i> = <i>x_n</i>.
 * </blockquote>
 * 
 * Here <i>obj_1</i>, ..., <i>obj_k</i> are <i>object identifiers</i> and
 * <i>X_1</i> = <i>x_1</i>, ..., <i>X_n</i> = <i>x_n</i> are assignments of
 * values to random variables. The random variables can be basic variables
 * (number variables and random function application variables) parameterized by
 * concrete objects or object identifiers. They can also be <i>origin
 * variables</i>, which specify the value of an origin function on an object
 * identifier. The values asserted for the variables can be concrete objects or
 * object identifiers.
 * 
 * <p>
 * Each PartialWorld object uses object identifiers to represent non-guaranteed
 * objects of certain types, and concrete tuple representations for
 * non-guaranteed objects of other types. The method <code>getIdTypes</code>
 * returns the set of types for which object identifiers are used.
 * 
 * <p>
 * The identifiers <i>obj_1</i>, ..., <i>obj_k</i> about which a PartialWorld
 * makes existential assertions are called its <i>asserted identifiers</i>.
 * However, when a client iterates over the objects that exist in a
 * PartialWorld, or samples an object randomly, the PartialWorld uses additional
 * identifiers to represent the resulting objects. Specifically, such additional
 * identifiers may be returned by the <code>sample</code> method of the set
 * returned by <code>getSatisfiers</code>, and by the <code>next</code> method
 * of iterators over this set. These identifiers, along with the asserted ones,
 * form the <i>common ground</i> between a PartialWorld instance and its
 * clients. The client can get and set the values of random variables on all
 * identifiers in the common ground. The <code>setValue</code> method
 * automatically adds identifiers to the asserted set when they are used as
 * arguments or values of a variable.
 * 
 * <p>
 * A PartialWorld may automatically remove some identifiers from the common
 * ground when a number variable is uninstantiated or has its value decreased,
 * when a new asserted identifier is created using
 * <code>addIdentifierForPOPApp<code>, or when an identifier is 
 * moved using <code>moveIdentifier</code>. Asserted identifiers are never
 * removed automatically; only additional identifiers are removed. Note that
 * when an identifier that is already in the common ground is added to the
 * asserted set (either by <code>assertIdentifier</code> or automatically by
 * <code>setValue</code>), this does not cause any other identifiers to be
 * removed.
 * 
 * <p>
 * Because asserted identifiers can always be added using
 * <code>addIdentifierForPOPApp</code> and are not automatically removed, it is
 * possible to create a PartialWorld in which the number of identifiers asserted
 * to satisfy a number variable is greater than the value asserted for that
 * number variable. Such a PartialWorld describes an empty set of worlds. It is
 * up to the client to avoid such partial worlds, explicitly removing asserted
 * identifiers with <code>removeIdentifier</code> as necessary.
 */
public interface PartialWorld {
	/**
	 * A partial world that has no asserted identifiers and does not assign values
	 * to any basic random variables.
	 */
	static final PartialWorld EMPTY_INST = new EmptyPartialWorld();

	static class EmptyPartialWorld extends AbstractPartialWorld {
		EmptyPartialWorld() {
			super(Collections.EMPTY_SET);
			basicVarToValue = Collections.EMPTY_MAP;
			objToUsesAsValue = MultiMap.EMPTY_MULTI_MAP;
			objToUsesAsArg = MultiMap.EMPTY_MULTI_MAP;
			assertedIdToPOPApp = Collections.EMPTY_MAP;
			popAppToAssertedIds = IndexedMultiMap.EMPTY_INDEXED_MULTI_MAP;
			commIdToPOPApp = Collections.EMPTY_MAP;
			popAppToCommIds = IndexedMultiMap.EMPTY_INDEXED_MULTI_MAP;
			bayesNet = DGraph.EMPTY_GRAPH;
			varToUninstParent = MapWithPreimages.EMPTY_MAP_WITH_PREIMAGES;
			varToLogProb = Collections.EMPTY_MAP;
			derivedVarToValue = Collections.EMPTY_MAP;
		}
	}

	// Methods dealing with instantiated basic variables

	/**
	 * Returns an unmodifiable Set representing the set of basic variables that
	 * are instantiated in this world.
	 * 
	 * @return unmodifiable Set of BasicVar objects
	 */
	Set getInstantiatedVars();

	/**
	 * Indicates whether var is instantiated in this world or not.
	 */
	public boolean isInstantiated(BayesNetVar var);

	/**
	 * Returns the value of the given variable in this partial world, or null if
	 * the given variable is an uninstantiated basic variable. Note that this
	 * method returns null on all basic variables that are not instantiated in
	 * this world, even if the value of the variable is determined because its
	 * arguments cannot exist.
	 */
	Object getValue(BayesNetVar var);

	/**
	 * Instantiates the given variable to the given value (replacing any previous
	 * value), or uninstantiates the variable if the given value is null. This
	 * method instantiates the variable even if some of its arguments are concrete
	 * non-guaranteed objects that do not necessarily exist in this world.
	 * 
	 * <p>
	 * If the variable is a number variable and the given value is less than its
	 * old value, then all variables defined on concrete non-guaranteed objects
	 * that no longer exist are uninstantiated. Variables defined on object
	 * identifiers that satisfy this number variable are not affected (we don't
	 * know which identifiers to remove). We also don't do anything about
	 * variables whose values no longer exist.
	 */
	void setValue(BasicVar var, Object value);

	/**
	 * Uninstantiates the variables for the values of <code>f</code> on all tuples
	 * of arguments of the form (arg1, ..., argK, i) where (arg1, ..., argK) are
	 * given by <code>initialArgs</code> and i is greater than or equal to
	 * <code>len</code>. Assumes that in the current world, there are no
	 * instantiated variables beyond the first uninstantiated variable in this
	 * sequence.
	 */
	void truncateList(RandomFunction f, Object[] initialArgs, int len);

	/**
	 * Uninstantiates the number variables for <code>pop</code> on all tuples of
	 * arguments of the form (arg1, ..., argK, i) where (arg1, ..., argK) are
	 * given by <code>initialArgs</code> and i is greater than or equal to
	 * <code>len</code>. Assumes that in the current world, there are no
	 * instantiated variables beyond the first uninstantiated variable in this
	 * sequence.
	 */
	void truncateNumberList(POP pop, Object[] initialArgs, int len);

	/**
	 * Returns the set of basic variables that have the given object as their
	 * value in this world.
	 * 
	 * @return unmodifiable Set of BasicVar
	 */
	Set getVarsWithValue(Object value);

	/**
	 * Returns the set of instantiated basic variables that have the given object
	 * as an argument.
	 * 
	 * @return unmodifiable Set of BasicVar
	 */
	Set getVarsWithArg(Object arg);

	/**
	 * Returns the set of argument lists that the function <code>func</code> maps
	 * to the value <code>val</code>. This method requires <code>func</code> to be
	 * a random function, since the values of non-random and origin functions may
	 * not be represented explicitly.
	 * 
	 * @return Set of List objects representing argument lists
	 */
	Set getInverseTuples(RandomFunction func, Object val);

	/**
	 * If <code>var</code> is an instantiated basic variable, this method returns
	 * the log probability of <code>var</code> having the value that it has in
	 * this world, given that its parents have the values specified in this world.
	 * Otherwise returns 0 (log of 1), because uninstantiated variables and
	 * non-basic variables do not contribute to the joint distribution. This
	 * method yields a fatal error if the world is not complete enough to
	 * determine the given variable's distribution.
	 */
	double getLogProbOfValue(BayesNetVar var);

	/**
	 * Uses {@link #getLogProbOfValue(BayesNetVar)} in order to return the
	 * probability of a value given its parents.
	 */
	double getProbOfValue(BayesNetVar var);

	// Methods dealing with objects and POP applications

	/**
	 * Returns the set of objects that satisfy the given POP application in this
	 * world. The objects may be represented as concrete objects or identifiers,
	 * depending on the way this PartialWorld implementation handles objects of
	 * the relevant type.
	 * 
	 * <p>
	 * The set returned by this method will remain correct if new basic random
	 * variables are instantiated or new object identifiers are added (to this
	 * world's common ground or its set of asserted identifiers). The set may not
	 * remain correct if already-instantiated random variables are changed, or if
	 * object identifiers are moved from one number variable to another or removed
	 * from the world.
	 * 
	 * @throws IllegalStateException
	 *           if all the given generating objects exist in the world, but the
	 *           corresponding number variable is not instantiated
	 */
	ObjectSet getSatisfiers(NumberVar popApp);

	/**
	 * Returns the NumberVar (i.e., POP and generating objects) such that the
	 * given object satisfies that POP applied to those generating objects in this
	 * world. Returns null if the given object does not satisfy any POP
	 * application (this includes the case where <code>obj</code> is an object
	 * identifier that is not valid in this world).
	 */
	NumberVar getPOPAppSatisfied(Object obj);

	// Methods dealing with object identifiers

	/**
	 * Returns the set of types that are represented with object identifiers in
	 * this partial world.
	 * 
	 * @return unmodifiable Set of Type
	 */
	Set getIdTypes();

	/**
	 * Returns the set of object identifiers that are asserted to exist in this
	 * partial world.
	 * 
	 * @return unmodifiable Set of ObjectIdentifier
	 */
	Set getAssertedIdentifiers();

	/**
	 * Returns the set of object identifiers that are asserted to satisfy the
	 * given POP application in this partial world. The behavior of this method
	 * does not depend on whether the number variable for this POP application is
	 * instantiated or not.
	 * 
	 * @return unmodifiable IndexedSet of ObjectIdentifier
	 */
	IndexedSet getAssertedIdsForPOPApp(NumberVar popApp);

	/**
	 * Asserts that the given object identifier satisfies the given POP
	 * application. The given identifier does not need to be in this world's
	 * common ground already; if it is, any previous assertion about it is
	 * removed. The assertion is made even if the corresponding number variable is
	 * not instantiated, and even if adding this identifier will overload that
	 * number variable.
	 * 
	 * @throws IllegalArgumentException
	 *           if the type of <code>id</code> is different from the type of
	 *           object generated by <code>newPOPApp</code>
	 */
	void assertIdentifier(ObjectIdentifier id, NumberVar newPOPApp);

	/**
	 * Asserts that the given object identifier satisfies the POP application that
	 * it satisfies in this world's common ground. That is, this method adds
	 * <code>id</code> to the set of asserted identifiers in this world.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>id</code> is not in this world's common ground
	 */
	void assertIdentifier(ObjectIdentifier id);

	/**
	 * Returns a new object identifier that is asserted to satisfy the given POP
	 * application in this world, and to be distinct from all other object
	 * identifiers in this world's common ground. The identifier is added even if
	 * the corresponding number variable is not instantiated, and even if adding
	 * this identifier will overload that number variable.
	 */
	ObjectIdentifier addIdentifierForPOPApp(NumberVar popApp);

	/**
	 * Removes the given object identifier from the set of asserted identifiers in
	 * this partial world, and from this world's common ground. Automatically
	 * uninstantiates all variables that have this identifier as an argument
	 * (variables that have this identifier as a value are unaffected; the client
	 * should give new values to all such variables). Has no effect if the given
	 * identifier is not currently asserted or in the common ground.
	 */
	void removeIdentifier(ObjectIdentifier id);

	/**
	 * Returns true if the given POP application is overloaded: that is, the
	 * number of identifiers that are asserted to satisfy it is greater than the
	 * value of the corresponding number variable, or some identifiers are
	 * asserted to satisfy it and its number variable is not instantiated.
	 */
	boolean isOverloaded(NumberVar popApp);

	// Method dealing with the Bayes net over this world's variables

	/**
	 * Returns a directed graph where the nodes are variables, and there is an
	 * edge from variable X to variable Y if Y depends directly on X given the
	 * context represented by this partial world. The graph includes at least the
	 * variables that are instantiated in this partial world. It may also include
	 * certain derived variables that are not instantiated because their values
	 * are determined by the instantiated variables.
	 * 
	 * <p>
	 * If a variable <code>var</code> is not supported in this world, then its
	 * parents in the returned graph are those variables accessed by
	 * <code>var.getDistrib</code> (or <code>var.getValue</code> if
	 * <code>var</code> is a derived variable) before the first uninstantiated
	 * variable.
	 */
	DGraph getBayesNet();

	/**
	 * Provides the Bayes net, the mapping from variables to uninstantiated
	 * parents, the mapping from basic variables to log probabilities, and the
	 * mapping from derived variables to values for this partial world. This
	 * method relieves the partial world of the need to recompute parent sets, log
	 * probabilities, and derived variable values itself. It is useful when some
	 * other object -- such as a PartialWorldDiff -- has already updated these
	 * data structures.
	 * 
	 * @param newBayesNet
	 *          DGraph whose nodes are BayesNetVar objects
	 * 
	 * @param varToUninstParent
	 *          Map from BayesNetVar to BasicVar
	 * 
	 * @param newVarLogProbs
	 *          Map from BasicVar to Double
	 * 
	 * @param newDerivedVarValues
	 *          Map from DerivedVar to Object
	 * 
	 * @throws IllegalArgumentException
	 *           if the given information is not sufficient to update this partial
	 *           world's data structures without additional computation
	 */
	void updateBayesNet(DGraph newBayesNet, MapWithPreimages varToUninstParent,
			Map newVarLogProbs, Map newDerivedVarValues);

	/**
	 * Returns the set of derived variables that are included in this world's
	 * Bayes net.
	 * 
	 * @return unmodifiable Set of DerivedVar
	 */
	Set getDerivedVars();

	/**
	 * Adds the given derived variable to this world's Bayes net, and sets its
	 * parents appropriately.
	 * 
	 * @return true if the variable was actually added; false if it was already
	 *         present
	 */
	boolean addDerivedVar(DerivedVar var);

	/**
	 * Removes the given derived variable from this world's Bayes net.
	 * 
	 * @return true if the variable was actually removed; false if it was not
	 *         present
	 */
	boolean removeDerivedVar(DerivedVar var);

	// Methods for adding and removing listeners for changes to this
	// partial world.

	/**
	 * Adds the given listener to a list of listeners to be notified when changes
	 * are made to this partial world.
	 */
	void addListener(WorldListener listener);

	/**
	 * Removes the given listener from the list of objects to be notified when
	 * changes are made to this partial world.
	 */
	void removeListener(WorldListener listener);

	// Method for printing this partial world

	/**
	 * Prints this partial world to the given stream.
	 */
	void print(PrintStream s);

	// Low-level access methods that return map views of this partial
	// world. These maps can be used as underlying maps for MapDiff
	// and MultiMapDiff. They should all be treated as unmodifiable.

	/**
	 * Map from instantiated BasicVars to their values.
	 */
	Map basicVarToValueMap();

	/**
	 * MultiMap from objects to the instantiated BasicVars that have them as
	 * values.
	 */
	MultiMap objToUsesAsValueMap();

	/**
	 * MultiMap from objects to the instantiated BasicVars that have them as
	 * arguments.
	 */
	MultiMap objToUsesAsArgMap();

	/**
	 * Map from asserted ObjectIdentifiers to the POP applications (NumberVars)
	 * that they satisfy.
	 */
	Map assertedIdToPOPAppMap();

	/**
	 * Map from POP applications (NumberVars) to IndexedSets of the
	 * ObjectIdentifiers that are asserted to satisfy them. POP applications with
	 * no identifiers are not necessarily included in this map.
	 */
	IndexedMultiMap popAppToAssertedIdsMap();

	/**
	 * MapWithPreimages from BayesNetVars to their first uninstantiated parents. A
	 * variable is included as a key in this map only if it is not supported by
	 * this world, which means it has an uninstantiated parent.
	 */
	MapWithPreimages varToUninstParentMap();

	/**
	 * Map from instantiated VarWithDistrib objects to Double values representing
	 * their log probabilities given their parents.
	 */
	Map varToLogProbMap();

	/**
	 * Map from DerivedVars that are included in this world's Bayes net to their
	 * values.
	 */
	Map derivedVarToValueMap();

	/**
	 * Value used in the maps returned by <code>varToLogProbMap</code> and
	 * <code>derivedVarToValueMap</code> for basic variables that are not
	 * supported in the world, and for derived variables whose values are not
	 * determined. This object is actually a Double object with value -1. When
	 * using <code>derivedVarToValueMap</code>, clients should compare values to
	 * <code>UNDET</code> with <code>==</code> to avoid confusion with other
	 * Double objects that have value -1.
	 */
	static final Double UNDET = new Double(-1.0);
}
