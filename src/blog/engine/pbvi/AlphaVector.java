package blog.engine.pbvi;

import blog.engine.onlinePF.PFEngine.PFEngineOnline;
import blog.world.PartialWorld;

public interface AlphaVector {
	public Double getValue(PartialWorld w);
	public Double getValue(PFEngineOnline b);
	public void setValue(PartialWorld w, Double value);
	public void normalizeValues(Integer denom);
}
