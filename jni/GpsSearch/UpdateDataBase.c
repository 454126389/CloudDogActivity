#include <stdio.h>
#include <fcntl.h>
#include <pthread.h>
#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "DataBase.h"
#include "PointSearch.h"

#define __UPDATEDATABASE_C__
#include "UpdateDataBase.h"

//#define DATABASE_PASS_PASSWORD

typedef struct _BASICDB
{
    int pDBFile;
    int32 LastSearchStatus;
    int32 CurSearchRange;
    int32 nStartIndex;
    int32 nEndIndex;
    int32 nLastIndex;
    uint8 FPassword[PASSWORD_LEN];
    uint8 PPassword[PASSWORD_LEN];
    uint8 DPassword[PASSWORD_LEN];
}BASICDBTYPE;

#define SIZE_PER_BASICDB_RECORD                     16

static char DBFileName[256];					// = "/sdcard/r9555b";

/* 
    for search use 
    0x09 ===> 100m
    0x12 ===> 200m
    0x1B ===> 300m
    0x24 ===> 400m
    0x2D ===> 500m
    0x36 ===> 600m
    0x3F ===> 700m
    0x48 ===> 800m
    0x51 ===> 900m
    0x5A ===> 1000m
    0x64 ===> 1100m
    0x6D ===> 1200m
    0x76 ===> 1300m
*/
static uint8 SearchRangeArray[] = {0x09, 0x12, 0x1B, 0x24, 0x2D, 0x36, 0x3F, 0x48, 0x51, 0x5A, 0x64, 0x6D, 0x76};

#define MAX_SEARCH_RANGE            (sizeof(SearchRangeArray)/sizeof(uint8))

#undef LOGD
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, "update", fmt, ##args)

static void GetDBFileNameFromJava(JNIEnv *env, jobject thiz)
{
	jfieldID FieldID;
	jstring str;
	jclass clazz = (*env)->GetObjectClass(env, thiz);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "UPDATE_FILE_NAME", "Ljava/lang/String;");
	str = (jstring)(*env)->GetStaticObjectField(env, clazz, FieldID);
	GetStringFromJava(env, str, DBFileName);
	(*env)->DeleteLocalRef(env, clazz);
	//LOGD("update file name %s", DBFileName);
}

