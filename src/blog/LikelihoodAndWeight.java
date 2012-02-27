package blog;

/**
 * A class for representing likelihoods coming from proposed distributions.
 */
public class LikelihoodAndWeight {

    public LikelihoodAndWeight() {
    }

    public LikelihoodAndWeight(double likelihood, double weight) {
	this.likelihood = likelihood;
	this.weight = weight;
    }
    
    /** The value that must be taken in computing the final results from sampling. */
    public double weightedLikelihood() {
	return likelihood*weight;
    }
    
    /** In-place multiplication of likelihood and weight by those of another LikelihoodAndWeight. */
    public void add(LikelihoodAndWeight another) {
	likelihood += another.likelihood;
	weight     += another.weight;
    }

    /** In-place multiplication of likelihood and weight by those of another LikelihoodAndWeight. */
    public void multiply(LikelihoodAndWeight another) {
	likelihood *= another.likelihood;
	weight     *= another.weight;
    }

    public void scalarProduct(double scalar) {
	likelihood *= scalar;
	weight     *= scalar;
    }
    
    public String toString() {
        return "(" + likelihood + ", " + weight + ")";
    }
    
    public double likelihood = 0.0;
    public double weight = 0.0;
    
}
