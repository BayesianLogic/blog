package blog;

public class WeightedValue {
    public Object value;
    public double weight;
    public WeightedValue(Object value, double weight) {
	this.value = value;
	this.weight = weight;
    }
    
    public String toString() {
	return "(" + value + ", " + weight + ")";
    }
}
