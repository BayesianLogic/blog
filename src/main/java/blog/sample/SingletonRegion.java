/**
 * 
 */
package blog.sample;

/**
 * @author leili
 * @since Apr 25, 2012
 * 
 */
public class SingletonRegion extends Region {

	private Object value;

	public SingletonRegion(Object value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object value) {
		return this.value == value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see blog.sample.Region#getOneValue()
	 */
	@Override
	public Object getOneValue() {
		return value;
	}

}
