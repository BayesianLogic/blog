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

package blog.model;

import java.util.*;
import java.lang.reflect.*;
import java.io.PrintStream;

import blog.ConstantInterp;
import blog.EvalContext;
import blog.FunctionInterp;
import blog.GenericObject;
import blog.common.Util;


/**
 * Represents the symbol for a non-random function, whose value for the given
 * tuple of arguments is constant over worlds. Non-random functions do not have
 * dependency statement like the random functions do. Instead, the function
 * symbol's interpretation is given by an object that implements the
 * FunctionInterp interface.
 * 
 * @see blog.model.Function
 */
public class NonRandomFunction extends Function {

	public static NonRandomFunction createConstant(String name, Type ret_type,
			Object value) {
		List params = Collections.singletonList(value);
		return new NonRandomFunction(name, Collections.EMPTY_LIST, ret_type,
				new ConstantInterp(params));
	}

	/**
	 * Creates a non-random constant with the given name and type.
	 */
	public NonRandomFunction(String fname, Type ret_type) {
		super(fname, Collections.EMPTY_LIST, ret_type);
	}

	public NonRandomFunction(String fname, List arg_types, Type ret_type) {

		super(fname, arg_types, ret_type);

	}

	/**
	 * @param fname
	 *          the name of this non-random function
	 * 
	 * @param arg_types
	 *          List of Type objects for function arguments
	 * 
	 * @param ret_type
	 *          return type of this function
	 * 
	 * @param interpClass
	 *          implementation of FunctionInterp used for the function's
	 *          interpretation
	 * 
	 * @param interpParams
	 *          List of ArgSpec objects whose denotations are passed to the
	 *          interpretation's constructor. These must be non-random and must
	 *          contain no free variables.
	 */
	public NonRandomFunction(String fname, List arg_types, Type ret_type,
			Class interpClass, List interpParams) {
		super(fname, arg_types, ret_type);
		this.interpClass = interpClass;
		this.interpParams = interpParams;
	}

	public NonRandomFunction(String fname, List arg_types, Type ret_type,
			FunctionInterp interp) {
		super(fname, arg_types, ret_type);
		this.interpClass = interp.getClass();
		this.interp = interp;
	}

	/**
	 * Sets the interpretation of this function.
	 */
	public void setInterpretation(FunctionInterp interp) {
		this.interpClass = interp.getClass();
		this.interp = interp;
	}

	/**
	 * Sets the interpretation of this function to be an instance of the given
	 * class, constructed with the given parameters.
	 */
	public void setInterpretation(Class interpClass, List interpParams) {
		this.interpClass = interpClass;
		this.interpParams = interpParams;
		this.interp = null;
	}

	/**
	 * Sets the interpretation of this function to be a ConstantInterp with the
	 * given value.
	 */
	public void setConstantInterp(Object value) {
		List params = Collections.singletonList(value);
		setInterpretation(new ConstantInterp(params));
	}

	/**
	 * Returns the interpretation of this function, or null if this function's
	 * interpretation has not been set or constructed yet.
	 */
	public FunctionInterp getInterpretation() {
		return interp;
	}

	/**
	 * Returns the FunctionInterp implementation used for this function's
	 * interpretation, or null if this function's interpretation class has not
	 * been set.
	 */
	public Class getInterpClass() {
		return interpClass;
	}

	public Object getValue() {
		return interp.getValue(Collections.EMPTY_LIST);
	}

	public Object getValue(Object[] args) {
		for (int i = 0; i < args.length; ++i) {
			// Don't need to check for invalid objects because non-random
			// function interpretations return Model.NULL on non-guaranteed
			// objects anyway.
			if (args[i] == Model.NULL) {
				return Model.NULL;
			}

			if (args[i] instanceof GenericObject) {
				return null; // can't determine value on generic object
			}
		}

		return interp.getValue(Arrays.asList(args));
	}

	public Object getValueSingleArg(Object arg) {
		if (arg == Model.NULL) {
			return Model.NULL;
		}

		return interp.getValue(Collections.singletonList(arg));
	}

	/**
	 * Returns the set of argument tuples that yield the given value, or null if
	 * this set cannot be computed easily.
	 * 
	 * @return unmodifiable Set of List
	 */
	public Set getInverseTuples(Object value) {
		return interp.getInverseTuples(value);
	}

	/**
	 * Returns the set of values for argument <code>argIndex</code> that, in
	 * combination with the given values for the other arguments, yield the given
	 * function value. If this set cannot be computed straightforwardly, returns
	 * null.
	 * 
	 * @param args
	 *          tuple of arguments; the entry at <code>argIndex</code> is ignored
	 */
	public Set getInverseArgs(Object[] args, int argIndex, Object value) {
		return interp.getInverseArgs(Arrays.asList(args), argIndex,
				getArgTypes()[argIndex], value);
	}

	public Object getValueInContext(Object[] args, EvalContext context,
			boolean stable) {
		return getValue(args);
	}

