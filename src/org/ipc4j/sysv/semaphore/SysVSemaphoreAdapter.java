package org.ipc4j.sysv.semaphore;

import org.ipc4j.sysv.SysVIPCAdapter;

public class SysVSemaphoreAdapter extends SysVIPCAdapter {
	public SysVSemaphoreAdapter() {
		super();
	}

	public SysVSemaphoreAdapter(int key) {
		super(key);
	}
	
	@Override
	public native boolean exists();

	@Override
	public native int getId() throws SysVSemaphoreException;

	@Override
	public void create(int permission) throws SysVSemaphoreException
	{
		create(permission, 1);
	}
	
	public native void create(int permission, int count) throws SysVSemaphoreException;

	@Override
	public native void destroy() throws SysVSemaphoreException;

	@Override
	public native void setPermission(int permission) throws SysVSemaphoreException;

	@Override
	public native int getPermission() throws SysVSemaphoreException;

	@Override
	public native void setOwnerUserId(int ownerUserId) throws SysVSemaphoreException;

	@Override
	public native int getOwnerUserId() throws SysVSemaphoreException;

	@Override
	public native void setOwnerGroupId(int ownerGroupId) throws SysVSemaphoreException;

	@Override
	public native int getOwnerGroupId() throws SysVSemaphoreException;

	@Override
	public native int getCreatorUserId() throws SysVSemaphoreException;

	@Override
	public native int getCreatorGroupId() throws SysVSemaphoreException;
	
    static {
        System.loadLibrary("ipc4j");
    }
}
