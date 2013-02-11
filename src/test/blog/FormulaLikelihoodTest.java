package test.blog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import expmt.FormulaLikelihood;

import blog.BLOGUtil;
import blog.common.Util;
import blog.model.DisjFormula;
import blog.model.Formula;
import blog.model.Model;


import junit.framework.TestCase;

public class FormulaLikelihoodTest extends TestCase {

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(FormulaLikelihoodTest.class);
	}

	private static String modelDescription = "type Aircraft;"
			+ "guaranteed Aircraft a, a2;" + "random Real Position(Aircraft);"
			+ "Position(a) = 0;" + "" + "type Blip;" + "guaranteed Blip b, b2;"
			+ "origin Aircraft Source(Blip);" + "random Real ApparentPos(Blip);"
			+ "ApparentPos(blip) ~ Gaussian(Position(Source(blip)));" + ""
			+ "random Boolean Sunny;" + "Sunny = true;" + ""
			+ "random Boolean Windy;" + "Windy = true;" + "" + "random Blip B1;"
			+ "B1 ~ UniformChoice({Blip b});" + "" + "random Blip B2;"
			+ "B2 ~ UniformChoice({Blip b});" + "" + "random Boolean f(Real);"
			+ "f(r) = true;";

	public void testAreMutuallyExclusiveLiteralsGivenIndependence() {
		Model model = Model.readFromString(modelDescription);

		Object[][] tests = { { "ApparentPos(b) = 3", "ApparentPos(b) = 3", false },
				{ "ApparentPos(b) = 3", "ApparentPos(b) = 4", true },
				{ "Position(a) = 3", "ApparentPos(b) = 3", false },
				{ "Position(a) = 3", "ApparentPos(b) = 4", false },

				{ "Sunny", "ApparentPos(b) = 4", false },
				{ "!Sunny", "ApparentPos(b) = 4", false }, { "Sunny", "!Sunny", true },
				{ "Sunny", "Sunny", false }, { "Sunny", "Windy", false },
				{ "Sunny", "!Windy", false }, { "Windy", "Windy", false }, };

		for (int i = 0; i != tests.length; i++)
			checkAreMutuallyExclusiveLiteralsGivenIndependence(tests[i], model);
	}

	private static void checkAreMutuallyExclusiveLiteralsGivenIndependence(
			Object[] test, Model model) {

		System.out.println("Test: " + Util.join(test));

		String literalString0 = (String) test[0];
		String literalString1 = (String) test[1];
		boolean areMX = ((Boolean) test[2]).booleanValue();

		Formula literal0 = (Formula) BLOGUtil
				.parseLiteral_NE(literalString0, model);
		Formula literal1 = (Formula) BLOGUtil
				.parseLiteral_NE(literalString1, model);
		boolean result;
		result = FormulaLikelihood.areMutuallyExclusiveLiterals(literal0, literal1);
		assertEquals(areMX, result);
		result = FormulaLikelihood.areMutuallyExclusiveLiterals(literal1, literal0);
		assertEquals(areMX, result);
	}

	public void testIsDNFWithMutuallyExclusiveDisjunctsGivenIndependence() {
		Model model = Model.readFromString(modelDescription);
		Object[][] tests = {
				{ "Sunny", true },
				{ "Source(b) = a", true },

				{ "Source(b) = a & Source(b) = a", true },
				{ "Source(b) = a & Source(b) != a", true },
				{ "Source(b) != a & Source(b) != a", true },
				{ "Source(b) = a & Source(b) = a2", true },
				{ "Source(b) = a | Source(b) = a2", true },
				{ "Source(b) = a | Source(b) != a2", false },
				{ "Source(b) = a | Source(b) = a", false },

				{ "a = Source(b) & Source(b) = a", true },
				{ "a = Source(b) & Source(b) != a", true },
				{ "a != Source(b) & Source(b) != a", true },
				{ "a = Source(b) & Source(b) = a2", true },
				{ "a = Source(b) | Source(b) = a2", true },
				{ "a = Source(b) | Source(b) != a2", false },
				{ "a = Source(b) | Source(b) = a", false },

				{ "a = Source(b) & Source(b) = Source(b2)", true },
				{ "a = Source(b) & Source(b) != Source(b2)", true },
				{ "a != Source(b) & Source(b) != Source(b2)", true },
				{ "a = Source(b) & Source(b) = Source(b2)", true },
				{ "a = Source(b) | Source(b) = Source(b2)", false },
				{ "a = Source(b) | Source(b) != Source(b2)", false },
				{ "a = Source(b) | Source(b) = Source(b2)", false },

				{ "ApparentPos(b) = 3 | ApparentPos(b) = 3", false },
				{ "ApparentPos(b) = 3 | ApparentPos(b) = 4", true },
				{ "Position(a) = 3 | ApparentPos(b) = 3", false },
				{ "Position(a) = 3 | ApparentPos(b) = 4", false },

				{ "Sunny | ApparentPos(b) = 4", false },
				{ "!Sunny | ApparentPos(b) = 4", false },
				{ "Sunny | !Sunny", true },
				{ "Sunny | Sunny", false },
				{ "Sunny | Windy", false },
				{ "Sunny | !Windy", false },
				{ "Windy | Windy", false },

				{ "Sunny & ApparentPos(b) = 3 | Sunny & ApparentPos(b) = 4", true },
				{ "Sunny & ApparentPos(b) = 3 | Sunny & ApparentPos(b) = 3", false },
				{
						"(Sunny & ApparentPos(b) = 3) | (Sunny & ApparentPos(b) = 3) | (Sunny & ApparentPos(b) = 4)",
						false },
				{
						"(Sunny & ApparentPos(b) = 3) | (Sunny & ApparentPos(b) = 4) | (Sunny & ApparentPos(b) = 5)",
						true }, { "Sunny & ApparentPos(b) = 3 | Sunny", false },
				{ "Sunny & ApparentPos(b) = 3 | ApparentPos(b) = 3", false },
				{ "Sunny & ApparentPos(b) = 3 | ApparentPos(b) != 3", true },
				{ "Sunny & ApparentPos(b) = 3 | Sunny & ApparentPos(b) != 3", true },
				{ "Sunny & ApparentPos(b) = 3 | Sunny & ApparentPos(b) = 3", false },
				{ "Sunny & ApparentPos(b) = 3 | !Sunny & ApparentPos(b) = 3", true },
				{ "Sunny & ApparentPos(b) = 3 | !Sunny", true },

				{ "Sunny & ApparentPos(b) = 4", true },
				{ "Sunny & ApparentPos(b) = 3", true }, { "Sunny", true },
				{ "ApparentPos(b) = 3", true }, { "ApparentPos(b) != 3", true },
				{ "Sunny & ApparentPos(b) != 3", true },
				{ "Sunny & ApparentPos(b) = 3", true },
				{ "!Sunny & ApparentPos(b) = 3", true }, { "!Sunny", true }, };

		for (int i = 0; i != tests.length; i++)
			checkIsDNFWithMutuallyExclusiveDisjunctsGivenIndependence(tests[i], model);
	}

	private static void checkIsDNFWithMutuallyExclusiveDisjunctsGivenIndependence(
			Object[] test, Model model) {
		System.out.println(Util.join(test));
		String formulaString = (String) test[0];
		boolean areMX = ((Boolean) test[1]).booleanValue();

		Formula formula = (Formula) BLOGUtil.parseFormula_NE(formulaString, model);
		DisjFormula dnf = formula.getPropDNF();

		boolean result;
		result = FormulaLikelihood.isDNFWithMutuallyExclusiveDisjuncts(dnf);
		assertEquals(areMX, result);
	}
}
