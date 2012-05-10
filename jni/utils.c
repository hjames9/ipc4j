#include "utils.h"

void throwException( JNIEnv* env, const char* name, const char* msg )
{
    jclass cls = (*env)->FindClass( env, name );
    if( NULL != cls ) {
        (*env)->ThrowNew( env, cls, msg );
    }

    (*env)->DeleteLocalRef( env, cls );
}

void throwExceptionErrno( JNIEnv* env, const char* name, const char* msg, int ierrno )
{
    jclass exceptionCls = (*env)->FindClass( env, name );
    if( NULL != exceptionCls ) {
    	jmethodID exceptionConstructor = (*env)->GetMethodID( env, exceptionCls, "<init>", "(Ljava/lang/String;)V" );
    	if( NULL != exceptionConstructor ) {
    		jstring str = (*env)->NewStringUTF( env, msg );
			jthrowable exceptionObj = (*env)->NewObject( env, exceptionCls, exceptionConstructor, str );

			jclass enumCls = (*env)->FindClass( env, "org/ipc4j/ErrnoType" );
			if( NULL != enumCls ) {
				//Create ErrnoType
				jmethodID MID_fromInt = (*env)->GetStaticMethodID( env, enumCls, "fromInt", "(I)Lorg/ipc4j/ErrnoType;" );
				if( NULL != MID_fromInt ) {
					jobject errnoType = (*env)->CallStaticObjectMethod( env, enumCls, MID_fromInt, ierrno );

					//Set ErrnoType on exception object
					jmethodID MID_SetErrnoType = (*env)->GetMethodID( env, exceptionCls, "setErrnoType", "(Lorg/ipc4j/ErrnoType;)V" );
					if( NULL != MID_SetErrnoType && NULL != errnoType ) {
						(*env)->CallVoidMethod( env, exceptionObj, MID_SetErrnoType, errnoType );
					}
				}

				(*env)->DeleteLocalRef( env, enumCls );
			}

			if( NULL != exceptionObj ) {
				(*env)->Throw( env, exceptionObj );
			}
    	}

    	(*env)->DeleteLocalRef( env, exceptionCls );
    }
}

key_t getKey( JNIEnv* env, jobject obj )
{
    jfieldID fid;
    jint ji = 0;
    key_t key = 0;

    jclass cls = (*env)->GetObjectClass( env, obj );

    if(NULL == cls) {
    	throwException( env, "java/lang/ClassNotFoundException", "Class cannot be found" );
    	return -1;
    }

    fid = (*env)->GetFieldID( env, cls, "key", "I" );

    if(NULL == fid) {
    	throwException( env, "java/lang/NoSuchFieldException", "Key field cannot be found" );
    	return -1;
    }

    ji = (*env)->GetIntField( env, obj, fid );

    key = ji;

    return key;
}
