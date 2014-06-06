package blog.engine.pbvi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.bn.RandFuncAppVar;
import blog.common.Util;

public class LiftedProperties {
	private Set<Object> ngos;
	private Map<RandFuncAppVar, Object> properties;
	private Map<Object, Set<RandFuncAppVar>> objToProperties;
	
	
	public LiftedProperties() {
		ngos = new HashSet<Object>();
		properties = new HashMap<RandFuncAppVar, Object>();
		objToProperties = new HashMap<Object, Set<RandFuncAppVar>>();
	}
	
	public void addObject(Object obj) {
		ngos.add(obj);
		objToProperties.put(obj, new HashSet<RandFuncAppVar>());
	}
	
	public void addProperty(RandFuncAppVar var, Object value) {
		properties.put(var, value);
		Object[] args = var.args();
		for (Object a : args) {
			if (objToProperties.containsKey(a)) {
				objToProperties.get(a).add(var);
			}
		}
	}
	
	public Map<Object, Object> findNgoSubstitution(Set<Object> ngos, LiftedProperties other) {
		return findNgoSubstitution(new HashSet<Object>(ngos), 
				new HashSet<Object>(other.ngos), 
				getRelevantProperties(ngos), 
				new HashMap<RandFuncAppVar, Object>(other.properties), 
				new HashMap<Object, Object>());		
	}
	
	// TODO: Right now, assuming not more than one ngo in a var
	private Map<RandFuncAppVar, Object> getRelevantProperties(
			Set<Object> ngos) {
		Map<RandFuncAppVar, Object> result = new HashMap<RandFuncAppVar, Object>();
		for (Object ngo : ngos) {
			Set<RandFuncAppVar> leftSideProperties = objToProperties.get(ngo);
			for (RandFuncAppVar var : leftSideProperties) {
				result.put(var, properties.get(var));
			}
		}
		
		return result;
	}

	public Map<Object, Object> findNgoSubstitution(LiftedProperties other) {
		return findNgoSubstitution(new HashSet<Object>(ngos), 
				new HashSet<Object>(other.ngos), 
				properties, 
				other.properties, 
				new HashMap<Object, Object>());
	}
	
	//my objs -> other objs
	private Map<Object, Object> findNgoSubstitution(
			Set<Object> myNgos,
			Set<Object> otherNgos,
			Map<RandFuncAppVar, Object> myProperties,
			Map<RandFuncAppVar, Object> otherProperties,
			Map<Object, Object> partialSolution) {
		if (myNgos.size() > otherNgos.size()) return null;	
		if (myNgos.isEmpty()) return partialSolution;
		Object ngo = (Object) Util.getFirst(myNgos);
		myNgos.remove(ngo);
		for (Object otherNgo : otherNgos) {
			//if (!ngo.getType().equals(otherNgo.getType())) continue;
			//check for conflict using new matching ngo -> otherNgo
			Map<RandFuncAppVar, Object> newMyProperties = new HashMap<RandFuncAppVar, Object>();
			Map<RandFuncAppVar, Object> newOtherProperties = new HashMap<RandFuncAppVar, Object>(otherProperties);
			boolean conflict = false;
			for (RandFuncAppVar v : myProperties.keySet()) {
				Object[] args = v.args().clone();
				boolean containsUnsubstituted = false;
				for (int i = 0; i < args.length; i++) {
					Object a = args[i];
					if (!(a instanceof Object)) continue;
					if (a.equals(ngo)) {
						args[i] = otherNgo;
					} else if (partialSolution.containsKey(a)) {
						args[i] = partialSolution.get(a);
					} else {
						containsUnsubstituted = true;
						break;
					}
				}
				if (containsUnsubstituted) {
					newMyProperties.put(v, myProperties.get(v));
				} else {
					RandFuncAppVar substitutedVar = new RandFuncAppVar(v.func(), args);
					if (otherProperties.containsKey(substitutedVar)) {
						Object otherValue = otherProperties.get(substitutedVar);
						Object myValue = myProperties.get(v);
						if (!myValue.equals(otherValue)) { //conflict
							conflict = true;
							break;
						} else {
							newOtherProperties.remove(substitutedVar);
						}
					} else {
						conflict = true;
						break;
					}
				}
			}
			if (conflict) continue;
			
			//new partial solution = partial solution + (ngo, otherNgo)
			Map<Object, Object> newPartialSolution = new HashMap<Object, Object>(partialSolution);
			newPartialSolution.put(ngo, otherNgo);
			
			//solution = recurse with new partial solution, unresolved properties and unmatched ngos
			Set<Object> newOtherNgos = new HashSet<Object>(otherNgos);
			newOtherNgos.remove(otherNgo);
			Map<Object, Object> solution =  
					findNgoSubstitution(
					myNgos,
					newOtherNgos,
					newMyProperties,
					newOtherProperties,
					newPartialSolution);
			if (solution != null)
				return solution;
		}
		myNgos.add(ngo);
		return null;
	}
	
	@Override
	public String toString() {
		return properties.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LiftedProperties)) return false;
		return findNgoSubstitution((LiftedProperties) other) != null;
	}
}
