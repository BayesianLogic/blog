package blog.engine.onlinePF.runner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import blog.bn.BayesNetVar;
import blog.engine.onlinePF.ObservabilitySignature;
import blog.engine.onlinePF.ObservableRandomFunction;
import blog.engine.onlinePF.PFEngine.PFEngineOP;
import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.engine.onlinePF.absyn.PolicyModel;
import blog.engine.onlinePF.evidenceGenerator.EvidenceQueryDecisionGeneratorwPolicy;
import blog.engine.onlinePF.inverseBucket.TimedParticle;
import blog.model.Evidence;
import blog.model.FuncAppTerm;
import blog.model.Model;
import blog.model.RandomFunction;
import blog.model.ValueEvidenceStatement;

/**
 * A kind of particle filter runner, specialized to handle particle filters which are partitioned (bucketed) by observation variables
 * Furthermore, it handles evidence input.
 * @author cheng
 *
 */
public class PFRunnerObsCWA extends PFRunnerOnline{
	/**
	 * Overloaded constructor Main difference is it replaces the 
	 * - particleFilter with a PFEngineSampled
	 * - evidenceGenerator with EvidenceGeneratorwPolicy
	 * @param model see parent
	 * @param queryStrings see parent 
	 * @param particleFilterProperties see parent
	 * @param pm a file that specifies the policy model to be used.
	 */
	public PFRunnerObsCWA(Model model,
			Collection queryStrings, Properties particleFilterProperties, PolicyModel pm) {
		super(model, particleFilterProperties, 15);
		
		particleFilter = new PFEngineOP(model, particleFilterProperties);
		for (RandomFunction orf: (List<RandomFunction>) model.getObsFun()){
			queryStrings.add(((ObservableRandomFunction) orf).queryString);
		}		
		evidenceGenerator = new EvidenceQueryDecisionGeneratorwPolicy(model, queryStrings, eviCommunicator, queryResultCommunicator, pm);
	}
	
	/**
	 * Overridden advancePhase1 to augment evidence with observations that ensure cwa
	 */
	@Override
	public void advancePhase1() {
		Evidence evidence;
		Collection queries;
		beforeEvidenceAndQueries();
		if ((evidence = evidenceGenerator.getLatestObservation()) == null 
				| (queries = evidenceGenerator.getLatestQueries()) == null){
			System.err.println("Evidence/Query should not be null");
			System.exit(1);
		}
		particleFilter.beforeTakingEvidence();
		this.includeCWA(evidence, queries);
		particleFilter.take(evidence);
		particleFilter.answer(queries);
		particleFilter.afterAnsweringQueries();
		
		postEvidenceAndQueryIO();
	}
	
	/**
	 * somewhat hacky, gets a single particle from the particle filter
	 * @param pf
	 */
	private TimedParticle getRepresentative(PFEngineOnline pf){
		return pf.particles.get(0).copy();
	}
	
	private void includeCWA(Evidence evidence, Collection queries){
		TimedParticle tmp = getRepresentative(particleFilter);
		tmp.answer(queries);
		ObservabilitySignature newOS = ObservabilitySignature.getOSbyIndex(tmp.getOS()).spawnChild(tmp);
		
		Collection evidenceValueEvStmt = evidence.getValueEvidence();
		HashSet<String> observedVar = new HashSet<String>();
		HashSet<String> observedObservable = new HashSet<String>();
		for (Object o : evidenceValueEvStmt){
			ValueEvidenceStatement v = (ValueEvidenceStatement) o;
			if (v.getLeftSide() instanceof FuncAppTerm){
				FuncAppTerm fat = (FuncAppTerm) v.getLeftSide();
				if (fat.getFunction() instanceof ObservableRandomFunction){
					observedVar.add(fat.toString());
					observedObservable.add("observable_"+fat.toString());
				}
			}
		}
		newOS.observedValues.clear();
		for (Iterator<BayesNetVar> i = newOS.unobservables.iterator(); i.hasNext();){
			BayesNetVar bnv = i.next();
			if (observedObservable.contains(bnv.toString())){
				newOS.observables.add(bnv);
				i.remove();
			}
		}
		for (Iterator<BayesNetVar> i = newOS.observables.iterator(); i.hasNext();){
			BayesNetVar bnv = i.next();
			if (!observedObservable.contains(bnv.toString())){
				newOS.unobservables.add(bnv);
				i.remove();
			}
		}
		Evidence newEv = newOS.getEvidence();
		
		for (Object o : newEv.getValueEvidence()){
			ValueEvidenceStatement v = (ValueEvidenceStatement) o;
			evidence.addValueEvidence(v);
		}
		evidence.checkTypesAndScope(model);
		if (evidence.compile()!=0)
			System.exit(1);
	}
}
