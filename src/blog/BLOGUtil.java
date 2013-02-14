package blog;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.VarWithDistrib;
import blog.common.Histogram;
import blog.common.Util;
import blog.distrib.ListInterp;
import blog.model.ArgSpec;
import blog.model.ArgSpecQuery;
import blog.model.AtomicFormula;
import blog.model.ChoiceEvidenceStatement;
import blog.model.DependencyModel;
import blog.model.EqualityFormula;
import blog.model.Evidence;
import blog.model.Formula;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.ModelEvidenceQueries;
import blog.model.NegFormula;
import blog.model.NonRandomFunction;
import blog.model.Term;
import blog.model.ValueEvidenceStatement;
import blog.parse.Parse;
import blog.sample.AfterSamplingListener;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.InstantiatingEvalContext;
import blog.semant.Semant;
import blog.world.PartialWorld;

/*added by cheng*/
import blog.model.ChoiceFunction;

/**
 * A class defining static helper methods on basic interfaces (therefore not
 * belonging to any of their specific implementations).
 * 
 * @author Rodrigo
 */
public class BLOGUtil {

	/**
	 * Ensures given variables are instantiated and supported in a given context,
	 * sampling further variables if necessary.
	 */
	public static void ensureDetAndSupported(
			Collection<? extends BayesNetVar> vars, InstantiatingEvalContext context) {
		for (BayesNetVar var : vars) {
			var.ensureDetAndSupported(context);
		}
	}

	/**
	 * Ensures given variables are instantiated and supported in a given world,
	 * sampling further variables if necessary.
	 */
	public static void ensureDetAndSupported(
			Collection<? extends BayesNetVar> vars, PartialWorld world) {
		ensureDetAndSupported(vars, new ClassicInstantiatingEvalContext(world));
	}

	/**
	 * Shorthand for {@link #ensureDetAndSupported(Collection, PartialWorld)}
	 * applied to a single variable.
	 */
	public static void ensureDetAndSupported(BayesNetVar var, PartialWorld world) {
		ensureDetAndSupported(Util.list(var), world);
	}

	/**
	 * Same as {@link #ensureDetAndSupported(BayesNetVar, PartialWorld)}, which a
	 * variable sampling listener.
	 */
	public static void ensureDetAndSupportedWithListener(Collection vars,
			PartialWorld world, AfterSamplingListener afterSamplingListener) {
		ClassicInstantiatingEvalContext context1 = new ClassicInstantiatingEvalContext(
				world);
		context1.afterSamplingListener = afterSamplingListener;
		ClassicInstantiatingEvalContext context = context1;
		ensureDetAndSupported(vars, context);
	}

	/**
	 * Sets the value of a variable in a world, if this variable is basic (you
	 * cannot set the value of a derived variable).
	 */
	public static void setIfBasicVar(BayesNetVar var, Object value,
			PartialWorld world) {
		if (var instanceof BasicVar)
			world.setValue((BasicVar) var, value);
	}

	/**
	 * Sets given evidence in a given world (only basic variables -- derived vars
	 * will be a consequence of basic variables anyway).
	 */
	public static void setBasicVars(Evidence evidence, PartialWorld world) {
		for (BayesNetVar var : evidence.getEvidenceVars()) {
			if (var instanceof BasicVar)
				world.setValue((BasicVar) var, evidence.getObservedValue(var));
		}
	}
	
	
	/*added by cheng*/
	public static void setChoiceInterp(Evidence evidence, PartialWorld world) {
		setChoiceInterp(evidence, new ClassicInstantiatingEvalContext(world));
	}
	

	
	public static void setChoiceInterp(Evidence evidence, InstantiatingEvalContext context) {
		for (BayesNetVar var : evidence.getEvidenceVars()) {
			if (var instanceof DerivedVar){
				if (((DerivedVar) var).getArgSpec() instanceof FuncAppTerm){
					FuncAppTerm fat = (FuncAppTerm) ((DerivedVar) var).getArgSpec();
					if (fat == null){
						System.err.println("error in blogutil.setchoiceinterp");
						System.exit(0);
					}
					if (fat.getFunction() instanceof ChoiceFunction){
						ChoiceFunction f = (ChoiceFunction) fat.getFunction();
						f.addInterp(fat, context);
					}
				}
			}
		}
	}
	public static double setAndGetProbability(BayesNetVar rv, Object value,
			PartialWorld world) {
		BLOGUtil.ensureDetAndSupported(rv, world);
		BLOGUtil.setIfBasicVar(rv, value, world);
		return world.getProbOfValue(rv);
	}

	/**
	 * Returns the value of a variable described in a given string, in given
	 * world, according to a given model, sampling values if necessary.
	 */
	public static Object getValue(String varString, PartialWorld world,
			Model model) {
		ArgSpecQuery query = BLOGUtil.parseQuery_NE("query " + varString + ";",
				model);
		BLOGUtil.ensureDetAndSupported(query.getVariable(), world);
		return world.getValue(query.getVariable());
	}

