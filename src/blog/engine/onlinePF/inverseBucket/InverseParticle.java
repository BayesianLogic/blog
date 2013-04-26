package blog.engine.onlinePF.inverseBucket;

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


}
