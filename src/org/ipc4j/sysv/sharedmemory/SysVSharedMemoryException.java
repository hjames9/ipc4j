package org.ipc4j.sysv.sharedmemory;

import org.ipc4j.ErrnoType;
import org.ipc4j.sysv.SysVIPCException;

public class SysVSharedMemoryException extends SysVIPCException {
	private static final long serialVersionUID = 1L;
	
	public SysVSharedMemoryException() {
		super();
	}
	
	public SysVSharedMemoryException(String message) {
		super(message);
	}
	
	public SysVSharedMemoryException(Exception e) {
		super(e);
	}
	
	public SysVSharedMemoryException(String message, Exception e) {
		super(message, e);
	}
	
	public SysVSharedMemoryException(ErrnoType errnoType) {
		super(errnoType);
	}
}
