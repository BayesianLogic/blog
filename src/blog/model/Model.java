/*
 * Copyright (c) 2005, 2006, 2012 Regents of the University of California
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

package blog.model;

import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import blog.Main;
import blog.ObjectIdentifier;
import blog.common.Util;
import blog.type.Timestep;
import fove.Parfactor;

/**
 * This class contains all the information available about the structure of the
 * model. In particular, it keeps lists of user-defined types and functions.
 * Attributes of those types and functions are stored in the individual Type and
 * Function objects.
 * 
 * <p>
 * Please note that the mutator methods contain little error-checking, if
 * any.Since the type- and semantic checking of the model is done dynamically
 * while the model file is being parsed, it is assumed that if an error occurs
 * it is caught by the type/semantic checker.
 * 
 * @author leili
 */
public class Model {
	/**
	 * The BLOG null value. We use this object rather than the Java null value so
	 * that we can always use <code>equals</code> to compare BLOG objects, and
	 * methods can return null as an error indicator without worrying about the
	 * possibility that a BLOG value might actually be null.
	 */
	public static final Object NULL = new Object() {
		public String toString() {
			return "<null>";
		}
	};

	/**
	 * Class constructor.
	 */
	public Model() {
	}

	/**
	 * Copy constructor.
	 */
	public Model(Model another) {
		types = new ArrayList<Type>(another.types);
		functions = new ArrayList<Function>(another.functions);
		functionsByName = new HashMap<String, List<Function>>(
				another.functionsByName);
		parfactors = new ArrayList<Parfactor>(another.parfactors);
	}

	/** Reads a model from a file and returns it. */
	public static Model readFromFile(String filename) {
		List readersAndOrigins = Main.makeReaders(Util.list(filename));
		Model model = new Model();
		model.augmentFromReadersAndOrigins(readersAndOrigins);
		return model;
	}

	/** Reads a model from a string and returns it. */
	public static Model readFromString(String modelDescription) {
		Model model = new Model();
		model.augmentFromString(modelDescription);
		return model;
	}

	/**
	 * Given a list of readers and theirs origins, parses the reader content and
	 * augments the model accordingly.
	 * 
	 * @param readersAndOrigins
	 *          2-element arrays, each containing a Reader and a string with its
	 *          origin description
	 */
	public void augmentFromReadersAndOrigins(List readersAndOrigins) {
		Evidence evidence = new Evidence();
		LinkedList queries = new LinkedList();
		Main.setup(this, evidence, queries, readersAndOrigins, new LinkedList(),
				false /* verbose */, false);
	}

	/**
	 * Augments a model with the contents parsed from a string.
	 */
	public void augmentFromString(String modelString) {
		List readersAndOrigins = Util.list(new Object[] {
				new StringReader(modelString), modelString }); // description of string
																												// is self.
		augmentFromReadersAndOrigins(readersAndOrigins);
	}

	/**
	 * Creates a new user-defined type with the given name and adds it to this
	 * model.
	 * 
	 * @return the newly created type
	 */
	public Type addType(String typeName) {
		Type type = new Type(typeName);
		types.add(type);
		return type;
	}

	/**
	 * Returns the user-defined types in this model.
	 * 
	 * @return unmodifiable Collection of Type
	 */
	public Collection<Type> getTypes() {
		return Collections.unmodifiableCollection(types);
	}

	/**
	 * Returns a Set consisting of the user-defined types named in the given
	 * string. The string should be a comma-separated list of type names (spaces
	 * are optional). The string can also be the special string "none", which
	 * yields an empty set, or "all", which yields the set of all user-defined
	 * types.
	 * 
	 * <p>
	 * If some of the named types are undefined, this method prints an error
	 * message and returns null.
	 */
	public Set<Type> getListedTypes(String typeList) {
		Set<Type> listedTypes = new HashSet<Type>();
		if (typeList.equals("none")) {
			return listedTypes;
		}

		if (typeList.equals("all")) {
			listedTypes.addAll(types);
			return listedTypes;
		}

		boolean correct = true;
		StringTokenizer st = new StringTokenizer(typeList, ", ", false);
		while (st.hasMoreTokens()) {
			String typeName = st.nextToken();
			Type type = Type.getType(typeName);
			if (type == null) {
				System.err.println("Undefined type: " + typeName);
				correct = false;
			} else {
				listedTypes.add(type);
			}
		}

		if (correct) {
			return listedTypes;
		}
		return null;
	}

