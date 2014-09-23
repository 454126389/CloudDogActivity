#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <math.h>
#include <pthread.h>

#include "config.h"


#define __JNI_C__
#include "common.h"
#include "List.h"
#include "user_setting.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "DataBase.h"
#include "BasicDataBase.h"
#include "UpdateDataBase.h"
#include "SelfDataBase.h"
#include "GpsData.h"
#include "GpsdataListener.h"
#include "PointSearch.h"
#include "AlarmProcess.h"
#include "EnterExitDataBase.h"
#include "Display.h"
#include "mcu.h"
#include "radar.h"
//#include "sqlite3.h"

int32 JniInit(JNIEnv *env, jobject thiz)
{
	jclass clazz = (*env)->GetObjectClass(env, thiz);
	(*env)->GetJavaVM(env, &JniGlobalVariable.vm);
	JniGlobalVariable.pEnv = env;
	JniGlobalVariable.version = (*env)->GetVersion(env);
	JniGlobalVariable.thiz = (*env)->NewGlobalRef(env, thiz);
	//JniGlobalVariable.clazz = (*env)->GetObjectClass(env, thiz);
    JniGlobalVariable.NotifyUIMethod = (*env)->GetMethodID(env, clazz, "JniCallBack", "(I[B)I");
    //OutsideVarInitial(env, thiz, clazz);
    //InsideVarInitial();

    if(DataBaseInit() != DATABASE_SUCCESS)
    {
        LOGD("Init the database failed");
    }

    GpsDataInit();
    SearchPointInit();
    mcu_init();
    radar_init();
	#ifdef _CUSTOMMAIN_EXTRA_POINT_
	CustomExtraVoiceInitial();
	#endif /* ifdef _CUSTOMMAIN_EXTRA_POINT_ */
	DisplayInit();

    return JNI_TRUE;
}

int32 JniDeInit(JNIEnv *env, jobject thiz)
{
	(*env)->DeleteGlobalRef(env, JniGlobalVariable.thiz);

    if(DataBaseDeInit() != DATABASE_SUCCESS)
    {
        LOGD("DeInit the database failed");
    }
    DisplayDeInit();
    GpsDataDeInit();
    SearchPointDeInit();
    radar_deinit();
    mcu_deinit();
#ifdef _CUSTOMMAIN_EXTRA_POINT_
    CustomExtraVoiceDeInit();
#endif
    LOGD("jni Deinit success");
    return JNI_TRUE;
}

static int32 NotifyImplement(JNIEnv *env, int32 code, char *arg, int32 size)
{
	int32 ret;

	jbyteArray buf = (*env)->NewByteArray(env, size);
	(*env)->SetByteArrayRegion(env, buf, 0, size, arg);
	ret = (int32)(*env)->CallIntMethod(env, JniGlobalVariable.thiz, JniGlobalVariable.NotifyUIMethod, code, buf);
	(*env)->DeleteLocalRef(env, buf);

	return ret;
}

int32 NotifyUI(int32 code, char *arg, int32 size)
{
    return NotifyImplement(JniGlobalVariable.pEnv, code, arg, size);
}

void JniFillGpsData(GPSDATA_TYPE *pGpsPoint, jint *GpsBuf)
{
    pGpsPoint->glatitude = GpsBuf[0];
    pGpsPoint->glongitude = GpsBuf[1];
    pGpsPoint->gspeed = GpsBuf[2];
    pGpsPoint->gcursor = GpsBuf[3];
    pGpsPoint->year = GpsBuf[4];
    pGpsPoint->month = GpsBuf[5];
    pGpsPoint->day = GpsBuf[6];
    pGpsPoint->hour = GpsBuf[7];
    pGpsPoint->minute = GpsBuf[8];
    pGpsPoint->second = GpsBuf[9];
}

int32 JniRun(JNIEnv *env, jobject thiz, jintArray IGPSData)
{
	jintArray GpsDataArray;
	jint GpsDataBuf[11];
	struct gps_listener *pListener;
	struct list_head *pos;

	(*env)->GetIntArrayRegion(env, IGPSData, 0, 11, GpsDataBuf);

    JniFillGpsData(pCurGpsPoint, GpsDataBuf);
    if(SearchPoint(pCurGpsPoint->glatitude, pCurGpsPoint->glongitude) != SEARCH_SUCCESS)
    {
        //LOGD("Search database fail");
    }
    AlarmProcess();

    for(pos = gps_listener_list.next; pos != &gps_listener_list; )
    {
    	pListener = list_entry(pos, struct gps_listener, node);
    	pos = pos->next;

    	if(pListener->listener(pCurGpsPoint) == 1)
    	{
    		UnRegisterGpsdataListener(pListener);
    	}
    }

    return JNI_TRUE;
}

