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

	public boolean innerStateEquals (InverseParticle p){
		return (((AbstractPartialWorld) this.curWorld).innerStateEquals((AbstractPartialWorld) p.curWorld));
	}
	public InverseParticle(Set idTypes, int numTimeSlicesInMemory,
			Sampler sampler) {
		super(idTypes, numTimeSlicesInMemory, sampler);
	}
	
	public InverseParticle copy(){
		InverseParticle copy = new InverseParticle(idTypes, numTimeSlicesInMemory, sampler);
		DefaultPartialWorld newWorld = (DefaultPartialWorld) ((DefaultPartialWorld) curWorld)
				.clone();
		copy.setWorld(newWorld);
		return copy;
	}

	public boolean equals (Object o){
		return ((AbstractPartialWorld) curWorld).innerStateEquals((AbstractPartialWorld)(((InverseParticle)o).curWorld));
	}
	public int hashCode(){
		return ((AbstractPartialWorld) curWorld).hashCode();
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

}