	/**
	 * Adds the given user-defined function to this model.
	 * 
	 * @throws IllegalStateException
	 *           if there is already a user-defined function with the same
	 *           signature
	 */
	public void addFunction(Function f) {
		List funcsWithName = (List) functionsByName.get(f.getName());
		if (funcsWithName != null) {
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				Function g = (Function) iter.next();
				if (Arrays.equals(f.getArgTypes(), g.getArgTypes())) {
					throw new IllegalStateException("Can't add function with signature "
							+ f.getSig() + " because there is already a user-defined "
							+ " function with this signature.");
				}
			}
		} else {
			funcsWithName = new ArrayList();
			functionsByName.put(f.getName(), funcsWithName);
		}
		funcsWithName.add(f);
		functions.add(f);
	}

	/**
	 * Adds the observable function to the model's hashmap
	 * 
	 * @param obsFun: the observable function corresponding to referenceFunName
	 * @param referenceFunName the name of the random function referenced by f
	 * @throws IllegalStateException
	 *           if there is already a observable function with the same
	 *           signature corresponding to the same function
	 *           or if no function specified by obsFun's signature and referenceFunName
	 *           has been defined
	 */
	public void addObservableFunction(RandomFunction obsFun, String referenceFunName) {
		List funcsWithName = (List) functionsByName.get(referenceFunName);
		Function referencedFun = null;
		if (funcsWithName != null) {
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				Function g = (Function) iter.next();
				if (Arrays.equals(obsFun.getArgTypes(), g.getArgTypes())) {
					referencedFun = g;
				}
			}
		} else {
			throw new IllegalStateException("Can't add observableFunction with signature "
					+ obsFun.getSig() + "because the referenced function is not found");
		}
		if (observableToFunc.containsKey(obsFun.getSig()))
			throw new IllegalStateException("Can't add observableFunction with signature "
					+ obsFun.getSig() + " because there is already a observable function defined");
		if (referencedFun == null || !(referencedFun instanceof RandomFunction)){
			throw new IllegalStateException("No random function found with given name"
					+ referenceFunName);
		}
		
		observableToFunc.put(obsFun.getSig(), (RandomFunction) referencedFun);
		
		((RandomFunction) referencedFun).setObservableFun(obsFun);
	}
	
	/** Removes function from model. */
	public void removeFunction(Function f) {
		List funcsWithName = (List) functionsByName.get(f.getName());
		if (funcsWithName != null) {
			Function toBeRemoved = null;
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				Function g = (Function) iter.next();
				if (Arrays.equals(f.getArgTypes(), g.getArgTypes())) {
					toBeRemoved = g;
					break;
				}
			}
			if (toBeRemoved != null) {
				funcsWithName.remove(toBeRemoved);
				functions.remove(toBeRemoved);
			}
		}
	}

	/**
	 * Remove the first function with given name (since <i>which</i> one is not
	 * defined, this method is recommended for removing functions when there is
	 * only one function with given name.
	 */
	public void removeFunctionWithName(String name) {
		Function f = (Function) Util.getFirstOrNull(getFuncsWithName(name));
		if (f != null)
			removeFunction(f);
	}

	/**
	 * Returns the user-defined or built-in Function object with the specified
	 * signature, or null if no such function exists. If there is a user-defined
	 * function with the same signature as a built-in function, the user-defined
	 * one is returned.
	 */
	public Function getFunction(FunctionSignature sig) {
		List funcsWithName = (List) functionsByName.get(sig.getName());
		if (funcsWithName != null) {
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				Function f = (Function) iter.next();
				if (Arrays.equals(sig.getArgTypes(), f.getArgTypes())) {
					return f;
				}
			}
		}

		return BuiltInFunctions.getFunction(sig);
	}

	/**
	 * Returns all user-defined or built-in functions with the given name.
	 * 
	 * @return unmodifiable Collection of Function
	 */
	public Collection getFuncsWithName(String name) {
		Collection funcsWithName = (Collection) functionsByName.get(name);
		Collection builtInsWithName = BuiltInFunctions.getFuncsWithName(name);
		if (funcsWithName == null) {
			funcsWithName = builtInsWithName;
		} else {
			funcsWithName = Util.disjointUnion(funcsWithName, builtInsWithName);
		}
		return funcsWithName;
	}

	/**
	 * Returns the user-defined or built-in Function with the given name that is
	 * applicable to the given type list, or null if no such function exists. If
	 * both a user-defined function and a built-in function apply, then the
	 * user-defined one is returned. We assume that there is not more than one
	 * user-defined function that applies.
	 * 
	 * @param name
	 *          function name
	 * @param types
	 *          types of arguments
	 * @return Function with the name applicable to this type
	 */
	public Function getApplicableFunc(String name, Type[] types) {
		FunctionSignature sig = new FunctionSignature(name, types);
		Function f = getFunction(sig);
		if (f != null) {
			return f;
		}
		Collection funcsWithName = getFuncsWithName(name);
		for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
			f = (Function) iter.next();
			if (f.appliesTo(types)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Returns the user-defined or built-in functions with the given name that
	 * could apply to some tuple of objects with the given types. There could be
	 * multiple such functions if the given tuple contains a very general type,
	 * and several existing functions apply to different subtypes of that type.
	 * 
	 * @return unmodifiable Collection of Function
	 */
	public Collection getOverlappingFuncs(String name, Type[] types) {
		Collection funcsWithName = getFuncsWithName(name);
		Collection overlappingFuncs = new ArrayList();
		for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if (f.overlapsWith(types)) {
				overlappingFuncs.add(f);
			}
		}

		return Collections.unmodifiableCollection(overlappingFuncs);
	}

	/**
	 * Returns the unique random function with the given name and number of
	 * arguments. If there are no random functions matching this description,
	 * returns null. If there is more than one such function, returns the special
	 * object Model.MULTIPLE_FUNCTIONS. This method exists to support dependency
	 * statements that don't state the types of arguments.
	 */
	public Object getRandomFunc(String name, int numArgs) {
		List funcsWithName = (List) functionsByName.get(name);
		if (funcsWithName == null) {
			return null;
		}

		Object result = null;
		for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if ((f instanceof RandomFunction) && (f.getArgTypes().length == numArgs)) {
				if (result == null) {
					result = f;
				} else {
					result = MULTIPLE_FUNCTIONS;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the user-defined functions in this model.
	 * 
	 * @return unmodifiable Collection of Function
	 */
	public Collection<Function> getFunctions() {
		return Collections.unmodifiableCollection(functions);
	}

	/**
	 * Returns the value of the non-random constant with the given name, or null
	 * if there is no such non-random constant. This method works only for
	 * user-defined constants and the built-in constants "true" and "false", not
	 * for other constants such as numeric or string literals.
	 */
	public Object getConstantValue(String name) {
		if (name.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if (name.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}

		NonRandomFunction f = ((NonRandomFunction) getFunction(new FunctionSignature(
				name)));
		return (f == null) ? null : f.getValue();
	}

	/**
	 * Adds to the model a non-random constant with the given name, denoting a new
	 * enumerated object of the given type.
	 * 
	 * @return the new non-random constant. To get its value (the new enumerated
	 *         object), use its <code>getValue()</code> method.
	 */
	public NonRandomFunction addEnumeratedObject(String name, Type type) {
		NonRandomFunction constant = new NonRandomFunction(name, type);
		Object o = type.addGuaranteedObject(constant);
		constant.setConstantInterp(o);
		addFunction(constant);
		return constant;
	}

	/**
	 * Adds the given parfactor to this model.
	 */
	public void addParfactor(Parfactor pf) {
		parfactors.add(pf);
	}

	/**
	 * Returns the parfactors in this model, in the order they were declared.
	 * 
	 * @return unmodifiable list of parfactors
	 */
	public List<Parfactor> getParfactors() {
		return Collections.unmodifiableList(parfactors);
	}

	/** Prints this model to the given stream. */
	public void print(PrintStream s) {
		// Print guaranteed objects
		for (Iterator iter = types.iterator(); iter.hasNext();) {
			Type type = (Type) iter.next();
			s.println("guaranteed objects of type " + type + ": "
					+ type.getGuaranteedObjects());
		}

		// Print nonrandom functions
		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if (f instanceof NonRandomFunction) {
				((NonRandomFunction) f).print(s);
			}
		}

		// Print number statements
		for (Iterator typeIter = types.iterator(); typeIter.hasNext();) {
			Type type = (Type) typeIter.next();
			for (Iterator popIter = type.getPOPs().iterator(); popIter.hasNext();) {
				((POP) popIter.next()).printNumberStatement(s);
			}
		}

		// Print dependency statements
		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if (f instanceof RandomFunction) {
				((RandomFunction) f).printDepStatement(s);
			}
		}

		// Print parfactors
		for (Iterator iter = parfactors.iterator(); iter.hasNext();) {
			Parfactor pf = (Parfactor) iter.next();
			pf.print(s);
			s.println();
		}
	}

	/**
	 * Check types and scopes in the model. Also turns function names into
	 * Function objects, and variable names into LogicalVar objects. Returns true
	 * if no errors; otherwise prints error messages and returns false. The
	 * distinction between this method and <code>compile</code> is that this
	 * method does all its processing in the order in which functions were
	 * declared, whereas <code>compile</code> may jump around following the uses
	 * of nonrandom functions.
	 */
	public boolean checkTypesAndScope() {
		boolean correct = true;

		// Check dependency statements and non-random function interpretations
		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if (!f.checkTypesAndScope(this)) {
				correct = false;
			}
		}

		// Check number statements
		for (Iterator typeInfoIter = types.iterator(); typeInfoIter.hasNext();) {
			Type t = (Type) typeInfoIter.next();
			Iterator popIter = t.getPOPs().iterator();
			while (popIter.hasNext()) {
				POP pop = (POP) popIter.next();
				if (!pop.checkTypesAndScope(this)) {
					correct = false;
				}
			}
		}

		// Check parfactors
		for (Iterator pfIter = parfactors.iterator(); pfIter.hasNext();) {
			Parfactor pf = (Parfactor) pfIter.next();
			if (!pf.checkTypesAndScope(this)) {
				correct = false;
			}
		}

		return correct;
	}

	/**
	 * Returns true if every random function has a dependency statement, or if the
	 * model contains at least one parfactor. Otherwise prints appropriate error
	 * messages and returns false.
	 */
	public boolean checkCompleteness() {
		boolean complete = true;

		if (!parfactors.isEmpty()) {
			return complete;
		}

		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			if (f instanceof RandomFunction) {
				RandomFunction rf = (RandomFunction) f;
				DependencyModel depModel = rf.getDepModel();
				if (depModel == null) {
					System.err.println("No dependency statement found for "
							+ "random function " + rf.getSig());
					complete = false;
				}
			}
		}

		return complete;
	}

	/**
	 * Does compilation steps that can only be done correctly once the model is
	 * complete. Prints messages to standard error if any errors are encountered.
	 * Returns the number of errors encountered.
	 */
	public int compile() {
		int errors = 0;
		LinkedHashSet callStack = new LinkedHashSet();

		for (Iterator typeIter = types.iterator(); typeIter.hasNext();) {
			Type type = (Type) typeIter.next();
			for (Iterator popIter = type.getPOPs().iterator(); popIter.hasNext();) {
				errors += ((POP) popIter.next()).getDepModel().compile(callStack);
			}
		}

		for (Iterator iter = functions.iterator(); iter.hasNext();) {
			Function f = (Function) iter.next();
			errors += f.compile(callStack);
		}

		for (Iterator iter = parfactors.iterator(); iter.hasNext();) {
			Parfactor pf = (Parfactor) iter.next();
			errors += pf.compile(callStack);
		}

		return errors;
	}

	/**
	 * Returns a number that is one greater than the last number returned by this
	 * method.
	 */
	public static int nextCreationIndex() {
		return creationIndex++;
	}

	/**
	 * Compares two objects that can serve as arguments to basic random variables.
	 * The overall order is: Booleans, then integers, then timesteps, then
	 * guaranteed objects of user-defined types, then tuple representations of
	 * non-guaranteed objects, then object identifiers. Non-guaranteed objects
	 * have their own comparison method based on depths and generating objects.
	 * Guaranteed objects are compared first by the order in which their types
	 * were defined, then based on order within their type.
	 */
	public static int compareArgs(Object obj1, Object obj2) {
		// Booleans go first
		if (obj1 instanceof Boolean) {
			if (obj2 instanceof Boolean) {
				return (BuiltInTypes.BOOLEAN.getGuaranteedObjIndex(obj1) - BuiltInTypes.BOOLEAN
						.getGuaranteedObjIndex(obj2));
			}
			return -1; // obj1 goes first
		} else if (obj2 instanceof Boolean) {
			return 1; // obj2 goes first
		}

		// Next come integers
		if (obj1 instanceof Integer) {
			if (obj2 instanceof Integer) {
				return ((Integer) obj1).compareTo((Integer) obj2);
			}
			return -1; // obj1 goes first
		} else if (obj2 instanceof Integer) {
			return 1; // obj2 goes first
		}

		// Next come timesteps
		if (obj1 instanceof Timestep) {
			if (obj2 instanceof Timestep) {
				return ((Timestep) obj1).compareTo((Timestep) obj2);
			}
			return -1; // obj1 goes first
		} else if (obj2 instanceof Timestep) {
			return 1; // obj2 goes first
		}

		// Next come user-defined guaranteed objects.
		// User-defined guaranteed objects are compared by their types'
		// creation indices, then by their indices within the type
		if (obj1 instanceof EnumeratedObject) {
			if (obj2 instanceof EnumeratedObject) {
				int typeDiff = ((EnumeratedObject) obj1).getType().getCreationIndex()
						- ((EnumeratedObject) obj2).getType().getCreationIndex();
				if (typeDiff != 0) {
					return typeDiff;
				}
				return ((EnumeratedObject) obj1).compareTo(obj2);
			} else {
				return -1; // obj1 goes first
			}
		} else if (obj2 instanceof EnumeratedObject) {
			return 1; // obj2 goes first
		}

		// Next come tuple representations of non-guaranteed objects
		if (obj1 instanceof NonGuaranteedObject) {
			if (obj2 instanceof NonGuaranteedObject) {
				return ((NonGuaranteedObject) obj1).compareTo(obj2);
			}
			return -1; // obj1 goes first
		} else if (obj2 instanceof NonGuaranteedObject) {
			return 1; // obj2 goes first
		}

		// Next come object identifiers
		if (obj1 instanceof ObjectIdentifier) {
			if (obj2 instanceof ObjectIdentifier) {
				return ((ObjectIdentifier) obj1).compareTo(obj2);
			}
			return -1; // obj1 goes first
		} else if (obj2 instanceof ObjectIdentifier) {
			return 1; // obj2 goes first
		}

		// Objects of unrecognized types come last
		return 0; // both objects are of unrecognized type
	}

	/**
	 * Returns a number that is negative, positive, or zero according to whether
	 * <code>objs1</code> comes before, after, or at the same place as
	 * <code>objs2</code> in a lexicographic ordering.
	 * 
	 * @param objs1
	 *          an array of BLOG objects that can serve as arguments
	 * @param objs2
	 *          an array of BLOG objects that can serve as arguments
	 */
	public static int compareArgTuples(Object[] objs1, Object[] objs2) {
		for (int i = 0; i < objs1.length; ++i) {
			if (i >= objs2.length) {
				return 1; // objs2 is proper prefix of objs1, so it goes first
			}

			int objDiff = compareArgs(objs1[i], objs2[i]);
			if (objDiff != 0) {
				return objDiff;
			}
		}

		// If we get here, then objs1 is a prefix of objs2
		if (objs2.length > objs1.length) {
			return -1; // objs1 is proper prefix of objs2
		}
		return 0;
	}

	/**
	 * Returns the index of the given object among objects of its type. The index
	 * of a Boolean value is 0 for TRUE and 1 for FALSE; the index of a natural
	 * number is the number itself; and the index for a user-defined guaranteed
	 * object is determined by the order in which guaranteed objects of that type
	 * were declared (starting at 0). Other objects do not have indices; this
	 * method returns -1 for them.
	 */
	public static int getObjectIndex(Object obj) {
		if (obj instanceof Boolean) {
			return BuiltInTypes.BOOLEAN.getGuaranteedObjIndex(obj);
		}

		if (obj instanceof Integer) {
			int i = ((Integer) obj).intValue();
			if (i >= 0) {
				return i;
			}
		}

		if (obj instanceof Timestep) {
			return ((Timestep) obj).getValue();
		}

		if (obj instanceof EnumeratedObject) {
			return ((EnumeratedObject) obj).getIndex();
		}

		return -1;
	}

	/**
	 * Value returned by getRandomFunc when there are multiple random functions
	 * with the given name.
	 */
	public static final Object MULTIPLE_FUNCTIONS = new Object() {
	};

	/**
	 * Stores user-defined Type objects in the order they were declared.
	 */
	protected List<Type> types = new ArrayList<Type>(); // of Type

	/**
	 * Stores user-defined Function objects in the order they were declared.
	 */
	protected List<Function> functions = new ArrayList<Function>(); // of Function

	/**
	 * Maps function names to lists of functions with that name (and different
	 * argument types). For user-defined functions.
	 */
	protected Map<String, List<Function>> functionsByName = new HashMap<String, List<Function>>();

	/**
	 * Parfactors, in the order they were defined.
	 */
	protected List<Parfactor> parfactors = new ArrayList<Parfactor>();

	private static int creationIndex = 0;
	
	/**
	 * A map from observable functions to the actual random functions that they refer to
	 */
	private Map<FunctionSignature, RandomFunction> observableToFunc = new HashMap<FunctionSignature, RandomFunction>();
	public RandomFunction getRandomFuncByObservableSig (FunctionSignature fs){
		return observableToFunc.get(fs);
	}
}
