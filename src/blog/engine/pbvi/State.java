package blog.engine.pbvi;

import blog.world.AbstractPartialWorld;

public class State {
	private AbstractPartialWorld world;
	private int timestep;
	
	public State(AbstractPartialWorld w, int t) {
		world = w;
		timestep = t;
	}
	
	public AbstractPartialWorld getWorld() {
		return world;
	}
	
	public int getTimestep() {
		return timestep;
	}
	
	@Override
	public int hashCode() {
		return world.innerStatehashCode(timestep);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof State))
			return false;
		State s = (State) o;
		return s.world.innerStateEquals(world, timestep);
		
	}
}
