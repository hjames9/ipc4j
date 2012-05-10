package org.ipc4j.sysv.semaphore;

import org.ipc4j.ErrnoType;
import org.ipc4j.sysv.SysVIPCException;

public class SysVSemaphoreException extends SysVIPCException {
	private static final long serialVersionUID = 1L;
	
	public SysVSemaphoreException() {
		super();
	}
	
	public SysVSemaphoreException(String message) {
		super(message);
	}
	
	public SysVSemaphoreException(Exception e) {
		super(e);
	}
	
	public SysVSemaphoreException(String message, Exception e) {
		super(message, e);
	}
	
	public SysVSemaphoreException(ErrnoType errnoType) {
		super(errnoType);
	}
}
