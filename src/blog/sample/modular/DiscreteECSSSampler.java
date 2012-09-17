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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.BLOGUtil;
import blog.NonGuaranteedObject;
import blog.bn.BayesNetVar;
import blog.bn.DerivedVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.bn.VarWithDistrib;
import blog.common.AddedTupleIterator;
import blog.common.ExtensibleLinkedList;
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
 * @author amatsukawa
 * @date Sept 2012
 */
public class DiscreteECSSSampler extends LWSampler {

	/**
	 * @param model
	 * @param properties
	 */
	public DiscreteECSSSampler(Model model, Properties properties) {
		super(model, properties);
	}

	@Override
	public void nextSample() {
	}

	private void recursiveCardinalitySampler(Type t) {
		Collection<POP> pops = t.getPOPs();
		for (POP pop : pops) {
			Type[] argTypes = pop.getArgTypes();
			for (Type originType : argTypes) {
				if (!processed.get(originType)) {
					recursiveCardinalitySampler(originType);
				}
			}
		}
		// how to get all number variables of type t?
	}

	public double supportCardinalityEvidenceAndReturnWeight() {
		// TODO fill in
		return 1.0;
	}

	public double supportOtherEvidenceAndReturnWeight() {
		// TODO fill in
		return 1.0;
	}

	private HashMap<Type, Boolean> processed;

	private static final int intBound = -1; // TODO parse from Configuration
	private static final int depthBound = -1; // TODO parse from Configuration

}

class ECSSWorld extends DefaultPartialWorld {
	public ECSSWorld(Model model, Evidence evidence, int intBound,
			int depthBound) {
		super(Collections.EMPTY_SET);
		this.model = model;
		this.evidence = evidence;
		this.intBound = intBound;
		this.depthBound = depthBound;

		InitWorld();

	}
	
	protected void InitWorld() {
		
		/**
		 * Identify which # variable can be sampled first. 
		 * These are #vars that do not have any origin functions.
		 * There must exist one (or the model can have zero #vars, 
		 * in which case this sampler is eq. to LWSampler)
		 */
		for (Type generatedType : model.getTypes()) {
			
			Collection<POP> pops = generatedType.getPOPs();

			// set initial size of unused number statements for each type
			restPOPs.put(generatedType, new HashSet<POP>(pops));
			
			// init list of supported #vars
			waitingNumberVars.put(generatedType, new LinkedList<NumberVar>());

			for (POP pop : pops) {
				for (int i = 0; i < pop.getArgTypes().length; ++i) {
					objectsByType.put(pop.getArgTypes()[i], new ArrayList());
				}
				
				if (pop.getArgTypes().length == 0) {
					addNumberVar(new NumberVar(pop, Collections.EMPTY_LIST));
				}
			}
		}
		
		/**
		 * Determine what types serve as arguments to basic RVs. Initialize
		 * their object list to be empty. Add any RVs with no args to 
		 * the list of instantiable RVs, to be processed after all #vars.
		 */
		
		for (Function f : model.getFunctions()) {
			if (f instanceof RandomFunction) {
				for (int i = 0; i < f.getArgTypes().length; ++i) {
					objectsByType.put(f.getArgTypes()[i], new ArrayList());
				}

				if (f.getArgTypes().length == 0) {
					uninstRandomVars.add(new RandFuncAppVar((RandomFunction) f,
							Collections.EMPTY_LIST));
				}
			}
		}
		
		// Create initial object lists for those types. While doing so,
		// add uninstantiated variables that have these objects as arguments.
		for (Type type : objectsByType.keySet()) {
			if (type.isSubtypeOf(BuiltInTypes.INTEGER)) {
				addObjects(type, Collections.singleton(new Integer(0)));
				// intsAreArgs = true;
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
	}
	
	private void addNumberVar(NumberVar var) {
		Type type = var.pop().type();
		HashSet<POP> pops = restPOPs.get(type);
		pops.remove(var.pop());
		LinkedList<NumberVar> allNumVarsWithType = waitingNumberVars.get(type);
		allNumVarsWithType.add(var);
		if (pops.isEmpty()) {
			waitingNumberVars.remove(type);
			uninstNumberVars.add(allNumVarsWithType);
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
	
	/**
	 * For the generated new obj, take the Cartesian product with any existing
	 * objects of this arg type.
	 * 
	 * Note that if one type of var has not been instantiated at all, then 
	 * the Cartesian product will be empty.
	 */
	private void addFuncAppVars(RandomFunction f, Type newObjType,
			Collection newObjs) {
		for (Iterator iter = getAddedTupleIterator(Arrays.asList(f.getArgTypes()),
				newObjType, newObjs); iter.hasNext();) {
			List args = (List) iter.next();
			VarWithDistrib v = new RandFuncAppVar(f, args);
			if (Util.verbose()) {
				System.out.println("Adding uninstantiated var: " + v);
			}
			uninstRandomVars.add(v);
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

	// A list of non-number random functions that are supported. 
	// We assume for this sampler that the origin functions do not depend on
	// other random functions, so these can be sampled (LW) entirely separately
	// from #vars.
	ExtensibleLinkedList uninstRandomVars = new ExtensibleLinkedList();
	
	// #Vars that have some, but not all possible origin types instantiated.
	// In EECS, we cannot sample a #var until we have the full collection 
	// of them together. This is a list of RandFuncAppVar
	Map<Type, LinkedList<NumberVar>> waitingNumberVars 
		= new HashMap<Type, LinkedList<NumberVar>>();
	
	// #Vars that have all possible origin functions sampled, eg. ready for ECSS
	// sampling. This is a list of LinkedList<NumberVar>
	ExtensibleLinkedList uninstNumberVars = new ExtensibleLinkedList();
	
	// Remaining number of number statements for each type of variable
	// that is not fully supported.
	private Map<Type, HashSet<POP>> restPOPs = new HashMap<Type, HashSet<POP>>();
	
	// from Type to List of objects
	private Map<Type, List> objectsByType = new HashMap<Type, List>();
	
	private int intBound;
	private int depthBound;
}
