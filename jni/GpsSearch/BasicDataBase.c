#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <pthread.h>
#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "DataBase.h"
#include "PointSearch.h"
#include "UpdateDataBase.h"

#define __BASICDATABASE_C__
#include "BasicDataBase.h"

//#define DATABASE_PASS_PASSWORD

//#define DEBUG_REC

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
    int32 nDefaultRange;
    pthread_t hUpdateThread;
}BASICDBTYPE;

#define SIZE_PER_BASICDB_RECORD                     16

static char BasicDBFileName[256];					// = "/sdcard/r9555b";

#define READ_FILE_FLAG_FORWARD						0
#define READ_FILE_FLAG_BACKWARD						1

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
static uint8 SearchRangeArray[] = {0x09, 0x12, 0x1B, 0x24, 0x2D, 0x36, 0x3F, 0x48, 0x51, 0x5A, 0x64, 0x6D, 0x76, 0};

#define MAX_SEARCH_RANGE            (sizeof(SearchRangeArray)/sizeof(uint8))

#undef LOGD
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, "Basic", fmt, ##args)

static void GetDBFileNameFromJava(JNIEnv *env, jobject thiz)
{
	jfieldID FieldID;
	jstring str;
	jclass clazz = (*env)->GetObjectClass(env, thiz);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "BASE_FILE_NAME", "Ljava/lang/String;");
	str = (jstring)(*env)->GetStaticObjectField(env, clazz, FieldID);
	GetStringFromJava(env, str, BasicDBFileName);
	(*env)->DeleteLocalRef(env, clazz);
	//LOGD("base file name %s", BasicDBFileName);
}

static void GetDBPasswordFromJava(JNIEnv *env, jobject thiz, BASICDBTYPE *pInfo)
{
	jfieldID FieldID;
	jbyteArray array;
	char *encode;

	jclass clazz = (*env)->FindClass(env, "android/demo/CloudDogPreference");

	FieldID = (*env)->GetStaticFieldID(env, clazz, "mBaseDatabaseFKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz, FieldID);
	encode = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(pInfo->FPassword, encode, PASSWORD_LEN);
	(*env)->ReleaseByteArrayElements(env, array, encode, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "mBaseDatabasePKey", "[B");
	array = (jbyteArray)(*env)->GetStaticObjectField(env, clazz, FieldID);
	encode = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(pInfo->PPassword, encode, PASSWORD_LEN);

	(*env)->ReleaseByteArrayElements(env, array, encode, JNI_FALSE);
	(*env)->DeleteLocalRef(env, array);
	(*env)->DeleteLocalRef(env, clazz);

	if(DecodeKeyGenerate(pInfo->FPassword, pInfo->PPassword, pInfo->DPassword) == 0)
	{
		LOGD("Generate baseDb password error");
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
	FieldID = (*env)->GetStaticFieldID(env, clazz, "mBaseDatabaseVersion", "I");
	(*env)->SetStaticIntField(env, clazz, FieldID, (jint)version);
	(*env)->DeleteLocalRef(env, clazz);
}

static int32 DecodeRecord(PDATABASE pDataBase, int32 index, uint8 *buf)
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
        LOGD("Basic DataBase cann't seek addr %d", addr);
        return DATABASE_ERR_ADDR_EXCEED_SIZE;
    }

    size = read(pBasicDB->pDBFile, buf, pDataBase->nSizePerRecord);
    if(size != pDataBase->nSizePerRecord)
    {
        LOGD("Basic DataBase read %d record error, addr %d, size %d", index, addr, size);
        return DATABASE_ERR_NOT_ENOUGH_DATA;
    }

    return DATABASE_SUCCESS;
}

