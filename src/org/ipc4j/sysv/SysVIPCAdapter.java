package org.ipc4j.sysv;

public abstract class SysVIPCAdapter {
	private int key;
	
	public SysVIPCAdapter() {
	}

	public SysVIPCAdapter(int key) {
		setKey(key);
	}
	
	public void setKey(int key) {
		this.key = key;
	}
	
	public int getKey() {
		return key;
	}
	
	public abstract boolean exists() throws SysVIPCException;
	public abstract int getId() throws SysVIPCException;
	
	public abstract void create(int permission) throws SysVIPCException;
	public abstract void destroy() throws SysVIPCException;
	
	public abstract void setPermission(int permission) throws SysVIPCException;
	public abstract int getPermission() throws SysVIPCException;
	
	public abstract void setOwnerUserId(int ownerUserId) throws SysVIPCException;
	public abstract int getOwnerUserId() throws SysVIPCException;
	
	public abstract void setOwnerGroupId(int ownerGroupId) throws SysVIPCException;
	public abstract int getOwnerGroupId() throws SysVIPCException;
	
	public abstract int getCreatorUserId() throws SysVIPCException;
	public abstract int getCreatorGroupId() throws SysVIPCException;
	
	@Override
	public boolean equals(Object obj) {
		return ((Integer)getKey()).equals(((SysVIPCAdapter)obj).getKey());
	}
	
	@Override
	public int hashCode() {
		return ((Integer)getKey()).hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("key=%d", getKey());
	}
}
