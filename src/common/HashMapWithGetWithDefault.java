package common;

import java.util.HashMap;

/**
 * A {@link HashMap} with a get method that allows the user to provide a default in case the underlying value is <code>null</code>.
 */
public class HashMapWithGetWithDefault extends HashMap {
    public Object getWithDefault(Object key, Object defaultValue) {
	Object value = get(key);
	if (value == null)
	    return defaultValue;
	return value;
    }
}
