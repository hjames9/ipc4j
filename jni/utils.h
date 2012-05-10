#include <jni.h>
#include <errno.h>
#include <sys/types.h>

#define UNUSED_ARG( expr ) do { (void)(expr); } while (0)

void throwException( JNIEnv* env, const char* name, const char* msg );
void throwExceptionErrno( JNIEnv* env, const char* name, const char* msg, int ierrno );
key_t getKey( JNIEnv* env, jobject obj );
