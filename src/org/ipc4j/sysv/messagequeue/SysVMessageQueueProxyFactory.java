package org.ipc4j.sysv.messagequeue;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ipc4j.ProcessUtils;
import org.ipc4j.client.IPCProxyFactory;

public class SysVMessageQueueProxyFactory extends IPCProxyFactory implements Runnable, Closeable {
	private static Log logger = LogFactory.getLog(SysVMessageQueueProxyFactory.class);
	
	private SysVMessageQueueAdapter clientAdapter;
	private SysVMessageQueueAdapter dataAdapter;
	private long sendId;
	private long receiveId;
	
	private AtomicBoolean running = new AtomicBoolean(false);
	private Thread asynchronousFunctionThread;
	
	@SuppressWarnings("rawtypes")
	public static Object createInstance(int key, long sendId, long receiveId, Class serviceInterface, Map<String, Object> asynchronousObjects) {
		SysVMessageQueueProxyFactory factory = new SysVMessageQueueProxyFactory(key, sendId, receiveId);
		factory.setServiceInterface(serviceInterface);
		
		if(null != asynchronousObjects) {
			factory.setAsynchronousObjects(asynchronousObjects);
		}
		
		return factory.getProxy();
	}

	public SysVMessageQueueProxyFactory(int key, long sendId, long receiveId) {
		super();
		
		if(sendId == receiveId) {
			throw new IllegalArgumentException("sendId and receiveId cannot be the same value");
		}

		this.sendId = sendId;
		this.receiveId = receiveId;
		this.clientAdapter = new SysVMessageQueueAdapter(key);

		init();
		
		running.set(true);
		asynchronousFunctionThread = new Thread(this);
		asynchronousFunctionThread.setName(String.format("SysVMessageQueue async function listener thread %X", dataAdapter.getKey()));
		asynchronousFunctionThread.setDaemon(true);
		asynchronousFunctionThread.start();
	}
	
	private void init() {
		try {
			if(!clientAdapter.exists()) {
				clientAdapter.create(0644);
			}
			
			int pid = ProcessUtils.getProcessId(-1);

			ByteBuffer sendBuffer = ByteBuffer.allocate(1 + 4);
			sendBuffer.put(SysVMessageQueueMessageType.START.toByte());
			sendBuffer.putInt(pid);
			clientAdapter.send(sendId, sendBuffer.array(), false);
			
			ByteBuffer recvBuffer = ByteBuffer.wrap(clientAdapter.receive(receiveId, true));
			int dataAdapterKey = recvBuffer.getInt();
			this.dataAdapter = new SysVMessageQueueAdapter(dataAdapterKey);

			if(!dataAdapter.exists()) {
				dataAdapter.create(0644);
			}
		} catch(SysVMessageQueueException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
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
			 * Function name size
			 * Function name
			 * 
			 * Function parameter count
			 * 
			 * 0...N
			 * Function parameter size
			 * Function parameter
			 */
			ByteBuffer buffer = ByteBuffer.allocate(65535);
			
			//Message type
			buffer.put(SysVMessageQueueMessageType.DATA.toByte());
			
			//Function name and size
			buffer.putInt(method.getName().length());
			buffer.put(method.getName().getBytes());
			
			//Parameter count
			buffer.putInt((null == args) ? 0 : args.length);
			
			//Parameters
			if(null != args) {
				for(Object arg : args) {
					Class argClass = Class.forName(arg.getClass().getName());
					Class entryClass = null;
					boolean argClassAssignable = false;
					
					if(null != getAsynchronousObjects()) {
						for(Entry<String, Object> entry : getAsynchronousObjects().entrySet()) {
							entryClass = Class.forName(entry.getKey());
							
							if(ClassUtils.isAssignable(argClass, entryClass) || ClassUtils.isAssignable(entryClass, argClass)) {
								logger.info(String.format("Adding object for %s", entryClass.getName()));
								getAsynchronousObjects().put(entryClass.getName(), arg);
								argClassAssignable = true;
								break;
							}
						}	
					}
					
					Serializable argSerialized = null;
					
					if(!argClassAssignable) {
						argSerialized = (Serializable) arg;
					} else {
						if(null != entryClass) {
							argSerialized = (Serializable) entryClass;
						}
					}
	
					byte [] argBytes = SerializationUtils.serialize(argSerialized);
					buffer.putInt(argBytes.length);
					buffer.put(argBytes);
				}
			}
			
			byte [] sendBuffer = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
			
			//Send request
			dataAdapter.send(sendId, sendBuffer, false);
			
			//Receive response
			ByteBuffer receiveBuffer = ByteBuffer.wrap(dataAdapter.receive(receiveId, true));
			
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
		} catch(Throwable t) {
			logger.error(t.getMessage(), t);
			throw t;
		}
	}