	public boolean checkTypesAndScope(Model model) {
		if (interpClass == null) {
			return true; // no errors
		}

		boolean correct = true;
		Map scope = Collections.EMPTY_MAP;

		if (interpParams != null) {
			// Type-check the interpretation parameters, making sure they
			// contain no free variables
			for (Iterator iter = interpParams.iterator(); iter.hasNext();) {
				ArgSpec param = (ArgSpec) iter.next();
				if (!param.checkTypesAndScope(model, scope)) {
					correct = false;
				}
			}

			// For ConstantInterp, we can do additional checking
			if (interpClass == ConstantInterp.class) {
				if (interpParams.size() != 1) {
					System.err.println("ConstantInterp takes exactly one parameter.");
					correct = false;
				} else {
					ArgSpec param = (ArgSpec) interpParams.get(0);
					Type paramType = null;
					if (param instanceof Term) {
						paramType = ((Term) param).getType();
					} else if (param instanceof CardinalitySpec) {
						paramType = BuiltInTypes.NATURAL_NUM;
					} else {
						System.err.println(param.getLocation()
								+ ": Parameter to ConstantInterp must be a "
								+ "term or cardinality specification, not " + param);
						correct = false;
					}

					Type expected = getRetType();
					if ((paramType != null) && (expected != null)
							&& !paramType.isSubtypeOf(expected)) {
						System.err.println(param.getLocation()
								+ ": Incorrect value type for nonrandom " + "constant " + this
								+ ": expected " + expected + ", got " + paramType);
						correct = false;
					}
				}
			}
		}

		return correct;
	}

	/**
	 * Constructs this function's interpretation, if it hasn't been constructed
	 * already. If this function is already in the call stack, prints an error
	 * message saying that there is a cyclic dependency among the non-random
	 * functions.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		if (interp != null) {
			return 0; // already compiled
		}

		if (callStack.contains(this)) {
			System.err.println("Error: non-random portion of model contains "
					+ "a cycle:");
			boolean iterInCycle = false;
			for (Iterator iter = callStack.iterator(); iter.hasNext();) {
				Object cur = iter.next();
				if (iterInCycle) {
					System.err.println("\t<- " + cur.getClass().getName() + " " + cur);
				} else if (cur == this) {
					System.err.println("\t" + cur.getClass().getName() + " " + cur);
					iterInCycle = true;
				}
			}
			System.err.println("\t<- " + getClass().getName() + " " + this);
			return 1;
		}

		callStack.add(this);
		int errors;
		if (interpClass == null) {
			System.err.println("No definition found for non-random function " + this);
			errors = 1;
		} else {
			errors = initInterp(callStack);
		}

		callStack.remove(this);
		return errors;
	}

	private int initInterp(LinkedHashSet callStack) {
		int errors = 0;

		List paramValues = new ArrayList();
		for (Iterator iter = interpParams.iterator(); iter.hasNext();) {
			ArgSpec param = (ArgSpec) iter.next();
			int thisParamErrors = param.compile(callStack);
			errors += thisParamErrors;
			if (thisParamErrors == 0) {
				Object val = param.getValueIfNonRandom();
				if (val == null) {
					System.err.println(param.getLocation()
							+ ": Error in definition of non-random function " + this
							+ ".  Parameter " + param + " is random.");
					++errors;
				} else {
					paramValues.add(val);
				}
			}
		}

		if (errors > 0) {
			return errors; // can't compute parameters, so can't create interp
		}

		try {
			Class[] constrArgTypes = { List.class };
			Constructor ct = interpClass.getConstructor(constrArgTypes);
			Object[] constrArgs = { paramValues };
			interp = (FunctionInterp) ct.newInstance(constrArgs);
		} catch (InvocationTargetException e) {
			System.err.println("Error initializing interpretation for " + this + ": "
					+ e.getCause().getMessage());
			++errors;
		} catch (NoSuchMethodException e) {
			System.err.println("Error initializing interpretation for " + this + ": "
					+ interpClass + " does not have "
					+ "a constructor with a single argument of " + "type List.");
			++errors;
		} catch (ClassCastException e) {
			System.err.println("Error initializing interpretation for " + this + ": "
					+ interpClass + " does not "
					+ "implement the FunctionInterp interface.");
			++errors;
		} catch (Exception e) {
			System.err.println("Error initializing interpretation for " + this
					+ ": couldn't instantiate class " + interpClass);
			++errors;
		}

		return errors;
	}

	/**
	 * Prints a description of this NonRandomFunction to the given stream.
	 */
	public void print(PrintStream s) {
		s.print("nonrandom ");
		s.print(getRetType());
		s.print(" ");
		s.print(getName());

		if (getArgTypes().length > 0) {
			s.print("(");
			s.print(getArgTypes()[0]);
			for (int i = 1; i < getArgTypes().length; ++i) {
				s.print(", ");
				s.print(getArgTypes()[i]);
			}
			s.print(")");
		}
		s.println();

		if (interpClass != null) {
			s.print("\tdefined by " + interpClass);
			if (interpParams != null) {
				s.print(interpParams); // List prints brackets automatically
			}
			s.println();
		}
	}

	/**
	 * Compares two NonRandomFunctions; they are considered equal if they have the
	 * same interpretation class, parameters and function interpretation.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof NonRandomFunction))
			return false;
		NonRandomFunction oNRF = (NonRandomFunction) o;
		return Util.equalsOrBothNull(interpClass, oNRF.interpClass)
				&& Util.equalsOrBothNull(interpParams, oNRF.interpParams)
				&& Util.equalsOrBothNull(interp, oNRF.interp);
	}

	public int hashCode() {
		int code = interpClass.hashCode();
		if (interpParams != null)
			for (Iterator it = interpParams.iterator(); it.hasNext();)
				code ^= it.next().hashCode();
		code ^= interp.hashCode();
		return code;
	}

	private Class interpClass;

	private List interpParams; // of ArgSpec

	private FunctionInterp interp;
}
