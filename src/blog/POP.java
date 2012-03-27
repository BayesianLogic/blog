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

/**
 * Represents a potential object pattern (POP), which includes the type of
 * object to be generated and a tuple of origin functions.
 */
public class POP {
	/**
	 * Creates a new potential object pattern for the given type and the given
	 * tuple of origin functions, with the given dependency model.
	 * 
	 * @param type
	 *          the type of object generated
	 * 
	 * @param originFuncList
	 *          List of OriginFunction objects
	 * 
	 * @param numberst
	 *          dependency model for this POP
	 */
	public POP(Type type, List originFuncList, DependencyModel numberst) {
		this.type = type;
		originFuncs = new OriginFunction[originFuncList.size()];
		originFuncList.toArray(originFuncs);
		this.numberst = numberst;

		argTypes = new Type[originFuncs.length];
		for (int i = 0; i < originFuncs.length; i++) {
			argTypes[i] = originFuncs[i].getRetType();
		}
	}

	public final Type type() {
		return type;
	}

	public final OriginFunction[] originFuncs() {
		return originFuncs;
	}

	public Type[] getArgTypes() {
		return argTypes;
	}

	/**
	 * Returns the logical variables that stand for the generating objects in this
	 * POP's dependency model.
	 */
	public LogicalVar[] getGenObjVars() {
		if (genObjVars == null) {
			throw new IllegalStateException(
					"Generating object variables not set for " + this);
		}
		return genObjVars;
	}

	/**
	 * Sets the variables that will stand for the generating objects in this POP's
	 * dependency model.
	 * 
	 * @param vars
	 *          List of String objects representing the variables
	 */
	public void setGenObjVars(List vars) {
		genObjVars = new LogicalVar[vars.size()];
		for (int i = 0; i < genObjVars.length; ++i) {
			genObjVars[i] = new LogicalVar((String) vars.get(i), argTypes[i]);
		}
	}

	public DependencyModel getDepModel() {

		return numberst;

	}

	public void setDepModel(DependencyModel depModel) {
		numberst = depModel;
	}

	/**
	 * Returns the index of <code>f</code> in this POP's origin function list, or
	 * -1 if it is not in the list.
	 */
	public int getOriginFuncIndex(OriginFunction f) {
		for (int i = 0; i < originFuncs.length; ++i) {
			if (f == originFuncs[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns a BitSet where the ith bit is true if this POP uses the ith origin
	 * function in the list of origin functions for its type.
	 */
	public BitSet getOriginFuncSet() {
		List typeOriginFuncs = type.getOriginFunctions();
		BitSet originFuncSet = new BitSet(typeOriginFuncs.size());
		for (int i = 0; i < typeOriginFuncs.size(); ++i) {
			if (getOriginFuncIndex((OriginFunction) typeOriginFuncs.get(i)) != -1) {
				originFuncSet.set(i);
			}
		}
		return originFuncSet;
	}

	/**
	 * Returns a basic random variable for this POP with no generating objects.
	 */
	public NumberVar rv() {
		Object[] originObjs = {};
		return new NumberVar(this, originObjs, true);
	}

	/**
	 * Returns a basic random variable for this POP with the given single
	 * generating object.
	 */
	public NumberVar rv(Object genObj) {
		Object[] genObjs = { genObj };
		return new NumberVar(this, genObjs, true);
	}

	/**
	 * Returns a basic random variable for this POP with the given two generating
	 * objects.
	 */
	public NumberVar rv(Object genObj1, Object genObj2) {
		Object[] genObjs = { genObj1, genObj2 };
		return new NumberVar(this, genObjs, true);
	}

	/**
	 * Returns a basic random variable for this POP with the given array of
	 * generating objects.
	 */
	public NumberVar rvWithArgs(Object[] genObjs) {
		return new NumberVar(this, genObjs);
	}

	/**
	 * Prints the number statement for this POP to the given stream.
	 */
	public void printNumberStatement(PrintStream s) {
		s.print("#" + type + "(");
		if (originFuncs.length > 0) {
			for (int i = 0; i < originFuncs.length; ++i) {
				s.print(originFuncs[i] + " = " + genObjVars[i]);
				if (i + 1 < originFuncs.length) {
					s.print(", ");
				}
			}
		}
		s.println(")");

		numberst.print(s);
	}

	/**
	 * Checks types and scopes in this POP and its number statement. Returns true
	 * if everything is correct; otherwise prints error messages to standard error
	 * and returns false.
	 */
	public boolean checkTypesAndScope(Model model) {
		Map scope = new HashMap();
		for (int i = 0; i < genObjVars.length; ++i) {
			scope.put(genObjVars[i].getName(), genObjVars[i]);
		}

		return numberst.checkTypesAndScope(model, scope);
	}

	/**
	 * Returns a string of the form #Type(f1, ..., fK) where Type is the type of
	 * object generated and f1, ..., fK are the origin functions.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("#");
		buf.append(type);

		buf.append("(");
		for (int i = 0; i < originFuncs.length; ++i) {
			buf.append(originFuncs[i]);
			if (i + 1 < originFuncs.length) {
				buf.append(", ");
			}
		}
		buf.append(")");

		return buf.toString();
	}

	private Type type;
	private OriginFunction[] originFuncs;
	private Type[] argTypes;
	private DependencyModel numberst;
	private LogicalVar[] genObjVars;
}
