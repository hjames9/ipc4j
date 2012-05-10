#include "org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter.h"
#include "utils.h"

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <string.h>

struct messageBuffer
{
	long type;
	signed char buffer[ 65535 ];
};

int getMsgId( JNIEnv* env, jobject obj )
{
	key_t key = getKey( env, obj );

	int result = msgget( key, 0 );

	if( -1 == result ) {
		switch( errno )
		{
		case EACCES:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
			break;
		case ENOENT:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue does not exist", errno );
			break;
		default:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue cannot be accessed", errno );
			break;
		}
	}

	return result;
}

void doMsgData( JNIEnv* env, jobject obj, int cmd, struct msqid_ds* buffer )
{
	int id = getMsgId( env, obj );

	if( -1 != id ) {

		int result = msgctl( id, cmd, buffer );

		if( -1 == result ) {
			switch( errno )
			{
			case EACCES:
			case EPERM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
				break;
			case EFAULT:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Buffer isn't accessible", errno );
				break;
			case EIDRM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue was removed", errno );
				break;
			case EINVAL:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Invalid values specified", errno );
				break;
			default:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
				break;
			}
		}
	}
}

jobject createDate( JNIEnv* env, jobject obj, time_t timeVal )
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jclass dateClass = (*env)->FindClass( env, "java/util/Date" );
	jobject date;

	if( NULL != dateClass ) {
		jmethodID cid = (*env)->GetMethodID( env, dateClass, "<init>", "(J)V" );

		if( NULL != cid ) {
			jobject dateLocal = (*env)->NewObject( env, dateClass, cid, timeVal * 1000 );
			date = (*env)->NewWeakGlobalRef( env, dateLocal );
		} else {
			throwException( env, "java/lang/NoSuchMethodException", "Date class constructor not found" );
		}
	} else {
		throwException( env, "java/lang/ClassNotFoundException", "Date class cannot be found" );
	}

	return date;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    exists
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_exists
  (JNIEnv* env, jobject obj)
{
	key_t key = getKey( env, obj );

	int result = msgget( key, 0 );

	if(-1 == result && ENOENT == errno ) {
		return 0;
	} else {
		return 1;
	}
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getMsgId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getId
  (JNIEnv* env, jobject obj)
{
	return getMsgId( env, obj );
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    create
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_create
  (JNIEnv* env, jobject obj, jint permission)
{
	key_t key = getKey( env, obj );

	int result = msgget( key, permission | IPC_CREAT | IPC_EXCL );

	if( -1 == result ) {
		switch( errno )
		{
		case ENOMEM:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue cannot be created as not enough memory exists", errno );
			break;
		case ENOSPC:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue cannot be created as system limited reached", errno );
			break;
		case EACCES:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
			break;
		case EEXIST:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue already exists", errno );
			break;
		default:
			throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue already exists", errno );
			break;
		}
	}
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_destroy
  (JNIEnv* env, jobject obj)
{
	int result = getMsgId( env, obj );

	if( -1 != result )
	{
		result = msgctl( result, IPC_RMID, 0 );

		if( -1 == result )
		{
			switch( errno )
			{
			case EPERM:
			case EACCES:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
				break;
			case EINVAL:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue invalid id", errno );
				break;
			case EIDRM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue was removed", errno );
				break;
			case EFAULT:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Buffer isn't accessible", errno );
				break;
			default:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue cannot be removed", errno );
				break;
			}
		}
	}
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    send
 * Signature: (J[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_send
  (JNIEnv* env, jobject obj, jlong type, jbyteArray data, jboolean block)
{
	jboolean retVal = 0;
	int result = getMsgId( env, obj );

	if( -1 != result )
	{
		struct messageBuffer bufferData;
		bufferData.type = type;

		jsize dataSize = (*env)->GetArrayLength( env, data );
		int flag = ( block ) ? 0 : IPC_NOWAIT;

		(*env)->GetByteArrayRegion( env, data, 0, dataSize, bufferData.buffer );

		result = msgsnd( result, &bufferData, dataSize, flag );

		if( -1 == result )
		{
			switch( errno )
			{
			case EACCES:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
				break;
			case EAGAIN:
				//This only happens if there's a queue limit reached and we are in non-blocking mode.
				//Do nothing and just let retVal return false
				break;
			case EFAULT:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Buffer isn't accessible", errno );
				break;
			case EIDRM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue was removed", errno );
				break;
			case EINTR:
				throwException( env, "java/lang/InterruptedException", "System signal caught" );
				break;
			case EINVAL:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue invalid id or mtype or message size", errno );
				break;
			case ENOMEM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "System is out of memory cannot send message", errno );
				break;
			default:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message cannot be sent", errno );
				break;
			}
		}
		else
		{
			retVal = 1;
		}
	}

	return retVal;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    receive
 * Signature: (JZ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_receive
  (JNIEnv* env, jobject obj, jlong type, jboolean block)
{
	int result = getMsgId( env, obj );
	jbyteArray data = NULL;

	if( -1 != result )
	{
		int flag = ( block ) ? 0 : IPC_NOWAIT;

		int bufferSize = 65535;
		struct messageBuffer bufferData;

		ssize_t sizeRecv = msgrcv( result, &bufferData, bufferSize, type, flag );
		if( -1 == sizeRecv && ( ENOMSG != errno ) )
		{
			switch( errno )
			{
			case E2BIG:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message is too large for buffer", errno );
				break;
			case EACCES:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
				break;
			case EAGAIN:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue limit reached", errno );
				break;
			case EFAULT:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Buffer isn't accessible", errno );
				break;
			case EIDRM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue was removed", errno );
				break;
			case EINTR:
				throwException( env, "java/lang/InterruptedException", "System signal caught" );
				break;
			case EINVAL:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue invalid id or mtype or message size", errno );
				break;
			case ENOMEM:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "System is out of memory cannot send message", errno );
				break;
			case ENOMSG:
				//This should never been reached because it only happens on non-blocking mode
				break;
			default:
				throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue data not received", errno );
				break;
			}
		}
		else
		{
			sizeRecv = ( -1 == sizeRecv ) ? 0 : sizeRecv;
			data = (*env)->NewByteArray( env, sizeRecv );
			(*env)->SetByteArrayRegion( env, data, 0, sizeRecv, bufferData.buffer );
		}
	}

	return data;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    setPermission
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_setPermission
  (JNIEnv* env, jobject obj, jint permission)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	buffer.msg_perm.mode = permission;

	doMsgData( env, obj, IPC_SET, &buffer );
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getPermission
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getPermission
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint permission = buffer.msg_perm.mode;
	return permission;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    setOwnerUserId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_setOwnerUserId
  (JNIEnv* env, jobject obj, jint ownerUserId)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	buffer.msg_perm.uid = ownerUserId;

	doMsgData( env, obj, IPC_SET, &buffer );
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getOwnerUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getOwnerUserId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint uid = buffer.msg_perm.uid;
	return uid;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    setOwnerGroupId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_setOwnerGroupId
  (JNIEnv* env, jobject obj, jint ownerGroupId)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	buffer.msg_perm.gid = ownerGroupId;

	doMsgData( env, obj, IPC_SET, &buffer );
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getOwnerGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getOwnerGroupId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint gid = buffer.msg_perm.gid;
	return gid;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    setMaxQueueSize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_setMaxQueueSize
  (JNIEnv* env, jobject obj, jint size)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	buffer.msg_qbytes = size;

	doMsgData( env, obj, IPC_SET, &buffer );
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getMaxQueueSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getMaxQueueSize
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint size = buffer.msg_qbytes;
	return size;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getCreatorUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getCreatorUserId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint creatorUserId = buffer.msg_perm.cuid;
	return creatorUserId;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getCreatorGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getCreatorGroupId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint creatorGroupId = buffer.msg_perm.cgid;
	return creatorGroupId;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getLastSendTime
 * Signature: ()Ljava/util/Date;
 */
JNIEXPORT jobject JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getLastSendTime
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jobject date = createDate( env, obj, buffer.msg_stime );
	return date;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getLastReceiveTime
 * Signature: ()Ljava/util/Date;
 */
JNIEXPORT jobject JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getLastReceiveTime
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jobject date = createDate( env, obj, buffer.msg_rtime );
	return date;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getLastChangeTime
 * Signature: ()Ljava/util/Date;
 */
JNIEXPORT jobject JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getLastChangeTime
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jobject date = createDate( env, obj, buffer.msg_ctime );
	return date;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getNumberOfMessagesInQueue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getNumberOfMessagesInQueue
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint numMessages = buffer.msg_qnum;
	return numMessages;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getLastSendProcessId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getLastSendProcessId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint sendProcessId = buffer.msg_lspid;
	return sendProcessId;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    getLastReceiveProcessId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_getLastReceiveProcessId
  (JNIEnv* env, jobject obj)
{
	struct msqid_ds buffer;
	doMsgData( env, obj, IPC_STAT, &buffer );

	jint sendProcessId = buffer.msg_lrpid;
	return sendProcessId;
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    clear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_clear
  (JNIEnv* env, jobject obj, jlong type)
{
	int result = getMsgId( env, obj );

	struct { long type; char mtext[ 1 ]; } buffer;

	int flags = IPC_NOWAIT | MSG_NOERROR;

	if( -1 != result )
	{
		ssize_t size = 0;

		do
		{
			size = msgrcv( result, &buffer, sizeof( buffer.mtext ), type, flags );

			if( -1 == size ) {
				switch( errno ) {
				case EACCES:
					throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue access denied", errno );
					break;
				case EAGAIN:
				case ENOMSG:
					//Successfully removed requested elements
					break;
				case EFAULT:
					throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Buffer isn't accessible", errno );
					break;
				case EIDRM:
					throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue was removed", errno );
					break;
				case EINTR:
					throwException( env, "java/lang/InterruptedException", "System signal caught" );
					break;
				case EINVAL:
					throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue invalid id or mtype or message size", errno );
					break;
				default:
					throwExceptionErrno( env, "org/ipc4j/sysv/messagequeue/SysVMessageQueueException", "Message queue error, data not cleared", errno );
					break;
				}
			}
		}
		while( -1 != size );
	}
}

/*
 * Class:     org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter
 * Method:    interrupt
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_messagequeue_SysVMessageQueueAdapter_interrupt
  (JNIEnv* env, jobject obj )
{
	//TODO: Do some signal hackery to get send() and receive() to return while blocking
	UNUSED_ARG( env );
	UNUSED_ARG( obj );
}
