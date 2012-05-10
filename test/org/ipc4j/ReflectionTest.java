package org.ipc4j;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.apache.commons.lang.reflect.MethodUtils;
import org.junit.Test;

public class ReflectionTest {

	public static interface TestListener
	{
	}
	
	public static class TestListenerImpl implements TestListener
	{
	}
	
	public static class Tester
	{
		public void foo(TestListener bar)
		{
		}
	}	
	
	@Test
	public void testReflection() {
		try {
			Tester.class.getMethod("foo", new Class[]{TestListener.class});
			
			Class[] interfaces = TestListenerImpl.class.getInterfaces();
			Method method = null;
			for(Class interfaze : interfaces)
			{
				try {
					method = Tester.class.getMethod("foo", new Class[]{interfaze}); //Throws NoSuchMethodException
				} catch(NoSuchMethodException e) {
				}
			}
			
			assertNotNull(method);
			method = MethodUtils.getMatchingAccessibleMethod(Tester.class, "foo", new Class[]{TestListenerImpl.class});
			Method method2 = MethodUtils.getMatchingAccessibleMethod(Tester.class, "foo", new Class[]{TestListenerImpl.class});
			
			assertEquals(method, method2);
			
			assertNotNull(method);			
		} catch (SecurityException e) {
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			fail(e.getMessage());
		}
	}
}
