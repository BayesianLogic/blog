package blog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.common.*;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.BuiltInTypes;
import blog.model.ConstantInterp;
import blog.model.DecisionEvidenceStatement;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.NonRandomFunction;
import blog.model.POP;
import blog.model.RandomFunction;
import blog.model.SymbolEvidenceStatement;
import blog.model.Type;
import blog.model.ValueEvidenceStatement;
import blog.type.Timestep;
import blog.world.PartialWorld;
import blog.world.AbstractPartialWorld;


/**
 * A class defining static helper methods on basic interfaces (therefore not
 * belonging to any of their specific implementations), regarding temporal
 * aspects (DBLOG) (general ones are in {@link BLOGUtil}.
 * 
 * @author Rodrigo
 */
public class DBLOGUtil {
	/**
	 * Identifies the largest time step in a world and uninstantiates all temporal
	 * random variables with a different time step.
	 */
	public static void uninstantiatePreviousTimeslices(PartialWorld world) {
		int largestTimestepIndex = findLargestTimestepIndex(world);
		if (largestTimestepIndex != -1)
			uninstantiateAllTemporalsWithAnIndexDifferentFrom(largestTimestepIndex,
					world);
	}
	
	public static BasicVar getVarWithTimestep(BasicVar var, int timestep) {
		if (timestep < 0) {
			System.out.println("DBLOGUtil.getVarWithTimestep: Illegal timestep: " + timestep);
			System.exit(0);
		}
		
		int curTimestep = DBLOGUtil.getTimestepIndex(var);
		if (curTimestep == -1) return var;
		Object[] args = var.args();
		BasicVar newVar = null;
		if (var instanceof RandFuncAppVar) {
			RandFuncAppVar funcAppVar = (RandFuncAppVar) var;
			RandomFunction f = funcAppVar.func();
			List newArgs = getArgsWithTimestep(args, f.getArgTypes());
			newVar = new RandFuncAppVar(f, newArgs);
		} 
		
		if (var instanceof NumberVar) {
			NumberVar numVar = (NumberVar) var;
			POP pop = numVar.pop();
			List newArgs = getArgsWithTimestep(args, pop.getArgTypes());
			newVar = new NumberVar(pop, newArgs);
		}
		if (newVar == null) {
			System.err.println("DBLOGUtil.getVarWithTimestep: failed to set " + var);
			System.exit(0);
		}
		return newVar;
	}
	
	public static List getArgsWithTimestep(Object[] args, Type[] argTypes) {
		List newArgs = new ArrayList();
		for (int i = 0; i < argTypes.length; i++) {
			if (argTypes[i].equals(BuiltInTypes.TIMESTEP)) {
				newArgs.add(BuiltInTypes.TIMESTEP.getGuaranteedObject(0));
			} else {
				newArgs.add(args[i]);
			}
		}
		return newArgs;
	}

	/**
	 * Returns a string obtained by replacing all identifiers <code>t</code> in a
	 * given string by the string representation of a timestep the index of which
	 * is given.
	 */
	public static String replaceTByTimeStep(String string, int timestepIndex) {
		return string.replaceAll("\\bt\\b", "@" + timestepIndex);
	}

	/**
	 * Returns the query obtained by instantiating a query string with a given
	 * time step.
	 */
	public static ArgSpecQuery getQueryForTimestep(String queryString,
			Model model, int timestepIndex) {
		String queryForLastestTimestepString = DBLOGUtil.replaceTByTimeStep(
				queryString, timestepIndex);
		ArgSpecQuery query = BLOGUtil.parseQuery_NE("query "
				+ queryForLastestTimestepString + ";", model);
		return query;
	}

	public static int findLargestTimestepIndex(PartialWorld world) {
		int largest = -1;
		Iterator timestepIndexIt = getTimestepIndicesIterator(world);
		while (timestepIndexIt.hasNext()) {
			Integer timestepIndex = (Integer) timestepIndexIt.next();
			if (timestepIndex.intValue() > largest)
				largest = timestepIndex.intValue();
		}
		return largest;
	}

	/**
	 * An iterator over the time step indices present in a partial world.
	 */
	public static class TimestepIndicesIterator extends FilteredIterator {
		public TimestepIndicesIterator(PartialWorld world) {
			super(world.getInstantiatedVars().iterator());
		}

