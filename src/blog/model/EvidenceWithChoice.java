package blog.model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import blog.BLOGUtil;
import blog.bn.BasicVar;
import blog.bn.BayesNetVar;
import blog.common.Util;
import blog.distrib.ListInterp;
import blog.world.PartialWorld;
import blog.model.NonRandomFunction;

	/*added by cheng*/
	public class EvidenceWithChoice extends Evidence{
		@Override
		public boolean isEmpty() {
			return getValueEvidence().isEmpty() && getSymbolEvidence().isEmpty() && getChoiceEvidence().isEmpty();
		}
		public Collection getChoiceEvidence() {
			return Collections.unmodifiableCollection(choiceEvidence);
		}

		
		@Override
		public double setEvidenceEnsureSupportedAndReturnLikelihood(
				PartialWorld curWorld) {
			setEvidenceAndEnsureSupported(curWorld);
			return getEvidenceProb(curWorld);
		}

		@Override
		public void setEvidenceAndEnsureSupported(PartialWorld curWorld) {
			setFixedFuncInterp(curWorld);
			BLOGUtil.setBasicVars(this, curWorld);
			BLOGUtil.ensureDetAndSupported(getEvidenceVars(), curWorld);
		}
		private void setFixedFuncInterp(PartialWorld world) {
			for (DecisionEvidenceStatement ces : choiceEvidence) {
				FuncAppTerm fat = (FuncAppTerm) ces.getLeftSide();
				NonRandomFunction f = (NonRandomFunction) fat.getFunction();
				ArgSpec[] args = fat.getArgs();
				Object[] argValues = new Object[args.length];
				for (int i = 0; i < args.length; ++i) {
					argValues[i] = args[i].getValueIfNonRandom();
				}
				((ListInterp) f.getInterpretation()).add(Arrays.asList(argValues));
			}
		}
		public void addChoiceEvidence(DecisionEvidenceStatement evid) {
			choiceEvidence.add(evid);
		}
 
		@Override
		public boolean checkTypesAndScope(Model model) {
			boolean correct = true;

			for (Iterator iter = choiceEvidence.iterator(); iter.hasNext();) {
				DecisionEvidenceStatement stmt = (DecisionEvidenceStatement) iter.next();
				if (!stmt.checkTypesAndScope(model)) {
					correct = false;
				}
			}
			return super.checkTypesAndScope(model) & correct;
		}
		@Override
		public int compile() {
			int errors = 0;
			LinkedHashSet callStack = new LinkedHashSet();

			for (Iterator iter = choiceEvidence.iterator(); iter.hasNext();) {
				DecisionEvidenceStatement stmt = (DecisionEvidenceStatement) iter.next();
				int thisStmtErrors = stmt.compile(callStack);
				errors += thisStmtErrors;
			}

			return errors+super.compile();
		}
		// List of ValueEvidenceStatement
		private List<DecisionEvidenceStatement> choiceEvidence = new ArrayList<DecisionEvidenceStatement>();

	}