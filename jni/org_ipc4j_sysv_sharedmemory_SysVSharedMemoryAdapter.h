/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter */

#ifndef _Included_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
#define _Included_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    exists
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_exists
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    create
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_create
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_destroy
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setPermission
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setPermission
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getPermission
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getPermission
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setOwnerUserId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setOwnerUserId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getOwnerUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getOwnerUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    setOwnerGroupId
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_setOwnerGroupId
  (JNIEnv *, jobject, jint);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getOwnerGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getOwnerGroupId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getCreatorUserId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getCreatorUserId
  (JNIEnv *, jobject);

/*
 * Class:     org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter
 * Method:    getCreatorGroupId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_ipc4j_sysv_sharedmemory_SysVSharedMemoryAdapter_getCreatorGroupId
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