static int32 ReadOneRecordExtra(PDATABASE pDataBase, int32 *index, uint8 *buf, char flag)
{
	char tmp_buf[SIZE_PER_RECORD];
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;

	do
	{
		if(ReadOneRecord(pDataBase, *index, tmp_buf) != DATABASE_SUCCESS)
		{
			break;
		}

		if(	((*(uint32 *)tmp_buf) == 0xFFFFFFFF) && (*(uint32 *)(tmp_buf + 4)) == 0xFFFFFFFF
			&& (*(uint32 *)(tmp_buf + 8)) == 0xFFFFFFFF && (*(uint32 *)(tmp_buf + 12)) == 0xFFFFFFFF)
		{
			if(flag == READ_FILE_FLAG_BACKWARD)
			{
				(*index)--;
				if(*index == 0)
				{
					break;
				}
			}
			else
			{
				(*index)++;
				if(*index >= pDataBase->nTotalRecord)
				{
					break;
				}
			}
			continue;
		}
		else
		{
			memcpy(buf, tmp_buf, pDataBase->nSizePerRecord);
			DecodeRecord(pDataBase, *index, buf);

			return DATABASE_SUCCESS;
		}
	}while(1);

	return DATABASE_ERR_OVER_READ;
}

static int32 CheckDataBase(PDATABASE pDataBase, uint8 *buf)
{
    int32 nTotalRecord = 0;
    uint8 TmpBuf[SIZE_PER_BASICDB_RECORD];
    
    if(ReadOneRecord(pDataBase, 0, buf) != DATABASE_SUCCESS)
    {
        return DATABASE_ERR_CHECK;
    }

    DecodeRecord(pDataBase, 0, buf);
    nTotalRecord = buf[0];
    nTotalRecord = (nTotalRecord << 8) | (buf[1]) ;
    nTotalRecord = (nTotalRecord << 8) | (buf[2]) ;
    nTotalRecord = (nTotalRecord << 8) | (buf[3]) ;
    
    if(ReadOneRecord(pDataBase, nTotalRecord, TmpBuf) != DATABASE_SUCCESS)
    {
        LOGD("Basic DataBase %s is not complete\r\n", pDataBase->pFileName);
        return DATABASE_ERR_CHECK;
    }
    
    return DATABASE_SUCCESS;
}

static int32 ParseRecord(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord)
{
	return CommonParseRecord(pDataBase, buf, pRecord);
}

