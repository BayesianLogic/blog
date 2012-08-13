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
import java.util.regex.*;

import blog.Timestep;

/**
 * Class with static methods and variables for dealing with the built-in types
 * that exist in every BLOG model. These include Boolean, Real, Integer,
 * NaturalNum, String, Character and types R<i>m</i>x<i>n</i>Matrix and
 * R<i>n</i>Vector for positive integers m, n.
 */
public class BuiltInTypes {
	/**
	 * Supertype for all built-in types. Used to determine whether a type is
	 * built in or not.
	 */
	public static final Type BUILT_IN = new Type("**BuiltIn**");

	/**
	 * Type for the built-in constant "null". A term of any type except BOOLEAN
	 * can have the same denotation as "null", so we treat this special type as
	 * being a subtype of every type except BOOLEAN.
	 */
	public static final Type NULL = new Type("**NullType**") {
		public boolean isSubtypeOf(Type other) {
			return (other != BOOLEAN);
		}
	};

	/**
	 * Type for the Boolean values "true" and "false". Objects of this type are
	 * represented as java.lang.Boolean objects.
	 */
	public static final Type BOOLEAN = new BooleanType();

	/**
	 * Type for real numbers. Objects of this type are represented as
	 * java.lang.Number objects.
	 */
	public static final Type REAL = new Type("Real", BUILT_IN, true) {
		public Term getCanonicalTerm(Object obj) {
			if (!(obj instanceof Number)) {
				if (obj == Model.NULL) {
					return new FuncAppTerm(BuiltInFunctions.NULL);
				}
				throw new IllegalArgumentException("Object " + obj
						+ " is not of type Real.");
			}

			double value = ((Number) obj).doubleValue();
			NonRandomFunction c = BuiltInFunctions.getLiteral(
					String.valueOf(value), this, new Double(value));
			return new FuncAppTerm(c);
		}
	};

	/**
	 * Type for integers. This is a subtype of the real numbers. Objects of this
	 * type are represented as java.lang.Integer objects.
	 */
	public static final Type INTEGER = new IntegralType("Integer", REAL);

	/**
	 * Type for natural numbers. This is a subtype of the integers. Objects of
	 * this type are represented as java.lang.Integer objects.
	 */
	public static final Type NATURAL_NUM = new IntegralType("NaturalNum",
			INTEGER);

	/**
	 * Type for finite strings of Unicode characters. Objects of this type are
	 * represented as java.lang.String objects.
	 */
	public static final Type STRING = new Type("String", BUILT_IN, true);

	/**
	 * Type for individual unicode characters. Objects of this type are
	 * represented as java.lang.Character objects.
	 */
	public static final Type CHARACTER = new CharacterType();

	/**
	 * Type for timesteps. Objects of this type are represented as Timestep
	 * objects, which extend Number (but not Integer, because Integer is final).
	 */
	public static Type TIMESTEP = new Type("Timestep", REAL, true) {
		public Object getGuaranteedObject(int index) {
			if (index >= 0) {
				return Timestep.at(index);
			}
			return null;
		}

		public Term getCanonicalTerm(Object obj) {
			if (!(obj instanceof Timestep)) {
				if (obj == Model.NULL) {
					return new FuncAppTerm(BuiltInFunctions.NULL);
				}
				throw new IllegalArgumentException("Object " + obj
						+ " not of type " + this);
			}
			NonRandomFunction c = BuiltInFunctions.getLiteral(obj.toString(),
					this, obj);
			return new FuncAppTerm(c);
		}
	};

	private BuiltInTypes() {
		// prevent instantiation
	}

	/**
	 * Returns the built-in type with the given name, or null if there is no
	 * such built-in type.
	 */
	public static Type getType(String name) {
		Type type = (Type) builtInTypes.get(name);
		if (type == null) {
			// TODO: don't touch this until we're sure we're not using TypeGenerators
			//			in any form whatsoever
			for (Iterator iter = typeGenerators.iterator(); iter.hasNext();) {
				TypeGenerator generator = (TypeGenerator) iter.next();
				type = generator.generateIfMatches(name);
				if (type != null) {
					builtInTypes.put(name, type);
					break;
				}
			}
		}
		return type; // null if not in table and doesn't match any generator
	}

