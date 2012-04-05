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

package blog.model;

import java.util.*;

import blog.AbstractFunctionInterp;
import blog.ConstantInterp;
import blog.FunctionInterp;
import blog.Timestep;
import Jama.*;

/**
 * Class with static methods and constants for built-in non-random functions
 * (including built-in constants). This class cannot be instantiated.
 */
public class BuiltInFunctions {
	/**
	 * Constant that always denotes Model.NULL.
	 */
	public static final NonRandomFunction NULL;

	/**
	 * Constant that denotes the natural number 0. The parser creates
	 * NonRandomConstant objects as needed to represent numeric constants that it
	 * actually encounters in a file, but some internal compilation code may need
	 * to use this constant even if it doesn't occur in a file.
	 */
	public static final NonRandomFunction ZERO;

	/**
	 * Constant that denotes the natural number 1. The parser creates
	 * NonRandomConstant objects as needed to represent numeric constants that it
	 * actually encounters in a file, but some internal compilation code may need
	 * to use this constant even if it doesn't occur in a file.
	 */
	public static final NonRandomFunction ONE;

	/**
	 * Constant that denotes the timestep 0. The parser creates NonRandomConstant
	 * objects as needed to represent timestep constants that it actually
	 * encounters in a file, but some internal compilation code may need to use
	 * this constant even if it doesn't occur in a file.
	 */
	public static final NonRandomFunction EPOCH;

	/**
	 * The LessThan relation on type Real (and its subtypes).
	 */
	public static NonRandomFunction LT;

	/**
	 * The LessThanOrEqual relation on type Real (and its subtypes).
	 */
	public static NonRandomFunction LEQ;

	/**
	 * The GreaterThan relation on type Real (and its subtypes)
	 */
	public static NonRandomFunction GT;

	/**
	 * The GreaterThanOrEqual relation on type Real (and its subtypes).
	 */
	public static NonRandomFunction GEQ;

	/**
	 * The successor function on natural numbers. Given a number n, it returns
	 * n+1.
	 */
	public static NonRandomFunction SUCC;

	/**
	 * The predecessor function on natural numbers. Given a positive number n, it
	 * returns n-1. Given the number 0, it returns Model.NULL.
	 */
	public static NonRandomFunction PRED;

	/**
	 * A function from integers to natural numbers that yields the non-negative
	 * part of the given integer <code>x</code>: that is, <code>min(x, 0)</code>.
	 */
	public static NonRandomFunction NON_NEG_PART;

	/**
	 * The function on integers <code>x<code>, <code>y</code> that returns x + y.
	 */
	public static NonRandomFunction PLUS;

	/**
	 * The function on integers <code>x<code>, <code>y</code> that returns x - y.
	 */
	public static NonRandomFunction MINUS;

	/**
	 * The function on reals <code>x<code>, <code>y</code> that returns x + y.
	 */
	public static NonRandomFunction RPLUS;

	/**
	 * The function on reals <code>x<code>, <code>y</code> that returns x - y.
	 */
	public static NonRandomFunction RMINUS;

	/**
	 * The predecessor function on timesteps. Given a positive timestep n, it
	 * returns n-1. Given the timestep 0, it returns Model.NULL.
	 */
	public static NonRandomFunction PREV;

	/**
	 * A function on strings <code>x</code>, <code>y</code> that returns the
	 * concatenation of <code>x</code> and <code>y</code>.
	 */
	public static NonRandomFunction CONCAT;

	/**
	 * A function on RVectors <code>x</code>, <code>y</code> that returns the
	 * element-wise sum of <code>x</code> and <code>y</code>.
	 */
	public static NonRandomFunction VPLUS;

	/**
	 * A function on RVectors <code>x</code>, <code>y</code> that returns the
	 * element-wise difference of <code>x</code> and <code>y</code>.
	 */
	public static NonRandomFunction VMINUS;

	/**
	 * A function that takes a string and returns true if the string is empty.
	 */
	public static NonRandomFunction IS_EMPTY_STRING;

	private BuiltInFunctions() {
		// prevent instantiation
	}

