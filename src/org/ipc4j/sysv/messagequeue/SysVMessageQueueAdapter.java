package org.ipc4j.sysv.messagequeue;

import java.util.Date;

import org.ipc4j.sysv.SysVIPCAdapter;

public class SysVMessageQueueAdapter extends SysVIPCAdapter {
	public SysVMessageQueueAdapter() {
		super();
	}

	public SysVMessageQueueAdapter(int key) {
		super(key);
	}
	
	@Override
	public native boolean exists();
	
	@Override
	public native int getId() throws SysVMessageQueueException;
	
	@Override
	public native void create(int permission) throws SysVMessageQueueException;
	
	@Override
	public native void destroy() throws SysVMessageQueueException;
	
	public native boolean send(long type, byte [] data, boolean block) throws SysVMessageQueueException, InterruptedException;
	public native byte [] receive(long type, boolean block) throws SysVMessageQueueException, InterruptedException;
	
	@Override
	public native void setPermission(int permission) throws SysVMessageQueueException;
	
	@Override
	public native int getPermission() throws SysVMessageQueueException;
	
	@Override
	public native void setOwnerUserId(int ownerUserId) throws SysVMessageQueueException;
	
	@Override
	public native int getOwnerUserId() throws SysVMessageQueueException;
	
	@Override
	public native void setOwnerGroupId(int ownerGroupId) throws SysVMessageQueueException;
	
	@Override
	public native int getOwnerGroupId() throws SysVMessageQueueException;
	
	public native void setMaxQueueSize(int size) throws SysVMessageQueueException;
	public native int getMaxQueueSize() throws SysVMessageQueueException;
	
	@Override
	public native int getCreatorUserId() throws SysVMessageQueueException;
	
	@Override
	public native int getCreatorGroupId() throws SysVMessageQueueException;
	
	public native Date getLastSendTime() throws SysVMessageQueueException;
	public native Date getLastReceiveTime() throws SysVMessageQueueException;
	public native Date getLastChangeTime() throws SysVMessageQueueException;
	
	public native int getNumberOfMessagesInQueue() throws SysVMessageQueueException;
	public native int getLastSendProcessId() throws SysVMessageQueueException;
	public native int getLastReceiveProcessId() throws SysVMessageQueueException;
	
	public native void clear(long type) throws SysVMessageQueueException;
	
	public void clear() throws SysVMessageQueueException
	{
		clear(0);
	}
	
	public native void interrupt() throws SysVMessageQueueException;
	
	@Override
	public String toString() {
		try {
			return String.format("key=%d, msgid=%d, owner=%d, perms=%o, messages=%d", getKey(), getId(), 
					getOwnerUserId(), getPermission(), getNumberOfMessagesInQueue());
		} catch (SysVMessageQueueException e) {
			return super.toString();	
		}
	}

    static {
        System.loadLibrary("ipc4j");
    }
}
