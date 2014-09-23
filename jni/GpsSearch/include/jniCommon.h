#ifndef __JNICOMMON_H__
#define __JNICOMMON_H__

#ifdef __JNI_C__
#define __JNI_DEC__
#else
#define __JNI_DEC__ extern
#endif

typedef enum
{
	RETURN_JNI_ERROR		= 0,
	RETURN_JNI_OK			= 1,
}RETURN_JNI_VALUE;

typedef struct
{
	JNIEnv *env;
	jobject thiz;
}JNI_VARIABLE;

__JNI_DEC__ JNI_VARIABLE jniVar;

#define TAG		"TAM"
#ifdef _SP_JNI_DEBUG_VERSION_
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#else
#define LOGD(fmt, args...)
#endif

#endif
