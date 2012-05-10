package org.ipc4j.server;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class IPCServiceExporter implements ServiceExporter {
	private Object service;
	private Class serviceInterface;
	private Method[] methods;
	private Map<Method, List<Class>> asynchronousCallbacks;

	@Override
	public void setService(Object service) {
		this.service = service;
	}
	
	@Override
	public Object getService() {
		return service;
	}

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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setAsynchronousCallbacksStr(Map<String, List<Class>> asynchronousCallbacksStr)
	{
		try {
			this.asynchronousCallbacks = new LinkedHashMap<Method, List<Class>>();
			for(Entry<String, List<Class>> entry : asynchronousCallbacksStr.entrySet()) {
				Method method = getServiceInterface().getMethod(entry.getKey(), entry.getValue().toArray(new Class[entry.getValue().size()]));
				asynchronousCallbacks.put(method, entry.getValue());
			}
		} catch(NoSuchMethodException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	
	@Override
	public void setAsynchronousCallbacks(Map<Method, List<Class>> asynchronousCallbacks)
	{
		this.asynchronousCallbacks = asynchronousCallbacks;		
	}
	
	@Override
	public Map<Method, List<Class>> getAsynchronousCallbacks()
	{
		return this.asynchronousCallbacks;
	}
}
