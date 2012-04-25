/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.sample.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import blog.BLOGUtil;
import blog.NonGuaranteedObject;
import blog.bn.BayesNetVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.bn.VarWithDistrib;
import blog.common.AddedTupleIterator;
import blog.common.ExtensibleLinkedList;
import blog.common.Util;
import blog.distrib.CondProbDistrib;
import blog.model.BuiltInTypes;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.Function;
import blog.model.Model;
import blog.model.POP;
import blog.model.Query;
import blog.model.RandomFunction;
import blog.model.SkolemConstant;
import blog.model.Type;
import blog.sample.LWSampler;
import blog.sample.Region;
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

		while (!isCurWorldSufficient(curWorld)) {
			VarWithDistrib var = curWorld.getNextUninstVar();
			if (var != null) {
				DependencyModel.Distrib distrib = var
						.getDistrib(new BlockInstantiatingEvalContextImpl(curWorld));
				Util.debug("Instantiating: " + var);
				Type varType = var.getType();
				CondProbDistrib cpd = distrib.getCPD();
				List args = distrib.getArgValues();
				Object value = cpd.sampleVal(args, varType);
				curWorld.setValue(var, value);

				// TODO special treatment for number variables
			} else {
				System.out.println("World is not complete, but no basic random "
						+ "variable is supported.  Please check for "
						+ "a possible cycle in your model.");
			}
		}

		// TODO compute weight
		if (evidence.isTrue(curWorld)) {
			weight = 1;
		} else
			weight = 0;
		// weight = supportEvidenceAndCalculateWeight();
		BLOGUtil.ensureDetAndSupportedWithListener(queryVars, curWorld,
				afterSamplingListener);

		if (Util.verbose()) {
			System.out.println("Generated world:");
			curWorld.print(System.out);
			System.out.println("Weight: " + weight);
		}

		++totalNumSamples;
		++numSamplesThisTrial;
		if (weight > 0) {
			++totalNumConsistent;
			++numConsistentThisTrial;
		}
		sumWeightsThisTrial += weight;
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

		// added number variables for those number statement without origin
		// functions
		for (Type generatedType : model.getTypes()) {
			for (POP pop : generatedType.getPOPs()) {
				for (int i = 0; i < pop.getArgTypes().length; ++i) {
					objectsByType.put(pop.getArgTypes()[i], new ArrayList());
				}

				if (pop.getArgTypes().length == 0) {
					uninstVars.add(new NumberVar(pop, Collections.EMPTY_LIST));
				}
			}
		}

		// add skolem constants defined in symbol evidence
		for (SkolemConstant c : evidence.getSkolemConstants()) {
			uninstVars.add(new RandFuncAppVar(c, Collections.EMPTY_LIST));
		}

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
			if ((depthBound < 0) || (varDepth < depthBound)) {
				if (getVarDepth(var) >= maxInt) {
					// We're creating non-guaranteed objects of greater depth,
					// so increase maxInt as well
					increaseMaxInt();
				}

				// Add objects generated by this number variable
				NumberVar nv = (NumberVar) var;
				addObjects(nv.pop().type(), getSatisfiers(nv));
			}
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
			if (isDirectParent(var, ev)) {
				return computeRegionFromEvidence(var, ev);
			}
		}

		return null;
	}

	Region computeRegionFromEvidence(BayesNetVar parent, BayesNetVar child) {
		// TODO
		return null;
	}

	/**
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 *         true if v1 is direct parent of v2
	 */
	boolean isDirectParent(BayesNetVar v1, BayesNetVar v2) {
		// TODO
		return false;
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
			VarWithDistrib v = new NumberVar(pop, genObjs);
			if (Util.verbose()) {
				System.out.println("Adding uninstantiated var: " + v);
			}
			uninstVars.add(v);
		}
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

	ExtensibleLinkedList uninstVars = new ExtensibleLinkedList();

	private Map<Type, List> objectsByType = new HashMap<Type, List>(); // from
																																			// Type to
																																			// List
	private boolean intsAreArgs = false;
	private int maxInt = 0; // max int added to object list so far

	private int intBound;
	private int depthBound;
}
