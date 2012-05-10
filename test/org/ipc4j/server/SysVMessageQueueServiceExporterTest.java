package org.ipc4j.server;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ipc4j.sysv.messagequeue.SysVMessageQueueProxyFactory;
import org.ipc4j.sysv.messagequeue.SysVMessageQueueServiceExporter;
import org.junit.Test;

public class SysVMessageQueueServiceExporterTest {

	public static class TestException extends Exception
	{
		public TestException(String message) {
			super(message);
		}
	}
	
	public static class Tester implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private int field;
		
		public Tester(int field) {
			this.field = field;
		}
		
		public int getField() {
			return field;
		}
	}
	
	public static interface TestListener extends Serializable
	{
		public int doFoo(int a, int c) throws TestException;
	}
	
	public static class TestListenerImpl implements TestListener 
	{
		@Override
		public int doFoo(int a, int c) throws TestException {
			System.out.println("Listener doFoo() being called");
			return a * c;
		}	
	}
	
	public interface TestService
	{
		public Tester doWork(Tester test) throws TestException;
		public Tester registerListener(TestListener listener) throws TestException;
		public Tester unregisterListener(TestListener listener) throws TestException;
	}
	
	public class TestServiceImpl implements TestService, Runnable
	{
		private TestListener listener;
		private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		
		public TestServiceImpl() {
		}
		
		@Override
		public Tester doWork(Tester test) throws TestException {
			System.out.println(String.format("Doing work service work %X", test.getField()));
			return new Tester(0xF00D);
			//throw new TestException("Testing exception!");
		}

		@Override
		public Tester registerListener(TestListener listener) throws TestException {
			this.listener = listener;
			
			if(null != listener) {
				executor.scheduleAtFixedRate(this, 3, 3, TimeUnit.SECONDS);
			}
			
			return new Tester(0xFADE);
		}
		
		@Override
		public void run() {
			try {
				int value = listener.doFoo(5, 7);
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}

		@Override
		public Tester unregisterListener(TestListener listener)
				throws TestException {
			this.listener = null;
			executor.shutdownNow();
			return new Tester(0xBADE);
		}		
	}
	
	@Test
	public void testSysVMessageQueueServiceExporter() {
		try {
			//Server
			SysVMessageQueueServiceExporter exporter = new SysVMessageQueueServiceExporter(0xDEADBEEF, 2L, 1L);
			exporter.setService(new TestServiceImpl());
			exporter.setServiceInterface(TestService.class);
			
			/*
			//As Method
			Map<Method, List<Class>> asynchronousCallbacks = new HashMap<Method, List<Class>>();
			List<Class> classes = new ArrayList<Class>();
			classes.add(TestListener.class);
			
			Method method = exporter.getServiceInterface().getMethod("registerListener", new Class[]{TestListener.class});
			asynchronousCallbacks.put(method, classes);
			exporter.setAsynchronousCallbacks(asynchronousCallbacks);
			*/
			
			//As String
			Map<String, List<Class>> asynchronousCallbacks = new HashMap<String, List<Class>>();
			List<Class> classes = new ArrayList<Class>();
			classes.add(TestListener.class);
			asynchronousCallbacks.put("registerListener", classes);
			exporter.setAsynchronousCallbacksStr(asynchronousCallbacks);
			
			//Client
			SysVMessageQueueProxyFactory factory = new SysVMessageQueueProxyFactory(0xDEADBEEF, 1L, 2L);
			factory.setServiceInterface(TestService.class);
			Map<String, Object> asynchronousObjects = new HashMap<String, Object>();
			asynchronousObjects.put("org.ipc_service.server.SysVMessageQueueServiceExporterTest$TestListener", null);
			factory.setAsynchronousObjects(asynchronousObjects);
			
			TestService testService = (TestService)factory.getProxy();
			//Tester tester = testService.doWork(new Tester(0xCAFE));
			//Tester tester2 = testService.doWork(new Tester(0xCAFEBEEF));
			//System.out.println(String.format("%X", tester.getField()));			
			
			TestListener listener = new TestListenerImpl();
			testService.registerListener(listener);
			
			Thread.sleep(2000000);
			
			//TODO: Figure out below case
			//testService.registerListener(new TestListenerImpl());
		} catch(InterruptedException e) {
		} catch(TestException e) {
			//System.out.println(e.getMessage());
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSetObject() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetObject() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetServiceInterface() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetServiceInterface() {
		//fail("Not yet implemented");
	}

}