	/**
	 * Returns the built-in function (or constant) with the given signature.
	 * Returns null if there is no such built-in function, or if the given name is
	 * a numeric, character, or string literal that is only created as needed by
	 * the parser.
	 */
	public static NonRandomFunction getFunction(Function.Sig sig) {
		List funcsWithName = (List) functions.get(sig.getName());
		if (funcsWithName != null) {
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				NonRandomFunction f = (NonRandomFunction) iter.next();
				if (Arrays.equals(sig.getArgTypes(), f.getArgTypes())) {
					return f;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the built-in constant symbol with the given name, which has the
	 * given return type and denotes the given value. Creates the constant symbol
	 * automatically if it hasn't been created yet.
	 */
	public static NonRandomFunction getLiteral(String name, Type type,
			Object value) {
		NonRandomFunction f = getFunction(new Function.Sig(name));
		if (f == null) {
			List params = Collections.singletonList(value);
			f = new NonRandomFunction(name, Collections.EMPTY_LIST, type,
					new ConstantInterp(params));
			addFunction(f);
		}
		return f;
	}

	/**
	 * Returns the built-in functions (and constants) with the given name.
	 * 
	 * @return unmodifiable List of Function
	 */
	public static List getFuncsWithName(String name) {
		List funcsWithName = (List) functions.get(name);
		return (funcsWithName == null) ? Collections.EMPTY_LIST : Collections
				.unmodifiableList(funcsWithName);
	}

	private static void addFunction(Function f) {
		List funcsWithName = (List) functions.get(f.getName());
		if (funcsWithName != null) {
			for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
				Function g = (Function) iter.next();
				if (Arrays.equals(g.getArgTypes(), f.getArgTypes())) {
					System.err.println("Warning: overwriting existing " + "function "
							+ g.getSig());
					iter.remove();
				}
			}
		} else {
			funcsWithName = new ArrayList();
			functions.put(f.getName(), funcsWithName);
		}
		funcsWithName.add(f);
	}

	static Map functions = new HashMap(); // from String to List of Function

	static {
		// Add non-random constants
		NULL = getLiteral("null", BuiltInTypes.NULL, Model.NULL);
		ZERO = getLiteral("0", BuiltInTypes.NATURAL_NUM, new Integer(0));
		ONE = getLiteral("1", BuiltInTypes.NATURAL_NUM, new Integer(1));
		EPOCH = getLiteral("@0", BuiltInTypes.TIMESTEP, Timestep.at(0));

		// Add non-random functions from (real x real) to Boolean
		List argTypes = new ArrayList();
		argTypes.add(BuiltInTypes.REAL);
		argTypes.add(BuiltInTypes.REAL);
		Type retType = BuiltInTypes.BOOLEAN;

		FunctionInterp ltInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				double arg1 = ((Number) args.get(0)).doubleValue();
				double arg2 = ((Number) args.get(1)).doubleValue();
				return Boolean.valueOf(arg1 < arg2);
			}
		};
		LT = new NonRandomFunction("LessThan", argTypes, retType, ltInterp);
		addFunction(LT);

		FunctionInterp leqInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				double arg1 = ((Number) args.get(0)).doubleValue();
				double arg2 = ((Number) args.get(1)).doubleValue();
				return Boolean.valueOf(arg1 <= arg2);
			}
		};
		LEQ = new NonRandomFunction("LessThanOrEqual", argTypes, retType, leqInterp);
		addFunction(LEQ);

		FunctionInterp gtInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				double arg1 = ((Number) args.get(0)).doubleValue();
				double arg2 = ((Number) args.get(1)).doubleValue();
				return Boolean.valueOf(arg1 > arg2);
			}
		};
		GT = new NonRandomFunction("GreaterThan", argTypes, retType, gtInterp);
		addFunction(GT);

		FunctionInterp geqInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				double arg1 = ((Number) args.get(0)).doubleValue();
				double arg2 = ((Number) args.get(1)).doubleValue();
				return Boolean.valueOf(arg1 >= arg2);
			}
		};
		GEQ = new NonRandomFunction("GreaterThanOrEqual", argTypes, retType,
				geqInterp);
		addFunction(GEQ);

		// Add non-random functions from natural number to natural number
		argTypes.clear();
		argTypes.add(BuiltInTypes.NATURAL_NUM);
		retType = BuiltInTypes.NATURAL_NUM;

		FunctionInterp succInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Integer arg = (Integer) args.get(0);
				return new Integer(arg.intValue() + 1);
			}
		};
		SUCC = new NonRandomFunction("Succ", argTypes, retType, succInterp);
		addFunction(SUCC);

		FunctionInterp predInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Integer arg = (Integer) args.get(0);
				if (arg.intValue() <= 0) {
					return Model.NULL;
				}
				return new Integer(arg.intValue() - 1);
			}
		};
		PRED = new NonRandomFunction("Pred", argTypes, retType, predInterp);
		addFunction(PRED);

		// Add non-random functions from integer to natural number
		argTypes.clear();
		argTypes.add(BuiltInTypes.INTEGER);
		retType = BuiltInTypes.NATURAL_NUM;

		FunctionInterp nonNegPartInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Integer arg = (Integer) args.get(0);
				int n = arg.intValue();
				return new Integer((n < 0) ? 0 : n);
			}
		};
		NON_NEG_PART = new NonRandomFunction("NonNegPart", argTypes, retType,
				nonNegPartInterp);
		addFunction(NON_NEG_PART);

		// Add non-random functions from (integer x integer) to integer
		argTypes.clear();
		argTypes.add(BuiltInTypes.INTEGER);
		argTypes.add(BuiltInTypes.INTEGER);
		retType = BuiltInTypes.INTEGER;

		FunctionInterp plusInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Integer arg1 = (Integer) args.get(0);
				Integer arg2 = (Integer) args.get(1);
				return new Integer(arg1.intValue() + arg2.intValue());
			}
		};
		PLUS = new NonRandomFunction("Sum", argTypes, retType, plusInterp);
		addFunction(PLUS);

		FunctionInterp minusInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Integer arg1 = (Integer) args.get(0);
				Integer arg2 = (Integer) args.get(1);
				return new Integer(arg1.intValue() - arg2.intValue());
			}
		};
		MINUS = new NonRandomFunction("Diff", argTypes, retType, minusInterp);
		addFunction(MINUS);

		// Add non-random functions from (real x real) to real
		argTypes.clear();
		argTypes.add(BuiltInTypes.REAL);
		argTypes.add(BuiltInTypes.REAL);
		retType = BuiltInTypes.REAL;

		FunctionInterp rplusInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Number arg1 = (Number) args.get(0);
				Number arg2 = (Number) args.get(1);
				return new Double(arg1.doubleValue() + arg2.doubleValue());
			}
		};
		RPLUS = new NonRandomFunction("RSum", argTypes, retType, rplusInterp);
		addFunction(RPLUS);

		FunctionInterp rminusInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Double arg1 = (Double) args.get(0);
				Double arg2 = (Double) args.get(1);
				return new Double(arg1.doubleValue() - arg2.doubleValue());
			}
		};
		RMINUS = new NonRandomFunction("RDiff", argTypes, retType, rminusInterp);
		addFunction(RMINUS);

		// Add non-random functions from timestep to timestep
		argTypes.clear();
		argTypes.add(BuiltInTypes.TIMESTEP);
		retType = BuiltInTypes.TIMESTEP;

		FunctionInterp prevInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				Timestep arg = (Timestep) args.get(0);
				if (arg.getValue() <= 0) {
					return Model.NULL;
				}
				return Timestep.at(arg.getValue() - 1);
			}
		};
		PREV = new NonRandomFunction("Prev", argTypes, retType, prevInterp);
		addFunction(PREV);

		// Add non-random functions from (string x string) to string
		argTypes.clear();
		argTypes.add(BuiltInTypes.STRING);
		argTypes.add(BuiltInTypes.STRING);
		retType = BuiltInTypes.STRING;

		FunctionInterp concatInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				String arg1 = (String) args.get(0);
				String arg2 = (String) args.get(1);
				return arg1.concat(arg2);
			}
		};
		CONCAT = new NonRandomFunction("Concat", argTypes, retType, concatInterp);
		addFunction(CONCAT);

		// add non-random functions from (vector x vector) to vector
		argTypes.clear();
		argTypes.add(BuiltInTypes.RVECTOR);
		argTypes.add(BuiltInTypes.RVECTOR);
		retType = BuiltInTypes.RVECTOR;

		FunctionInterp vecPlusInterp = new AbstractFunctionInterp() {
			private static final long serialVersionUID = 0;

			public Object getValue(List args) {
				Matrix arg1 = (Matrix) args.get(0);
				Matrix arg2 = (Matrix) args.get(1);
				return arg1.plus(arg2);
			}
		};
		VPLUS = new NonRandomFunction("VectorAdd", argTypes, retType, vecPlusInterp);
		addFunction(VPLUS);

		FunctionInterp vecMinusInterp = new AbstractFunctionInterp() {
			private static final long serialVersionUID = 0;

			public Object getValue(List args) {
				Matrix arg1 = (Matrix) args.get(0);
				Matrix arg2 = (Matrix) args.get(1);
				return arg1.minus(arg2);
			}
		};
		VMINUS = new NonRandomFunction("VectorSubtract", argTypes, retType,
				vecMinusInterp);
		addFunction(VMINUS);

		// Add non-random functions from string to Boolean
		argTypes.clear();
		argTypes.add(BuiltInTypes.STRING);
		retType = BuiltInTypes.BOOLEAN;

		FunctionInterp isEmptyStringInterp = new AbstractFunctionInterp() {
			public Object getValue(List args) {
				return Boolean.valueOf(((String) args.get(0)).length() == 0);
			}
		};
		IS_EMPTY_STRING = new NonRandomFunction("IsEmptyString", argTypes, retType,
				isEmptyStringInterp);
		addFunction(IS_EMPTY_STRING);
	}
}
