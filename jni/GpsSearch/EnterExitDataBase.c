#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <pthread.h>
#include <math.h>
#include <string.h>
#include "config.h"
#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "DataBase.h"
#include "thirdvoicetable.h"
#include "AlarmProcess.h"

#define __ENTEREXITDATABASE_C__
#include "EnterExitDataBase.h"

static EXTRA_VOICE_INFO extraBaseVoice =
{
	.nSizePerRecord = EXTRA_RECORD_SIZE,
	.nTotalRecord = 0,
};

static EXTRA_VOICE_INFO extraUpdateVoice =
{
	.nSizePerRecord = EXTRA_RECORD_SIZE,
	.nTotalRecord = 0,
};

static pthread_mutex_t extra_mutex;

static int32 CustomExtraLatLonCheck(int32 lat, int32 lon)
{
    if(( lat > 900000 ) || ( lat < -900000 ) || ( lon > 1800000 ) || ( lon < -1800000 ))
    {
        return -1;
    }

    return 1;
}

static int32 CustomExtraLatLonGet(uint8 *buf, int32 *lat, int32 *lon)
{
    *lat = ( buf[ 0 ] << 16 ) + ( buf[ 1 ] << 8 ) + buf[ 2 ];
    *lon = ( buf[ 3 ] << 16 ) + ( buf[ 4 ] << 8 ) + buf[ 5 ];

    if( CustomExtraLatLonCheck(*lat, *lon) > 0 )
    {
        return 1;
    }

    return -1;
}

