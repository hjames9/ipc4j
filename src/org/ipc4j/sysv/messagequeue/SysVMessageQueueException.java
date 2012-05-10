package org.ipc4j.sysv.messagequeue;

import org.ipc4j.ErrnoType;
import org.ipc4j.sysv.SysVIPCException;

public class SysVMessageQueueException extends SysVIPCException {
	private static final long serialVersionUID = 1L;
	
	public SysVMessageQueueException() {
		super();
	}
	
	public SysVMessageQueueException(String message) {
		super(message);
	}
	
	public SysVMessageQueueException(Exception e) {
		super(e);
	}
	
	public SysVMessageQueueException(String message, Exception e) {
		super(message, e);
	}
	
	public SysVMessageQueueException(ErrnoType errnoType) {
		super(errnoType);
	}
}