		public Object filter(int index, Object varObj) {
			BayesNetVar var = (BayesNetVar) varObj;
			int timestepIndex = getTimestepIndex(var);
			if (timestepIndex == -1 || alreadyReturned.contains(timestepIndex)) {
				return null;
			}
			alreadyReturned.add(timestepIndex);
			return timestepIndex;
		}

		private HashSet alreadyReturned = new HashSet();
	}

	/**
	 * Returns an iterator over the time step indices present in a partial world.
	 */
	public static Iterator getTimestepIndicesIterator(PartialWorld world) {
		return new TimestepIndicesIterator(world);
	}

	private static Pattern timestepPattern = Pattern.compile("@\\d+\\)\\z");

	public static int getTimestepIndex(BayesNetVar var) {
		return getTimestepIndex((Object) var);
	}
	
	public static int getTimestepIndex(Object var) {
		Matcher matcher = timestepPattern.matcher(var.toString());
		int time = -1;
		while (matcher.find()) {
			String match = matcher.group();
			int endIndex = match.length() - 1;
			time = Integer.parseInt(match.substring(1, endIndex));
		}
		//System.out.println(time + " " + var);
		return time;
	}

	public static void uninstantiateAllTemporalsWithAnIndexDifferentFrom(
			int largest, PartialWorld world) {
		LinkedList instantiatedVars = new LinkedList(world.getInstantiatedVars());
		Iterator varIt = instantiatedVars.iterator();
		while (varIt.hasNext()) {
			BasicVar var = (BasicVar) varIt.next();
			if (var.toString().contains("#")) continue; //TODO: avoid clearing number statements?
			//System.out.println("calling getTimestepIndex");
			int timestepIndex = getTimestepIndex(var);
			if (timestepIndex != -1 && timestepIndex <= largest - nsim) {
				//System.out.println("uninstantiating" + var);
				world.setValue(var, null);
				if (timestepIndex <= largest - nsim - 1) {
					System.out.println("Why is " + var.toString() + " instantiated when largest is " + largest + "?");
				}
			}
		}
		for (Iterator it = ((AbstractPartialWorld) world).getDecisionInterp().iterator(); it.hasNext();){
			Object o = it.next();
			int timestepIndex = getTimestepIndex(o);
			if (timestepIndex != -1 && timestepIndex <= largest - nsim) {
				it.remove();
			}
		}
	}

	public static boolean isTimestep(ArgSpec t) {
		return getTimestepInTimestepTerm(t) != null;
	}

	/**
	 * Returns the single timestep in the given argspec, or <code>null</code> if
	 * there is none in it, or exits if there are more than one.
	 */
	public static Timestep getSingleTimestepIn(ArgSpec argSpec) {
		Collection timesteps = getTimestepTermsIn(argSpec, new LinkedList());
		if (timesteps.size() > 1)
			Util.fatalError("DBLOGUtil.getTimestep called for argspec with more than one timestep: "
					+ argSpec);
		if (timesteps.size() == 0)
			return null;
		return getTimestepInTimestepTerm((ArgSpec) Util.getFirst(timesteps));

	}

	/**
	 * Returns a Timestep object if this is a constant timestep term, or null
	 * otherwise.
	 */
	public static Timestep getTimestepInTimestepTerm(ArgSpec timestepTerm) {
		if (!(timestepTerm instanceof FuncAppTerm))
			return null;
		FuncAppTerm funcAppTerm = (FuncAppTerm) timestepTerm;
		if (!(funcAppTerm.getFunction() instanceof NonRandomFunction))
			return null;
		NonRandomFunction nonRandomFunction = (NonRandomFunction) funcAppTerm
				.getFunction();
		if (!(nonRandomFunction.getInterpretation() instanceof ConstantInterp))
			return null;
		ConstantInterp interp = (ConstantInterp) nonRandomFunction
				.getInterpretation();
		Object value = interp.getValue(Util.list());
		if (!(value instanceof Timestep))
			return null;
		return (Timestep) value;
		// Prime example of why dynamic typing sucks. :-) Rodrigo
	}

	private static class TimestepSelector implements UnaryPredicate {
		public boolean evaluate(Object term) {
			return isTimestep((ArgSpec) term);
		}
	};

	private static final TimestepSelector timestepSelector = new TimestepSelector();

	/**
	 * Adds all timesteps in an ArgSpec to a collection and return that
	 * collection.
	 */
	public static Collection getTimestepTermsIn(ArgSpec argSpec,
			Collection timesteps) {
		argSpec.selectTerms(timestepSelector, timesteps);
		return timesteps;
	}

