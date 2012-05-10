package org.ipc4j.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public abstract class IPCProxyFactory implements ProxyFactory, InvocationHandler {
	private Class serviceInterface;
	private Map<String, Object> asynchronousObjects;

	@Override
	public void setServiceInterface(Class serviceInterface) {
		if (serviceInterface != null && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("'serviceInterface' must be an interface");
		}

		this.serviceInterface = serviceInterface;
	}
	
	@Override
	public Class getServiceInterface() {
		return serviceInterface;
	}
	
	@Override
	public Object getProxy()
	{
		Object obj = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[] { serviceInterface }, this);
		return obj;
	}
	
	@Override
	public Map<String, Object> getAsynchronousObjects() {
		return asynchronousObjects;
	}
	
	@Override
	public void setAsynchronousObjects(Map<String, Object> asynchronousObjects) {
		this.asynchronousObjects = asynchronousObjects;
	}
	
	@Override
	public abstract Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
