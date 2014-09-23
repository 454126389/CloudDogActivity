#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <math.h>
#include <pthread.h>
#include <jni.h>
#include "common.h"

static int32 JniInit(JNIEnv *env, jobject thiz)
{
	return JNI_FALSE;
}

static int32 JniDeInit(JNIEnv *env, jobject thiz)
{
	return JNI_FALSE;
}

static int32 JniRun(JNIEnv *env, jobject thiz, jintArray IGPSData)
{
	return JNI_FALSE;
}

static int32 JniSetting(JNIEnv *env, jobject thiz, jbyteArray setArray)
{
	return JNI_FALSE;
}

static int32 JniSelfPointSave(JNIEnv *env, jobject thiz, jint index, jbyteArray selfPointArray)
{
	return JNI_FALSE;
}

static int32 JniGetSelfPointFree(JNIEnv *env, jobject thiz)
{
	return 0;
}

static int32 JniUpdateBaseDataBase(JNIEnv *env, jobject thiz, jstring path)
{
	return 0;
}

static int32 JniUpdateUpdateDataBase(JNIEnv *env, jobject thiz, jstring path)
{
	return 0;
}

static int32 JniUpdateExtraVoice(JNIEnv *env, jobject thiz, jstring path)
{
	return 0;
}

static int32 JniTest(JNIEnv *env, jobject thiz)
{
	return 0;
}

static JNINativeMethod gMethods[] = {
	{"Init", "()I", JniInit},
	{"DeInit", "()I", JniDeInit},
	{"Run", "([I)I", JniRun},
	{"Setting", "([B)I", JniSetting},
	{"SaveSelfPoint", "(I[B)I", JniSelfPointSave},
	{"getSelfPointFreeIndex", "()I", JniGetSelfPointFree},
	{"updateBase", "(Ljava/lang/String;)I", JniUpdateBaseDataBase},
	{"updateUpdate", "(Ljava/lang/String;)I", JniUpdateUpdateDataBase},
	{"updateExtra", "(Ljava/lang/String;)I", JniUpdateExtraVoice},
	{"Test", "()I", JniTest}
};

#define gClassName "com/weifer/search/SearchPointClass"
#define gMethodNum  (sizeof(gMethods)/sizeof(JNINativeMethod))

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;
	if ( (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) )
		return JNI_ERR;

	cls = (*env)->FindClass(env, gClassName);
	(*env)->RegisterNatives(env, cls, gMethods, gMethodNum);
	return JNI_VERSION_1_4;
}

void JNI_OnUnload(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;
	if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4))
		return;
	cls = (*env)->FindClass(env, gClassName);
	(*env)->UnregisterNatives(env, cls);
}
