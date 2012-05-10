package org.ipc4j.sysv.sharedmemory;

import org.ipc4j.sysv.SysVIPCAdapter;

public class SysVSharedMemoryAdapter extends SysVIPCAdapter {
	public SysVSharedMemoryAdapter() {
		super();
	}

	public SysVSharedMemoryAdapter(int key) {
		super(key);
	}

	@Override
	public native boolean exists();

	@Override
	public native int getId() throws SysVSharedMemoryException;

	@Override
	public void create(int permission) throws SysVSharedMemoryException
	{
		create(permission, 1048576);
	}
	
	public native void create(int permission, int size) throws SysVSharedMemoryException;

	@Override
	public native void destroy() throws SysVSharedMemoryException;

	@Override
	public native void setPermission(int permission) throws SysVSharedMemoryException;

	@Override
	public native int getPermission() throws SysVSharedMemoryException;

	@Override
	public native void setOwnerUserId(int ownerUserId) throws SysVSharedMemoryException;

	@Override
	public native int getOwnerUserId() throws SysVSharedMemoryException;

	@Override
	public native void setOwnerGroupId(int ownerGroupId) throws SysVSharedMemoryException;

	@Override
	public native int getOwnerGroupId() throws SysVSharedMemoryException;

	@Override
	public native int getCreatorUserId() throws SysVSharedMemoryException;

	@Override
	public native int getCreatorGroupId() throws SysVSharedMemoryException;
	
    static {
        System.loadLibrary("ipc4j");
    }

}