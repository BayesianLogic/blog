package blog.engine.onlinePF.inverseBucket;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import blog.world.AbstractPartialWorld;
import blog.world.DefaultPartialWorld;
import blog.world.PartialWorld;
import blog.engine.Particle;
import blog.model.ArgSpecQuery;
import blog.model.Query;
import blog.sample.Sampler;

/**
 * warning: this works assuming that there are NO NUMBER STATEMENTS!
 * @author saasbook
 *
 */
public class InverseParticle extends Particle{

	public InverseParticle(Set idTypes, int numTimeSlicesInMemory,
			Sampler sampler) {
		super(idTypes, numTimeSlicesInMemory, sampler);
	}
	
	public InverseParticle copy(){
		InverseParticle copy = new InverseParticle(idTypes, numTimeSlicesInMemory, sampler);
		copy.timeStep=this.timeStep;
		DefaultPartialWorld newWorld = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
				.clone();
		copy.setWorld(newWorld);
		copy.weight = 1;
		copy.parent=this;
		
		return copy;
	}

	public boolean equals (Object o){
		if (UniversalBenchmarkTool.rememberHistory)
			return this.parent==((InverseParticle)o).parent && ((AbstractPartialWorld) curWorld).innerStateEquals((AbstractPartialWorld)(((InverseParticle)o).curWorld), this.timeStep);
		else
			return ((AbstractPartialWorld) curWorld).innerStateEquals((AbstractPartialWorld)(((InverseParticle)o).curWorld), this.timeStep);
	}
	public int hashCode(){
		if (!cached){
			if (UniversalBenchmarkTool.rememberHistory)
				if (this.parent == null)
					cachedhashcode = ((AbstractPartialWorld) curWorld).innerStatehashCode(this.timeStep);
				else
					cachedhashcode = this.parent.hashCode() ^ ((AbstractPartialWorld) curWorld).innerStatehashCode(this.timeStep);
			cached = true;
			return cachedhashcode;
		}
		else
			return cachedhashcode;
	}
	
	/**
	 * 
	 * @param queries the list of queries to be answered
	 * @param weight the weight for this particle (actually the weight for
	 * a particular observation sequence in the state for this particle
	 */
	public void updateQueriesStats(Collection<Query> queries, Double weight) {
		for (Query q : queries) {
			q.updateStats(getLatestWorld(), weight);
		}
	}

	private InverseParticle parent;
	private int timeStep = 0;
	public int getTimestep(){
		return timeStep;
	}
	public void advanceTimestep(){
		timeStep ++;
	}
	private int cachedhashcode = 0;
	private boolean cached = false;
}