int32 NotifyUIInThread(int32 code, char *arg, int32 size)
{
	uint8 isAttached = FALSE;
	JNIEnv *env;
	if(NULL == JniGlobalVariable.vm)
	{
		return FALSE;
	}

	int status = (*JniGlobalVariable.vm)->GetEnv(JniGlobalVariable.vm, (void **)&env, JniGlobalVariable.version);

	if(status < 0)
	{
		LOGD("GetEnv fail");
		status = (*JniGlobalVariable.vm)->AttachCurrentThread(JniGlobalVariable.vm, &env, NULL);
		if(status < 0)
		{
			LOGD("AttachCurrentThread fail");
			return FALSE;
		}

		isAttached = TRUE;
	}
	LOGD("env get success");
	status = NotifyImplement(env, code, arg, size);

	if(isAttached == TRUE)
	{
		(*JniGlobalVariable.vm)->DetachCurrentThread(JniGlobalVariable.vm);
	}

	return status;
}

int32 JniSetting(JNIEnv *env, jobject thiz, jbyteArray setArray)
{
	/*
	jclass clazz = (*env)->GetObjectClass(env, thiz);
	jmethodID mid = (*env)->GetMethodID(env, clazz, "GetUserSetting", "()[B");
	jbyteArray array = (jbyteArray)(*env)->CallObjectMethod(env, thiz, mid);
	*/
	char *buf = (*env)->GetByteArrayElements(env, setArray, JNI_FALSE);
	FillSetting(buf);
	(*env)->ReleaseByteArrayElements(env, setArray, buf, JNI_FALSE);
	return JNI_TRUE;
}

int32 JniSelfPointSave(JNIEnv *env, jobject thiz, jint index, jbyteArray selfPointArray)
{
	jboolean ret = JNI_FALSE;
	char *buf = (*env)->GetByteArrayElements(env, selfPointArray, &ret);
	char data[16];
	memcpy(data, buf, 16);
	(*env)->ReleaseByteArrayElements(env, selfPointArray, buf, JNI_FALSE);

	pthread_mutex_lock(&pSelfDataBase->mutex);
	//pSelfDataBase->EncodeRecord(pSelfDataBase, index, data);
	pSelfDataBase->SaveRecord(pSelfDataBase, index, data);
	pthread_mutex_unlock(&pSelfDataBase->mutex);

	return JNI_TRUE;
}

int32 JniGetSelfPointFree(JNIEnv *env, jobject thiz)
{
	DataBaseType *pDB;
	uint8 bFound = FALSE;

	list_for_each_entry(pDB, &DataBaseList, db_head)
	{
		if(pDB == pSelfDataBase)
		{
			bFound = TRUE;
			break;
		}
	}

	if(bFound == FALSE)
	{
		LOGD("seflpoint not register");
		return -1;
	}

	return (int32)pDB->GetFreeIndex(pDB);
}

static int32 JniUpdateBaseDataBase(JNIEnv *env, jobject thiz, jstring path)
{
	jboolean ret = JNI_FALSE;
	const char *pFile = (*env)->GetStringUTFChars(env, path, &ret);
	DataBaseType *pDB;
	uint8 bFound = FALSE;

	list_for_each_entry(pDB, &DataBaseList, db_head)
	{
		if(pDB == pBasicDataBase)
		{
			bFound = TRUE;
			break;
		}
	}

	if(bFound == FALSE)
	{
		LOGD("basic database not register");
		return JNI_FALSE;
	}
	else
	{
		if(pDB->Update != NULL)
		{
			pDB->Update(pDB, pFile);
		}
	}

	(*env)->ReleaseStringUTFChars(env, path, pFile);
	return JNI_TRUE;
}

int32 JniUpdateUpdateDataBase(JNIEnv *env, jobject thiz, jstring path)
{
	DataBaseType *pDB;
	uint8 bFound = FALSE;
	jboolean ret = JNI_FALSE;
	const char *pFile = (*env)->GetStringUTFChars(env, path, &ret);

	list_for_each_entry(pDB, &DataBaseList, db_head)
	{
		if(pDB == pUpdateDataBase)
		{
			bFound = TRUE;
			break;
		}
	}

	if(bFound == FALSE)
	{
		LOGD("update database not register");
	}

	{
		if(pUpdateDataBase->Update != NULL)
		{
			ret = pUpdateDataBase->Update(pUpdateDataBase, pFile);
		}
	}
	(*env)->ReleaseStringUTFChars(env, path, pFile);
	return ret;
}

int32 JniUpdateExtraVoice(JNIEnv *env, jobject thiz, jstring path)
{
	jboolean ret = JNI_FALSE;
	const char *pFile = (*env)->GetStringUTFChars(env, path, &ret);
	ExtraVoiceUpdate(pFile);
	(*env)->ReleaseStringUTFChars(env, path, pFile);

	return JNI_TRUE;
}

void *testThread(void *arg)
{
	uint8 ret = 1;
	//NotifyUIInThread(NOTIFY_UI_CODE_BASIC_DATABASE_UPDATE_FINISH, &ret, 1);
	//const uint8 *p = " 叮咚前方有雷射测速照相，本路段限速***公里，请依速限行驶。";
	uint8 buf[32];
	int i;
	if(pBasicVoice != NULL && pBasicVoice->read != NULL)
	{
		pBasicVoice->read(pBasicVoice, 2, buf);

		for(i = 0; i < 32; i++)
		{
			LOGD("%02x", buf[i]);
		}
	}

	return NULL;
}

int32 JniTest(JNIEnv *env, jobject thiz)
{
	//Test();

	pthread_t id;
	pthread_create(&id, NULL, testThread, NULL);



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