static void GetDBPasswordFromJava(JNIEnv *env, jobject thiz, BASICDBTYPE *pInfo)
{
	jfieldID FieldID;
	jbyteArray array;
	char *encode;

	jclass clazz = (*env)->FindClass(env, "android/demo/CloudDogPreference");

	FieldID = (*env)->GetStaticFieldID(env, clazz, "mUpdateDatabaseFKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz, FieldID);
	encode = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(pInfo->FPassword, encode, PASSWORD_LEN);
	(*env)->ReleaseByteArrayElements(env, array, encode, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "mUpdateDatabasePKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz, FieldID);
	encode = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(pInfo->PPassword, encode, PASSWORD_LEN);
	(*env)->ReleaseByteArrayElements(env, array, encode, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);
	(*env)->DeleteLocalRef(env, clazz);

	if(DecodeKeyGenerate(pInfo->FPassword, pInfo->PPassword, pInfo->DPassword) == 0)
	{
		LOGD("Generate updateDB password error");
	}

#if 0
	char str[] = "0123456789ABCDEF";
	char buf[128];

	int i;
	for(i = 0; i < PASSWORD_LEN; i++)
	{
		buf[3 * i] = str[(pInfo->FPassword[i] >> 4) & 0x0F];
		buf[3 * i + 1] = str[pInfo->FPassword[i] & 0x0F];
		buf[3 * i + 2] = ',';
	}

	buf[3 * i] = 0;
	LOGD("F key %s", buf);

	for(i = 0; i < PASSWORD_LEN; i++)
	{
		buf[3 * i] = str[(pInfo->PPassword[i] >> 4) & 0x0F];
		buf[3 * i + 1] = str[pInfo->PPassword[i] & 0x0F];
		buf[3 * i + 2] = ',';
	}

	buf[3 * i] = 0;
	LOGD("P key %s", buf);

	for(i = 0; i < PASSWORD_LEN; i++)
	{
		buf[3 * i] = str[(pInfo->DPassword[i] >> 4) & 0x0F];
		buf[3 * i + 1] = str[pInfo->DPassword[i] & 0x0F];
		buf[3 * i + 2] = ',';
	}

	buf[3 * i] = 0;
	LOGD("D key %s", buf);
#endif
}

static void SetVersionToJava(JNIEnv *env, uint32 version)
{
	jfieldID FieldID;
	jclass clazz = (*env)->FindClass(env, "android/demo/CloudDogPreference");
	FieldID = (*env)->GetStaticFieldID(env, clazz, "mUpdateDabaBaseVersion", "I");
	(*env)->SetStaticIntField(env, clazz, FieldID, version);
	(*env)->DeleteLocalRef(env, clazz);
}

static int32 DecodeRecord(PDATABASE pDataBase, uint8 index, uint8 *buf)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	uint8 tvaddr_tmp[PASSWORD_LEN], data_aero_numb = 0;
	uint8 decode_leng_fg = SIZE_PER_BASICDB_RECORD;
	uint8 i, k;

#ifdef DATABASE_PASS_PASSWORD
	return 0;
#endif



	return 0;
}

static int32 ReadOneRecord(PDATABASE pDataBase, int32 index, uint8 *buf)
{
    int32 size, addr;
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;

    addr = pDataBase->nSizePerRecord * index;

    //LOGD("read %d size %d", addr, pDataBase->nSizePerRecord);
    if(lseek(pBasicDB->pDBFile, addr, SEEK_SET) == -1)
    {
        LOGD("update DataBase cann't seek addr %d", addr);
        return DATABASE_ERR_ADDR_EXCEED_SIZE;
    }

    size = read(pBasicDB->pDBFile, buf, pDataBase->nSizePerRecord);
    if(size != pDataBase->nSizePerRecord)
    {
        LOGD("update DataBase read %d record error, addr %d, size %d", index, addr, size);
        return DATABASE_ERR_NOT_ENOUGH_DATA;
    }

    //LOGD("%x, %x, %x, %x", buf[0], buf[1], buf[2], buf[3]);
    DecodeRecord(pDataBase, index, buf);

    return DATABASE_SUCCESS;
}

static int32 CheckDataBase(PDATABASE pDataBase, uint8 *buf)
{
    int32 nTotalRecord = 0;
    uint8 TmpBuf[SIZE_PER_BASICDB_RECORD];
    
    if(ReadOneRecord(pDataBase, 0, buf) != DATABASE_SUCCESS)
    {
        return DATABASE_ERR_CHECK;
    }

    nTotalRecord = buf[0];
    nTotalRecord = (nTotalRecord << 8) | (buf[1]) ;
    nTotalRecord = (nTotalRecord << 8) | (buf[2]) ;
    nTotalRecord = (nTotalRecord << 8) | (buf[3]) ;
    
    if(ReadOneRecord(pDataBase, nTotalRecord, TmpBuf) != DATABASE_SUCCESS)
    {
        LOGD("update DataBase %s is not complete\r\n", pDataBase->pFileName);
        return DATABASE_ERR_CHECK;
    }
    
    return DATABASE_SUCCESS;
}

static int32 ParseRecord(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord)
{
	return CommonParseRecord(pDataBase, buf, pRecord);
}

static int32 CheckInRange(PDATABASE pDataBase, int32 latitude, int32 longitude, int32 start, int32 end)
{
    int32 tmplat = 0, tmplon = 0;
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    
    if( ReadOneRecord(pDataBase, start, buf) != DATABASE_SUCCESS )
    {
        LOGD("%s read %d record fail", __FUNCTION__, start);
        return SEARCH_ERR_RECORD_READ_FAIL;
    }

    tmplat = LATITUDE_FROM_BUF(buf);
    tmplon = LONGITUDE_FROM_BUF(buf);

    if(latitude < tmplat)
    {
        LOGD("lat(%d) is smaller than range", latitude);
        return SEARCH_ERR_REACH_HEAD;
    }

    if( ReadOneRecord(pDataBase, end, buf) != DATABASE_SUCCESS )
    {
        LOGD("%s read %d record fail", __FUNCTION__, end);
        return SEARCH_ERR_RECORD_READ_FAIL;
    }

    tmplat = LATITUDE_FROM_BUF(buf);
    tmplon = LONGITUDE_FROM_BUF(buf);
    
    if(latitude > tmplat)
    {
        LOGD("lat(%d) is larger than range", latitude);
        return SEARCH_ERR_REACH_TAIL;
    }

    return SEARCH_SUCCESS;
}

static int32 RecursiveSearch(PDATABASE pDataBase, int32 start, int32 end, int32 *result, int32 *range)
{
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 mid = (start + end) / 2;
    int32 tmplat = 0;
    
    if(start >= end)
    {
        LOGD("Error, start(%d), end(%d), range(%d,%d)", start, end, range[LAT_MIN], range[LAT_MAX]);
        return SEARCH_ERR_NOT_FOUND;
    }
    else
    {
        if( ReadOneRecord(pDataBase, mid, buf) != DATABASE_SUCCESS )
        {
            LOGD("%s read %d record fail", __FUNCTION__, start);
            return SEARCH_ERR_RECORD_READ_FAIL;
        }

        tmplat = LATITUDE_FROM_BUF(buf);
        //LOGD("%d %d %d %d %d", start, end, tmplat, range[LAT_MIN], range[LAT_MAX]);
        /*
        if((tmplat & 0xFFFFFF00) > (latitude & 0xFFFFFF00))
        {
            return RecursiveSearch(pDataBase, latitude, start, mid - 1, result);
        }
        else if((tmplat & 0xFFFFFF00) < (latitude & 0xFFFFFF00))
        {
            return RecursiveSearch(pDataBase, latitude, mid + 1, end, result);
        }
        */
        if(tmplat > range[LAT_MAX])
        {
            return RecursiveSearch(pDataBase, start, mid, result, range);
        }
        else if(tmplat < range[LAT_MIN])
        {
            return RecursiveSearch(pDataBase, mid, end, result, range);
        }
        else
        {
            *result = mid;
            //LOGD("OK, %d", mid);
            return SEARCH_SUCCESS;
        }
    }

    return SEARCH_SUCCESS;
}

static int32 SearchGetStartIndex(PDATABASE pDataBase, int32 max, int32 *start)
{
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 tmplat = 0;
    uint8 bFound = FALSE, flag;

    if( ReadOneRecord(pDataBase, *start, buf) != DATABASE_SUCCESS )
    {
        LOGD("%s read %d record fail", __FUNCTION__, *start);
        return SEARCH_ERR_RECORD_READ_FAIL;
    }

    tmplat = LATITUDE_FROM_BUF(buf);

    if(tmplat > max)
    {
        flag = SEARCH_DOWN;
    }
    else
    {
        flag= SEARCH_UP;
    }
    
    do
    {
        if( ReadOneRecord(pDataBase, *start, buf) != DATABASE_SUCCESS )
        {
            LOGD("%s read %d record fail", __FUNCTION__, *start);
            return SEARCH_ERR_RECORD_READ_FAIL;
        }

        tmplat = LATITUDE_FROM_BUF(buf);
        
        if(flag == SEARCH_DOWN)
        {
            if(*start == 1)
            {
                bFound = TRUE;
                break;
            }
            else if(tmplat < max)
            {
                bFound = TRUE;
                (*start)++;
                break;
            }
            (*start)--;
        }
        else
        {
            if(*start == pDataBase->nTotalRecord)
            {
                bFound = TRUE;
                break;
            }
            else if(tmplat > max)
            {
                bFound = TRUE;
                (*start)--;
                break;
            }
            (*start)++;
        }
    }while(1);

    if(bFound)
    {
        return SEARCH_SUCCESS;
    }

    return SEARCH_ERR_NOT_FOUND;
}

static int32 SearchFromIndex(PDATABASE pDataBase, int32 latitude, int32 longitude, int32 *start, int32 *range, SubmitRecord pSubmit, int32 flag)
{
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 tmplat = 0, tmplon = 0;
    uint8 bFound = FALSE;
    
    //LOGD("start %d", flag);
    do
    {
        if( ReadOneRecord(pDataBase, *start, buf) != DATABASE_SUCCESS )
        {
            LOGD("%s read %d record fail", __FUNCTION__, *start);
            return SEARCH_ERR_RECORD_READ_FAIL;
        }

        tmplat = LATITUDE_FROM_BUF(buf);
        tmplon = LONGITUDE_FROM_BUF(buf);
        //LOGD("%d,%d,(%d,%d)", tmplat, tmplon, range[LAT_MIN], range[LAT_MAX]);
        if(IS_IN_RANGE(tmplat, range[LAT_MIN], range[LAT_MAX]))
        {
            if(IS_IN_RANGE(tmplon, range[LON_MIN], range[LON_MAX]))
            {
                if(pSubmit != NULL)
                {
                	DataBase_Record_Format *record = (DataBase_Record_Format *)malloc(sizeof(DataBase_Record_Format));
                	record->addrInFile = *start;

                    if(pDataBase->ParseRecord(pDataBase, buf, record) == DATABASE_SUCCESS)
                    {
                        pSubmit(record);
                    }
                }
            }

            bFound = TRUE;
        }

        if(flag == SEARCH_DOWN)
        {
            if(*start == 1)
            {
                break;
            }
            else if(tmplat < range[LAT_MIN] )
            {
                if(bFound == FALSE)
                {
                	//LOGD("can't find the latitude by down search at %s", __FILE__);
                    return SEARCH_ERR_NOT_FOUND;
                }
                else
                {
                    (*start)++;
                    break;
                }
            }
            
            (*start)--;
        }
        else
        {
            if(*start == pDataBase->nTotalRecord)
            {
                break;
            }
            else if(tmplat > range[LAT_MAX])
            {
                if(bFound == FALSE)
                {
                	//LOGD("can't find the latitude by UP search at %s", __FILE__);
                    return SEARCH_ERR_NOT_FOUND;
                }
                else
                {
                    (*start)--;
                    break;
                }
            }
            (*start)++;
        }
    }while(1);

    return SEARCH_SUCCESS;
}

static int32 SearchRangeDetail(PDATABASE pDataBase, int32 latitude, int32 longitude, int32 start, SubmitRecord pSubmit, int32 *range)
{
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 tmpIndex;

    tmpIndex = start;
    ret = SearchFromIndex(pDataBase, latitude, longitude, &tmpIndex, range, pSubmit, SEARCH_DOWN);
    if(ret == SEARCH_SUCCESS)
    {
        pBasicDB->nStartIndex = tmpIndex;
    }
    
    start += 1;
    ret = SearchFromIndex(pDataBase, latitude, longitude, &start, range, pSubmit, SEARCH_UP);
    if(ret == SEARCH_SUCCESS)
    {
        pBasicDB->nEndIndex = start;
    }
    
    return ret;
}

static int32 SearchRangeExtend(PDATABASE pDataBase, int32 latitude, int32 longitude, SubmitRecord pSubmit, int32 *range)
{
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 tmplat = 0, tmplon = 0;
    int32 flag = 0;
    uint8 bFound = FALSE;
    int32 start = 0;

    start = pBasicDB->nStartIndex;
    ret = SearchFromIndex(pDataBase, latitude, longitude, &start, range, pSubmit, SEARCH_DOWN);
    if(ret == SEARCH_SUCCESS)
    {
        pBasicDB->nStartIndex = start;
    }

    start = pBasicDB->nEndIndex;
    ret = SearchFromIndex(pDataBase, latitude, longitude, &start, range, pSubmit, SEARCH_UP);
    if(ret == SEARCH_SUCCESS)
    {
        pBasicDB->nEndIndex = start;
    }
    
    return SEARCH_SUCCESS;
}

static int32 Search(PDATABASE pDataBase, int32 latitude, int32 longitude, SubmitRecord pSubmit)
{
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 index = 0;
    int32 range[4];

    range[LAT_MIN] = latitude - SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LAT_MAX] = latitude + SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LON_MIN] = longitude - SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LON_MAX] = longitude + SearchRangeArray[DEFAULT_SEARCH_RANGE];

    pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
    if(pBasicDB->LastSearchStatus == LAST_SEARCH_NOT_FOUND)
    {
        //LOGD("LAST_SEARCH_NOT_FOUND");
        ret = CheckInRange(pDataBase, latitude, longitude, 1, pDataBase->nTotalRecord);
        if(ret != SEARCH_SUCCESS)
        {
            return ret;
        }
        
        ret = RecursiveSearch(pDataBase, 1, pDataBase->nTotalRecord, &index, range);
        
        if(ret != SEARCH_SUCCESS)
        {
            return ret;
        }
        //LOGD("Recursive index %d", index);
        pBasicDB->LastSearchStatus = LAST_SEARCH_RECURSIVE_OK;
    }

    if(pBasicDB->LastSearchStatus == LAST_SEARCH_RECURSIVE_OK)
    {
        ret = SearchRangeDetail(pDataBase, latitude, longitude, index, pSubmit, range);
        
        if(ret != SEARCH_SUCCESS)
        {
            pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
            return ret;
        }
        
        //LOGD("start %d, End %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);
        
        pBasicDB->nLastIndex = index;
        pBasicDB->LastSearchStatus = LAST_SEARCH_DETAIL_OK;
        pBasicDB->CurSearchRange = 1;

        return ret;
    }

    if(pBasicDB->LastSearchStatus == LAST_SEARCH_DETAIL_OK)
    {
        //LOGD("LAST_SEARCH_DETAIL_OK, %d, %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);
        range[LAT_MIN] = latitude - SearchRangeArray[pBasicDB->CurSearchRange];
        range[LAT_MAX] = latitude + SearchRangeArray[pBasicDB->CurSearchRange];
        
        ret = SearchRangeExtend(pDataBase, latitude, longitude, pSubmit, range);
        pBasicDB->CurSearchRange++;

        if(pBasicDB->CurSearchRange > pDataBase->nSearchRange)
        {
            pBasicDB->CurSearchRange = 0;
            pBasicDB->LastSearchStatus = LAST_SEARCH_RANGE_OK;
        }
    }
    else if(pBasicDB->LastSearchStatus == LAST_SEARCH_RANGE_OK)
    {
        //LOGD("LAST_SEARCH_RANGE_OK, %d, %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);

        range[LAT_MIN] = latitude - SearchRangeArray[pBasicDB->CurSearchRange];
        range[LAT_MAX] = latitude + SearchRangeArray[pBasicDB->CurSearchRange];

        index = pBasicDB->nLastIndex;
        ret = SearchGetStartIndex(pDataBase, range[LAT_MIN], &index);
        if(ret != SEARCH_SUCCESS)
        {
            pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
            return ret;
        }
        
        pBasicDB->nLastIndex = index;
        pBasicDB->nStartIndex = index;
        ret = SearchFromIndex(pDataBase, latitude, longitude, &index, range, pSubmit, SEARCH_UP);
        
        if(ret != SEARCH_SUCCESS)
        {
            pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
            return ret;
        }
        
        
        pBasicDB->nEndIndex = index;
        pBasicDB->CurSearchRange = 1;
        pBasicDB->LastSearchStatus = LAST_SEARCH_DETAIL_OK;
    }

    return ret;
}

