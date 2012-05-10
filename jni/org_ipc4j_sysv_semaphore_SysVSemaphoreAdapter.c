#include "org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter.h"
#include "utils.h"

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>

int getSemId( JNIEnv* env, jobject obj )
{
	key_t key = getKey( env, obj );

	int result = semget( key, 0, 0 );

	if( -1 == result ) {
		switch( errno )
		{
		case EACCES:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore access denied", errno );
			break;
		case ENOENT:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore does not exist", errno );
			break;
		default:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore cannot be accessed", errno );
			break;
		}
	}

	return result;
}

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    exists
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_exists
  (JNIEnv* env, jobject obj);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getId
  (JNIEnv* env, jobject obj);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    create
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_create
  (JNIEnv* env, jobject obj, jint permission, jint count)
{
	key_t key = getKey( env, obj );

	int result = semget( key, count, permission | IPC_CREAT | IPC_EXCL );

	if( -1 == result ) {
		switch( errno )
		{
		case ENOMEM:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore cannot be created as not enough memory exists", errno );
			break;
		case ENOSPC:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore cannot be created as system limited reached", errno );
			break;
		case EACCES:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore access denied", errno );
			break;
		case EEXIST:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore already exists", errno );
			break;
		case EINVAL:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore count specified is invalid", errno );
			break;
		default:
			throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore already exists", errno );
			break;
		}
	}
}

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_destroy
  (JNIEnv* env, jobject obj)
{
	int result = getSemId( env, obj );

	if( -1 != result )
	{
		result = semctl( result, IPC_RMID, 0 );

		if( -1 == result )
		{
			switch( errno )
			{
			case EPERM:
			case EACCES:
				throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore access denied", errno );
				break;
			case EINVAL:
				throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore invalid id", errno );
				break;
			case EIDRM:
				throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore was removed", errno );
				break;
			case EFAULT:
				throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Buffer isn't accessible", errno );
				break;
			default:
				throwExceptionErrno( env, "org/ipc4j/sysv/semaphore/SysVSemaphoreException", "Semaphore cannot be removed", errno );
				break;
			}
		}
	}
}

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    setPermission
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_setPermission
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getPermission
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getPermission
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    setOwnerUserId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_setOwnerUserId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getOwnerUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getOwnerUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    setOwnerGroupId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_setOwnerGroupId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getOwnerGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getOwnerGroupId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getCreatorUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getCreatorUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter
 * Method:    getCreatorGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_semaphore_SysVSemaphoreAdapter_getCreatorGroupId
  (JNIEnv *, jobject);
