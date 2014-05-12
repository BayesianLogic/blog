/*
 * Copyright (c) 2005 - 2012, Regents of the University of California
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.objgen.ObjectSet;
import blog.type.Timestep;

/**
 * Class with static methods and constants for built-in non-random functions
 * (including built-in constants). This class cannot be instantiated.
 * 
 * @author unknown
 * @author leili
 * @date 2/11/2014
 */
public class BuiltInFunctions {
  /**
   * internal names for builtin functions
   */
  public static final String PLUS_NAME = "__PLUS";
  public static final String MINUS_NAME = "__MINUS";
  public static final String MULT_NAME = "__MULT";
  public static final String DIV_NAME = "__DIV";
  public static final String MOD_NAME = "__MOD";
  public static final String POWER_NAME = "__POWER";
  public static final String SUB_MAT_NAME = "__SUB_MAT";
  public static final String SUB_ARRAY_NAME = "__SUB_ARRAY";
  public static final String GT_NAME = "__GREATERTHAN";
  public static final String GEQ_NAME = "__GREATERTHANOREQUAL";
  public static final String LT_NAME = "__LESSTHAN";
  public static final String LEQ_NAME = "__LESSTHANOREQUAL";

  // can be called by user
  public static final String SUCC_NAME = "Succ";
  public static final String PRED_NAME = "Pred";
  public static final String PREV_NAME = "Prev";
  public static final String INV_NAME = "inv";
  public static final String DET_NAME = "det";
  public static final String IS_EMPTY_NAME = "IsEmptyString";
  // public static final String CONCAT_NAME = "Concat"; //Concat replaced by +
  public static final String MIN_NAME = "min";
  public static final String MAX_NAME = "max";
  public static final String ROUND_NAME = "round";
  public static final String TRANSPOSE_NAME = "transpose";
  public static final String SIN_NAME = "sin";
  public static final String COS_NAME = "cos";
  public static final String TAN_NAME = "tan";
  public static final String ATAN2_NAME = "atan2";
  public static final String COL_SUM_NAME = "sum";
  public static final String VSTACK_NAME = "vstack";
  public static final String EYE_NAME = "eye";
  public static final String ZEROS_NAME = "zeros";
  public static final String ONES_NAME = "ones";
  public static final String TOINT_NAME = "toInt";
  public static final String TOREAL_NAME = "toReal";
  public static final String ABS_NAME = "abs";

  
  public static final String CONCATE_NAME = "concate";
  
  
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
   * Constant that denotes PI.
   */
  public static final NonRandomFunction PI;

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
   * The function on integers <code>x<code>, <code>y</code> that returns x * y.
   */
  public static NonRandomFunction MULT;

  /**
   * The function on integers <code>x<code>, <code>y</code> that returns x / y.
   */
  public static NonRandomFunction DIV;

  /**
   * The function on integers <code>x<code>, <code>y</code> that returns x % y.
   */
  public static NonRandomFunction MOD;

  /**
   * The function on reals <code>x<code>, <code>y</code> that returns x + y.
   */
  public static NonRandomFunction RPLUS;

  /**
   * The function on reals <code>x<code>, <code>y</code> that returns x - y.
   */
  public static NonRandomFunction RMINUS;

  /**
   * The function on reals <code>x<code>, <code>y</code> that returns x * y.
   */
  public static NonRandomFunction RMULT;

  /**
   * The function on reals <code>x<code>, <code>y</code> that returns x ^ y.
   */
  public static NonRandomFunction POWER;

  /**
   * The function on reals <code>x<code>, <code>y</code> that returns x / y.
   */
  public static NonRandomFunction RDIV;

  /**
   * RealMatrix + RealMatrix returns RealMatrix
   */
  public static NonRandomFunction PLUS_MAT;

  /**
   * RealMatrix - RealMatrix returns RealMatrix
   */
  public static NonRandomFunction MINUS_MAT;

  /**
   * RealMatrix * RealMatrix returns RealMatrix (matrix multiplication)
   */
  public static NonRandomFunction TIMES_MAT;

