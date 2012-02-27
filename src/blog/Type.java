/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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
import java.io.PrintStream;
import common.Util;

/**
 * Represents a type in a BLOG model.  Stores a set of potential object 
 * patterns (POPs), a set of origin functions, and a set of explicitly 
 * enumerated guaranteed objects.
 *
 * <p>We ensure that there is only one Type object for each type, so 
 * Type objects can be compared with ==.
 */
public class Type {
    /**
     * Creates a new type with the given name that is not a sub-type
     * of any other type.  It initially has no POPs, no origin
     * functions, and no enumerated guaranteed objects.
     */
    public Type(String name) {
	this.name = name;
    }

    /**
     * Creates a type with the given name that is a direct sub-type of 
     * <code>supertype</code>.  It initially has no POPs, no origin 
     * functions, and no enumerated guaranteed objects.
     *
     * <p>Note that there's no way to change the supertype of a type once 
     * it's created.  Thus, the types must be created in order from the 
     * root of the type hierarchy downward.  So it is impossible to make 
     * the supertype relation cyclic.  
     *
     * @param supertype a Type object or null
     */
    public Type(String name, Type supertype) {
	this.name = name;
	this.supertype = supertype;
    }

    /**
     * Creates a type with the given name that is a direct sub-type of 
     * <code>supertype</code>.  It initially has no POPs, no origin 
     * functions, and no enumerated guaranteed objects.  If 
     * <code>infinite</code> is true, the type has infinitely many 
     * guaranteed objects.
     *
     * @param supertype a Type object or null
     */
    public Type(String name, Type supertype, boolean infinite) {
	this.name = name;
	this.supertype = supertype;
	this.infiniteGuaranteed = infinite;
    }

    /**
     * Returns the name of this type.
     */
    public String getName() {
	return name;
    }

    /**
     * Returns true if this type is a subtype of the given type.  A type 
     * counts as a subtype of itself.
     */
    public boolean isSubtypeOf(Type other) {
	return ((other == this) 
		|| ((supertype != null) && supertype.isSubtypeOf(other)));
    }

    /**
     * Returns true if this is a built-in type.
     */
    public boolean isBuiltIn() {
	return isSubtypeOf(BuiltInTypes.BUILT_IN);
    }

    /**
     * Prints the type name.
     */
    public void print(PrintStream s){
	s.print(name);
    }

    /**
     * Returns the set of potential object patterns (POPs) for this type.
     *
     * @return unmodifiable Collection of POP objects
     */
    public Collection getPOPs() {
	return Collections.unmodifiableCollection(pops);
    }

    /**
     * Returns a list of origin functions for this type.  Although the 
     * order of origin functions does not affect the semantics of the 
     * model, the origin functions are always returned in the same order.  
     * This facilitates looking up the POP that uses a certain subset 
     * of the origin functions (the subset can be represented as a BitSet 
     * where bit indices correspond to the indices of origin functions 
     * in this list).  
     *
     * @return unmodifiable List of OriginFunction objects
     */
    public List getOriginFunctions() {
	return Collections.unmodifiableList(originFuncs);
    }

    /**
     * Returns the list of explicitly enumerated guaranteed objects of 
     * this type, in the order they were introduced in the model file.  
     * Or, if this is a built-in type with a finite set of guaranteed
     * objects, returns those objects in some fixed order.
     *
     * @return unmodifiable list of Object
     */
    public List getGuaranteedObjects() {
	return Collections.unmodifiableList(userDefGuarObjs);
    }

    /**
     * Returns a list of all the values that can be returned by an
     * expression with this return type.  The list includes all the
     * guaranteed objects of this type, with Model.NULL added to the
     * end if it serves as the type's default value.  The list's
     * <code>get</code> and <code>indexOf</code> methods can be used
     * to convert efficiently between objects and indices.
     *
     * @throws IllegalStateException if the set of objects of this
     *         type is infinite or random
     */
    public List range() {
	if (range == null) {
	    if (!hasFiniteGuaranteed()) {
		throw new IllegalStateException
		    ("Can't enumerate infinite set of objects of type " 
		     + this);
	    }
	    if (!pops.isEmpty()) {
		throw new IllegalStateException
		    ("Can't enumerate random set of objects of type " + this);
	    }
	    
	    range = new RangeList();
	}
	return range;
    }

    /**
     * Returns the index of the given object in this type's list of 
     * guaranteed objects.  Returns -1 if this type has infinitely many 
     * guaranteed objects (and thus does not maintain a list of them) 
     * or if the given object is not a guaranteed object of this type.
     */
    public int getGuaranteedObjIndex(Object obj) {
	if (obj instanceof EnumeratedObject) {
	    EnumeratedObject enumObj = (EnumeratedObject) obj;
	    if (enumObj.getType().equals(this)) {
		return enumObj.getIndex();
	    }
	}
	return -1;
    }

    /**
     * Returns the guaranteed object of this type that has the given 
     * index, or null if no such object exists.
     */
    public Object getGuaranteedObject(int index) {
	List guarObjs = getGuaranteedObjects();
	if ((index >= 0) && (index < guarObjs.size())) {
	    return guarObjs.get(index);
	}
	return null;
    }

    /**
     * Returns the default value for functions with this return type.
     */
    public Object getDefaultValue() {
	return Model.NULL;
    }

    /**
     * Returns true if this type has a finite number of guaranteed objects.
     */
    public boolean hasFiniteGuaranteed() {
	return !infiniteGuaranteed;
    }