	/**
	 * Returns a collection with the BayesNetVars associated to a set of queries.
	 */
	public static Collection getQueriesVars(Collection queries) {
		Collection result = new LinkedList();
		for (Iterator iter = queries.iterator(); iter.hasNext();) {
			BayesNetVar var = ((ArgSpecQuery) iter.next()).getVariable();
			result.add(var);
		}
		return result;
	}

	/**
	 * Parses description string of a model.
	 */
	public static Model parseModel_NE(String description) {
		return Model.readFromString(description);
	}

	/**
	 * Parses description string of a list of queries, using model as background,
	 * and returns it.
	 * 
	 * @throws Exception
	 *           in case of a parsing error.
	 */
	public static List parseQueries(String description, Model model)
			throws Exception {
		ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
		return meq.queries;
	}

	/**
	 * Same as {@link #parseQueries(String, Model)} but terminating program rather
	 * than throwing an exception when there is an error.
	 */
	public static List parseQueries_NE(String description, Model model) {
		try {
			ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
			return meq.queries;
		} catch (Exception e) {
			Util.fatalError(e);
		}
		return null;
	}

	public static ModelEvidenceQueries parseAndTranslateFromString(Model m,
			String description) {
		ModelEvidenceQueries meq = new ModelEvidenceQueries();
		meq.model = m;
		Parse parse = Parse.parseString(description);
		Semant sem = new Semant(meq, parse.getErrorMsg());
		sem.transProg(parse.getParseResult());
		return sem.getModelEvidenceQueries();
	}

	/**
	 * Parses description string of a query, using model as background, and
	 * returns the query. Assumes there is only one query in the string.
	 * 
	 * @throws Exception
	 *           in case of a parsing error.
	 */
	public static ArgSpecQuery parseQuery(String description, Model model)
			throws Exception {
		ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
		return (ArgSpecQuery) Util.getFirst(meq.queries);
	}

	/**
	 * Same as {@link #parseQuery(String, Model)} but terminating program rather
	 * than throwing an exception when there is an error.
	 */
	public static ArgSpecQuery parseQuery_NE(String description, Model model) {
		try {
			ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
			return (ArgSpecQuery) Util.getFirst(meq.queries);
		} catch (Exception e) {
			Util.fatalError(e);
		}
		return null;
	}

	/**
	 * Parses description string of evidence, using model as background, and
	 * returns the evidence.
	 * 
	 * @throws Exception
	 *           in case of a parsing error.
	 */
	public static Evidence parseEvidence(String description, Model model)
			throws Exception {
		ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
		meq.evidence.compile();
		return meq.evidence;
	}

	/**
	 * Same as {@link #parseEvidence(String, Model)} but terminating program
	 * rather than throwing an exception when there is an error.
	 */
	public static Evidence parseEvidence_NE(String description, Model model) {
		try {
			ModelEvidenceQueries meq = parseAndTranslateFromString(model, description);
			return meq.evidence;
		} catch (Exception e) {
			Util.fatalError(e);
		}
		return null;
	}

	public static ValueEvidenceStatement parseValueEvidenceStatement_NE(
			String description, Model model) {
		Evidence evidence = parseEvidence_NE(description, model);
		if (evidence.getValueEvidence().size() == 1
				&& evidence.getSymbolEvidence().size() == 0)
			return (ValueEvidenceStatement) Util
					.getFirst(evidence.getValueEvidence());
		Util.fatalError(description + " is not a ValueEvidenceStatement.");
		return null;
	}

	/**
	 * Returns a variable parsed from a string description, according to a model.
	 */
	public static BayesNetVar parseVariable(String description, Model model)
			throws Exception {
		return parseQuery("query " + description + ";", model).getVariable();
	}

	/**
	 * Returns a variable parsed from a string description, according to a model,
	 * issuing an error if an exception is raised.
	 */
	public static BayesNetVar parseVariable_NE(String description, Model model) {
		return parseQuery_NE("query " + description + ";", model).getVariable();
	}

	/**
	 * Returns a BasicVar parsed from a string description, according to a model.
	 */
	public static BasicVar parseBasicVar(String description, Model model)
			throws Exception {
		return (BasicVar) parseVariable(description, model);
	}

	/**
	 * Returns a BasicVar parsed from a string description, according to a model,
	 * issuing an error if an exception is raised.
	 */
	public static BasicVar parseBasicVar_NE(String description, Model model) {
		return (BasicVar) parseVariable_NE(description, model);
	}

	/**
	 * Returns a VarWithDistrib parsed from a string description, according to a
	 * model.
	 */
	public static VarWithDistrib parseVarWithDistrib(String description,
			Model model) throws Exception {
		return (VarWithDistrib) parseVariable(description, model);
	}

