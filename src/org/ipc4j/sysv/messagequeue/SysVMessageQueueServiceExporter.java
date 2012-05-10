package org.ipc4j.sysv.messagequeue;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ipc4j.ProcessUtils;
import org.ipc4j.server.IPCServiceExporter;

public class SysVMessageQueueServiceExporter extends IPCServiceExporter implements Runnable, Closeable {
	private static Log logger = LogFactory.getLog(SysVMessageQueueServiceExporter.class);
	private SysVMessageQueueAdapter adapter;
	
	private long sendId;
	private long receiveId;
	private int count = 0;
	private AtomicBoolean running = new AtomicBoolean(false);
	
	private Map<SysVMessageQueueAdapter, ServiceThread> serviceThreads = new ConcurrentHashMap<SysVMessageQueueAdapter, ServiceThread>();
	private Thread listenerThread;
	
	private class ServiceThread extends Thread implements InvocationHandler
	{
		private SysVMessageQueueAdapter newAdapter;
		private int clientPid;

		public ServiceThread(SysVMessageQueueAdapter newAdapter) {
			this.newAdapter = newAdapter;
		}
		
		public void setClientPid(int clientPid) {
			this.clientPid = clientPid;
		}
		
		public int getClientPid() {
			return clientPid;
		}
		
		@SuppressWarnings("rawtypes")
		@Override		
		public void run() {
			while(running.get()) {
				try {
					/**
					 * 
					 * Communication protocol
					 * 
					 * Message type
					 * 
					 * Function name size
					 * Function name
					 * 
					 * Function parameter count
					 * 
					 * 0...N
					 * Function parameter size
					 * Function parameter
					 *  
					 */
					ByteBuffer buffer = ByteBuffer.wrap(newAdapter.receive(receiveId, true));
					SysVMessageQueueMessageType messageType = SysVMessageQueueMessageType.fromByte(buffer.get());
					
					if(SysVMessageQueueMessageType.DATA == messageType) {
						//Function name size
						int nameSize = buffer.getInt();
						
						//Function name
						byte [] name = new byte[nameSize];
						buffer.get(name);
						String nameStr = new String(name);
						
						//Function parameter count
						int parameterCount = buffer.getInt();
						Class [] classes = new Class[parameterCount];
						Object[] objects = new Object[parameterCount];
						
						//Function parameters
						for(int iter = 0; iter < parameterCount; ++iter) {
							int bufferSize = buffer.getInt();
							byte [] parameterBuffer = new byte[bufferSize];
							buffer.get(parameterBuffer);
							
							Object object = SerializationUtils.deserialize(parameterBuffer);
							Class clazz = null;
							
							if(object instanceof Class) {
								object = null;
								clazz = (Class)object;
							} else {
								clazz = object.getClass();	
							}

							objects[iter] = object;
							classes[iter] = clazz;
						}			
						
						logger.debug(String.format("Executing function named %s", nameStr));
						
						//Lets find the function and execute!
						//This doesn't find methods with interface signatures and passed in derived classes
						//Method method = getServiceInterface().getMethod(nameStr, classes);
						Method method = MethodUtils.getMatchingAccessibleMethod(getServiceInterface(), nameStr, classes);
						if(null == method) {
							throw new NoSuchMethodException(nameStr);
						}
						Object result = null;
						
						//Check to see if function is asynchronous
						if(null != getAsynchronousCallbacks() && getAsynchronousCallbacks().containsKey(method)) {
							List<Class> asynchronousClasses = getAsynchronousCallbacks().get(method);
							
							for(Class clazz : asynchronousClasses) {
								Object object = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
								
								int index = -1;
								int counter = 0;
								
								for(Class currClass : classes) {
									if(ClassUtils.isAssignable(currClass, clazz) || ClassUtils.isAssignable(clazz, currClass)) {
										index = counter;
										break;
									}
									
									counter++;
								}
								
								if(index >= 0) {
									classes[index] = clazz;
									objects[index] = object;
								}
							}
						}
						
						try {
							result = method.invoke(getService(), objects);
						} catch(InvocationTargetException e) {
							Throwable targetException = e.getTargetException();
							byte [] exceptionBytes = SerializationUtils.serialize(targetException);
							int exceptionSize = exceptionBytes.length;
							
							ByteBuffer exceptionByteBuffer = ByteBuffer.allocate(exceptionSize + 4 + 1);
							exceptionByteBuffer.put(SysVMessageQueueMessageType.EXCEPTION.toByte());
							exceptionByteBuffer.putInt(exceptionSize);
							exceptionByteBuffer.put(exceptionBytes);
							
							newAdapter.send(sendId, exceptionByteBuffer.array(), false);
						}
						
						//Send the result back to caller
						if(result instanceof Serializable) {
							byte [] resultBuffer = SerializationUtils.serialize((Serializable) result);
							int resultSize = resultBuffer.length;
							
							ByteBuffer resultByteBuffer = ByteBuffer.allocate(resultSize + 4 + 1);
							resultByteBuffer.put(SysVMessageQueueMessageType.RETURN.toByte());
							resultByteBuffer.putInt(resultSize);
							resultByteBuffer.put(resultBuffer);
							
							newAdapter.send(sendId, resultByteBuffer.array(), false);
						} else if(null == result) { //Return type is void
							int resultSize = 0;
							
							ByteBuffer resultByteBuffer = ByteBuffer.allocate(resultSize + 4 + 1);
							resultByteBuffer.put(SysVMessageQueueMessageType.RETURN.toByte());
							resultByteBuffer.putInt(resultSize);
							
							newAdapter.send(sendId, resultByteBuffer.array(), false);
						}
					}
				} catch (SysVMessageQueueException e) {
					switch(e.getErrnoType()) {
					case ESRCH:
					case EIDRM:
						break;
					default:
						logger.error(e.getMessage(), e);
						break;
					}
					
					if(!newAdapter.exists()) {
						//TODO: clean up hashmaps
						return;
					}
				} catch (SecurityException e) {
					logger.error(e.getMessage(), e);
				} catch (NoSuchMethodException e) {
					logger.error(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					logger.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		/**
		 * 
		 * Used to handle registration of asynchronous callbacks!
		 * 
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			try {				
				/**
				 * 
				 * Communication protocol
				 * 
				 * Message type
				 * 
				 * Object name size
				 * Object name
				 * 
				 * Function name size
				 * Function name
				 * 
				 * Function parameter count
				 * 
				 * 0...N
				 * Function parameter size
				 * Function parameter
				 *  
				 */
				ByteBuffer buffer = ByteBuffer.allocate(65535);
				
				//Message type
				buffer.put(SysVMessageQueueMessageType.DATA.toByte());
				
				//Object name and size
				buffer.putInt(method.getDeclaringClass().getName().length());
				buffer.put(method.getDeclaringClass().getName().getBytes());
				
				//Function name and size
				buffer.putInt(method.getName().length());
				buffer.put(method.getName().getBytes());
				
				//Parameter count
				buffer.putInt((null == args) ? 0 : args.length);
				
				//Parameters
				if(null != args) {
					for(Object arg : args) {
						byte [] argBytes = SerializationUtils.serialize((Serializable)arg);
						buffer.putInt(argBytes.length);
						buffer.put(argBytes);
					}
				}
				
				byte [] sendBuffer = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
				
				//Send request
				//TODO: Remove hardcoded type
				newAdapter.send(receiveId + 2, sendBuffer, false);
				
				//Receive response
				//TODO: Remove hardcoded type
				//TODO: Get rid of duplicated code
				ByteBuffer receiveBuffer = ByteBuffer.wrap(newAdapter.receive(sendId + 2, true));
				
				SysVMessageQueueMessageType messageType = SysVMessageQueueMessageType.fromByte(receiveBuffer.get());		
				int resultSize = receiveBuffer.getInt();
				Object object = null;
				
				if(resultSize > 0) {
					byte [] receiveBytes = new byte[resultSize];
					receiveBuffer.get(receiveBytes);
					object = SerializationUtils.deserialize(receiveBytes);
				}
				
				switch(messageType) {
				case RETURN:
				default:
					return object;
				case EXCEPTION:
					throw (Throwable)object;
				}
			} catch(SysVMessageQueueException e) {
				return null;
			} catch(Throwable t) {
				throw t;
			}
		}
	}
	
	
	public SysVMessageQueueServiceExporter(int key, long sendId, long receiveId) {
		super();
		
		if(sendId == receiveId) {
			throw new IllegalArgumentException("sendId and receiveId cannot be the same value");
		}

		this.adapter = new SysVMessageQueueAdapter(key);
		this.sendId = sendId;
		this.receiveId = receiveId;
		
		try {
			if(!adapter.exists()) {
				adapter.create(0644);
			}
			
			adapter.clear();			
		} catch(SysVMessageQueueException e) {
			logger.error(e.getMessage(), e);
		}
		
		ScheduledThreadPoolExecutor cleanUp = new ScheduledThreadPoolExecutor(1);
		cleanUp.setThreadFactory(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setDaemon(true);
				thread.setName("SysVMessageQueue cleanup thread");
				return thread;
			}
			
		});
		
