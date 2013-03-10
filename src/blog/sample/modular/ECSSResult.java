package blog.sample.modular;

public class ECSSResult {
	
	private int[] vals;
	private double weight;

	public ECSSResult(int[] vals, double weight) {
		this.vals = vals;
		this.weight = weight;
	}
	
	public int[] getVals() {
		return vals;
	}
	
	public double getWeight() {
		return weight;
	}
}
