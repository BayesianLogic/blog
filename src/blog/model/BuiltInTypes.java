/*
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import blog.type.Timestep;

/**
 * Class with static methods and variables for dealing with the built-in types
 * that exist in every BLOG model. These include Boolean, Real, Integer,
 * NaturalNum, String, Character and types R<i>m</i>x<i>n</i>Matrix and
 * R<i>n</i>Vector for positive integers m, n.
 */
public class BuiltInTypes {

  public static final String INTEGER_TYPE_NAME = "Integer";
  public static final String NATURALNUM_TYPE_NAME = "NaturalNum"; // for the
                                                                  // moment just
                                                                  // integer
  public static final String REAL_TYPE_NAME = "Real";
  public static final String NULL_TYPE_NAME = "**NullType**";
  public static final String STRING_TYPE_NAME = "String";
  public static final String TIMESTEP_TYPE_NAME = "Timestep";
  public static final String BOOLEAN_TYPE_NAME = "Boolean";
  public static final String SET_TYPE_NAME = "Set";
  public static final String MAP_TYPE_NAME = "Map";

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
  public static final Type NULL = new Type(NULL_TYPE_NAME) {
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
  public static final Type REAL = new Type(REAL_TYPE_NAME, BUILT_IN, true) {
    public Term getCanonicalTerm(Object obj) {
      if (!(obj instanceof Number)) {
        if (obj == Model.NULL) {
          return new FuncAppTerm(BuiltInFunctions.NULL);
        }
        throw new IllegalArgumentException("Object " + obj
            + " is not of type Real.");
      }

      double value = ((Number) obj).doubleValue();
      NonRandomFunction c = BuiltInFunctions.getLiteral(String.valueOf(value),
          this, new Double(value));
      return new FuncAppTerm(c);
    }
  };

  /**
   * Type for integers. This is a subtype of the real numbers. Objects of this
   * type are represented as java.lang.Integer objects.
   */
  public static final Type INTEGER = new IntegralType(INTEGER_TYPE_NAME, REAL);

  /**
   * Type for natural numbers. This is a subtype of the integers. Objects of
   * this type are represented as java.lang.Integer objects.
   */
  // public static final Type NATURAL_NUM = new IntegralType("NaturalNum",
  // INTEGER);
  // modified by leili, now Integer type and natural_num are the same
  public static final Type NATURAL_NUM = INTEGER;

  /**
   * Type for finite strings of Unicode characters. Objects of this type are
   * represented as java.lang.String objects.
   */
  public static final Type STRING = new Type(STRING_TYPE_NAME, BUILT_IN, true);

  /**
   * Type for individual unicode characters. Objects of this type are
   * represented as java.lang.Character objects.
   */
  public static final Type CHARACTER = new CharacterType();

  /**
   * Type for arrays of elements of a given type. Objects of this type
   * are represented as blog.common.numerical.MatrixLib objects.
   */
  public static final Type ARRAY = new ArrayType(BUILT_IN);
  public static final Type ARRAY_REAL = new ArrayType(REAL);
  public static final Type ARRAY_REAL_2 = new ArrayType(REAL, 2);
  public static final Type REAL_MATRIX = new RealMatrixType();

  public static final Type ARRAY_INTEGER = new ArrayType(INTEGER);
  public static final Type ARRAY_INTEGER_2 = new ArrayType(INTEGER, 2);

  /**
   * Type for object sets. Objects of this type are represented as
   * java.util.ArrayList objects.
   */
  public static final Type SET = new Type(SET_TYPE_NAME, BUILT_IN);

  /**
   * Type for mappings between any two types. Objects of this type
   * are represented as java.util.Map objects.
   */
  public static final Type MAP = new Type(MAP_TYPE_NAME, BUILT_IN);

  // /**
  // * Type for lists of elements of a given type. Objects of this type
  // * are represented as java.util.ArrayList objects.
  // */
  // public static final Type LIST = new Type("List", BUILT_IN, true);

  /**
   * Type for timesteps. Objects of this type are represented as Timestep
   * objects, which extend Number (but not Integer, because Integer is final).
   */
  public static Type TIMESTEP = new Type(TIMESTEP_TYPE_NAME, REAL, true) {
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
        throw new IllegalArgumentException("Object " + obj + " not of type "
            + this);
      }
      NonRandomFunction c = BuiltInFunctions.getLiteral(obj.toString(), this,
          obj);
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
      // in any form whatsoever
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
      super(BOOLEAN_TYPE_NAME, BUILT_IN);
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
        throw new IllegalArgumentException("Object " + obj + " not of type "
            + this);
      }
      NonRandomFunction c = BuiltInFunctions.getLiteral(String.valueOf(obj),
          this, obj);
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
        throw new IllegalArgumentException("Object " + obj + " is not of type "
            + this);
      }
      NonRandomFunction c = BuiltInFunctions.getLiteral(String.valueOf(obj),
          this, obj);
      return new FuncAppTerm(c);
    }
  }

  private static class CharacterType extends Type {
    public CharacterType() {
      super("Character", BUILT_IN);
    }

    public Object getGuaranteedObject(int index) {
      if ((index >= Character.MIN_VALUE) && (index <= Character.MAX_VALUE)) {
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

  // /**
  // * ListGenerator - A generator object for list types with associated
  // * generics
  // *
  // * @author awong
  // */
  // private static class ListGenerator implements TypeGenerator {
  // private static final Pattern MATRIX_PATTERN = Pattern
  // .compile("List<([A-Za-z]*)>");
  //
  // @Override
  // public Type generateIfMatches(String name) {
  // Matcher matcher = MATRIX_PATTERN.matcher(name);
  // if (matcher.matches()) {
  // int innerType = Integer.parseInt(matcher.group(1));
  // // if (n == 1) {
  // // return BuiltInTypes.getType("R" + m + "Vector");
  // // }
  // // return new MatrixType(name, m, n, RMATRIX);
  // }
  // return null;
  // }
  // }

  /**
   * Real Matrix type.
   *
   * Row vectors are represented as matrices with one row.
   * Column vectors are represented as matrices with one column.
   */
  private static class RealMatrixType extends Type {
    public RealMatrixType() {
      super("RealMatrix", BUILT_IN);
    }
  }

  /*
   * A reference to BuiltInTypes is necessary to load the class,
   * thereby loading built-in types into the type hierarchy
   */
  public static void ping() {
  }

  private static Map<String, Type> builtInTypes = new HashMap<String, Type>();
  private static List typeGenerators = new ArrayList();

  static {
    addType(BUILT_IN);
    addType(BOOLEAN);
    addType(REAL);
    addType(INTEGER);
    addType(TIMESTEP);
    addType(STRING);
    addType(CHARACTER);
    addType(ARRAY_REAL);
    addType(ARRAY_REAL_2);
    addType(ARRAY_INTEGER);
    addType(ARRAY_INTEGER_2);

    // special treatment for NaturalNum
    // builtInTypes.put(NATURALNUM_TYPE_NAME, NATURAL_NUM);

    // TODO
    // addType(LIST);

    // typeGenerators.add(new ListGenerator());
    // typeGenerators.add(new RMatrixGenerator());
    // typeGenerators.add(new RVectorGenerator());
  }
}
