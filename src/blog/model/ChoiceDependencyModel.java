/*added by cheng*/

package blog.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import blog.bn.BasicVar;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.distrib.EqualsCPD;
import blog.model.DependencyModel.Distrib;
import blog.sample.EvalContext;

public class ChoiceDependencyModel extends DependencyModel {
	private class ChoiceCPD implements CondProbDistrib{
		Object mdv;
		public ChoiceCPD(Object defaultVal){
			mdv=defaultVal;
		}
		@Override
		public double getProb(List args, Object childValue) {
			return 1;
		}

		@Override
		public double getLogProb(List args, Object childValue) {
			return 0;
		}

		@Override
		public Object sampleVal(List args, Type childType) {
			return mdv;
		}
	
	}

	public ChoiceDependencyModel(Type childType, Object defaultVal) {
		super(new LinkedList(), childType, defaultVal);
	}


	public List getClauseList() {

		return null;

	}

	public Object getDefaultValue() {
		return super.getDefaultValue();
	}

	/**
	 * Returns the CPD and argument values for the first satisfied clause in the
	 * context obtained by binding the given variables to the given objects. If
	 * any of the given objects does not exist, returns a distribution that is
	 * deterministically equal to the given <code>valueWhenArgsDontExist</code>.
	 * If the context is not complete enough to determine the first satisfied
	 * clause and its argument values, this method returns null.
	 */
	@SuppressWarnings("unused")
	public Distrib getDistribWithBinding(EvalContext context, LogicalVar[] vars,
			Object[] objs, Object valueWhenArgsDontExist) {
		DependencyModel.Distrib distrib = getDistrib(context);
		if (objs[0] instanceof blog.bn.DerivedVar){
			int a =1;
		}
		return distrib;
	}

	/**
	 * Returns the CPD and argument values for the first satisfied clause in the
	 * given context. If the context is not complete enough to determine the first
	 * satisfied clause and its argument values, then this method returns null.
	 */
	public Distrib getDistrib(EvalContext context) {
		return new Distrib(new ChoiceCPD(super.getDefaultValue()), new ArrayList());
	}


	public Clause getActiveClause(EvalContext context) {
		System.err.println("getEqualParent called in ChoiceDependencyModel");
		System.exit(1);
		return null;
	}

	public BasicVar getEqualParent(EvalContext context) {
		System.err.println("getEqualParent called in ChoiceDependencyModel");
		System.exit(1);
		return null;
	}

	public void print(PrintStream s) {
		System.err.println("getEqualParent called in ChoiceDependencyModel");
		System.exit(1);
	}

	public boolean checkTypesAndScope(Model model, Map scope) {
		boolean correct = true;
		return correct;
	}

	public int compile(LinkedHashSet callStack) {
		return 0;
	}


}