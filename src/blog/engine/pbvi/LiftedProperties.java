package blog.engine.pbvi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blog.bn.RandFuncAppVar;
import blog.common.Util;
import blog.model.FuncAppTerm;

public class LiftedProperties {
	public boolean debug = false;
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
	
	public Set<Object> getObjects() {
		return Collections.unmodifiableSet(ngos);
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
		if (debug) {
			System.out.println("findNgoSubstition" + myNgos + otherNgos + myProperties + otherProperties + partialSolution);
		}
		if (myNgos.size() != otherNgos.size()) return null;	
		if (myNgos.isEmpty()) return partialSolution;
		Object ngo = (Object) Util.getFirst(myNgos);
		
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
					if (!myNgos.contains(a)) continue;
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
					if (debug) {
						System.out.println("findNgoSubstition substitutedVar" + substitutedVar + " ngo" + ngo + " otherNgo" + otherNgo);
					}
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
			if (debug && conflict) {
				System.out.println("findNgoSubstition conflict");
			}
			if (conflict) continue;
			
			//new partial solution = partial solution + (ngo, otherNgo)
			Map<Object, Object> newPartialSolution = new HashMap<Object, Object>(partialSolution);
			newPartialSolution.put(ngo, otherNgo);
			
			//solution = recurse with new partial solution, unresolved properties and unmatched ngos
			Set<Object> newOtherNgos = new HashSet<Object>(otherNgos);
			newOtherNgos.remove(otherNgo);
			Set<Object> myNewNgos = new HashSet<Object>(myNgos);
			myNewNgos.remove(ngo);
			Map<Object, Object> solution =  
					findNgoSubstitution(
					myNewNgos,
					newOtherNgos,
					newMyProperties,
					newOtherProperties,
					newPartialSolution);
			if (solution != null)
				return solution;
		}
		return null;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (Object ngo1 : ngos) {
			for (Object ngo2 : ngos) {
				if (ngo1.hashCode() == ngo2.hashCode() && !ngo1.equals(ngo2)) {
					FuncAppTerm ngo1Term = (FuncAppTerm) ngo1;
					FuncAppTerm ngo2Term = (FuncAppTerm) ngo2;
					ngo1Term.equalsDebug(ngo2Term);
				}
					
			}
		}
		return s + ngos + " " + properties.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LiftedProperties)) return false;
		return findNgoSubstitution((LiftedProperties) other) != null;
	}

	public void add(LiftedProperties other) {
		for (Object ngo : other.ngos) {
			this.addObject(ngo);
		}
		for (RandFuncAppVar var : other.properties.keySet()) {
			this.addProperty(var, other.properties.get(var));
		}	
	}
	
	public LiftedProperties replace(Object old, Object n) {
		LiftedProperties result = new LiftedProperties();
		for (Object ngo : ngos) {
			result.addObject(ngo);
		}
		for (RandFuncAppVar var : properties.keySet()) {
			Object[] args = var.args().clone();
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals(old))
					args[i] = n;
			}
			result.addProperty(new RandFuncAppVar(var.func(), args), properties.get(var));
		}
		return result;
	}
}
