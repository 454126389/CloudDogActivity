#ifndef __COMMON_H__
#define __COMMON_H__

typedef unsigned long           uint64;
typedef long                    int64;
typedef unsigned int            uint32;
typedef int                     int32;
typedef unsigned short          uint16;
typedef short					int16;
typedef unsigned char           uint8;
typedef char                    int8;

#ifndef NULL
#define NULL                    (void *)0
#endif

#ifndef TRUE
#define TRUE                    (1)
#endif

#ifndef FALSE
#define FALSE                   (0)
#endif

#define DEBUG

#include <jni.h>
#include <android/log.h>
#ifdef DEBUG
#define TAG                                     "DataBase"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#else
#define LOGD(fmt, args...)
#endif

typedef struct
{
	JavaVM *vm;
	JNIEnv *pEnv;
	jobject thiz;
	//jclass clazz;
    jmethodID NotifyUIMethod;
    uint32 version;
}JNIVariable;

#ifdef __JNI_C__
#define __JNI_DEC__
#else
#define __JNI_DEC__ extern
#endif

typedef enum {
	NOTIFY_UI_CODE_TTS_PLAY												= 0x0001,
	NOTIFY_UI_CODE_RADAR_WARNNING,
	NOTIFY_UI_CODE_TTS_PLAY_IMMEDIATELY,
	NOTIFY_UI_CODE_TTS_BLOCK_PLAY,
	NOTIFY_UI_CODE_ALARM_DISTANCE,
	NOTIFY_UI_CODE_BASIC_DATABASE_UPDATE_FINISH,
	NOTIFY_UI_CODE_ALARM_POINT_STATUS,
}NOTIFY_CODE_INDEX;

__JNI_DEC__ JNIVariable JniGlobalVariable;
__JNI_DEC__ int32 NotifyUI(int32 code, char *arg, int32 size);
__JNI_DEC__ int32 NotifyUIInThread(int32 code, char *arg, int32 size);
#undef __JNI_DEC__

#endif

