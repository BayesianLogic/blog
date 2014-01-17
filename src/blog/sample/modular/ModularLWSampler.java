/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.sample.modular;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.BLOGUtil;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.bn.VarWithDistrib;
import blog.common.AddedTupleIterator;
import blog.common.ExtensibleLinkedList;
import blog.common.Histogram;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.CardinalitySpec;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.Formula;
import blog.model.Function;
import blog.model.ImplicitSetSpec;
import blog.model.Model;
import blog.model.NonGuaranteedObject;
import blog.model.POP;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.model.Type;
import blog.sample.IntRegion;
import blog.sample.LWSampler;
import blog.sample.Region;
import blog.sample.SingletonRegion;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;

/**
 * 
 * @author leili
 * @date Apr 22, 2012
 */
public class ModularLWSampler extends LWSampler {

	private static final int intBound = -1; // TODO parse from Configuration
	private static final int depthBound = -1; // TODO parse from Configuration

	/**
	 * @param model
	 * @param properties
	 */
	public ModularLWSampler(Model model, Properties properties) {
		super(model, properties);
	}

	@Override
	public void nextSample() {
		WorldWithBlock curWorld = (WorldWithBlock) getBaseWorld();
		if (curWorld == null) {
			curWorld = new WorldWithBlock(model, evidence, intBound, depthBound);
			Util.debug("Creating initial possible world");
		}
		this.curWorld = curWorld;
		double w = 0;
		weight = 1;
		while (!isCurWorldSufficient(curWorld)) {
			VarWithDistrib var = curWorld.getNextUninstVar();
			if (var != null) {
				if (var.canSample(curWorld)) {
					w = sampleAndComputeWeight(var, curWorld);
					if (w < 0)
						break;
				} else {
					curWorld.putBackVarWithDistrib(var);
				}
			} else {
				System.out.println("World is not complete, but no basic random "
						+ "variable is supported.  Please check for "
						+ "a possible cycle in your model.");
			}
			weight *= w;
		}

		if (!evidence.isTrue(curWorld))
			weight = 0;
		BLOGUtil.ensureDetAndSupportedWithListener(queryVars, curWorld,
				afterSamplingListener);

		// if (Util.verbose()) {
		// System.out.println("Generated world:");
		// curWorld.print(System.out);
		// System.out.println("Weight: " + weight);
		// }

        // FIXME: remove duplication with LWSampler
		++totalNumSamples;
		++numSamplesThisTrial;
		if (weight > 0) {
			++totalNumConsistent;
			++numConsistentThisTrial;
		}
        if(Histogram.USING_LOG_WEIGHT) {
          logSumWeightsThisTrial = Util.logSum(logSumWeightsThisTrial, weight);
        } else {
          logSumWeightsThisTrial = Util.logSum(logSumWeightsThisTrial, java.lang.Math.log(weight));
        }
	}
	
	public void printStats() {
		super.printStats("Modular");
	}

	private double sampleAndComputeWeight(VarWithDistrib var,
			WorldWithBlock curWorld) {
		DependencyModel.Distrib distrib = var
				.getDistrib(new BlockInstantiatingEvalContextImpl(curWorld));
		Util.debug("Instantiating: " + var);
		Type varType = var.getType();
		CondProbDistrib cpd = distrib.getCPD();
		List args = distrib.getArgValues();
		Region r = curWorld.getSatisfyingRegion(var);

		double w = 0;
		if (r.isEmpty())
			return -1;
		Object value = null;
		if (r.isSingleton()) {
			value = r.getOneValue();
			w = cpd.getProb(args, value);
		} else {
			do {
				value = cpd.sampleVal(args, varType);
			} while (!r.contains(value));
			w = computeCPD(cpd, args, r);
		}
		curWorld.setValue(var, value);
		return w; // TODO return weight
	}

	/**
	 * compute the cumulative probability within the region
	 * 
	 * @param cpd
	 * @param args
	 * @param rg
	 * @return
	 */
	private double computeCPD(CondProbDistrib cpd, List args, Region rg) {
		if (rg == Region.FULL_REGION)
			return 1;
		Class cls = cpd.getClass();
		try {
			Method method = cls.getMethod("cdf", new Class[] { List.class,
					Object.class });
			IntRegion reg = (IntRegion) rg;
			Object high = method.invoke(cpd, new Object[] { args, reg.getMax() });
			Object low = method.invoke(cpd, new Object[] { args, reg.getMin() - 1 });
			return ((Number) high).doubleValue() - ((Number) low).doubleValue();
		} catch (Exception ex) {
			Util.debug("no idea how to compute the weight!!!");
		}
		return 1;
	}

