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

import blog.EqualsCPD;
import blog.EvalContext;
import blog.bn.BasicVar;
import blog.bn.RandFuncAppVar;
import blog.distrib.CondProbDistrib;

/**
 * Represents a clause in dependency statements and number statements.
 * 
 * Each clause has a predicate, a conditional probability distribution (CPD),
 * and a list of arguments to this conditional probability distribution. Each
 * argument is assumed to be of class ArgSpec. If the condition is checked and
 * turns out to be true, then the arguments are evaluated and the CPD is used to
 * sample a value for these arguments.
 */
public class Clause {

	/**
	 * Creates a new clause.
	 * 
	 * @param cond
	 *          the condition under which this clause applies
	 * @param cpdClass
	 *          the class of the conditional probability distribution used in this
	 *          clause
	 * @param cpdParams
	 *          List of ArgSpec objects representing CPD parameters. These must be
	 *          non-random and must contain no free variables.
	 * @param cpdArgs
	 *          List of ArgSpec objects whose denotations will be passed to the
	 *          CPD each time it is invoked
	 */
	public Clause(Formula cond, Class cpdClass, List cpdParams, List cpdArgs) {
		this.cond = cond;
		this.cpdClass = cpdClass;
		this.cpdParams = cpdParams;
		this.cpd = null;
		this.cpdArgs = cpdArgs;
	}

	/**
	 * Creates a new clause using the given CondProbDistrib object.
	 * 
	 * @param cond
	 *          the condition under which this clause applies
	 * @param cpd
	 *          conditional probability distribution for this clause
	 * @param cpdArgs
	 *          List of ArgSpec objects whose denotations will be passed to the
	 *          CPD each time it is invoked
	 */
	public Clause(Formula cond, CondProbDistrib cpd, List cpdArgs) {
		this.cond = cond;
		this.cpdClass = cpd.getClass();
		this.cpdParams = Collections.EMPTY_LIST;
		this.cpd = cpd;
		this.cpdArgs = cpdArgs;
	}

	public Formula getCond() {

		return cond;

	}

	public Class getCPDClass() {
		if (cpd != null) {
			return cpd.getClass();
		}
		return cpdClass;
	}

	public CondProbDistrib getCPD() {

		return cpd;

	}

	/**
	 * @return List of ArgSpec objects
	 */
	public List getArgs() {

		return cpdArgs;

	}

	/**
	 * Returns this clause's CPD, and the values of this clause's arguments
	 * evaluated in the given context. Returns null if the partial world in the
	 * given context is not complete enough to evaluate the arguments.
	 */
	public DependencyModel.Distrib getDistrib(EvalContext context) {
		context.pushEvaluee(this);
		List argValues = new ArrayList();

		for (Iterator iter = cpdArgs.iterator(); iter.hasNext();) {
			ArgSpec argSpec = (ArgSpec) iter.next();
			Object argValue = argSpec.evaluate(context);
			if (argValue == null) {
				break; // CPD arg not determined
			}
			argValues.add(argValue);
		}

		context.popEvaluee();
		if (argValues.size() == cpdArgs.size()) {
			// all CPD args were determined
			return new DependencyModel.Distrib(cpd, argValues);
		}
		return null;
	}

	/**
	 * If, in the given context, this clause specifies that the child is equal to
	 * one of its parents, then this method returns that "equal parent". Otherwise
	 * it returns null. This method also returns null if the given context is not
	 * complete enough to determine the equal parent.
	 */
	public BasicVar getEqualParent(EvalContext context) {
		if (cpd instanceof EqualsCPD) {
			ArgSpec arg = (ArgSpec) cpdArgs.get(0);
			if (arg instanceof FuncAppTerm) {
				FuncAppTerm t = (FuncAppTerm) arg;
				if (t.getFunction() instanceof RandomFunction) {
					Object[] argValues = new Object[t.getArgs().length];
					for (int i = 0; i < t.getArgs().length; ++i) {
						argValues[i] = t.getArgs()[i].evaluate(context);
						if (argValues[i] == null) {
							return null;
						}
					}
					return new RandFuncAppVar((RandomFunction) t.getFunction(), argValues);
				}
			}
		}

		return null;
	}

