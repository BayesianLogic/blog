package blog.sample.modular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import blog.bn.NumberVar;
import blog.distrib.CondProbDistrib;
import blog.model.CardinalitySpec;
import blog.model.DependencyModel;
import blog.model.Evidence;
import blog.model.Function;
import blog.model.ImplicitSetSpec;
import blog.model.Model;
import blog.model.NonGuaranteedObject;
import blog.model.OriginFunction;
import blog.model.POP;
import blog.model.Type;
import blog.model.ValueEvidenceStatement;
import blog.objgen.ObjectSet;
import blog.sample.ClassicInstantiatingEvalContext;
import blog.sample.LWSampler;
import blog.world.DefaultPartialWorld;

public class GeraldECSSSampler extends LWSampler {
	
	// Decision decisions/assumptions:
	// Right now it only works for evidence cardinality functions
	// Used a fringe/queue implemenation instead of automatic calling from parent to child
	// Assumed you can sample entire pop after type, instead of partially along the way
	// TODO:Regenerating distributions is costly, so we cache them
	// max int is tweakable
	// run time will also be proportional to the number of distributions we generate
	// instead of generating distrubtions for "MAX_INT" could we instead check when the prob gets so low that its no longer worth generating?
	
	public GeraldECSSSampler(Model model, Properties properties) {
		super(model, properties);
	}
	
	@Override
	public void nextSample() {
		System.out.println("New Sample ==== ");
		this.curWorld = new GeraldECSSWorld(model, evidence);
		
		weight = 1;
		
		if (!evidence.isTrue(curWorld)) {
			weight = 0;
		}
		System.out.println("Weight: " + weight);
		
		++totalNumSamples;
		++numSamplesThisTrial;
		if (weight > 0) {
			++totalNumConsistent;
			++numConsistentThisTrial;
		}
		sumWeightsThisTrial += weight;
	}
}

class GeraldECSSWorld extends DefaultPartialWorld {
	
	public GeraldECSSWorld(Model model, Evidence evidence) {
		this.model = model;
		this.evidence = evidence;
		init();
	}
	
	private void init() {
		
		// Grab the type and value of cardinality evidence and save it to cardEvidence
		for (Object obj : evidence.getValueEvidence()) {
			ValueEvidenceStatement ves = (ValueEvidenceStatement) obj;
			CardinalitySpec leftSide = (CardinalitySpec) ves.getLeftSide();
			ImplicitSetSpec setSpec = (ImplicitSetSpec) leftSide.getSetSpec();
			Type type = setSpec.getType();
			Integer val = (Integer)ves.getObservedValue();
			
			cardEvidence.put(type, val);
			
		}
		
		for (Type type: model.getTypes()) {
			
			typeToUnsampledPOPs.put(type, new HashSet<POP>(type.getPOPs()));

			
			if (cardEvidence.containsKey(type)) {
				Set<Type> originTypes = new HashSet<Type>();
				for (OriginFunction origin: type.getOriginFunctions()) {
					Type originType = origin.getRetType();
					originTypes.add(originType);
					
					// create a reverse mapping of the type and everything that depends on it
					Set<Type> reverseOriginTypes;
					if (reverseOrigin.containsKey(originType)) {
						reverseOriginTypes = reverseOrigin.get(originType);
					} else {
						reverseOriginTypes = new HashSet<Type>();
						reverseOrigin.put(originType, reverseOriginTypes);
					}
					reverseOriginTypes.add(type);
					
				}
				
				typeToOriginTypes.put(type, originTypes);
			} else {
				for (POP pop : type.getPOPs()) {
					if (pop.getArgTypes().length == 0) {
						addNumberVar(new NumberVar(pop, Collections.EMPTY_LIST));
					}
				}
			}
		}
		
		for (Function function : model.getFunctions()) {
		}
		
		sampleVars();
	}
	
