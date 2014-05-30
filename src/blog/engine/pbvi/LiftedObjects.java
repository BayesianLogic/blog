package blog.engine.pbvi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.model.NonGuaranteedObject;

public class LiftedObjects {
	private Set<NonGuaranteedObject> ngos;
	private Map<RandFuncAppVar, Object> properties;
	
	public LiftedObjects() {
		ngos = new HashSet<NonGuaranteedObject>();
		properties = new HashMap<RandFuncAppVar, Object>();
	}

	public void addObject(NonGuaranteedObject obj) {
		ngos.add(obj);
	}
	
	public void addProperty(RandFuncAppVar var, Object value) {
		properties.put(var, value);
	}
	
	public Map<NonGuaranteedObject, NonGuaranteedObject> findNgoSubstitution(LiftedObjects other) {
		return findNgoSubstitution(new HashSet<NonGuaranteedObject>(ngos), 
				new HashSet<NonGuaranteedObject>(other.ngos), 
				properties, 
				other.properties, 
				new HashMap<NonGuaranteedObject, NonGuaranteedObject>());
	}
	
	private Map<NonGuaranteedObject, NonGuaranteedObject> findNgoSubstitution(
			Set<NonGuaranteedObject> myNgos,
			Set<NonGuaranteedObject> otherNgos,
			Map<RandFuncAppVar, Object> myProperties,
			Map<RandFuncAppVar, Object> otherProperties,
			Map<NonGuaranteedObject, NonGuaranteedObject> partialSolution) {
		if (myNgos.size() != otherNgos.size()) return null;	
		if (myNgos.isEmpty()) return partialSolution;
		NonGuaranteedObject ngo = (NonGuaranteedObject) Util.getFirst(myNgos);
		myNgos.remove(ngo);
		for (NonGuaranteedObject otherNgo : otherNgos) {
			if (!ngo.getType().equals(otherNgo.getType())) continue;
			//check for conflict using new matching ngo -> otherNgo
			Map<RandFuncAppVar, Object> newMyProperties = new HashMap<RandFuncAppVar, Object>(myProperties);
			Map<RandFuncAppVar, Object> newOtherProperties = new HashMap<RandFuncAppVar, Object>();
			boolean conflict = false;
			for (RandFuncAppVar v : otherProperties.keySet()) {
				Object[] args = v.args().clone();
				boolean containsUnsubstituted = false;
				for (int i = 0; i < args.length; i++) {
					Object a = args[i];
					if (a instanceof NonGuaranteedObject) continue;
					if (a.equals(otherNgo)) {
						args[i] = ngo;
					} else if (partialSolution.containsKey(a)) {
						args[i] = partialSolution.get(a);
					} else {
						containsUnsubstituted = true;
						break;
					}
				}
				if (containsUnsubstituted) {
					newOtherProperties.put(v, otherProperties.get(v));
				} else {
					RandFuncAppVar substitutedVar = new RandFuncAppVar(v.func(), args);
					if (myProperties.containsKey(substitutedVar)) {
						Object myValue = myProperties.get(substitutedVar);
						Object otherValue = otherProperties.get(v);
						if (!myValue.equals(otherValue)) { //conflict
							conflict = true;
							break;
						} else {
							newMyProperties.remove(substitutedVar);
						}
					} else {
						conflict = true;
						break;
					}
				}
			}
			if (conflict) continue;
			
			//new partial solution = partial solution + (ngo, otherNgo)
			Map<NonGuaranteedObject, NonGuaranteedObject> newPartialSolution = new HashMap<NonGuaranteedObject, NonGuaranteedObject>(partialSolution);
			newPartialSolution.put(ngo, otherNgo);
			
			//solution = recurse with new partial solution, unresolved properties and unmatched ngos
			otherNgos.remove(otherNgo);
			Map<NonGuaranteedObject, NonGuaranteedObject> solution =  
					findNgoSubstitution(
					myNgos,
					otherNgos,
					newMyProperties,
					newOtherProperties,
					newPartialSolution);
			if (solution != null)
				return solution;
			otherNgos.add(otherNgo);
		}
		myNgos.add(ngo);
		return null;
	}
}
