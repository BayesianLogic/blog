package blog.objgen;

import blog.type.Timestep;

public class TimestepNode extends IntegerNode {
	protected Object correspondingObj(Integer i) {
		return Timestep.at(i.intValue());
	}
}