  /**
   * RealMatrix * Real returns RealMatrix
   */
  public static NonRandomFunction TIMES_MAT_SCALAR;

  /**
   * Real * RealMatrix returns RealMatrix
   */
  public static NonRandomFunction TIMES_SCALAR_MAT;

  /**
   * inv(RealMatrix) returns RealMatrix (matrix inverse)
   */
  public static NonRandomFunction INV_MAT;

  /**
   * det(RealMatrix) returns Real (matrix determinant)
   */
  public static NonRandomFunction DET_MAT;

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
   * RealMatrix[Integer] returns RealMatrix (i-th row of matrix)
   */
  public static NonRandomFunction SUB_MAT;

  /**
   * RealArray[Integer] returns Real (i-th element of array)
   */
  public static NonRandomFunction SUB_REAL_ARRAY;

  /**
   * IntegerArray[Integer] returns Integer (i-th element of array)
   */
  public static NonRandomFunction SUB_INT_ARRAY;

  /**
   * a function on Set <code>x</code> returns the minimal value from the set
   */
  public static NonRandomFunction MIN;

  /**
   * a function on Set <code>x</code> returns the maximal value from the set
   */
  public static NonRandomFunction MAX;

  /**
   * a function on Real <code>x</code> returns the nearest integer to
   * <code>x</code>
   */
  public static NonRandomFunction ROUND;

  /**
   * transpose(RealMatrix) returns RealMatrix
   */
  public static NonRandomFunction TRANSPOSE_REAL_MAT;

  /**
   * transpose(IntegerMatrix) returns IntegerMatrix
   */
  public static NonRandomFunction TRANSPOSE_INT_MAT;

  /**
   * A function that takes a string and returns true if the string is empty.
   */
  public static NonRandomFunction IS_EMPTY_STRING;

  /**
   * Take scalar <code>x</code> (in radians) and return <code>sin(x)</code>.
   */
  public static NonRandomFunction SIN;

  /**
   * Take scalar <code>x</code> (in radians) and return <code>cos(x)</code>.
   */
  public static NonRandomFunction COS;

  /**
   * Take scalar <code>x</code> (in radians) and return <code>tan(x)</code>.
   */
  public static NonRandomFunction TAN;

  /**
   * Take scalars <code>x</code> and <code>y</code> and return
   * <code>atan2(y, x)</code>.
   */
  public static NonRandomFunction ATAN2;

  /**
   * Take RealMatrix x and return RealMatrix y where elements are the sum of
   * columns of x.
   */
  public static NonRandomFunction COL_SUM;

  /**
   * Take RealMatrix x and y and return RealMatrix z which is the concatenation
   * [x; y].
   */
  public static NonRandomFunction VSTACK;

  /**
   * Return an identity matrix.
   */
  public static NonRandomFunction EYE;

  /**
   * Return a matrix of zeros.
   */
  public static NonRandomFunction ZEROS;

  /**
   * Return a matrix of ones.
   */
  public static NonRandomFunction ONES;

  /**
   * The function takes an Integer, an Real or a MatrixLib with single element,
   * and converts it to an Integer
   */
  public static NonRandomFunction TO_INT;

  /**
   * The function takes a Real or a MatrixLib with single element,
   * and converts it to a Real
   */
  public static NonRandomFunction TO_REAL;

  
  /*
   * Take a list of arguments, and convert them to a row-vector
   * TODO: Currently only accept double elements as input arguments
   */
  public static TemplateFunction CONCATE;
  
  /**
   * Return the absolute value of a Real value.
   */
  public static NonRandomFunction ABS;

  private BuiltInFunctions() {
    // prevent instantiation
  }

