#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "List.h"

#define TAG					"FILE"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

typedef struct
{
	int fd;
	jobject thiz;
	struct list_head head;
}FILE_OPEN_LIST_TYPE;

static struct list_head list;

jobject JniOpen(JNIEnv *env, jobject thiz, jstring name)
{
	int fd = -1;
	char *file;
	jobject mFileDescriptor;

	file = (*env)->GetStringUTFChars(env, name, JNI_FALSE);
	if(file != NULL)
	{
		fd = open(file, O_RDWR | O_DIRECT | O_SYNC);
	}
	(*env)->ReleaseStringUTFChars(env, name, file);

	if(fd >= 0)
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
		FILE_OPEN_LIST_TYPE *node = malloc(sizeof(FILE_OPEN_LIST_TYPE));
		node->fd = fd;
		node->thiz = (*env)->NewGlobalRef(env, thiz);
		list_add(&node->head, &list);

		LOGD("Open %s file OK, %d", file, fd);
	}
	else
	{
		return JNI_FALSE;
	}

	return mFileDescriptor;
}

jint JniClose(JNIEnv *env, jobject thiz)
{
	int fd;
	FILE_OPEN_LIST_TYPE *node;

	list_for_each_entry(node, &list, head)
	{
		if(node->thiz == thiz)
		{
			LOGD("close %d OK", node->fd);
			close(fd);
			(*env)->DeleteGlobalRef(env, node->thiz);
			list_del(&node->head);
			break;
		}
	}

	return 0;
}

static JNINativeMethod gMethods[] = {
	{"open", "(Ljava/lang/String;)I", JniOpen},
	{"close", "(I)I", JniClose},
};

#define gClassName "android/demo/FileOperate"
#define gMethodNum  (sizeof(gMethods)/sizeof(JNINativeMethod))

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;
	if ( (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) )
		return JNI_ERR;

	cls = (*env)->FindClass(env, gClassName);
	(*env)->RegisterNatives(env, cls, gMethods, gMethodNum);

	INIT_LIST_HEAD(&list);
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