static void CustomExtraVoiceGetValue(void)
{
	jfieldID FieldID;
	jstring str;
	jbyteArray array;
	char *strArray;
	JNIEnv *env = JniGlobalVariable.pEnv;
	jobject thiz = JniGlobalVariable.thiz;
	jclass clazz = (*env)->GetObjectClass(env, thiz);
	uint8 len;

	FieldID = (*env)->GetStaticFieldID(env, clazz, "EXTRA_BASE_FILE_NAME", "Ljava/lang/String;");
	str = (jstring)(*env)->GetStaticObjectField(env, clazz, FieldID);
	GetStringFromJava(env, str, extraBaseVoice.fileName);
	LOGD("Get extra base file name %s", extraBaseVoice.fileName);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "EXTRA_UPDATE_FILE_NAME", "Ljava/lang/String;");
	str = (jstring)(*env)->GetStaticObjectField(env, clazz, FieldID);
	GetStringFromJava(env, str, extraUpdateVoice.fileName);
	LOGD("Get extra update file name %s", extraUpdateVoice.fileName);


	jclass clazz_tmp = (*env)->FindClass(env, "android/demo/CloudDogPreference");
	FieldID = (*env)->GetStaticFieldID(env, clazz_tmp, "mBaseDatabaseFKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz_tmp, FieldID);
	len = (*env)->GetArrayLength(env, array);
	strArray = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(extraBaseVoice.key, strArray, len);
	(*env)->ReleaseByteArrayElements(env, array, strArray, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	FieldID = (*env)->GetStaticFieldID(env, clazz_tmp, "mBaseDatabasePKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz_tmp, FieldID);
	len = (*env)->GetArrayLength(env, array);
	strArray = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(extraBaseVoice.key + len, strArray, len);
	(*env)->ReleaseByteArrayElements(env, array, strArray, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	FieldID = (*env)->GetStaticFieldID(env, clazz_tmp, "mUpdateDatabaseFKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz_tmp, FieldID);
	len = (*env)->GetArrayLength(env, array);
	strArray = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(extraUpdateVoice.key, strArray, len);
	(*env)->ReleaseByteArrayElements(env, array, strArray, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	FieldID = (*env)->GetStaticFieldID(env, clazz_tmp, "mUpdateDatabasePKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz_tmp, FieldID);
	len = (*env)->GetArrayLength(env, array);
	strArray = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(extraUpdateVoice.key + len, strArray, len);
	(*env)->ReleaseByteArrayElements(env, array, strArray, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	(*env)->DeleteLocalRef(env, clazz);
}

static void ExtraVoiceRecordDecode(EXTRA_VOICE_INFO *pInfo, uint32 index, uint8 *buf)
{
	uint8 keyIndex;
	uint8 tmp[EXTRA_RECORD_SIZE];
	uint8 i;

}

static uint32 ExtraVoiceReadOneRecord(EXTRA_VOICE_INFO *pInfo, uint32 index, uint8 *buf)
{
	uint32 size;

	if(lseek(pInfo->hFile, pInfo->nSizePerRecord * index, SEEK_SET) == -1)
	{
		LOGD("seek %s at %d error", pInfo->fileName, index << 5);
		return EXTRA_VOICE_FILE_READ_ERROR;
	}

	size = read(pInfo->hFile, buf, pInfo->nSizePerRecord);
	if(size != EXTRA_RECORD_SIZE)
	{
		LOGD("read %s file error", pInfo->fileName);
		return EXTRA_VOICE_FILE_READ_ERROR;
	}

	if(index != 0 && index != (pInfo->nTotalRecord + 1))
	{
		ExtraVoiceRecordDecode(pInfo, index, buf);
	}

	return EXTRA_VOICE_SUCCESS;
}

uint32 InitExtraVoiceFile(EXTRA_VOICE_INFO *pInfo)
{
	uint8 buf[EXTRA_RECORD_SIZE];
	uint32 size;
	int32 ret = EXTRA_VOICE_SUCCESS;
	uint32 i;

	pthread_mutex_init(&extra_mutex, NULL);

	if(pInfo->fileName == NULL)
	{
		return EXTRA_VOICE_NO_FILE;
	}

	if((pInfo->hFile = open(pInfo->fileName, O_RDONLY)) < 0)
	{
		LOGD("open %s file error", pInfo->fileName);
		return EXTRA_VOICE_NO_FILE;
	}

	if((ret = ExtraVoiceReadOneRecord(pInfo, 0, buf)) != EXTRA_VOICE_SUCCESS)
	{
		LOGD("read the %s header fail", pInfo->fileName);
		return ret;
	}

	pInfo->nTotalRecord = (buf[0] << 24) | (buf[1] << 16) | (buf[2] << 8) | buf[3];
	size = lseek(pInfo->hFile, 0, SEEK_END);
	if(size != (pInfo->nSizePerRecord * (pInfo->nTotalRecord + 2)))
	{
		LOGD("the %s size error, (%d), total record(%d)", pInfo->fileName, size, pInfo->nTotalRecord);
		return EXTRA_VOICE_FILE_SIZE_ERROR;
	}

	pInfo->nVersion = (buf[4] << 24) | (buf[5] << 16) | (buf[6] << 8) | buf[7];

	//check the last record is same as 0xFF

	if((ret = ExtraVoiceReadOneRecord(pInfo, pInfo->nTotalRecord + 1, buf)) != EXTRA_VOICE_SUCCESS)
	{
		LOGD("read the %s last record error", pInfo->fileName);
		return ret;
	}

	for(i = 0; i < pInfo->nSizePerRecord; i++)
	{
		if(buf[i] != 0xFF)
		{
			break;
		}
	}

	if(i != pInfo->nSizePerRecord)
	{
		return EXTRA_VOICE_FILE_TAIL_ERROR;
	}

	if(pInfo->nTotalRecord != 0)
	{
		if((ret = ExtraVoiceReadOneRecord(pInfo, 1, buf)) != EXTRA_VOICE_SUCCESS)
		{
			return ret;
		}

		if(CustomExtraLatLonGet(buf, &pInfo->nStartLat, &pInfo->nStartLon) < 0)
		{
			LOGD("start data error");
			return EXTRA_VOICE_FILE_DATA_ERROR;
		}

		if((ret = ExtraVoiceReadOneRecord(pInfo, pInfo->nTotalRecord, buf)) != EXTRA_VOICE_SUCCESS)
		{
			return ret;
		}

		if(CustomExtraLatLonGet(buf, &pInfo->nEndLat, &pInfo->nEndLon) < 0)
		{
			LOGD("end data error");
			return EXTRA_VOICE_FILE_DATA_ERROR;
		}

		/* for test the key
		char debug[128] = {0};
		char *debugstr = "0123456789ABCDEF";
		for(i = 0; i < 22; i++)
		{
			debug[3 * i] = debugstr[buf[i + 10] >> 4];
			debug[3 * i + 1] = debugstr[buf[i + 10] & 0x0F];
			debug[3 * i + 2] = ',';
		}
		LOGD("%s", debug);
		*/
	}

	//for test
	pInfo->read = ExtraVoiceReadOneRecord;
	LOGD("Init %s voice file success, total record %d, version %d", pInfo->fileName, pInfo->nTotalRecord, pInfo->nVersion);
	return EXTRA_VOICE_SUCCESS;
}

static int32 ExtraVoiceRecursiveSearch(EXTRA_VOICE_INFO *pInfo, int32 lat, uint32 start, uint32 end)
{
	uint32 mid = (start + end) / 2;
	int32 ret;
	int32 tlat, tlon;
	uint8 buf[EXTRA_RECORD_SIZE];

	ret = ExtraVoiceReadOneRecord(pInfo, mid, buf);

	if(ret < 0)
	{
		return EXTRA_VOICE_FILE_READ_ERROR;
	}

	if(CustomExtraLatLonGet(buf, &tlat, &tlon) < 0)
	{
		return EXTRA_VOICE_FILE_DATA_ERROR;
	}

	//LOGD("search %d, %d, %d", start, end, tlat);

	if(tlat == lat)
	{
		return mid;
	}
	else if(start == mid)
	{
		return EXTRA_VOICE_SEARCH_ERROR;
	}
	else if(tlat < lat)
	{
		return ExtraVoiceRecursiveSearch(pInfo, lat, mid, end);
	}
	else if(tlat > lat)
	{
		return ExtraVoiceRecursiveSearch(pInfo, lat, start, mid);
	}

	return EXTRA_VOICE_SEARCH_SUCCESS;
}

static int32 ExtraVoiceCheck(EXTRA_VOICE_INFO *pInfo, int32 lat, int32 lon, uint32 index, uint32 record_index, uint8 *buf)
{
	int32 ret;
	int32 tlat, tlon;

	ret = ExtraVoiceReadOneRecord(pInfo, record_index, buf);
	if(ret < 0)
	{
		return EXTRA_VOICE_FILE_READ_ERROR;
	}

	if(CustomExtraLatLonGet(buf, &tlat, &tlon) < 0)
	{
		return EXTRA_VOICE_FILE_DATA_ERROR;
	}

	if(lat != tlat)
	{
		return EXTRA_VOICE_SEARCH_LAT_ERROR;
	}

	if(tlon == lon && index == buf[6])
	{
		return EXTRA_VOICE_SEARCH_SUCCESS;
	}

	return EXTRA_VOICE_SEARCH_ERROR;
}

static int32 ExtraVoiceGetTarget(EXTRA_VOICE_INFO *pInfo, int lat, int lon, int index, int record_index, uint8 *buf)
{
	int32 ret;
	uint32 i;
	int32 tlat, tlon;

	if(ExtraVoiceCheck(pInfo, lat, lon, index, record_index, buf) == EXTRA_VOICE_SUCCESS)
	{
		return EXTRA_VOICE_SUCCESS;
	}

	for(i = (record_index - 1); i > 1; i--)
	{
		ret = ExtraVoiceCheck(pInfo, lat, lon, index, i, buf);
		if(ret == EXTRA_VOICE_SUCCESS)
		{
			return EXTRA_VOICE_SUCCESS;
		}
		else if(ret == EXTRA_VOICE_SEARCH_LAT_ERROR)
		{
			break;
		}
	}

	for(i = (record_index + 1); i < pInfo->nTotalRecord; i++)
	{
		ret = ExtraVoiceCheck(pInfo, lat, lon, index, i, buf);
		if(ret == EXTRA_VOICE_SUCCESS)
		{
			return EXTRA_VOICE_SUCCESS;
		}
		else if(ret == EXTRA_VOICE_SEARCH_LAT_ERROR)
		{
			break;
		}
	}

	return EXTRA_VOICE_SEARCH_ERROR;
}

static int32 ExtraVoiceSearchInInfo(EXTRA_VOICE_INFO *pInfo, int32 lat, int32 lon, uint32 index, uint8 **str)
{
	int32 ret;
	uint8 buf[EXTRA_RECORD_SIZE];

	if(pInfo->hFile < 0 || pInfo->nTotalRecord <= 0)
	{
		return EXTRA_VOICE_NO_FILE;
	}

	if(lat == pInfo->nStartLat)
	{
		ret = 1;
	}
	else if(lat == pInfo->nEndLat)
	{
		ret = pInfo->nTotalRecord;
	}
	else
	{
		ret = ExtraVoiceRecursiveSearch(pInfo, lat, 1, pInfo->nTotalRecord);
	}

	if(ret < 0)
	{
		LOGD("extra voice recursive search error %d", ret);
		return ret;
	}

	ret = ExtraVoiceGetTarget(pInfo, lat, lon, index, ret, buf);
	if(ret < 0)
	{
		return ret;
	}

	if(str != NULL)
	{
		int i;
		//check the string end;
		#define STRING_START_INDEX					10
		for(i = (EXTRA_RECORD_SIZE - 1); i >= STRING_START_INDEX; i--)
		{
			if(buf[i] != 0xFF)
			{
				break;
			}
		}
		*str = (uint8 *)malloc(i - STRING_START_INDEX + 2);
		memcpy(*str, &buf[STRING_START_INDEX], i - STRING_START_INDEX + 1);
		*((*str) + i - STRING_START_INDEX + 1) = 0;
	}

	return EXTRA_VOICE_SUCCESS;
}

int32 ExtraVoiceSearch(int32 lat, int32 lon, uint32 index, uint8 **str)
{
	pthread_mutex_lock(&extra_mutex);
	if(ExtraVoiceSearchInInfo(&extraUpdateVoice, lat, lon, index, str) == EXTRA_VOICE_SUCCESS)
	{
		pthread_mutex_unlock(&extra_mutex);
		return EXTRA_VOICE_SUCCESS;
	}
	pthread_mutex_unlock(&extra_mutex);

	if(ExtraVoiceSearchInInfo(&extraBaseVoice, lat, lon, index, str) == EXTRA_VOICE_SUCCESS)
	{
		return EXTRA_VOICE_SUCCESS;
	}

	return EXTRA_VOICE_SEARCH_ERROR;
}

unsigned int CustomExtraVoiceInitial(void)
{
    CustomExtraVoiceGetValue();

    InitExtraVoiceFile(&extraBaseVoice);
    InitExtraVoiceFile(&extraUpdateVoice);
    pBasicVoice = &extraBaseVoice;
    return 0;
}     /* CustomExtraVoiceInitial */

void CustomExtraVoiceDeInit(void)
{
	if(extraBaseVoice.hFile >= 0)
	{
		close(extraBaseVoice.hFile);
	}

	if(extraUpdateVoice.hFile >= 0)
	{
		close(extraBaseVoice.hFile);
	}
}

int32 ExtraVoiceUpdate(const char *path)
{
	char cmd[1024];
	pthread_mutex_lock(&extra_mutex);
	if(extraUpdateVoice.hFile >= 0)
	{
		close(extraUpdateVoice.hFile);
	}

	//sprintf(cmd, "mv %s %s", path, extraUpdateVoice.fileName);
	//system(cmd);
	remove(extraUpdateVoice.fileName);
	rename(path, extraUpdateVoice.fileName);

	CustomExtraVoiceGetValue();
	InitExtraVoiceFile(&extraUpdateVoice);
	pthread_mutex_unlock(&extra_mutex);
	return 0;
}