    /**
     * Returns the canonical term for the given object, or null if the 
     * object has no canonical term.  For an EnumeratedObject, the 
     * canonical term is a FuncAppTerm composed of that object's constant 
     * symbol and no arguments.  For built-in types, the guaranteed 
     * objects may have other canonical terms.
     *
     * @throws IllegalArgumentException if the given object is not of 
     *                                  this type
     */
    public Term getCanonicalTerm(Object o) {
	if (o == Model.NULL) {
	    return new FuncAppTerm(BuiltInFunctions.NULL);
	}

	if (o instanceof EnumeratedObject) {
	    return new FuncAppTerm(((EnumeratedObject) o).getConstant());
	}
	return null;
    }

    /**
     * Returns the POP that uses the given set of origin functions, 
     * or null if no such POP exists.
     *
     * @param originFuncSet a BitSet where bit i stands for the ith origin 
     *                      function in the list returned by 
     *                      getOriginFunctions
     */
    public POP getPOPWithOriginFuncs(BitSet originFuncSet) {
	return (POP) originFuncsToPOP.get(originFuncSet);
    }

    /**
     * Returns the POP that uses the given set of origin functions, 
     * or null if not such POP exists.
     *
     * @param originFuncSet Set of OriginFunction objects  
     */
    public POP getPOPWithOriginFuncs(Set originFuncSet) {
	BitSet bitSet = new BitSet(originFuncs.size());
	for (int i = 0; i < originFuncs.size(); ++i) {
	    if (originFuncSet.contains(originFuncs.get(i))) {
		bitSet.set(i);
	    }
	}
	return getPOPWithOriginFuncs(bitSet);
    }

    /**
     * Returns the types that are strict ancestors of this type 
     * in the type graph.  The type graph contains a node for each type, 
     * with an edge from type sigma to type tau if sigma is the return 
     * type of some origin function for tau.  Note that this graph may 
     * be cyclic and may even include self-loops.  The strict ancestors of 
     * a this type are those types tau such that there is a nonzero-length 
     * directed path from tau to this type.  
     *
     * @return a Set of Type objects
     */
    public Set getStrictAncestors() {
	Set strictAncestors = new HashSet();
	
	// Find ancestors by depth-first search with repeated-node checking
	Stack stack = new Stack();
	stack.push(this);
	while (!stack.isEmpty()) {
	    Type curType = (Type) stack.pop();
	    for (Iterator iter = curType.getOriginFunctions().iterator(); 
		 iter.hasNext(); ) {
		Type parentType = ((Function) iter.next()).getRetType();
		if (!strictAncestors.contains(parentType)) {
		    strictAncestors.add(parentType);
		    stack.push(parentType);
		}
	    }
	}

	return strictAncestors;
    }

    /**
     * Adds the given POP to the set of POPs for this type. 
     *
     * @throws IllegalArgumentException if the given POP uses the same set 
     *                                  of origin functions as an 
     *                                  existing POP
     */
    public void addPOP(POP pop) {
	BitSet originFuncSet = pop.getOriginFuncSet();
	if (getPOPWithOriginFuncs(originFuncSet) != null) {
	    throw new IllegalArgumentException
		("POP " + pop + " uses same set of origin"
		 + " functions as existing POP.");
	}
	pops.add(pop);
	originFuncsToPOP.put(originFuncSet, pop);
	range = null; // invalidate cached range
    }

    /**
     * Adds the given function as a origin function for this type.
     */
    public void addOriginFunction(OriginFunction f) {
	originFuncs.add(f);
    }

    /**
     * Adds a new guaranteed object with the given canonical constant
     * symbol.  Warning: for efficiency, we do not check to make sure
     * a guaranteed object has not already been added with the same
     * symbol.
     * 
     * @return the object added
     */
    public EnumeratedObject addGuaranteedObject(NonRandomFunction constant) {
	EnumeratedObject obj 
	    = new EnumeratedObject(this, userDefGuarObjs.size(), constant);
	userDefGuarObjs.add(obj);
	return obj;
    }

    /**
     * Returns an index indicating when this type was declared (or otherwise 
     * created).
     */
    public int getCreationIndex() {
	return creationIndex;
    }

    /**
     * Returns the name of this type.
     */
    public String toString() {
	return name;
    }

    private class RangeList extends AbstractList {
	private RangeList() {
	    guarObjs = getGuaranteedObjects();
	    includesNull = (getDefaultValue() == Model.NULL);
	}

	public int size() {
	    return (guarObjs.size() + (includesNull ? 1 : 0));
	}

	public Object get(int index) {
	    if ((index == guarObjs.size()) && includesNull) {
		return Model.NULL;
	    }
	    return guarObjs.get(index);
	}

	public int indexOf(Object o) {
	    if ((o == Model.NULL) && includesNull) {
		return guarObjs.size();
	    }
	    return getGuaranteedObjIndex(o);
	}

	public boolean contains(Object o) {
	    if (o == Model.NULL) {
		return includesNull;
	    }
	    return (getGuaranteedObjIndex(o) != -1);
	}

	private List guarObjs;
	private boolean includesNull;
    }

    private String name;
    private Type supertype;
    private int creationIndex = Model.nextCreationIndex();

    private List pops = new ArrayList(); // of POP
    private List originFuncs = new ArrayList(); // of OriginFunction
    private List userDefGuarObjs = new ArrayList(); // of EnumeratedObject
    private boolean infiniteGuaranteed = false;
    
    private Map originFuncsToPOP = new HashMap(); // from BitSet to POP
    private List range;
}