  /**
   * Returns the built-in function (or constant) with the given signature.
   * Returns null if there is no such built-in function, or if the given name is
   * a numeric, character, or string literal that is only created as needed by
   * the parser.
   */
  public static NonRandomFunction getFunction(FunctionSignature sig) {

    // TODO change to another hashmap from signature to function
    List funcsWithName = (List) functions.get(sig.getName());
    if (funcsWithName != null) {
      for (Iterator iter = funcsWithName.iterator(); iter.hasNext();) {
        NonRandomFunction f = (NonRandomFunction) iter.next();
        if (Arrays.equals(sig.getArgTypes(), f.getArgTypes())) {
          return f;
        }
      }
    }

    // find template functions compatible with sig
    // TODO: to add more template functions
    for(int i = 0; i < templateFunctions.size(); ++ i) {
    	if(templateFunctions.get(i).getName().equals(sig.getName())) {
    		NonRandomFunction f = templateFunctions.get(i).getSpecificFunc(sig.getArgTypes());
    		if(f != null) return f;
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
    NonRandomFunction f = getFunction(new FunctionSignature(name));
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

  private static void addTemplate(TemplateFunction t) {
	  templateFunctions.add(t);
  }
  
  static ArrayList<TemplateFunction> templateFunctions = new ArrayList<TemplateFunction>();
  
  static {
    // Add non-random constants
    NULL = getLiteral("null", BuiltInTypes.NULL, Model.NULL);
    ZERO = getLiteral("0", BuiltInTypes.INTEGER, new Integer(0));
    ONE = getLiteral("1", BuiltInTypes.INTEGER, new Integer(1));
    EPOCH = getLiteral("@0", BuiltInTypes.TIMESTEP, Timestep.at(0));
    PI = getLiteral("pi", BuiltInTypes.REAL, new Double(Math.PI));

    // Add non-random functions from (real x real) to Boolean
    List<Type> argTypes = new ArrayList<Type>();
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
    LT = new NonRandomFunction(LT_NAME, argTypes, retType, ltInterp);
    addFunction(LT);

    FunctionInterp leqInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double arg1 = ((Number) args.get(0)).doubleValue();
        double arg2 = ((Number) args.get(1)).doubleValue();
        return Boolean.valueOf(arg1 <= arg2);
      }
    };
    LEQ = new NonRandomFunction(LEQ_NAME, argTypes, retType, leqInterp);
    addFunction(LEQ);

    FunctionInterp gtInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double arg1 = ((Number) args.get(0)).doubleValue();
        double arg2 = ((Number) args.get(1)).doubleValue();
        return Boolean.valueOf(arg1 > arg2);
      }
    };
    GT = new NonRandomFunction(GT_NAME, argTypes, retType, gtInterp);
    addFunction(GT);

