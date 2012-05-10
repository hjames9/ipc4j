#include "org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter.h"
#include "utils.h"

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>

int getShmId( JNIEnv* env, jobject obj )
{
	key_t key = getKey( env, obj );

	int result = shmget( key, 0, 0 );

	if( -1 == result ) {
		switch( errno )
		{
		case EACCES:
			throwExceptionErrno( env, "org/ipc4j/sysv/sharedmemory/SysVSharedMemoryException", "Shared memory access denied", errno );
			break;
		case ENOENT:
			throwExceptionErrno( env, "org/ipc4j/sysv/sharedmemory/SysVSharedMemoryException", "Shared memory does not exist", errno );
			break;
		default:
			throwExceptionErrno( env, "org/ipc4j/sysv/sharedmemory/SysVSharedMemoryException", "Shared memory cannot be accessed", errno );
			break;
		}
	}

	return result;
}

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    exists
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_exists
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    create
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_create
  (JNIEnv* env, jobject obj, jint permission, jint size)
{
	UNUSED_ARG( env );
	UNUSED_ARG( obj );
	UNUSED_ARG( permission );
	UNUSED_ARG( size );
}

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_destroy
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setPermission
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setPermission
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getPermission
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getPermission
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setOwnerUserId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setOwnerUserId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getOwnerUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getOwnerUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setOwnerGroupId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setOwnerGroupId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getOwnerGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getOwnerGroupId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getCreatorUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getCreatorUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getCreatorGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getCreatorGroupId
  (JNIEnv *, jobject);
