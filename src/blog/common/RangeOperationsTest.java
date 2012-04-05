package blog.common;

import static blog.common.RangeOperations.*;

import junit.framework.TestCase;

/** Tests for {@link RangeOperations}. */
public class RangeOperationsTest extends TestCase {

	public void test() {
		Object result;

		result = RangeOperations.run(Axis("x", 1, 5), X());
		System.out.println(result);
		assertEquals("[1, 2, 3, 4, 5]", result.toString());

		result = RangeOperations.run(Axis("x", 1, 5), Axis("y", 1, 2),
				Axis("z", 1, 3), new EnvironmentEntrySet());
		System.out.println(result);
		assertEquals(
				"[[[x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1], [x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1]], [[x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1], [x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1]], [[x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1], [x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1]], [[x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1], [x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1]], [[x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1], [x=1;y=1;z=1, x=1;y=1;z=1, x=1;y=1;z=1]]]",
				result.toString());

		result = RangeOperations.run(Axis("x", 1, 5), Axis("i", 1, 2),
				Axis("j", 1, 3), X());
		System.out.println(result);
		assertEquals(
				"[[[1, 1, 1], [1, 1, 1]], [[2, 2, 2], [2, 2, 2]], [[3, 3, 3], [3, 3, 3]], [[4, 4, 4], [4, 4, 4]], [[5, 5, 5], [5, 5, 5]]]",
				result.toString());

		result = RangeOperations.run(Axis("x", 1, 5), Axis("y", 1, 2),
				Summation("i", 1, 3), X());
		System.out.println(result);
		assertEquals(
				"[[3.0, 3.0], [6.0, 6.0], [9.0, 9.0], [12.0, 12.0], [15.0, 15.0]]",
				result.toString());

		result = RangeOperations.run(Axis("x", 1, 5), Summation("i", 1, 3), X());
		System.out.println(result);
		assertEquals("[3.0, 6.0, 9.0, 12.0, 15.0]", result.toString());

		result = RangeOperations.run(Summation("x", 1, 3), new Seven());
		System.out.println(result);
		assertEquals("21.0", result.toString());

		result = RangeOperations.run(Summation("x", 1, 3), X());
		System.out.println(result);
		assertEquals("6.0", result.toString());

		result = RangeOperations.run(Averaging("x", 0, 10), X());
		System.out.println(result);
		assertEquals("5.0", result.toString());

		result = RangeOperations.run(Averaging("x", 0, 10), Axis("y", 1, 3), X());
		System.out.println(result);
		assertEquals("[5.0, 5.0, 5.0]", result.toString());

		result = RangeOperations.run(Averaging("x", 0, 10), Axis("y", 1, 3),
				Axis("z", 1, 2), X());
		System.out.println(result);
		assertEquals("[[5.0, 5.0], [5.0, 5.0], [5.0, 5.0]]", result.toString());

	}

	public static class EnvironmentEntrySet extends DAEFunction {
		public Object evaluate(DependencyAwareEnvironment environment) {
			return Util.join(environment.entrySet(), ";");
		}
	}

	public static class Seven extends DAEFunction {
		public Object evaluate(DependencyAwareEnvironment environment) {
			return 7;
		}
	}

	public static class X extends DAEFunction {
		public Object evaluate(DependencyAwareEnvironment environment) {
			return environment.get("x");
		}
	}

	public static X X() {
		return new X();
	}
}
