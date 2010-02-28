package org.junit.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * <p>
 * The custom runner <code>Parameterized</code> implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 * </p>
 * 
 * For example, to test a Fibonacci function, write:
 * 
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 * 	&#064;Parameters
 * 	public static List&lt;Object[]&gt; data() {
 * 		return Arrays.asList(new Object[][] {
 * 				Fibonacci,
 * 				{ { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 },
 * 						{ 6, 8 } } });
 * 	}
 * 
 * 	private int fInput;
 * 
 * 	private int fExpected;
 * 
 * 	public FibonacciTest(int input, int expected) {
 * 		fInput= input;
 * 		fExpected= expected;
 * 	}
 * 
 * 	&#064;Test
 * 	public void test() {
 * 		assertEquals(fExpected, Fibonacci.compute(fInput));
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Each instance of <code>FibonacciTest</code> will be constructed using the
 * two-argument constructor and the data values in the
 * <code>&#064;Parameters</code> method.
 * </p>
 */
public class Parameterized extends Suite {
	/**
	 * Annotation for a method which provides parameters to be injected into the
	 * test class constructor by <code>Parameterized</code>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Parameters {
	}

	private class TestClassRunnerForParameters extends
			BlockJUnit4ClassRunner {

		private final Object[] fParameters;
		private final String fTestName;

		TestClassRunnerForParameters(Class<?> type,
				Object[] parameters, String testName) throws InitializationError {
			super(type);
			fParameters= parameters;
			fTestName= testName;
		}

		@Override
		public Object createTest() throws Exception {
			return getTestClass().getOnlyConstructor().newInstance(fParameters);
		}


		@Override
		protected String getName() {
			return String.format("[%s]", fTestName);
		}

		@Override
		protected String testName(final FrameworkMethod method) {
			return String.format("%s[%s]", method.getName(), fTestName);
		}

		@Override
		protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

		@Override
		protected Statement classBlock(RunNotifier notifier) {
			return childrenInvoker(notifier);
		}
	}

	private final ArrayList<Runner> runners= new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public Parameterized(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner>emptyList());
		Map<String, Object[]> parametersList= getNamedParameters(getTestClass());
		for (Map.Entry<String, Object[]> entry : parametersList.entrySet()) {
			runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(),
					entry.getValue(), entry.getKey()));
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object[]> getNamedParameters(TestClass klass) throws Throwable{
		Map<String, Object[]> map = new LinkedHashMap<String, Object[]>();
		Object parameters= getParametersMethod(klass).invokeExplosively(null);
		//if parameters is a map then we assume the user has given us named params.
	 
		try{
			if(parameters instanceof Map<?,?>){
				return (Map<String, Object[]>) parameters;
			}
			Collection<Object[]> parameterList = (Collection<Object[]>) parameters;
			int i = 0;
			for (Object[] objArray : parameterList) {
				map.put(i+"", objArray);
				i++;
			}
		}catch (ClassCastException e) {
			throw new Exception(String.format(
					"%s.%s() must have a return type of Collection<Object[]> or Map<String, Object[]>.",
					getTestClass().getName(), getParametersMethod(getTestClass()).getName()));
		}
		
		return map;
	}


	private FrameworkMethod getParametersMethod(TestClass testClass)
			throws Exception {
		List<FrameworkMethod> methods= testClass
				.getAnnotatedMethods(Parameters.class);
		for (FrameworkMethod each : methods) {
			int modifiers= each.getMethod().getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
				return each;
		}

		throw new Exception("No public static parameters method on class "
				+ testClass.getName());
	}

}
