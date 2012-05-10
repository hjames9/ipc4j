package org.ipc4j.client;

import java.util.Map;

import org.ipc4j.ServiceInterface;

public interface ProxyFactory extends ServiceInterface {
	public Object getProxy();
	public Map<String, Object> getAsynchronousObjects();
	public void setAsynchronousObjects(Map<String, Object> asynchronousObjects);
}