    FunctionInterp geqInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double arg1 = ((Number) args.get(0)).doubleValue();
        double arg2 = ((Number) args.get(1)).doubleValue();
        return Boolean.valueOf(arg1 >= arg2);
      }
    };
    GEQ = new NonRandomFunction(GEQ_NAME, argTypes, retType, geqInterp);
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
    SUCC = new NonRandomFunction(SUCC_NAME, argTypes, retType, succInterp);
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
    PRED = new NonRandomFunction(PRED_NAME, argTypes, retType, predInterp);
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
    PLUS = new NonRandomFunction(PLUS_NAME, argTypes, retType, plusInterp);
    addFunction(PLUS);

    // Multiply non-random functions from (integer x integer) to integer
    FunctionInterp multInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer arg1 = (Integer) args.get(0);
        Integer arg2 = (Integer) args.get(1);
        return new Integer(arg1.intValue() * arg2.intValue());
      }
    };
    MULT = new NonRandomFunction(MULT_NAME, argTypes, retType, multInterp);
    addFunction(MULT);

    FunctionInterp minusInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer arg1 = (Integer) args.get(0);
        Integer arg2 = (Integer) args.get(1);
        return new Integer(arg1.intValue() - arg2.intValue());
      }
    };
    MINUS = new NonRandomFunction(MINUS_NAME, argTypes, retType, minusInterp);
    addFunction(MINUS);

    // Divide non-random functions from (integer x integer) to integer
    FunctionInterp divInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer arg1 = (Integer) args.get(0);
        Integer arg2 = (Integer) args.get(1);
        return new Integer(arg1.intValue() / arg2.intValue());
      }
    };
    DIV = new NonRandomFunction(DIV_NAME, argTypes, retType, divInterp);
    addFunction(DIV);

    // Mod non-random functions from (integer x integer) to integer
    FunctionInterp modInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer arg1 = (Integer) args.get(0);
        Integer arg2 = (Integer) args.get(1);
        return new Integer(arg1.intValue() % arg2.intValue());
      }
    };
    MOD = new NonRandomFunction(MOD_NAME, argTypes, retType, modInterp);
    addFunction(MOD);

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
    RPLUS = new NonRandomFunction(PLUS_NAME, argTypes, retType, rplusInterp);
    addFunction(RPLUS);

    FunctionInterp rminusInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double arg1 = (Double) args.get(0);
        Double arg2 = (Double) args.get(1);
        return new Double(arg1.doubleValue() - arg2.doubleValue());
      }
    };
    RMINUS = new NonRandomFunction(MINUS_NAME, argTypes, retType, rminusInterp);
    addFunction(RMINUS);

    FunctionInterp rmultInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Number arg1 = (Number) args.get(0);
        Number arg2 = (Number) args.get(1);
        return new Double(arg1.doubleValue() * arg2.doubleValue());
      }
    };
    RMULT = new NonRandomFunction(MULT_NAME, argTypes, retType, rmultInterp);
    addFunction(RMULT);

    FunctionInterp rdivInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Number arg1 = (Number) args.get(0);
        Number arg2 = (Number) args.get(1);
        return new Double(arg1.doubleValue() / arg2.doubleValue());
      }
    };
    RDIV = new NonRandomFunction(DIV_NAME, argTypes, retType, rdivInterp);
    addFunction(RDIV);

    FunctionInterp powerInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Number arg1 = (Number) args.get(0);
        Number arg2 = (Number) args.get(1);
        return new Double(Math.pow(arg1.doubleValue(), arg2.doubleValue()));
      }
    };
    POWER = new NonRandomFunction(POWER_NAME, argTypes, retType, powerInterp);
    addFunction(POWER);

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
    PREV = new NonRandomFunction(PREV_NAME, argTypes, retType, prevInterp);
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
    CONCAT = new NonRandomFunction(PLUS_NAME, argTypes, retType, concatInterp);
    addFunction(CONCAT);

    // Add non-random functions from string to Boolean
    argTypes.clear();
    argTypes.add(BuiltInTypes.STRING);
    retType = BuiltInTypes.BOOLEAN;

    FunctionInterp isEmptyStringInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        return Boolean.valueOf(((String) args.get(0)).length() == 0);
      }
    };
    IS_EMPTY_STRING = new NonRandomFunction(IS_EMPTY_NAME, argTypes, retType,
        isEmptyStringInterp);
    addFunction(IS_EMPTY_STRING);

    // Add non-random functions from (RealMatrix x int) to RealMatrix
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.REAL_MATRIX;

    // Return the i-th row of the matrix,
    // or if the matrix has a single row, return the i-th column.
    FunctionInterp subMatInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat = (MatrixLib) args.get(0);
        int i = (Integer) args.get(1);
        if (mat.numRows() > 1) {
          return mat.sliceRow(i);
        } else {
          return mat.elementAt(0, i);
        }
      }
    };
    SUB_MAT = new NonRandomFunction(SUB_MAT_NAME, argTypes, retType,
        subMatInterp);
    addFunction(SUB_MAT);

    // Array subscription (aka indexing)
    FunctionInterp subVecInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat = (MatrixLib) args.get(0);
        int i = (Integer) args.get(1);
        return mat.elementAt(0, i);
      }
    };

    // Array indexing for Real arrays:
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_ARRAY);
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.REAL;
    SUB_REAL_ARRAY = new NonRandomFunction(SUB_ARRAY_NAME, argTypes, retType,
        subVecInterp);
    addFunction(SUB_REAL_ARRAY);

    // Array indexing for Integer arrays:
    argTypes.clear();
    argTypes.add(BuiltInTypes.INTEGER_ARRAY);
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.INTEGER;
    SUB_INT_ARRAY = new NonRandomFunction(SUB_ARRAY_NAME, argTypes, retType,
        subVecInterp);
    addFunction(SUB_INT_ARRAY);

    // Add non-random functions from (RealMatrix x RealMatrix) to RealMatrix
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;

    // matrix plus
    FunctionInterp matPlusInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat1 = (MatrixLib) args.get(0);
        MatrixLib mat2 = (MatrixLib) args.get(1);
        return mat1.plus(mat2);
      }
    };
    PLUS_MAT = new NonRandomFunction(PLUS_NAME, argTypes, retType,
        matPlusInterp);
    addFunction(PLUS_MAT);

    // matrix minus
    FunctionInterp matMinusInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat1 = (MatrixLib) args.get(0);
        MatrixLib mat2 = (MatrixLib) args.get(1);
        return mat1.minus(mat2);
      }
    };
    MINUS_MAT = new NonRandomFunction(MINUS_NAME, argTypes, retType,
        matMinusInterp);
    addFunction(MINUS_MAT);

    // matrix multiplication
    FunctionInterp matTimesInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat1 = (MatrixLib) args.get(0);
        MatrixLib mat2 = (MatrixLib) args.get(1);
        return mat1.timesMat(mat2);
      }
    };
    TIMES_MAT = new NonRandomFunction(MULT_NAME, argTypes, retType,
        matTimesInterp);
    addFunction(TIMES_MAT);

    // Add non-random functions from (RealMatrix x Real) to RealMatrix
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    argTypes.add(BuiltInTypes.REAL);
    retType = BuiltInTypes.REAL_MATRIX;

    // matrix scalar multiplication
    FunctionInterp matScalarTimesInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double val = (Double) args.get(1);
        MatrixLib mat = (MatrixLib) args.get(0);
        return mat.timesScale(val);
      }
    };
    TIMES_MAT_SCALAR = new NonRandomFunction(MULT_NAME, argTypes, retType,
        matScalarTimesInterp);
    addFunction(TIMES_MAT_SCALAR);

    // Add non-random functions from (Real x RealMatrix) to RealMatrix
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL);
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;

    // scalar matrix multiplication
    FunctionInterp scalarMatTimesInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double val = (Double) args.get(0);
        MatrixLib mat2 = (MatrixLib) args.get(1);
        return mat2.timesScale(val);
      }
    };
    TIMES_SCALAR_MAT = new NonRandomFunction(MULT_NAME, argTypes, retType,
        scalarMatTimesInterp);
    addFunction(TIMES_SCALAR_MAT);

    // Add non-random functions from RealMatrix to RealMatrix
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;

    // matrix inverse
    FunctionInterp matInverseInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat1 = (MatrixLib) args.get(0);
        return mat1.inverse();
      }
    };
    INV_MAT = new NonRandomFunction(INV_NAME, argTypes, retType,
        matInverseInterp);
    addFunction(INV_MAT);

    // Add non-random functions from RealMatrix to Real
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL;

    // matrix determinant
    FunctionInterp matDeterminantInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib mat1 = (MatrixLib) args.get(0);
        return mat1.det();
      }
    };
    DET_MAT = new NonRandomFunction(DET_NAME, argTypes, retType,
        matDeterminantInterp);
    addFunction(DET_MAT);

    // now adding support for min of set
    argTypes.clear();
    argTypes.add(BuiltInTypes.SET);
    retType = BuiltInTypes.INTEGER;
    FunctionInterp minInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        // TODO
        ObjectSet s = (ObjectSet) args.get(0);
        Iterator oi = s.iterator();
        if (!oi.hasNext())
          return Model.NULL;

        Comparable o = (Comparable) oi.next();
        while (oi.hasNext()) {
          Comparable no = (Comparable) oi.next();
          if (no.compareTo(o) < 0)
            o = no;
        }

        return o;
      }
    };

    MIN = new NonRandomFunction(MIN_NAME, argTypes, retType, minInterp);
    addFunction(MIN);

    FunctionInterp maxInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        // TODO
        ObjectSet s = (ObjectSet) args.get(0);
        Iterator oi = s.iterator();
        if (!oi.hasNext())
          return Model.NULL;

        Comparable o = (Comparable) oi.next();
        while (oi.hasNext()) {
          Comparable no = (Comparable) oi.next();
          if (no.compareTo(o) > 0)
            o = no;
        }

        return o;
      }
    };

    MAX = new NonRandomFunction(MAX_NAME, argTypes, retType, maxInterp);
    addFunction(MAX);

    // Add non-random functions from Real to Integer
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL);
    retType = BuiltInTypes.INTEGER;

    FunctionInterp roundInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double num = (Double) args.get(0);
        return Math.round(num);
      }
    };

    ROUND = new NonRandomFunction(ROUND_NAME, argTypes, retType, roundInterp);
    addFunction(ROUND);

    // Transpose function for Real matrices
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;

    FunctionInterp transposeInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib matrix = (MatrixLib) args.get(0);
        return matrix.transpose();
      }
    };

    TRANSPOSE_REAL_MAT = new NonRandomFunction(TRANSPOSE_NAME, argTypes,
        retType, transposeInterp);
    addFunction(TRANSPOSE_REAL_MAT);

    // Transpose function for Integer matrices (uses the same FunctionInterp
    // above)
    // Does not work yet ("java.util.ArrayList cannot be cast to
    // blog.common.numerical.MatrixLib")
    argTypes.clear();
    argTypes.add(BuiltInTypes.INTEGER_MATRIX);
    retType = BuiltInTypes.INTEGER_MATRIX;
    TRANSPOSE_INT_MAT = new NonRandomFunction(TRANSPOSE_NAME, argTypes,
        retType, transposeInterp);
    addFunction(TRANSPOSE_INT_MAT);

    // Trigonometric functions on scalars:
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL);
    retType = BuiltInTypes.REAL;

    FunctionInterp sinInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double radians = (Double) args.get(0);
        return Math.sin(radians);
      }
    };
    SIN = new NonRandomFunction(SIN_NAME, argTypes, retType, sinInterp);
    addFunction(SIN);

    FunctionInterp cosInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double radians = (Double) args.get(0);
        return Math.cos(radians);
      }
    };
    COS = new NonRandomFunction(COS_NAME, argTypes, retType, cosInterp);
    addFunction(COS);

    FunctionInterp tanInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double radians = (Double) args.get(0);
        return Math.tan(radians);
      }
    };
    TAN = new NonRandomFunction(TAN_NAME, argTypes, retType, tanInterp);
    addFunction(TAN);

    FunctionInterp atan2Interp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Double y = (Double) args.get(0);
        Double x = (Double) args.get(1);
        return Math.atan2(y, x);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL);
    argTypes.add(BuiltInTypes.REAL);
    retType = BuiltInTypes.REAL;
    ATAN2 = new NonRandomFunction(ATAN2_NAME, argTypes, retType, atan2Interp);
    addFunction(ATAN2);

    FunctionInterp colSumInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib matrix = (MatrixLib) args.get(0);
        return matrix.columnSum();
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;
    COL_SUM = new NonRandomFunction(COL_SUM_NAME, argTypes, retType,
        colSumInterp);
    addFunction(COL_SUM);

    FunctionInterp vstackInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        MatrixLib a = (MatrixLib) args.get(0);
        MatrixLib b = (MatrixLib) args.get(1);
        return MatrixFactory.vstack(a, b);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    argTypes.add(BuiltInTypes.REAL_MATRIX);
    retType = BuiltInTypes.REAL_MATRIX;
    VSTACK = new NonRandomFunction(VSTACK_NAME, argTypes, retType, vstackInterp);
    addFunction(VSTACK);

    
  //TODO: to complete CONCATE
    CONCATE = new TemplateFunction(CONCATE_NAME){

		@Override
		public NonRandomFunction getSpecificFunc(Type[] argTypes) {
			int n = argTypes.length;
			if(n < 1) return null;
			
			for(int i=1;i<n;++i)
				if(!argTypes[i].equals(argTypes[0])) return null;
			
			/*
			 * TODO: Currently only support convert elements to Real Matrix!!!
			 */
			for(int i=0;i<n;++i)
				if(!argTypes[i].isSubtypeOf(BuiltInTypes.REAL)) return null;
			
			FunctionInterp concateInterp = new AbstractFunctionInterp() {
			      public Object getValue(List args) {
			    	  int n = args.size();
			    	  double[][] val = new double[1][n];
			    	  for(int i=0;i<n;++i)
			    		  val[0][i] = (Double)args.get(i);
			    	  return MatrixFactory.fromArray(val);
			      }
			    };
			List<Type> args = new ArrayList<Type>();
			for(int i=0;i<n;++i)
				args.add(BuiltInTypes.REAL);
			 
			// TODO: convert REAL_ARRAY to REAL_MATRIX
			NonRandomFunction retFunc = 
					new NonRandomFunction(CONCATE_NAME, args, BuiltInTypes.REAL_ARRAY, concateInterp);
			return retFunc;
		}
    	
    };
    addTemplate(CONCATE);
    
    
    FunctionInterp eyeInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer size = (Integer) args.get(0);
        return MatrixFactory.eye(size);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.REAL_MATRIX;
    EYE = new NonRandomFunction(EYE_NAME, argTypes, retType, eyeInterp);
    addFunction(EYE);

    FunctionInterp zerosInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer rows = (Integer) args.get(0);
        Integer cols = (Integer) args.get(1);
        return MatrixFactory.zeros(rows, cols);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.INTEGER);
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.REAL_MATRIX;
    ZEROS = new NonRandomFunction(ZEROS_NAME, argTypes, retType, zerosInterp);
    addFunction(ZEROS);

    FunctionInterp onesInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Integer rows = (Integer) args.get(0);
        Integer cols = (Integer) args.get(1);
        return MatrixFactory.ones(rows, cols);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.INTEGER);
    argTypes.add(BuiltInTypes.INTEGER);
    retType = BuiltInTypes.REAL_MATRIX;
    ONES = new NonRandomFunction(ONES_NAME, argTypes, retType, onesInterp);
    addFunction(ONES);

    FunctionInterp toIntInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Object obj = args.get(0);
        if (obj instanceof Number) {
          return ((Number) obj).intValue();
        } else if (obj instanceof Boolean) {
          return ((Boolean) obj).booleanValue() ? 1 : 0;
        } else if (obj instanceof MatrixLib) {
          return (int) ((MatrixLib) obj).elementAt(0, 0);
        } else {
          System.err.println(obj.toString()
              + " cannot be converted to Integer ");
          return 0;
        }
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.BUILT_IN);
    retType = BuiltInTypes.INTEGER;
    TO_INT = new NonRandomFunction(TOINT_NAME, argTypes, retType, toIntInterp);
    addFunction(TO_INT);

    FunctionInterp toRealInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        Object obj = args.get(0);
        if (obj instanceof Number) {
          return ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
          return ((Boolean) obj).booleanValue() ? 1 : 0;
        } else if (obj instanceof MatrixLib) {
          return ((MatrixLib) obj).elementAt(0, 0);
        } else {
          System.err.println(obj.toString()
              + " cannot be converted to Real");
          return 0;
        }
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.BUILT_IN);
    retType = BuiltInTypes.REAL;
    TO_REAL = new NonRandomFunction(TOREAL_NAME, argTypes, retType, toRealInterp);
    addFunction(TO_REAL);

    FunctionInterp absInterp = new AbstractFunctionInterp() {
      public Object getValue(List args) {
        double val = ((Number) args.get(0)).doubleValue();
        return Math.abs(val);
      }
    };
    argTypes.clear();
    argTypes.add(BuiltInTypes.REAL);
    retType = BuiltInTypes.REAL;
    ABS = new NonRandomFunction(ABS_NAME, argTypes, retType, absInterp);
    addFunction(ABS);
  };
}


/*
 * Author: yiwu
 * Date: 2014-4-22
 */
abstract class TemplateFunction {
	private String name;
	public TemplateFunction(String _name) {
		name = _name;
	}
	public String getName() {
		return name;
	}
	public abstract NonRandomFunction getSpecificFunc(Type[] argTypes);
}
