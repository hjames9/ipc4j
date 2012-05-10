package org.ipc4j.sysv;

import org.ipc4j.ErrnoType;

public abstract class SysVIPCException extends Exception {
	private static final long serialVersionUID = 1L;

	private ErrnoType errnoType = ErrnoType.SUCCESS;

	public SysVIPCException() {
		super();
	}
	
	public SysVIPCException(Exception e) {
		super(e);
	}
	
	public SysVIPCException(String message) {
		super(message);
	}
	
	public SysVIPCException(String message, Exception e) {
		super(message, e);
	}
	
	public SysVIPCException(ErrnoType errnoType) {
		setErrnoType(errnoType);
	}
	
	public void setErrnoType(ErrnoType errnoType) {
		this.errnoType = errnoType;
	}
	
	public ErrnoType getErrnoType() {
		return errnoType;
	}
	
	public int getErrorNumber() {
		return errnoType.getErrorNumber();
	}

	public String getErrorString() {
		return errnoType.toString();
	}

	public String getErrorMessage() {
		return errnoType.getErrorMessage();
	}
}
