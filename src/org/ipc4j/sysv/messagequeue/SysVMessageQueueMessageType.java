package org.ipc4j.sysv.messagequeue;

public enum SysVMessageQueueMessageType {
	DATA(((byte) 0x0)), START((byte) 0x1), STOP((byte) 0x2), RETURN((byte) 0x3), EXCEPTION((byte) 0x4);
	
	private byte value;
	
	private SysVMessageQueueMessageType(byte value) {
		this.value = value;
	}
	
	public byte toByte() {
		return value;
	}
	
	public static SysVMessageQueueMessageType fromByte(byte b) {
		switch(b) {
		case 0x0:
			return DATA;
		case 0x1:
			return START;
		case 0x2:
			return STOP;
		case 0x3:
			return RETURN;
		case 0x4:
			return EXCEPTION;
		default:
			throw new IllegalArgumentException(String.format("Incorrect byte %d", b));
		}
	}
}
