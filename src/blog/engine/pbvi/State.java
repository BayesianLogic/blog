package blog.engine.pbvi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blog.DBLOGUtil;
import blog.bn.BasicVar;
import blog.bn.NumberVar;
import blog.bn.RandFuncAppVar;
import blog.model.ArgSpec;
import blog.model.BuiltInTypes;
import blog.model.FuncAppTerm;
import blog.model.Function;
import blog.model.POP;
import blog.model.RandomFunction;
import blog.model.Type;
import blog.world.AbstractPartialWorld;
import blog.world.DefaultPartialWorld;

public class State {
	private AbstractPartialWorld world;
	private int timestep;

	public State(AbstractPartialWorld w, int t) {
		world = w;
		timestep = t;
		//zeroTimestep();
	}

	public AbstractPartialWorld getWorld() {
		return world;
	}

	public int getTimestep() {
		return timestep;
	}

	public void zeroTimestep(int zero) {
		Timer.start("zeroTimestep");
		Set<BasicVar> basicVars = new HashSet<BasicVar>(world.getChangedVars());
		for (BasicVar var : basicVars) {
			int timestep = DBLOGUtil.getTimestepIndex(var);
			if (timestep == -1 || timestep == zero) continue;
			Object value = world.getValue(var);
			Object[] args = var.args();
			BasicVar newVar = null;
			if (var instanceof RandFuncAppVar) {
				RandFuncAppVar funcAppVar = (RandFuncAppVar) var;
				RandomFunction f = funcAppVar.func();
				List newArgs = zeroTimestepArg(args, f.getArgTypes(), zero);
				newVar = new RandFuncAppVar(f, newArgs);
			} 
			
			if (var instanceof NumberVar) {
				NumberVar numVar = (NumberVar) var;
				POP pop = numVar.pop();
				List newArgs = zeroTimestepArg(args, pop.getArgTypes(), zero);
				newVar = new NumberVar(pop, newArgs);
			}
			if (newVar == null) {
				System.err.println("Cannot zero: " + var);
				System.exit(0);
			}
			world.setValue(newVar, value);
			world.setValue(var, null);
		}

		this.timestep = zero;
		Timer.record("zeroTimestep");
	}

	private List zeroTimestepArg(Object[] args, Type[] argTypes, int zero) {
		List newArgs = new ArrayList();
		for (int i = 0; i < argTypes.length; i++) {
			if (argTypes[i].equals(BuiltInTypes.TIMESTEP)) {
				newArgs.add(BuiltInTypes.TIMESTEP.getGuaranteedObject(zero));
			} else {
				newArgs.add(args[i]);
			}
		}
		return newArgs;
	}


	@Override
	public int hashCode() {
		AbstractPartialWorld w = (DefaultPartialWorld) ((DefaultPartialWorld) world).clone();
		State s = new State(w, timestep);
		s.zeroTimestep(0);
		return s.world.innerStatehashCode(0);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof State))
			return false;
		State s = (State) o;
		boolean equality = false; 
		Timer.start("STATE_COMPARISON");
		State toCompare = s;
		if (timestep != s.timestep) {
			AbstractPartialWorld w = (DefaultPartialWorld) ((DefaultPartialWorld) s.world).clone();
			toCompare = new State(w, s.timestep);
			toCompare.zeroTimestep(timestep);
			//return false;
		}
		equality = toCompare.world.innerStateEquals(world, timestep);
		Timer.record("STATE_COMPARISON");
		return equality;
		
	}
	
	@Override
	public String toString() {
		return "State: " + world.toString();
	}
}