	private static void addType(Type type) {
		builtInTypes.put(type.getName(), type);
	}

	private static class BooleanType extends Type {
		BooleanType() {
			super("Boolean", BUILT_IN);
			builtInGuarObjs.add(Boolean.TRUE);
			builtInGuarObjs.add(Boolean.FALSE);
		}

		public List getGuaranteedObjects() {
			return Collections.unmodifiableList(builtInGuarObjs);
		}

		// "true" comes first
		public int getGuaranteedObjIndex(Object obj) {
			if (obj instanceof Boolean) {
				if (((Boolean) obj).booleanValue() == true) {
					return 0;
				}
				return 1;
			}

			return -1;
		}

		public Object getDefaultValue() {
			return Boolean.FALSE;
		}

		public Term getCanonicalTerm(Object obj) {
			if (!(obj instanceof Boolean)) {
				throw new IllegalArgumentException("Object " + obj
						+ " not of type " + this);
			}
			NonRandomFunction c = BuiltInFunctions.getLiteral(
					String.valueOf(obj), this, obj);
			return new FuncAppTerm(c);
		}

		private List builtInGuarObjs = new ArrayList();
	}

	private static class IntegralType extends Type {
		public IntegralType(String name, Type superType) {
			super(name, superType, true); // true means infinite type
		}

		public Object getGuaranteedObject(int index) {
			if (index >= 0) {
				return new Integer(index);
			}
			return null;
		}

		public Term getCanonicalTerm(Object obj) {
			if (!(obj instanceof Integer)) {
				if (obj == Model.NULL) {
					return new FuncAppTerm(BuiltInFunctions.NULL);
				}
				throw new IllegalArgumentException("Object " + obj
						+ " is not of type " + this);
			}
			NonRandomFunction c = BuiltInFunctions.getLiteral(
					String.valueOf(obj), this, obj);
			return new FuncAppTerm(c);
		}
	}

	private static class CharacterType extends Type {
		public CharacterType() {
			super("Character", BUILT_IN);
		}

		public Object getGuaranteedObject(int index) {
			if ((index >= Character.MIN_VALUE)
					&& (index <= Character.MAX_VALUE)) {
				return new Character((char) index);
			}
			return null;
		}

		public List getGuaranteedObjects() {
			if (allCharacters == null) {
				allCharacters = new ArrayList();
				for (char c = Character.MIN_VALUE; c <= Character.MAX_VALUE; ++c) {
					allCharacters.add(new Character(c));
				}
			}
			return Collections.unmodifiableList(allCharacters);
		}

		private List allCharacters;
	}

	private static interface TypeGenerator {
		Type generateIfMatches(String name);
	}

/* TODO: Retain RMatrixGenerator comment for now, as it demonstrates how
 * 			a TypeGenerator	is created.
 */
	
//	private static class RMatrixGenerator implements TypeGenerator {
//		private static final Pattern MATRIX_PATTERN = Pattern
//				.compile("R([1-9][0-9]*)x([1-9][0-9]*)Matrix");
//
//		public Type generateIfMatches(String name) {
//			Matcher matcher = MATRIX_PATTERN.matcher(name);
//			if (matcher.matches()) {
//				int m = Integer.parseInt(matcher.group(1));
//				int n = Integer.parseInt(matcher.group(2));
//				if (n == 1) {
//					return BuiltInTypes.getType("R" + m + "Vector");
//				}
//				return new MatrixType(name, m, n, RMATRIX);
//			}
//			return null;
//		}
//	}

	private static Map builtInTypes = new HashMap();
	private static List typeGenerators = new ArrayList();

	static {
		addType(BUILT_IN);
		addType(BOOLEAN);
		addType(REAL);
		addType(INTEGER);
		addType(NATURAL_NUM);
		addType(TIMESTEP);
		addType(STRING);
		addType(CHARACTER);

//		typeGenerators.add(new RMatrixGenerator());
//		typeGenerators.add(new RVectorGenerator());
	}
}
