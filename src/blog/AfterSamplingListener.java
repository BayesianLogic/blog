package blog;

/** An interface for listeners to sampling. */
public interface AfterSamplingListener {
	public void evaluate(BasicVar var, Object newValue, double prob);
}