static int32 Update(PDATABASE pDataBase, const char *pUpdateFile)
{
	char cmd[1024];
	LOGD("update database start update, %s", pUpdateFile);

	DataBaseUnRegister(pDataBase);
	remove(pDataBase->pFileName);
	if(rename(pUpdateFile, pDataBase->pFileName) == -1)
	{
		LOGD("rename %s %s error", pUpdateFile, pDataBase->pFileName);
	}
	//sprintf(cmd, "mv %s %s", pUpdateFile, pDataBase->pFileName);
	//int ret = system(cmd);

	//if(ret <= 0) {

	//}

	DataBaseRegister(pDataBase);
	LOGD("update database udate finish");
	return 0;
}

static int32 DeInit(PDATABASE pDataBase)
{
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;

    if(pBasicDB != NULL && pBasicDB->pDBFile >= 0)
    {
        close(pBasicDB->pDBFile);
    }
    pDataBase->nTotalRecord = 0;
    pDataBase->nVersion = 0;
    free(pBasicDB);
    
    return DATABASE_SUCCESS;
}

static int32 Init(PDATABASE pDataBase, const char *file)
{
    int pFile = -1;
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    int32 ret = DATABASE_SUCCESS;
    int32 nTotalRecord;
    BASICDBTYPE *pInfo;
    const char *path = DBFileName;
    
    pInfo = (BASICDBTYPE *)malloc(sizeof(BASICDBTYPE));

    memset(pInfo, 0, sizeof(BASICDBTYPE));

    GetDBFileNameFromJava(JniGlobalVariable.pEnv, JniGlobalVariable.thiz);
    GetDBPasswordFromJava(JniGlobalVariable.pEnv, JniGlobalVariable.thiz, pInfo);

    if(file != NULL)
    {
    	path = file;
    }

    pDataBase->pFileName = path;

    if((pFile = open(path, O_RDONLY)) < 0)
    {
        LOGD("open database file %s failed", path);
        free(pInfo);
        return DATABASE_ERR_NO_FILE;
    }

    pthread_mutex_init(&pDataBase->mutex, NULL);
    pInfo->pDBFile = pFile;
    pInfo->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
    pInfo->CurSearchRange = 0;
    

    pDataBase->extra = pInfo;
    pDataBase->nSizePerRecord = SIZE_PER_BASICDB_RECORD;
    pDataBase->nSearchRange = DEFAULT_SEARCH_RANGE;

    do
    {
        if(CheckDataBase(pDataBase, buf) != DATABASE_SUCCESS)
        {
            ret = DATABASE_ERR_CHECK;
            break;
        }

        nTotalRecord = buf[0];
        nTotalRecord = (nTotalRecord << 8) | (buf[1]) ;
        nTotalRecord = (nTotalRecord << 8) | (buf[2]) ;
        nTotalRecord = (nTotalRecord << 8) | (buf[3]) ;
        
        pDataBase->nTotalRecord = nTotalRecord;
        nTotalRecord = buf[4];
        nTotalRecord = (nTotalRecord << 8) | (buf[5]) ;
        nTotalRecord = (nTotalRecord << 8) | (buf[6]) ;
        nTotalRecord = (nTotalRecord << 8) | (buf[7]) ;
        pDataBase->nVersion = nTotalRecord;

        SetVersionToJava(JniGlobalVariable.pEnv, pDataBase->nVersion);
        LOGD("update db has %d record, version %d", pDataBase->nTotalRecord, pDataBase->nVersion);

        return ret;
    }while(0);

    close(pInfo->pDBFile);
    free(pInfo);
    return ret;
}

static DataBaseType gUpdateDatabase =
{
    .Init = Init,
    .DeInit = DeInit,
    .ReadRecord = ReadOneRecord,
    .ParseRecord = ParseRecord,
    .Search = Search,
    .Update = Update,
};

static DataBaseType gDababaseForBasicUpdate =
{
	.Init = Init,
	.DeInit = DeInit,
	.ReadRecord = ReadOneRecord,
};

DataBaseType *pUpdateDataBase = &gUpdateDatabase;
DataBaseType *pDataBaseForBasicUpdate = &gDababaseForBasicUpdate;