	public boolean checkTypesAndScope(Model model, Map scope, Type childType) {
		boolean correct = true;

		if (!cond.checkTypesAndScope(model, scope)) {
			correct = false;
		}

		for (Iterator iter = cpdArgs.iterator(); iter.hasNext();) {
			if (!((ArgSpec) iter.next()).checkTypesAndScope(model, scope)) {
				correct = false;
			}
		}

		// for EqualsCPD, we can do additional checking
		if (correct && (cpdClass == EqualsCPD.class)) {
			if (cpdArgs.size() != 1) {
				System.err.println(getLocation()
						+ "EqualsCPD takes exactly one argument");
				correct = false;
			} else {
				ArgSpec arg = (ArgSpec) cpdArgs.get(0);
				Type argType = null;
				if (arg instanceof Term) {
					argType = ((Term) arg).getType();
				} else if (arg instanceof Formula) {
					argType = BuiltInTypes.BOOLEAN;
				} else if (arg instanceof CardinalitySpec) {
					argType = BuiltInTypes.NATURAL_NUM;
				} else {
					System.err.println(arg.getLocation() + ": Illegal value for "
							+ "deterministic distribution: " + arg);
					correct = false;
				}

				if ((argType != null) && !argType.isSubtypeOf(childType)) {
					System.err.println(arg.getLocation()
							+ ": Value for deterministic distribution has "
							+ "wrong type (expected " + childType + ", got " + argType + ")");
					correct = false;
				}
			}
		}

		// Type-check the CPD parameters, making sure they contain no
		// free variables
		if (cpdParams != null) {
			for (Iterator iter = cpdParams.iterator(); iter.hasNext();) {
				ArgSpec param = (ArgSpec) iter.next();
				if (!param.checkTypesAndScope(model, Collections.EMPTY_MAP)) {
					correct = false;
				}
			}

		}

		return correct;
	}

	/**
	 * Creates the CPD object for this clause (if it doesn't already exist), and
	 * does any necessary compilation on the condition and CPD arguments. Prints
	 * messages to standard error if any errors occur. Returns the number of
	 * errors encountered.
	 * 
	 * @param callStack
	 *          Set of objects whose compile methods are parents of this method
	 *          invocation. Ordered by invocation order. Used to detect cycles.
	 */
	public int compile(LinkedHashSet callStack) {
		callStack.add(this);
		int errors = 0;

		errors += cond.compile(callStack);

		for (Iterator iter = cpdArgs.iterator(); iter.hasNext();) {
			errors += ((ArgSpec) iter.next()).compile(callStack);
		}

		if (cpd == null) {
			errors += initCPD(callStack);
		}

		callStack.remove(this);
		return errors;
	}

	private int initCPD(LinkedHashSet callStack) {
		int errors = 0;

		List paramValues = new ArrayList();
		for (Iterator iter = cpdParams.iterator(); iter.hasNext();) {
			ArgSpec param = (ArgSpec) iter.next();
			int thisParamErrors = param.compile(callStack);
			errors += thisParamErrors;
			if (thisParamErrors == 0) {
				Object val = param.getValueIfNonRandom();
				if (val == null) {
					System.err.println("Error initializing CPD at " + getLocation()
							+ ": parameter " + param + " is random.  Random "
							+ "parameters should be passed as arguments.");
					++errors;
				} else {
					paramValues.add(param.getValueIfNonRandom());
				}
			}
		}

		if (errors > 0) {
			return errors; // can't compute parameters, so can't create CPD
		}

		try {
			Class[] constrArgTypes = { List.class };
			Constructor ct = cpdClass.getConstructor(constrArgTypes);
			Object[] constrArgs = { paramValues };
			cpd = (CondProbDistrib) ct.newInstance(constrArgs);
		} catch (InvocationTargetException e) {
			System.err.println("Error initializing CPD at " + getLocation() + ": "
					+ e.getCause().getClass().getName() + " ("
					+ e.getCause().getMessage() + ")");
			++errors;
		} catch (NoSuchMethodException e) {
			System.err.println("Error initializing CPD at " + getLocation() + ": "
					+ cpdClass + " does not have a "
					+ "constructor with a single argument of type " + "List.");
			++errors;
		} catch (ClassCastException e) {
			System.err.println("Error initializing CPD at " + getLocation() + ": "
					+ cpdClass + " does not implement "
					+ "the CondProbDistrib interface.");
			++errors;
		} catch (Exception e) {
			System.err.println("Error initializing CPD at " + getLocation()
					+ ": couldn't instantiate class " + cpdClass);
			++errors;
		}

		return errors;
	}

	/**
	 * Sets the location of this clause, for instance, the file name and line
	 * number where it appears. The location can be any object whose toString
	 * method returns an identifying string that can be used in error messages.
	 */
	public void setLocation(Object loc) {
		location = loc;
	}

	/**
	 * Returns the object specified by the last call to setLocation. If
	 * setLocation has not been called, returns the string "(no location)".
	 */
	public Object getLocation() {
		return location;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("if ");
		buf.append(cond);
		buf.append(" then ~ ");
		buf.append(cpdClass.getName());
		buf.append("(");

		Iterator argsIter = cpdArgs.iterator();
		if (argsIter.hasNext()) {
			buf.append(argsIter.next());
			while (argsIter.hasNext()) {
				buf.append(", ");
				buf.append(argsIter.next());
			}
		}

		buf.append(")");
		return buf.toString();
	}

	private static String DEFAULT_LOCATION = "(no location)";

	private Object location = DEFAULT_LOCATION;

	private Formula cond;
	private Class cpdClass;
	private List cpdParams; // of ArgSpec;
	private CondProbDistrib cpd;
	private List cpdArgs; // of ArgSpec
}
