package blog.objgen;

import blog.Timestep;

public class TimestepNode extends IntegerNode {
	protected Object correspondingObj(Integer i) {
		return Timestep.at(i.intValue());
	}
}