		cleanUp.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					Set<Entry<SysVMessageQueueAdapter, ServiceThread>> entries = serviceThreads.entrySet();
					Iterator<Entry<SysVMessageQueueAdapter, ServiceThread>> iterator = entries.iterator();
					
					while(iterator.hasNext()) {
						Entry<SysVMessageQueueAdapter, ServiceThread> entry = iterator.next();
						if(!ProcessUtils.isUNIXProcessAlive(entry.getValue().getClientPid())) {
							entry.getKey().destroy();
							iterator.remove();
						}
					}
				} catch (SysVMessageQueueException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}, 3, 3, TimeUnit.SECONDS);

		
		running.set(true);
		listenerThread = new Thread(this);
		listenerThread.setName(String.format("SysVMessageQueue listener thread %X", adapter.getKey()));
		listenerThread.setDaemon(true);
		listenerThread.start();
	}
	
	@Override
	public void run() {
		while(running.get()) {
			try {
				if(adapter.exists()) {
					ByteBuffer buffer = ByteBuffer.wrap(adapter.receive(receiveId, true));
					SysVMessageQueueMessageType messageType = SysVMessageQueueMessageType.fromByte(buffer.get());
					int clientPid = buffer.getInt();
					
					switch(messageType) {
					case START:
						//Generate new key
						SysVMessageQueueAdapter newAdapter = new SysVMessageQueueAdapter(adapter.getKey() + count + 1);
						
						if(!newAdapter.exists()) {
							newAdapter.create(0644);
						}
						
						newAdapter.clear();
						
						int newId = newAdapter.getId();
						
						ServiceThread newClient = new ServiceThread(newAdapter);
						newClient.setClientPid(clientPid);
						newClient.setName(String.format("SysVMessageQueue service thread %X", newAdapter.getKey()));
						newClient.setDaemon(true);
						newClient.start();
						
						serviceThreads.put(newAdapter, newClient);
						
						ByteBuffer sendBuffer = ByteBuffer.allocate(8);
						sendBuffer.putInt(newAdapter.getKey());
						sendBuffer.putInt(newId);
						
						adapter.send(sendId, sendBuffer.array(), false);
						count++;
						break;
					case STOP:
						running.set(false);
						break;
					}
				}
			} catch(SysVMessageQueueException e) {
				logger.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		} 
	}

	@Override
	public void close() throws IOException {
		try {
			running.set(false);
			adapter.send(receiveId, new byte[]{SysVMessageQueueMessageType.STOP.toByte()}, false);
			listenerThread.join();
			
			for(Map.Entry<SysVMessageQueueAdapter, ServiceThread> entry : serviceThreads.entrySet()) {
				entry.getKey().send(receiveId, new byte[]{SysVMessageQueueMessageType.STOP.toByte()}, false);
				entry.getValue().join();
			}			
		} catch (SysVMessageQueueException e) {
			throw new IOException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
