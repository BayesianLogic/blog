package blog.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbols used across the whole life cycle of blog program. It will ensure that
 * Symbols with the same name could be directly compared with ==.
 * 
 * @author leili
 * @date Apr 7, 2012
 * 
 */
public class Symbol {
	private String name;

	/**
	 * factory pattern
	 * 
	 * @param n
	 */
	private Symbol(String n) {
		name = n;
	}

	private static Map<String, Symbol> dict = new HashMap<String, Symbol>();

	public String toString() {
		return name;
	}

	/**
	 * Make return the unique symbol associated with a string. Repeated calls to
	 * <tt>symbol("abc")</tt> will return the same Symbol.
	 */
	public static Symbol Symbol(String n) {
		String u = n.intern();
		Symbol s = dict.get(u);
		if (s == null) {
			s = new Symbol(u);
			dict.put(u, s);
		}
		return s;
	}
}