static int32 CheckInRange(PDATABASE pDataBase, int32 latitude, int32 longitude, int32 *start, int32 *end)
{
    int32 tmplat = 0, tmplon = 0;
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    
    if( ReadOneRecordExtra(pDataBase, start, buf, READ_FILE_FLAG_FORWARD) != DATABASE_SUCCESS )
    {
        LOGD("%s read %d record fail", __FUNCTION__, *start);
        return SEARCH_ERR_RECORD_READ_FAIL;
    }

    tmplat = LATITUDE_FROM_BUF(buf);
    tmplon = LONGITUDE_FROM_BUF(buf);

    if(latitude < tmplat)
    {
        LOGD("lat(%d) is smaller than range", latitude);
        return SEARCH_ERR_REACH_HEAD;
    }

    if( ReadOneRecordExtra(pDataBase, end, buf, READ_FILE_FLAG_BACKWARD) != DATABASE_SUCCESS )
    {
        LOGD("%s read %d record fail", __FUNCTION__, *end);
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

static int32 RecursiveSearch(PDATABASE pDataBase, int32 *start, int32 *end, int32 *result, int32 *range)
{
    uint8 buf[SIZE_PER_BASICDB_RECORD], buf_tmp[SIZE_PER_BASICDB_RECORD], *pdat;
    
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    int32 ret = SEARCH_SUCCESS;
    int32 mid = (*start + *end) / 2;
    int32 tmplat = 0;
    int32 mid_up = 0, tmp;
    uint8 flag = READ_FILE_FLAG_BACKWARD;
    
#ifdef DEBUG_REC
    LOGD("recursive from %d to %d", *start, *end);
#endif
    if(*start >= *end || *start == mid)
    {
        LOGD("Error, start(%d), end(%d), range(%d,%d)", *start, *end, range[LAT_MIN], range[LAT_MAX]);
        return SEARCH_ERR_NOT_FOUND;
    }
    else
    {
    	tmp = mid;
    	mid_up = mid;
    	pdat = buf;
        if( ReadOneRecordExtra(pDataBase, &mid, buf, READ_FILE_FLAG_BACKWARD) != DATABASE_SUCCESS )
        {
            LOGD("%s read %d record fail", __FUNCTION__, *start);
            return SEARCH_ERR_RECORD_READ_FAIL;
        }

        if(mid <= *start)
        {
        	if( ReadOneRecordExtra(pDataBase, &mid_up, buf_tmp, READ_FILE_FLAG_FORWARD) != DATABASE_SUCCESS )
			{
				LOGD("%s read %d record fail", __FUNCTION__, *start);
				return SEARCH_ERR_RECORD_READ_FAIL;
			}

        	if(mid_up >= *end)
        	{
        		LOGD("has not valid data in (%d, %d) at %s", *start, *end, __FILE__);
        		return SEARCH_ERR_NOT_FOUND;
        	}

        	mid = mid_up;
        	pdat = buf_tmp;
        	flag = READ_FILE_FLAG_FORWARD;
        }

		tmplat = LATITUDE_FROM_BUF(pdat);
		if(tmplat > range[LAT_MAX])
		{
			if (flag == READ_FILE_FLAG_FORWARD)
			{
				LOGD("latitude reach the up size at %s", __FILE__);
				return SEARCH_ERR_NOT_FOUND;
			}
#ifdef DEBUG_REC
			LOGD("start recursive down, %d, %d", *start, mid);
#endif
			return RecursiveSearch(pDataBase, start, &mid, result, range);
		}
		else if(tmplat < range[LAT_MIN])
		{
#ifdef DEBUG_REC
			LOGD("start recursive up, %d, %d", mid, *end);
#endif
			return RecursiveSearch(pDataBase, &mid, end, result, range);
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
    uint8 read_flag = READ_FILE_FLAG_BACKWARD;

    if( ReadOneRecordExtra(pDataBase, start, buf, READ_FILE_FLAG_BACKWARD) != DATABASE_SUCCESS )
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
        read_flag = READ_FILE_FLAG_FORWARD;
    }
    
    do
    {
        if( ReadOneRecordExtra(pDataBase, start, buf, read_flag) != DATABASE_SUCCESS )
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
    char read_flag = READ_FILE_FLAG_FORWARD;
    
    if(flag == SEARCH_DOWN) {
    	read_flag = READ_FILE_FLAG_BACKWARD;
    }
#ifdef DEBUG_REC
    LOGD("start search from %d direction %d", *start, flag);
#endif
    do
    {
        if( ReadOneRecordExtra(pDataBase, start, buf, read_flag) != DATABASE_SUCCESS )
        {
            LOGD("%s read %d record fail", __FUNCTION__, *start);
            return SEARCH_ERR_RECORD_READ_FAIL;
        }

        tmplat = LATITUDE_FROM_BUF(buf);
        tmplon = LONGITUDE_FROM_BUF(buf);

#ifdef DEBUG_REC
        LOGD("check %d(%d,%d) range %d-%d", *start, tmplat, tmplon, range[LAT_MIN], range[LAT_MAX]);
#endif

        if(IS_IN_RANGE(tmplat, range[LAT_MIN], range[LAT_MAX]))
        {
            if(IS_IN_RANGE(tmplon, range[LON_MIN], range[LON_MAX]))
            {
                if(pSubmit != NULL)
                {
                    //DataBase_Record_Format *record = (DataBase_Record_Format *)malloc(sizeof(DataBase_Record_Format));
                	DataBase_Record_Format *record = (DataBase_Record_Format *)malloc(sizeof(DataBase_Record_Format));
                	record->addrInFile = *start;

                	record->ori_data = buf;
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
                	//LOGD("can't find the latitude by down search");
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
                	//LOGD("can't find the latitude by up search");
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
    int32 start, end;

    range[LAT_MIN] = latitude - SearchRangeArray[pBasicDB->nDefaultRange];
    range[LAT_MAX] = latitude + SearchRangeArray[pBasicDB->nDefaultRange];
    range[LON_MIN] = longitude - SearchRangeArray[pBasicDB->nDefaultRange];
    range[LON_MAX] = longitude + SearchRangeArray[pBasicDB->nDefaultRange];
    pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
    do
    {
    	if(pBasicDB->LastSearchStatus == LAST_SEARCH_NOT_FOUND)
		{
			//LOGD("LAST_SEARCH_NOT_FOUND");
			start = 1;
			end = pDataBase->nTotalRecord;

			ret = CheckInRange(pDataBase, latitude, longitude, &start, &end);
			if(ret != SEARCH_SUCCESS)
			{
				break;
			}

			ret = RecursiveSearch(pDataBase, &start, &end, &index, range);

			if(ret != SEARCH_SUCCESS)
			{
				break;
			}
#ifdef DEBUG_REC
			LOGD("Recursive index %d", index);
#endif
			pBasicDB->LastSearchStatus = LAST_SEARCH_RECURSIVE_OK;
		}

		if(pBasicDB->LastSearchStatus == LAST_SEARCH_RECURSIVE_OK)
		{
			ret = SearchRangeDetail(pDataBase, latitude, longitude, index, pSubmit, range);

			if(ret != SEARCH_SUCCESS)
			{
				pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
				break;
			}

			//LOGD("start %d, End %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);

			pBasicDB->nLastIndex = index;
			pBasicDB->LastSearchStatus = LAST_SEARCH_DETAIL_OK;
			pBasicDB->CurSearchRange = 1;

			break;
		}

		if(pBasicDB->LastSearchStatus == LAST_SEARCH_DETAIL_OK)
		{
#ifdef DEBUG_REC
			LOGD("LAST_SEARCH_DETAIL_OK, %d, %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);
#endif
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
#ifdef DEBUG_REC
			LOGD("LAST_SEARCH_RANGE_OK, %d, %d", pBasicDB->nStartIndex, pBasicDB->nEndIndex);
#endif
			range[LAT_MIN] = latitude - SearchRangeArray[pBasicDB->CurSearchRange];
			range[LAT_MAX] = latitude + SearchRangeArray[pBasicDB->CurSearchRange];

			index = pBasicDB->nLastIndex;
			ret = SearchGetStartIndex(pDataBase, range[LAT_MIN], &index);
			if(ret != SEARCH_SUCCESS)
			{
				pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
				break;
			}

			pBasicDB->nLastIndex = index;
			pBasicDB->nStartIndex = index;
			ret = SearchFromIndex(pDataBase, latitude, longitude, &index, range, pSubmit, SEARCH_UP);

			if(ret != SEARCH_SUCCESS)
			{
				pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
				break;
			}


			pBasicDB->nEndIndex = index;
			pBasicDB->CurSearchRange = 1;
			pBasicDB->LastSearchStatus = LAST_SEARCH_DETAIL_OK;
		}
    } while (0);

    return ret;
}

int32 Test()
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pBasicDataBase->extra;
	int32 ret = SEARCH_SUCCESS;
	int32 index = 0;
	int32 range[4];
	int32 start, end;

	int latitude = 245257;
	int longitude = 1181371;

	range[LON_MIN] = longitude - SearchRangeArray[pBasicDB->nDefaultRange];
	range[LON_MAX] = longitude + SearchRangeArray[pBasicDB->nDefaultRange];
	range[LAT_MIN] = latitude - SearchRangeArray[pBasicDB->CurSearchRange];
	range[LAT_MAX] = latitude + SearchRangeArray[pBasicDB->CurSearchRange];

	pBasicDB->nStartIndex = 47361;
	pBasicDB->nEndIndex = 48781;

	index = pBasicDB->nLastIndex;
	ret = SearchGetStartIndex(pBasicDataBase, range[LAT_MIN], &index);
	if(ret != SEARCH_SUCCESS)
	{
		LOGD("index ok %d", index);
		pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
	}

	pBasicDB->nLastIndex = index;
	pBasicDB->nStartIndex = index;
	ret = SearchFromIndex(pBasicDataBase, latitude, longitude, &index, range, NULL, SEARCH_UP);

	if(ret != SEARCH_SUCCESS)
	{
		pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
	}


	pBasicDB->nEndIndex = index;
	pBasicDB->CurSearchRange = 1;
	pBasicDB->LastSearchStatus = LAST_SEARCH_DETAIL_OK;
}

static int32 DelRecord(PDATABASE pDataBase, int32 index)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	char buf[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

	if(lseek(pBasicDB->pDBFile, index * SIZE_PER_RECORD, SEEK_SET) == -1)
	{
		LOGD("delete record %d error", index);
		return -1;
	}

	write(pBasicDB->pDBFile, buf, sizeof(buf));
	return 0;
}

static uint8 *pDelRecord = NULL;
static void BasicUpdateCB(DataBase_Record_Format *pRecord)
{
	if(pDelRecord != NULL)
	{
		if(!memcmp(pDelRecord, pRecord->ori_data, 16))
		{
			LOGD("found the record %d", pRecord->addrInFile);
			pBasicDataBase->DelRecord(pBasicDataBase, pRecord->addrInFile);
		}
	}
}

static void *basicUpdateThread(void *arg)
{
	const char *pFile = (const char *)arg;
	PDATABASE pDB = pDataBaseForBasicUpdate;
	PDATABASE pDataBase = (PDATABASE)arg;
	BASICDBTYPE *pBasicDB = pDataBase->extra;
	uint32 lat, lon;
	uint8 buf[SIZE_PER_RECORD];
	int i;
	LOGD("basic database start update");
	pthread_mutex_lock(&pDataBase->mutex);
	pBasicDB->nDefaultRange = MAX_SEARCH_RANGE - 1;

	for(i = 1; i <= pDB->nTotalRecord; i++)
	{
		//LOGD("start update %d record", i);
		pDB->ReadRecord(pDB, i, buf);
		lat = LATITUDE_FROM_BUF(buf);
		lon = LONGITUDE_FROM_BUF(buf);
		//LOGD("update lat(%d) lon(%d)", lat, lon);
		pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
		pDelRecord = buf;
		Search(pDataBase, lat, lon, BasicUpdateCB);
	}

	pDB->DeInit(pDataBaseForBasicUpdate);
	pBasicDB->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
	pBasicDB->nDefaultRange = DEFAULT_SEARCH_RANGE;
	pthread_mutex_unlock(&pDataBase->mutex);
	LOGD("basic database update Finish");

	uint8 ret = 1;
	NotifyUIInThread(NOTIFY_UI_CODE_BASIC_DATABASE_UPDATE_FINISH, &ret, 1);
	return NULL;
}

static int32 Update(PDATABASE pDataBase, const char *pUpdateFile)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	PDATABASE pDB = pDataBaseForBasicUpdate;

	if(DATABASE_SUCCESS == pDB->Init(pDataBaseForBasicUpdate, pUpdateFile))
	{
		pthread_create(&pBasicDB->hUpdateThread, NULL, basicUpdateThread, pDataBase);
	}

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
    const char *path = BasicDBFileName;
    
    pInfo = (BASICDBTYPE *)malloc(sizeof(BASICDBTYPE));
    memset(pInfo, 0, sizeof(BASICDBTYPE));

    GetDBFileNameFromJava(JniGlobalVariable.pEnv, JniGlobalVariable.thiz);
    GetDBPasswordFromJava(JniGlobalVariable.pEnv, JniGlobalVariable.thiz, pInfo);

    if(file != NULL)
    {
    	path = file;
    }

    pDataBase->pFileName = path;

    if((pFile = open(path, O_RDWR)) < 0)
    {
        LOGD("open database file %s failed", path);
        free(pInfo);
        return DATABASE_ERR_NO_FILE;
    }

    pthread_mutex_init(&pDataBase->mutex, NULL);
    pInfo->pDBFile = pFile;
    pInfo->LastSearchStatus = LAST_SEARCH_NOT_FOUND;
    pInfo->CurSearchRange = 0;
    pInfo->nDefaultRange = DEFAULT_SEARCH_RANGE;
    
    pDataBase->pFileName = path;
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
        LOGD("Basic db has %d record, version %d", pDataBase->nTotalRecord, pDataBase->nVersion);
        SetVersionToJava(JniGlobalVariable.pEnv, pDataBase->nVersion);
        return ret;
    }while(0);

    close(pInfo->pDBFile);
    free(pInfo);

    return ret;
}

static DataBaseType gBasicDatabase = 
{
    .Init = Init,
    .DeInit = DeInit,
    .ReadRecord = ReadOneRecord,
    .ParseRecord = ParseRecord,
    .DecodeRecord = DecodeRecord,
    .Search = Search,
    .Update = Update,
    .DelRecord = DelRecord,
};

DataBaseType *pBasicDataBase = &gBasicDatabase;