	/**
	 * 
	 * Used for async callback
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		while(running.get()) {
			try {
				ByteBuffer buffer = ByteBuffer.wrap(dataAdapter.receive(sendId + 2, true));
				SysVMessageQueueMessageType messageType = SysVMessageQueueMessageType.fromByte(buffer.get());
		
				if(SysVMessageQueueMessageType.DATA == messageType) {
					//Object name size
					int objectNameSize = buffer.getInt();
					byte [] objectName = new byte[objectNameSize];
					buffer.get(objectName);
					String objectNameStr = new String(objectName);
					
					//Function name size
					int functionNameSize = buffer.getInt();
					
					//Function name
					byte [] functionName = new byte[functionNameSize];
					buffer.get(functionName);
					String functionNameStr = new String(functionName);
					
					//Function parameter count
					int parameterCount = buffer.getInt();
					Class [] classes = new Class[parameterCount];
					Object[] objects = new Object[parameterCount];
					
					//Function parameters
					for(int iter = 0; iter < parameterCount; ++iter) {
						int bufferSize = buffer.getInt();
						byte [] parameterBuffer = new byte[bufferSize];
						buffer.get(parameterBuffer);							
						objects[iter] = SerializationUtils.deserialize(parameterBuffer);
						classes[iter] = objects[iter].getClass();
					}
					
					Object object = null;
					Class objClass = Class.forName(objectNameStr);
					
					for(Entry<String, Object> entry : getAsynchronousObjects().entrySet()) {
						Class entryClass = Class.forName(entry.getKey());
						
						if(ClassUtils.isAssignable(objClass, entryClass) || ClassUtils.isAssignable(entryClass, objClass)) {
							object = entry.getValue();
							break;
						}
					}
					
					if(null == object) {
						logger.error(String.format("Object named %s not found", objectNameStr));
						throw new ClassNotFoundException(objectNameStr);
					}
					
					if(logger.isDebugEnabled()) {
						logger.debug(String.format("Executing function named %s", functionNameStr));
						logger.debug(String.format("Using object %s", objectName));
					}

					//Lets find the function and execute!
					//This doesn't find methods with interface signatures and passed in derived classes
					//Method method = getServiceInterface().getMethod(nameStr, classes);
					Method method = MethodUtils.getMatchingAccessibleMethod(object.getClass(), functionNameStr, classes);
					if(null == method) {
						throw new NoSuchMethodException(functionNameStr);
					}
					Object result = null;
					
					try {
						result = method.invoke(object, objects);
					} catch(InvocationTargetException e) {
						Throwable targetException = e.getTargetException();
						byte [] exceptionBytes = SerializationUtils.serialize(targetException);
						int exceptionSize = exceptionBytes.length;
						
						ByteBuffer exceptionByteBuffer = ByteBuffer.allocate(exceptionSize + 4 + 1);
						exceptionByteBuffer.put(SysVMessageQueueMessageType.EXCEPTION.toByte());
						exceptionByteBuffer.putInt(exceptionSize);
						exceptionByteBuffer.put(exceptionBytes);
						
						dataAdapter.send(receiveId + 2, exceptionByteBuffer.array(), false);
					}
					
					//Send the result back to caller
					if(result instanceof Serializable) {
						byte [] resultBuffer = SerializationUtils.serialize((Serializable) result);
						int resultSize = resultBuffer.length;
						
						ByteBuffer resultByteBuffer = ByteBuffer.allocate(resultSize + 4 + 1);
						resultByteBuffer.put(SysVMessageQueueMessageType.RETURN.toByte());
						resultByteBuffer.putInt(resultSize);
						resultByteBuffer.put(resultBuffer);
						
						dataAdapter.send(receiveId + 2, resultByteBuffer.array(), false);
					} else if(null == result) { //Return type is void
						int resultSize = 0;
						
						ByteBuffer resultByteBuffer = ByteBuffer.allocate(resultSize + 4 + 1);
						resultByteBuffer.put(SysVMessageQueueMessageType.RETURN.toByte());
						resultByteBuffer.putInt(resultSize);
						
						dataAdapter.send(receiveId + 2, resultByteBuffer.array(), false);
					}
				}
			} catch(SysVMessageQueueException e) {
				logger.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			running.set(false);
			dataAdapter.send(receiveId, new byte[]{SysVMessageQueueMessageType.STOP.toByte()}, false);
			asynchronousFunctionThread.join();
		} catch (SysVMessageQueueException e) {
			throw new IOException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage(), e);
		}		
	}
}