	private void sampleVars() {
		
		while (readyVars.size() > 0) {
			List<NumberVar> vars = readyVars.remove(0);
			
			// Sampling one at a time, so perform parent based sampling
			if (vars.size() == 1) {
				NumberVar var = vars.remove(0);
				DependencyModel.Distrib distrib = var.getDistrib(new ClassicInstantiatingEvalContext(this));
				CondProbDistrib cpd = distrib.getCPD();
				
				List args = distrib.getArgValues();
				Type varType = var.getType();
				Object value = cpd.sampleVal(args, varType);
				System.out.println("Setting " + var + " to " + value);
				this.setValue(var, value);
				
				// DECISION: Chose arraylist for ease of selection. pretty arbitrary decision
				List<NumberVar> nvs;
				Type type = var.pop().type();
				if (typeToNVs.containsKey(type)) {
					nvs = typeToNVs.get(type);
				} else {
					nvs = new ArrayList<NumberVar>();
					typeToNVs.put(type, nvs);
				}
				nvs.add(var);
				
				removePOP(var.pop());
				
				// since it is sampled, now go to type to unsampled and check if set is empty.
				
				
				// if type to unsampled is now empty, thet type is now fully sampled, so check "type to dependent pops" and push new
			
			} else {
				//sample using ecss
				double[][] prob = new double[vars.size()][MAX_INT + 1];
				int i = 0;
				System.out.println("About to sample using ECSS");
				for (NumberVar var : vars) {
					DependencyModel.Distrib distrib = var.getDistrib(new ClassicInstantiatingEvalContext(this));
					CondProbDistrib cpd = distrib.getCPD();
					List args = distrib.getArgValues();
					for (int j = 0; j <= MAX_INT; j++) {
						prob[i][j] = cpd.getProb(args, j);
					}
					System.out.println("HI");
					i++;
				}
				
				Type type = vars.get(0).pop().type();
				int[] values = ECSSSampler.ecss(cardEvidence.get(type), MAX_INT, prob); 
				
				i = 0;
				for (NumberVar var : vars) {
					this.setValue(var, values[i]);
					i++;
				}
				
				// update unsampled
				for (NumberVar var : vars) {
					removePOP(var.pop());
				}
				
				
				
			}		
		}
		
	}
	
	private void addNumberVar(NumberVar var) {
		List<NumberVar> vars = new LinkedList<NumberVar>();
		vars.add(var);
		readyVars.add(vars);
	}
	

	@SuppressWarnings("unchecked")
	private void addNumberVars(Type type) {
		List<NumberVar> vars = new LinkedList<NumberVar>();
		for (POP pop : type.getPOPs()) {
			Type[] argTypes = pop.getArgTypes();
			if (argTypes.length == 0) {
				vars.add(new NumberVar(pop, Collections.EMPTY_LIST));
			} else if (argTypes.length == 1) {
				List<NumberVar> nvs = typeToNVs.get(argTypes[0]);
				
				// TODO: figure out how to use multiple number vars in satisfier since this currently uses just one
				ObjectSet satisfiers = getSatisfiers(nvs.get(0));
				
				for( Object satisfier : satisfiers.toArray()) {
					List<NonGuaranteedObject> ngos = new ArrayList<NonGuaranteedObject>();
					ngos.add((NonGuaranteedObject)satisfier);
					vars.add(new NumberVar(pop, ngos));
				}
			} else {
				// TODO: not sure how to implement because satisfier() only takes one numbervar
				System.out.println("UNIMPLEMENTED ERROR: How to handle multiple origin funcs");
			}	
		}
		readyVars.add(vars);
		
	}
	
	// maintain the data structures by removing the POP
	private void removePOP(POP pop) {
		Type type = pop.type();
		
		if (typeToOriginTypes.containsKey(type) && typeToOriginTypes.get(type).size() == 0) {
			typeToOriginTypes.remove(type);
		}
		
		Set<POP> unsampledPOPs = typeToUnsampledPOPs.get(type);
		unsampledPOPs.remove(pop);
		if (unsampledPOPs.size() > 0) {
			return;
		}
		typeToUnsampledPOPs.remove(type);
		
		
		if (!reverseOrigin.containsKey(type)) {
			return;
		}
		Set<Type> childTypes = reverseOrigin.remove(type);
		for (Type childType : childTypes) {
			Set<Type> parentTypes = typeToOriginTypes.get(childType);
			parentTypes.remove(type);
			if (parentTypes.size() == 0) {
				addNumberVars(childType);
				
			}
		}
	}
	
	public static final int MAX_INT = 100;
	
	private Model model;
	private Evidence evidence;
	
	// #var types that have a cardinality evidence attached to them and therefore need all origin function to be
	// sampled before can sample the evidence of the type
	private Map<Type, Integer> cardEvidence = new HashMap<Type, Integer>();
	
	// Mapping of type to remaining POP's 
	// These POP's have not yet been sampled
	private Map<Type, Set<POP>> typeToUnsampledPOPs = new HashMap<Type, Set<POP>>();
	
	// Ready to sample
	private List<List<NumberVar>> readyVars = new LinkedList<List<NumberVar>>();
	
	// A type blip and the types that it is still waiting for
	private Map<Type, Set<Type>> typeToOriginTypes = new HashMap<Type, Set<Type>>();
	
	// The reverse of the "typeToOriginTypes" data structure. This contains airplane and all the types that depend on it
	private Map<Type, Set<Type>> reverseOrigin = new HashMap<Type, Set<Type>>();
	
	// A mapping from type to the number vars used to generate partial worlds for figuring out distributions
	private Map<Type, List<NumberVar>> typeToNVs = new HashMap<Type, List<NumberVar>>();
	
	
}