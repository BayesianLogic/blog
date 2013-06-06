package blog.engine.onlinePF.unused;

import blog.bn.BasicVar;
import blog.bn.RandFuncAppVar;
import blog.model.Function;
import blog.model.RandomFunction;
import blog.sample.AfterSamplingListener;

public class UpdateObservationListener implements AfterSamplingListener{

	@Override
	public void evaluate(BasicVar var, Object newValue, double prob) {	
		//sampling of observability values
		if (var instanceof RandFuncAppVar){
			RandFuncAppVar referencedVar = (RandFuncAppVar) var;
			RandomFunction obf = referencedVar.func().getObservableFun();
			if (obf != null){
				RandFuncAppVar observableVar = new RandFuncAppVar(obf, referencedVar.args(), false);
				
			}
		}
		return;
	}

}