	private boolean isCurWorldSufficient(PartialWorld world) {
		// TODO leili: find more efficient ways to check sufficient
		if (!evidence.isDetermined(world)) {
			return false;
		}

		for (Query q : queries) {
			for (BayesNetVar var : q.getVariables()) {
				if (!var.isDetermined(world)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * instantiate
	 * calculate weight of
	 * 
	 * @see blog.sample.LWSampler#supportEvidenceAndCalculateWeight()
	 */
	@Override
	protected double supportEvidenceAndCalculateWeight() {
		// TODO modify to block sampling
		BLOGUtil.setBasicVars(evidence, curWorld);
		BlockInstantiatingEvalContextImpl context = new BlockInstantiatingEvalContextImpl(
				curWorld);
		BLOGUtil.ensureDetAndSupported(evidence.getEvidenceVars(), context);
		return evidence.getEvidenceProb(curWorld);
	}

}

/**
 * PartialWorld implementation that maintains a list of uninstantiated
 * VarWithDistrib objects, and allows variables to be instantiated using a
 * special method of an iterator over this list. This is useful for rejection
 * sampling.
 * 
 * <p>
 * The list of uninstantiated basic variables only includes variables whose
 * arguments necessarily exist in this partial world. Thus, as more number
 * variables are instantiated and more objects necessarily exist, more variables
 * will be added to the list. The list is not represented explicitly -- if
 * integers or natural numbers serve as variable arguments, then the full list
 * is infinite. Thus, the WorldInProgress class does not allow random access to
 * the list. It only allows iteration, and additional variables are added lazily
 * to the explicit list as needed.
 * 
 * <p>
 * The hypothetical "full" list is ordered in such a way that every one of its
 * elements has a finite index. This is achieved by interleaving variables that
 * have integer arguments with variables that have non-guaranteed arguments.
 */
class WorldWithBlock extends DefaultPartialWorld {
	/**
	 * Creates a new WorldInProgress with no instantiated random variables.
	 * 
	 * @param intBound
	 *          maximum absolute value of integers (or natural numbers) to allow
	 *          as arguments of RVs that are included in the uninstantiated RV
	 *          list. To allow unbounded integers, pass -1 for this parameter.
	 * 
	 * @param depthBound
	 *          maximum depth of non-guaranteed objects to allow as arguments of
	 *          RVs that are included in the uninstantiated RVs list. Objects
	 *          generated by the empty tuple have depth 0; other non-guaranteed
	 *          objects have depth one greater than the maximum depth of their
	 *          generating objects. To allow unbounded depths, pass -1 for this
	 *          parameter.
	 */
	public WorldWithBlock(Model model, Evidence evidence, int intBound,
			int depthBound) {
		super(Collections.EMPTY_SET);
		this.model = model;
		this.evidence = evidence;
		this.intBound = intBound;
		this.depthBound = depthBound;

		init();

	}

	/**
	 * add all random variables without parents into the uninstantiated variables
	 */
	protected void init() {

		// added number variables for those number statement without origin
		// functions
		for (Type generatedType : model.getTypes()) {
			Collection<POP> pops = generatedType.getPOPs();

			// set initial size of unused number statements for each type
			restPOPs.put(generatedType, new HashSet<POP>(pops));

			for (POP pop : pops) {
				for (int i = 0; i < pop.getArgTypes().length; ++i) {
					objectsByType.put(pop.getArgTypes()[i], new ArrayList());
				}

				if (pop.getArgTypes().length == 0) {
					addNumberVar(new NumberVar(pop, Collections.EMPTY_LIST));
				}
			}
		}

		// Determine what types serve as arguments to basic RVs. Initialize
		// their object lists to be empty. As we're doing this, add any
		// random variables with empty arg lists to the list of uninstantiated
		// RVs.

		for (Function f : model.getFunctions()) {
			if (f instanceof RandomFunction) {
				for (int i = 0; i < f.getArgTypes().length; ++i) {
					objectsByType.put(f.getArgTypes()[i], new ArrayList());
				}

				if (f.getArgTypes().length == 0) {
					uninstVars.add(new RandFuncAppVar((RandomFunction) f,
							Collections.EMPTY_LIST));
				}
			}
		}

		// add skolem constants defined in symbol evidence
		// already added in step 1
		// for (SkolemConstant c : evidence.getSkolemConstants()) {
		// uninstVars.add(new RandFuncAppVar(c, Collections.EMPTY_LIST));
		// }

		// Create initial object lists for those types. While doing so,
		// add uninstantiated variables that have these objects as arguments.
		for (Type type : objectsByType.keySet()) {
			if (type.isSubtypeOf(BuiltInTypes.INTEGER)) {
				addObjects(type, Collections.singleton(new Integer(0)));
				intsAreArgs = true;
			} else if (type == BuiltInTypes.BOOLEAN) {
				addObjects(type, type.getGuaranteedObjects());
			} else if (type.isBuiltIn()) {
				Util.fatalError("Illegal argument type for random function: " + type,
						false);
			} else {
				// user-defined type
				addObjects(type, type.getGuaranteedObjects());
			}
		}

		// set the iterator
		lastIter = uninstVars.iterator();
	}

	/**
	 * set the value of the variable
	 * will potentially add new random variables into the possible world if this
	 * variable is number variable
	 * 
	 * @param var
	 * @param value
	 */
	public void setValue(VarWithDistrib var, Object value) {
		super.setValue(var, value);

		if (var instanceof NumberVar) {
			int varDepth = getVarDepth(var);
			NumberVar nv = (NumberVar) var;
			if ((depthBound < 0) || (varDepth < depthBound)) {
				if (getVarDepth(var) >= maxInt) {
					// We're creating non-guaranteed objects of greater depth,
					// so increase maxInt as well
					increaseMaxInt();
				}

				// Add objects generated by this number variable
				addObjects(nv.pop().type(), getSatisfiers(nv));
			}
			Type ty = nv.pop().type();
			Integer t = restNumberVars.get(ty) - 1;
			restNumberVars.put(ty, t);
		}

		if (var != lastVar) {
			Util.debug("Fatal error: last variable sampled is not the same as var");
		} else {
			lastIter.remove();
		}
	}

	/**
	 * 
	 * @return next uninstantiated variable
	 */
	public VarWithDistrib getNextUninstVar() {
		ensureListExtended();
		lastVar = (VarWithDistrib) lastIter.next();
		return lastVar;
	}

	// must used after getNextUninstVar
	private boolean hasNext() {
		return lastIter.hasNext();
	}

	private Iterator lastIter;
	private VarWithDistrib lastVar = null;

	void ensureListExtended() {
		if (!uninstVars.isEmpty()) {
			// Try extending the list of uninstantiated vars by
			// increasing maxInt.
			increaseMaxInt();
		}
	}

	/**
	 * Returns true if every basic random variable whose arguments necessarily
	 * exist in this world is instantiated.
	 */
	public boolean isComplete() {
		if (uninstVars.isEmpty()) {
			increaseMaxInt();
		}
		return uninstVars.isEmpty(); // no uninstantiated vars -> complete
	}

	private int getVarDepth(VarWithDistrib var) {
		int maxArgDepth = 0;
		for (int i = 0; i < var.args().length; ++i) {
			Object arg = var.args()[i];
			if (arg instanceof NonGuaranteedObject) {
				int d = ((NonGuaranteedObject) arg).getDepth();
				maxArgDepth = Math.max(d, maxArgDepth);
			}
		}
		return maxArgDepth;
	}

	/**
	 * Increases the maximum magnitude of integers (and natural numbers) in the
	 * object lists by 1. Adds any basic RVs that use the newly added integers to
	 * the uninstantiated variables list.
	 * 
	 * <p>
	 * Note that no variables will be added if integers don't serve as arguments
	 * to basic RVs in this model, or if they only occur in argument lists along
	 * with types that happen to have an empty extension in this world. In either
	 * case, if increasing maxInt by 1 doesn't yield new variables, then further
	 * increases won't do anything either. So there's no point in calling this
	 * method in a loop.
	 */
	void increaseMaxInt() {
		if (intsAreArgs && ((intBound < 0) || (maxInt < intBound))) {

			++maxInt;

			if (objectsByType.containsKey(BuiltInTypes.NATURAL_NUM)) {
				addObjects(BuiltInTypes.NATURAL_NUM,
						Collections.singleton(new Integer(maxInt)));
			}

			if (objectsByType.containsKey(BuiltInTypes.INTEGER)) {
				List newInts = new ArrayList();
				newInts.add(new Integer(maxInt));
				newInts.add(new Integer(-maxInt));
				addObjects(BuiltInTypes.INTEGER, newInts);
			}
		}
	}

	private void addObjects(Type newObjType, Collection newObjs) {
		if (newObjs.isEmpty()) {
			return;
		}

		List objs = (List) objectsByType.get(newObjType);
		if (objs != null) { // if type serves as an argument to some basic RV

			// Add function app vars with these objects as argument
			for (Iterator fIter = model.getFunctions().iterator(); fIter.hasNext();) {
				Function f = (Function) fIter.next();
				if (f instanceof RandomFunction) {
					RandomFunction rf = (RandomFunction) f;
					if (Arrays.asList(f.getArgTypes()).contains(newObjType)) {
						addFuncAppVars(rf, newObjType, newObjs);
					}
				}
			}

			// Add number vars with these object as arguments
			for (Iterator typeIter = model.getTypes().iterator(); typeIter.hasNext();) {
				Type generatedType = (Type) typeIter.next();
				for (Iterator popIter = generatedType.getPOPs().iterator(); popIter
						.hasNext();) {
					POP pop = (POP) popIter.next();
					if (Arrays.asList(pop.getArgTypes()).contains(newObjType)) {
						addNumberVars(pop, newObjType, newObjs);
					}
				}
			}

			// Don't need to worry about skolem constant vars because
			// they don't have arguments

			objs.addAll(newObjs);
		}
	}

	public Region getSatisfyingRegion(VarWithDistrib var) {
		for (BayesNetVar ev : evidence.getEvidenceVars()) {
			Region r = computeRegionFromEvidence(var, ev);
			if (r != null) {
				return r;
			}
		}
		return Region.FULL_REGION;
	}

	/**
	 * compute the feasible region based on the evidence,
	 * the method will only use the v2 if
	 * 1. v1 is a direct parent of v2, or
	 * 2. v1 and v2 are the same, i.e. corresponding to evidence variable
	 * 
	 * @param v1
	 *          the variable to sample
	 * @param v2
	 *          the variable corresponding to evidence
	 * @return
	 */
	private Region computeRegionFromEvidence(BayesNetVar v1, BayesNetVar v2) {
		if (v1.equals(v2)) {
			// the variable is in the evidence, it should be directly set to evidence
			// value
			return new SingletonRegion(evidence.getObservedValue(v2));
		}
		if (v2 instanceof DerivedVar) {
			ArgSpec as = ((DerivedVar) v2).getArgSpec();
			if (as instanceof CardinalitySpec) {
				// child is cardinality of a set
				ImplicitSetSpec iss = ((CardinalitySpec) as).getSetSpec();
				Type childType = iss.getType();
				if (v1 instanceof NumberVar) {
					// parent is number variable, potential match
					NumberVar numv1 = (NumberVar) v1;
					POP parentPOP = numv1.pop();
					Type parentType = parentPOP.type();
					if (!parentType.equals(childType)) {
						return null;
					} else {
						// potential match, if
						Object[] objs = numv1.args(); // generating objects for parent
																					// variable
						if (objs.length == 0) {
							// no any constraint
							return Region.FULL_REGION;
						} else {
							// v1 is parent of v2
							Formula cond = iss.getCond();
							if (cond.isDetermined(this) && cond.isTrue(this)) {
								int value = ((Number) evidence.getObservedValue(v2)).intValue();
								// get all previously satisfied var
								Set<BayesNetVar> pvars = getAlreadySampledParentVars(v2);
								for (BayesNetVar pv : pvars) {
									// eliminate the already sampled ones
									int a = ((Number) this.getValue(pv)).intValue();
									value -= a;
								}
								// v1 is parent of v2, and will be sampled immediately
								pvars.add(v1);
								if ((value > 0) && anyMoreNumberVar(childType, cond)) {
									return new IntRegion(0, value);
								} else {
									return new SingletonRegion(value);
								}
							}
						}
					}
				} /* TODO check other cases of v1 that can be potential parent of this */

			} else {
				// other cases of argspec in v1
			}

		} else {
			// other variables
			// might be parent
			// TODO check whether the condition has v1 as parent
		}
		return null;
	}

	/**
	 * get the already sampled variables that are parent of this one
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Set<BayesNetVar> getAlreadySampledParentVars(BayesNetVar var) {
		HashSet<BayesNetVar> vs = sampledParentVarMap.get(var);
		if (vs == null) {
			vs = new HashSet<BayesNetVar>();
			sampledParentVarMap.put(var, vs);
		}
		return vs;
	}

	/**
	 * is there any more number variables for this type?
	 */
	private boolean anyMoreNumberVar(Type type, Formula cond) {
		if (restPOPs.containsKey(type) && restPOPs.get(type).size() == 0) {
			if (restNumberVars.containsKey(type))
				return restNumberVars.get(type) > 1;
		}
		return true;
	}

	/**
	 * add the variable back to the list which is not instantiated
	 * 
	 * @param var
	 */
	void putBackVarWithDistrib(VarWithDistrib var) {
		uninstVars.add(var);
	}

	private void addFuncAppVars(RandomFunction f, Type newObjType,
			Collection newObjs) {
		for (Iterator iter = getAddedTupleIterator(Arrays.asList(f.getArgTypes()),
				newObjType, newObjs); iter.hasNext();) {
			List args = (List) iter.next();
			VarWithDistrib v = new RandFuncAppVar(f, args);
			if (Util.verbose()) {
				System.out.println("Adding uninstantiated var: " + v);
			}
			uninstVars.add(v);
		}
	}

	private void addNumberVars(POP pop, Type newObjType, Collection newObjs) {
		for (Iterator iter = getAddedTupleIterator(
				Arrays.asList(pop.getArgTypes()), newObjType, newObjs); iter.hasNext();) {
			List genObjs = (List) iter.next();
			NumberVar v = new NumberVar(pop, genObjs);
			if (Util.verbose()) {
				System.out.println("Adding uninstantiated var: " + v);
			}
			addNumberVar(v);
		}
	}

	private void addNumberVar(NumberVar var) {
		Type type = var.pop().type();
		Integer value = restNumberVars.get(type);
		if (value == null) {
			value = 1;
		} else {
			value = value + 1;
		}
		HashSet<POP> pops = restPOPs.get(type);
		pops.remove(var.pop());

		restNumberVars.put(type, value);
		uninstVars.add(var);
	}

	private Iterator getAddedTupleIterator(List argTypes, Type newObjType,
			Collection newObjs) {
		List orig = new ArrayList();
		List added = new ArrayList();
		for (Iterator iter = argTypes.iterator(); iter.hasNext();) {
			Type type = (Type) iter.next();
			orig.add(objectsByType.get(type));
			if (type == newObjType) {
				added.add(newObjs);
			} else {
				added.add(Collections.EMPTY_SET);
			}
		}

		return new AddedTupleIterator(orig, added);
	}

	protected Model model;
	protected Evidence evidence;

	// a stable and extensible list of uninstantiated random variables
	ExtensibleLinkedList uninstVars = new ExtensibleLinkedList();

	// remaining number of already generated number variables for each type,
	private Map<Type, Integer> restNumberVars = new HashMap<Type, Integer>();

	// remaining number of number statements for each type of variable
	private Map<Type, HashSet<POP>> restPOPs = new HashMap<Type, HashSet<POP>>();

	// map from child var to already sampled parent variables
	private Map<BayesNetVar, HashSet<BayesNetVar>> sampledParentVarMap = new HashMap<BayesNetVar, HashSet<BayesNetVar>>();

	// from Type to List of objects
	private Map<Type, List> objectsByType = new HashMap<Type, List>();

	private boolean intsAreArgs = false;
	private int maxInt = 0; // max int added to object list so far

	private int intBound;
	private int depthBound;
}
