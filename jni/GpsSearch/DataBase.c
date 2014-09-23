#include <string.h>
#include <pthread.h>

#include "config.h"
#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"

#define __DATABASE_C__
#include "DataBase.h"
#include "BasicDataBase.h"
#include "UpdateDataBase.h"
#include "SelfDataBase.h"

void GetStringFromJava(JNIEnv *env, jstring str, char *pBuf)
{
	size_t len;
	const char *pStr = (*env)->GetStringUTFChars(env, str, 0);

	len = (*env)->GetStringLength(env, str);
	memcpy(pBuf, pStr, len);

	(*env)->ReleaseStringUTFChars(env, str, pStr);

	pBuf[len] = 0;
	pBuf[len + 1] = 0;
}

int32 CommonParseRecord(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord)
{
    if(pRecord == NULL || buf == NULL)
    {
        LOGD("parameter error");
        return RECORD_ERR_PARAMETER;
    }
#if 0
    pRecord->driver_mode = buf[0];//(buf[0] >> 4) & 0x0F;           //mode
//    pRecord->speed_limit = (buf[0] & 0xFF) * 10;           //unit: KM
    pRecord->ep_cursor = buf[2];	// * 2;
    pRecord->sp_cursor = buf[3];	// * 2;
#else
    pRecord->driver_mode = (buf[0] >> 4) & 0x0F;           //mode
    pRecord->speed_limit = (buf[0] & 0x0F) * 10;           //unit: KM
    pRecord->voice_page = (buf[4] >> 4) & 0x0F;
    pRecord->ep_cursor = buf[2] * 2;
    pRecord->sp_cursor = buf[3] * 2;
#endif
    pRecord->voice_index = buf[1];
    pRecord->sp2ep_cursor = buf[4];
    pRecord->avg_spd_dist = buf[8];
    pRecord->ep_lat = LATITUDE_FROM_BUF(buf);
    pRecord->ep_lon = LONGITUDE_FROM_BUF(buf);
    pRecord->sp_ep_lat = (buf[12] << 8) | buf[13];
    pRecord->sp_ep_lon = (buf[14] << 8) | buf[15];
    pRecord->pDatabase = pDataBase;
    if( pRecord->ep_lat > 900000 )
    {
        //make the latitude become Negative
        pRecord->ep_lat |= (0xFF << 24);
    }

    if( pRecord->ep_lon > 1800000 )
    {
        //make the longitude become Negative
        pRecord->ep_lon |= (0xFF << 24);
    }

    if( pRecord->sp_ep_lat & 0xF000 )
    {
        //if the sp_ep_lat is negative
        pRecord->sp_lat = pRecord->ep_lat - (0xFFFF - pRecord->sp_ep_lat);
    }
    else
    {
        pRecord->sp_lat = pRecord->ep_lat + pRecord->sp_ep_lat;
    }

    if( pRecord->sp_ep_lon & 0xF000 )
    {
        pRecord->sp_lon = pRecord->ep_lon - (0xFFFF - pRecord->sp_ep_lon);
    }
    else
    {
        pRecord->sp_lon = pRecord->ep_lon + pRecord->sp_ep_lon;
    }

    //check the range
    if( (pRecord->ep_lat > 900000 || pRecord->ep_lat < -90000)
        || (pRecord->ep_lon > 1800000 || pRecord->ep_lon < -1800000)
        || (pRecord->sp_lat > 900000 || pRecord->sp_lat < -90000)
        || (pRecord->sp_lon > 1800000 || pRecord->sp_lon < -1800000) )
    {
        LOGD("record error, %d %d %d %d ", pRecord->ep_lat, pRecord->ep_lon, pRecord->sp_ep_lat, pRecord->sp_ep_lon);
        return RECORD_ERR_EXCEED_RANGE;
    }

    return RECORD_SUCCESS;
}

unsigned char DecodeKeyGenerate(char *FKey, char *PKey, char *decode)
{
	unsigned char i = 0;
	unsigned char sel_numb_decode = 0, pswd_empty_numb00 = 0, pswd_empty_numbFF = 0;
	unsigned char pswd_buffer_tmp[PASSWORD_LEN], flash_Security_Reg_used[PASSWORD_LEN];


	return 1;
}

int32 DataBaseUnRegister(PDATABASE pDataBase)
{
	int32 ret = DATABASE_SUCCESS;
	PDATABASE pDB;

	pthread_mutex_lock(&DataBaseMutex);
	list_for_each_entry(pDB, &DataBaseList, db_head)
	{
		if(pDB == pDataBase)
		{
			LOGD("found");
			pDB->DeInit(pDB);
			list_del(&pDB->db_head);
			break;
		}
	}
	pthread_mutex_unlock(&DataBaseMutex);

    return 0;
}

int32 DataBaseRegister(PDATABASE pDataBase)
{
    int32 ret = DATABASE_SUCCESS;


    if(pDataBase->Init != NULL)
    {
        ret = pDataBase->Init(pDataBase, NULL);
    }

    pthread_mutex_lock(&DataBaseMutex);
    if(ret == DATABASE_SUCCESS)
    {
        list_add_tail(&pDataBase->db_head, &DataBaseList);
    }
    pthread_mutex_unlock(&DataBaseMutex);

    return ret;
}

int32 DataBaseInit(void)
{
    int32 ret = DATABASE_SUCCESS;

    INIT_LIST_HEAD(&DataBaseList);
    pthread_mutex_init(&DataBaseMutex, NULL);
    ret = DataBaseRegister(pBasicDataBase);
    if(ret != DATABASE_SUCCESS)
    {
        LOGD("Register basic database fail");
    }

    ret = DataBaseRegister(pUpdateDataBase);
    if(ret != DATABASE_SUCCESS)
	{
		LOGD("Register update database fail");
	}

    ret = DataBaseRegister(pSelfDataBase);
	if(ret != DATABASE_SUCCESS)
	{
		LOGD("Register self point database fail");
	}


    return ret;
}

int32 DataBaseDeInit(void)
{
    int32 ret = DATABASE_SUCCESS;
    PDATABASE pDB;

    pthread_mutex_lock(&DataBaseMutex);
    list_for_each_entry(pDB, &DataBaseList, db_head)
    {
        if(pDB != NULL && pDB->DeInit != NULL)
        {
            LOGD("Deinit database %s", pDB->pFileName);
            pDB->DeInit(pDB);
        }
    }
    pthread_mutex_unlock(&DataBaseMutex);

    return ret;
}
