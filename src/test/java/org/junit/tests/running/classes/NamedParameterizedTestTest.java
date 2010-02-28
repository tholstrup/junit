package org.junit.tests.running.classes;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Plan;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

 public class NamedParameterizedTestTest{
	 
	@RunWith(Parameterized.class)
	static public class FibonacciTest {
		@Parameters
		public static  Map<String, Object[]>  data() {
			Map<String, Object[]> map= new LinkedHashMap<String, Object[]>();
			map.put("passing test", new Object[] { 0, 0 });
			map.put("failing test1", new Object[] { 1, 1 });
			map.put("failing test2", new Object[] { 2, 1 });
			map.put("failing test3", new Object[] { 3, 2 });
			map.put("failing test4", new Object[] { 4, 3 });
			map.put("failing test5", new Object[] { 5, 5 });
			map.put("failing test6", new Object[] { 6, 8 });
			map.put("failing test6", new Object[] { 7, 9 });
			return map;
		}

		private int fInput;

		private int fExpected;

		public FibonacciTest(int input, int expected) {
			fInput= input;
			fExpected= expected;
		}

		@Test
		public void test() {
			assertEquals(fExpected, fib(fInput));
		}

		private int fib(int x) {
			return 0;
		}
	}

	@Test
	public void count() {
		Result result= JUnitCore.runClasses(FibonacciTest.class);
		assertEquals(7, result.getRunCount());
		assertEquals(6, result.getFailureCount());
	}

	@Test
	public void failuresNamedCorrectly() {
		Result result= JUnitCore.runClasses(FibonacciTest.class);
		assertEquals(String
				.format("test[failing test1](%s)", FibonacciTest.class.getName()), result
				.getFailures().get(0).getTestHeader());
	}

	@Test
	public void countBeforeRun() throws Exception {
		Runner runner= Request.aClass(FibonacciTest.class).getRunner();
		assertEquals(7, runner.testCount());
	}

	@Test
	public void plansNamedCorrectly() throws Exception {
		Runner runner= Request.aClass(FibonacciTest.class).getRunner();
		Plan plan= runner.getPlan();
		assertEquals("[passing test]", plan.getChildren().get(0).getDescription().getDisplayName());
	}
 }
