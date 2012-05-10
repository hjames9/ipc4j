package org.ipc4j.server;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.ipc4j.ServiceInterface;

public interface ServiceExporter extends ServiceInterface {
	public void setService(Object service);
	public Object getService();
	
	public void setAsynchronousCallbacksStr(Map<String, List<Class>> asynchronousCallbacksStr);
	public void setAsynchronousCallbacks(Map<Method, List<Class>> asynchronousCallbacks);
	public Map<Method, List<Class>> getAsynchronousCallbacks();
}