	private static class TimestepTermComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return getTimestepInTimestepTerm((ArgSpec) o1).compareTo(
					getTimestepInTimestepTerm((ArgSpec) o2));
		}

		public boolean equals(Object obj) {
			return obj instanceof TimestepTermComparator;
		}
	};

	/**
	 * Returns a list of Evidence objects, equivalent to a given Evidence object,
	 * ordered by maximum timestep present in their statements. (an absence of
	 * timesteps places the statement in the first Evidence object).
	 */
	public static List splitEvidenceByMaxTimestep(Evidence evidence) {
		List result = new LinkedList();
		HashMultiMap statementsByTimestep = getStatementsByTimestep(evidence);
		addAtemporalEvidenceToList(result, statementsByTimestep);
		addTemporalEvidenceToListInTimestepOrder(result, statementsByTimestep);
		return result;
	}

	private static HashMultiMap getStatementsByTimestep(Evidence evidence) {
		HashMultiMap statementsByTimestep = new HashMultiMap();
		Iterator it;
		for (it = evidence.getValueEvidence().iterator(); it.hasNext();) {
			ValueEvidenceStatement statement = (ValueEvidenceStatement) it.next();
			TreeSet timesteps = (TreeSet) getTimestepTermsIn(statement.getLeftSide(),
					new TreeSet(new TimestepTermComparator()));
			getTimestepTermsIn(statement.getOutput(), timesteps);
			Object maxTimestep = timesteps.isEmpty() ? null : timesteps.last();
			statementsByTimestep.add(maxTimestep, statement);
		}
		for (it = evidence.getSymbolEvidence().iterator(); it.hasNext();) {
			SymbolEvidenceStatement statement = (SymbolEvidenceStatement) it.next();
			TreeSet timesteps = (TreeSet) getTimestepTermsIn(statement.getSetSpec(),
					new TreeSet(new TimestepTermComparator()));
			Object maxTimestep = timesteps.isEmpty() ? null : timesteps.last();
			statementsByTimestep.add(maxTimestep, statement);
		}
		for (it = evidence.getDecisionEvidence().iterator(); it.hasNext();) {
			DecisionEvidenceStatement statement = (DecisionEvidenceStatement) it.next();
			TreeSet timesteps = (TreeSet) getTimestepTermsIn(statement.getLeftSide(),
					new TreeSet(new TimestepTermComparator()));
			getTimestepTermsIn(statement.getOutput(), timesteps);
			Object maxTimestep = timesteps.isEmpty() ? null : timesteps.last();
			statementsByTimestep.add(maxTimestep, statement);
		}
		return statementsByTimestep;
	}

	private static void addAtemporalEvidenceToList(List result,
			HashMultiMap statementsByTimestep) {
		result.add(Evidence.constructAndCompile((Collection) statementsByTimestep
				.get(null)));
		statementsByTimestep.remove(null);
	}

	private static void addTemporalEvidenceToListInTimestepOrder(
			final List result, HashMultiMap statementsByTimestep) {
		applyProcedureToEvidenceInTimestepOrderUpTo(-1, statementsByTimestep,
				new UnaryProcedure() {
					public void evaluate(Object evidenceObj) {
						result.add(evidenceObj);
					}
				});
	}

	private static void applyProcedureToEvidenceInTimestepOrderUpTo(
			int timestepIndex, HashMultiMap statementsByTimestep,
			UnaryProcedure procedure) {
		SortedSet sortedKeys = new TreeSet(new TimestepTermComparator());
		sortedKeys.addAll(statementsByTimestep.keySet());
		for (Iterator it = sortedKeys.iterator(); it.hasNext();) {
			ArgSpec key = (ArgSpec) it.next();
			if (timestepIndex != -1
					&& getTimestepInTimestepTerm(key).getValue() > timestepIndex)
				break;
			Evidence evidence = Evidence
					.constructAndCompile((Collection) statementsByTimestep.get(key));
			procedure.evaluate(evidence);
		}
	}

	public static Evidence getEvidenceUpTo(int timestepIndex, Evidence evidence) {
		final Evidence result = new Evidence();
		HashMultiMap statementsByTimestep = getStatementsByTimestep(evidence);
		applyProcedureToEvidenceInTimestepOrderUpTo(timestepIndex,
				statementsByTimestep, new UnaryProcedure() {
					public void evaluate(Object evidenceObj) {
						result.addAll((Evidence) evidenceObj);
					}
				});
		return result;
	}
	
	public static int nsim = 2;
}