	/**
	 * Returns a VarWithDistrib parsed from a string description, according to a
	 * model, issuing an error if an exception is raised.
	 */
	public static VarWithDistrib parseVarWithDistrib_NE(String description,
			Model model) {
		return (VarWithDistrib) parseVariable_NE(description, model);
	}

	/**
	 * Returns an ArgSpec parsed from a string description, according to a model.
	 */
	public static ArgSpec parseArgSpec(String description, Model model)
			throws Exception {
		ArgSpecQuery query = parseQuery("query " + description + ";", model);
		return query.argSpec();
	}

	/**
	 * Returns an ArgSpec parsed from a string description, according to a model,
	 * returning null if parse fails.
	 */
	public static ArgSpec parseArgSpec_NE(String description, Model model) {
		ArgSpecQuery query = parseQuery_NE("query " + description + ";", model);
		if (query == null)
			return null;
		return query.argSpec();
	}

	public static Term parseTerm_NE(String description, Model model) {
		return (Term) parseArgSpec_NE(description, model);
	}

	/**
	 * Returns a formula parsed from a string according to a model - THIS METHOD
	 * STILL DOESNT WORK FOR ALL CASES.
	 */
	public static Formula parseFormula(String description, Model model)
			throws Exception {
		ArgSpec argSpec = parseArgSpec(description, model);
		if (argSpec instanceof Formula)
			return (Formula) argSpec;
		if (argSpec instanceof Term)
			return new AtomicFormula((Term) argSpec);
		throw new Exception(description + " not a formula, but a "
				+ argSpec.getClass());
	}

	/**
	 * Returns a formula parsed from a string according to a model.
	 */
	public static Formula parseFormula_NE(String description, Model model) {
		try {
			Formula formula = parseFormula(description, model);
			return formula;
		} catch (Exception e) {
			Util.fatalError(e);
		}
		return null;
	}

	public static Formula parseLiteral(String description, Model model)
			throws Exception {
		Formula formula = parseFormula(description, model);
		if (isAtomicOrEquality(formula)
				|| (formula instanceof NegFormula && isAtomicOrEquality(((NegFormula) formula)
						.getNeg())))
			return formula;

		throw new Exception(description + " not a literal.");
	}

	public static Formula parseLiteral_NE(String description, Model model) {
		try {
			Formula formula = parseLiteral(description, model);
			return formula;
		} catch (Exception e) {
			Util.fatalError(e);
		}
		return null;
	}

	public static ModelEvidenceQueries parseModelEvidenceQueries_NE(
			String description) {
		ModelEvidenceQueries meq = new ModelEvidenceQueries();
		Main.stringSetup(meq.model, meq.evidence, meq.queries, description);
		return meq;
	}

	private static boolean isAtomicOrEquality(Formula formula) {
		return formula instanceof AtomicFormula
				|| formula instanceof EqualityFormula;
	}

	/** Removes all derived vars from a partial world. */
	public static void removeAllDerivedVars(PartialWorld world) {
		LinkedList derivedVars = new LinkedList(world.getDerivedVars());
		Iterator varIt = derivedVars.iterator();
		while (varIt.hasNext()) {
			DerivedVar var = (DerivedVar) varIt.next();
			world.removeDerivedVar(var);
		}
	}

	public static void uninstantiate(PartialWorld world, BasicVar var) {
		world.setValue(var, null);
	}

	/**
	 * Returns the probability of a variable having a value in a given world.
	 */
	public static double probability(VarWithDistrib var, Object value,
			PartialWorld world) {
		DependencyModel.Distrib distrib = var
				.getDistrib(new ClassicInstantiatingEvalContext(world));
		return distrib.getCPD().getProb(distrib.getArgValues(), value);
	}

	/**
	 * Indicates whether a collection of variables is independent given their
	 * parents in a given self-supporting partial world, which is determined by
	 * whether their set is disjoint from the set of their parents.
	 */
	public static boolean allVariablesAreIndependentGivenTheirParents(
			Collection vars, PartialWorld world) {
		Collection parents = getAllParents(vars, world);
		return !Util.intersect(parents, vars);
	}

	/**
	 * Returns a set with all parents of all variables in a given self-supporting
	 * partial world.
	 */
	public static HashSet getAllParents(Collection vars, PartialWorld world) {
		HashSet parents = new HashSet();
		for (Iterator it = vars.iterator(); it.hasNext();) {
			BayesNetVar var = (BayesNetVar) it.next();
			parents.addAll(var.getParents(world));
		}
		return parents;
	}

	/**
	 * Helper function to get the probability of a value in an answered query from
	 * its string.
	 */
	public static double getProbabilityByString(ArgSpecQuery query, Model model,
			String valueString) {
		Histogram hist = query.getHistogram();
		Object value = model.getConstantValue(valueString);
		return hist.getWeight(value) / hist.getTotalWeight();
	}
}
