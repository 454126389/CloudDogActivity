#include <stdio.h>
#include <fcntl.h>
#include <pthread.h>

#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "DataBase.h"
#include "PointSearch.h"

#define __SELFDATABASE_C__
#include "SelfDataBase.h"

//#define DATABASE_PASS_PASSWORD

struct self_item
{
	int32 index;
	struct list_head item_head;
};

#define SIZE_PER_BASICDB_RECORD                     16
#define FILE_HEADER_SIZE							64		// bytes

typedef struct _BASICDB
{
    int pDBFile;
    int32 HeaderSize;
    int32 FileSize;
    uint8 HeaderBuf[FILE_HEADER_SIZE];
    struct list_head item_list;
}BASICDBTYPE;

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
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, "selfpoint", fmt, ##args)

static void GetDBFileNameFromJava(JNIEnv *env, jobject thiz)
{
	jfieldID FieldID;
	jstring str;
	jclass clazz = (*env)->GetObjectClass(env, thiz);

	FieldID = (*env)->GetStaticFieldID(env, clazz, "SELF_POINT_FILE_NAME", "Ljava/lang/String;");
	str = (jstring)(*env)->GetStaticObjectField(env, clazz, FieldID);
	GetStringFromJava(env, str, DBFileName);
	(*env)->DeleteLocalRef(env, clazz);
	//LOGD("self point file name %s", DBFileName);
}

static int32 DecodeRecord(PDATABASE pDataBase, int32 index, uint8 *buf)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	uint8 i, tmp;


#ifdef DATABASE_PASS_PASSWORD
	return 0;
#endif

	for( i = 0; i < 16; i++ )
	{
		if(( i % 2 ) == 0 )
		{
			tmp = (buf[i] & 0xF0) | ((buf[i + 1] & 0xF0) >> 4);
		}
		else
		{
			buf[i] = ((buf[i - 1] & 0x0F) << 4) | ((buf[i] & 0x0F));
			buf[i - 1] = tmp;
		}
	}

	return 0;
}

static int32 ReadOneRecord(PDATABASE pDataBase, int32 index, uint8 *buf)
{
    int32 size, addr;
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;

    addr = pDataBase->nSizePerRecord * index + pBasicDB->HeaderSize;

    //LOGD("read %d size %d", addr, pDataBase->nSizePerRecord);
    if(lseek(pBasicDB->pDBFile, addr, SEEK_SET) == -1)
    {
        LOGD("self point DataBase cann't seek addr %d", addr);
        return DATABASE_ERR_ADDR_EXCEED_SIZE;
    }

    size = read(pBasicDB->pDBFile, buf, pDataBase->nSizePerRecord);
    if(size != pDataBase->nSizePerRecord)
    {
        LOGD("self point DataBase read %d record error, addr %d, size %d", index, addr, size);
        return DATABASE_ERR_NOT_ENOUGH_DATA;
    }

    DecodeRecord(pDataBase, index, buf);
    //LOGD("%x, %x, %x, %x", buf[0], buf[1], buf[2], buf[3]);

    return DATABASE_SUCCESS;
}

static int32 CheckDataBase(PDATABASE pDataBase, uint32 *recordSize)
{
	int32 size, addr;
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	uint8 *buf = pBasicDB->HeaderBuf;
	struct self_item *pItem = NULL;
	int i, j, tSize = 0;
	uint8 t;

	size = read(pBasicDB->pDBFile, buf, FILE_HEADER_SIZE);
	if(size != FILE_HEADER_SIZE)
	{
		LOGD("self point DataBase read header error, size %d", size);
		return DATABASE_ERR_NOT_ENOUGH_DATA;
	}

	for(i = 0; i < size; i++)
	{
		t = buf[i];
		for (j = 0; j < 8; j++)
		{
			if(t & (1 << j))
			{
				pItem = malloc(sizeof(struct self_item));
				pItem->index = i * 8 + j;
				//LOGD("add index %d", pItem->index);
				list_add_tail(&pItem->item_head, &pBasicDB->item_list);
				tSize++;
			}
		}
	}

	*recordSize = tSize;
	pBasicDB->FileSize = lseek(pBasicDB->pDBFile, 0, SEEK_END);

    return DATABASE_SUCCESS;
}

static int32 ParseRecord(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord)
{
	return CommonParseRecord(pDataBase, buf, pRecord);
}

static int32 Search(PDATABASE pDataBase, int32 latitude, int32 longitude, SubmitRecord pSubmit)
{
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    struct self_item *pitem;
    int32 ret = SEARCH_SUCCESS;
    int32 range[4];
    uint8 buf[SIZE_PER_BASICDB_RECORD];
    int32 tmplat, tmplon;

    range[LAT_MIN] = latitude - SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LAT_MAX] = latitude + SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LON_MIN] = longitude - SearchRangeArray[DEFAULT_SEARCH_RANGE];
    range[LON_MAX] = longitude + SearchRangeArray[DEFAULT_SEARCH_RANGE];

    list_for_each_entry(pitem, &pBasicDB->item_list, item_head)
    {
    	if( ReadOneRecord(pDataBase, pitem->index, buf) != DATABASE_SUCCESS )
		{
			LOGD("%s read %d record fail", __FUNCTION__, pitem->index);
			return SEARCH_ERR_RECORD_READ_FAIL;
		}

		tmplat = LATITUDE_FROM_BUF(buf);
		tmplon = LONGITUDE_FROM_BUF(buf);
    	//LOGD("check (%d, %d)", tmplat, tmplon);
		if(IS_IN_RANGE(tmplat, range[LAT_MIN], range[LAT_MAX]))
		{
			if(IS_IN_RANGE(tmplon, range[LON_MIN], range[LON_MAX]))
			{
				if(pSubmit != NULL)
				{
					//DataBase_Record_Format *record = (DataBase_Record_Format *)malloc(sizeof(DataBase_Record_Format));
					DataBase_Record_Format *record = (DataBase_Record_Format *)malloc(sizeof(DataBase_Record_Format));
					record->addrInFile = pitem->index;
					if(pDataBase->ParseRecord(pDataBase, buf, record) == DATABASE_SUCCESS)
					{
						//LOGD("%d: (%d,%d,%d) (%d,%d,%d)", record->addrInFile, record->ep_lat, record->ep_lon, record->ep_cursor, record->sp_lat, record->sp_lon, record->sp_cursor);
						pSubmit(record);
					}
				}
			}
		}
    }

    return ret;
}

static int32 SaveRecord(PDATABASE pDataBase, int32 index, uint8 *buf)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	int i, j;
	uint8 *pHeaderbuf = pBasicDB->HeaderBuf;
	int32 addr = 0;
	char *pTmp;
	int32 fileSize;
	struct self_item *pitem;

	if(index == -1) {
		return DATABASE_ERR_WRITE;
	}

	LOGD("save %d (%d, %d)", index, ((buf[5] << 16) | (buf[6] << 8) | buf[7]), ((buf[9] << 16) | (buf[10] << 8) | buf[11]));

	DecodeRecord(pDataBase, index, buf);
	pHeaderbuf[index / 8] |= (1 << (index % 8));

	addr = pDataBase->nSizePerRecord * (index) + pBasicDB->HeaderSize;
	if(lseek(pBasicDB->pDBFile, addr, SEEK_SET) == -1)
	{
		fileSize = lseek(pBasicDB->pDBFile, 0, SEEK_END);
		if(fileSize != addr)
		{
			pTmp = (char *)malloc(addr - fileSize);
			write(pBasicDB->pDBFile, pTmp, addr - fileSize);
			free(pTmp);
		}
	}

	write(pBasicDB->pDBFile, buf, pDataBase->nSizePerRecord);
	lseek(pBasicDB->pDBFile, 0, SEEK_SET);
	write(pBasicDB->pDBFile, pBasicDB->HeaderBuf, FILE_HEADER_SIZE);

	pitem = (struct self_item *)malloc(sizeof(struct self_item));
	pitem->index = index;
	list_add_tail(&pitem->item_head, &pBasicDB->item_list);

	return DATABASE_SUCCESS;
}

static int32 DelRecord(PDATABASE pDataBase, int32 index)
{
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	struct self_item *pitem;
	int32 size;
	uint8 bFound = FALSE;

	if(pBasicDB->HeaderBuf[index / 8] & (1 << (index % 8)) != 1)
	{
		LOGD("self point del index %d error, no this record", index);
		return DATABASE_SUCCESS;
	}

	pBasicDB->HeaderBuf[index / 8] &= (unsigned char )~(1 << (index % 8));

	if(lseek(pBasicDB->pDBFile, 0, SEEK_SET) == -1)
	{
		LOGD("%s seek error", __FUNCTION__);
		return DATABASE_ERR_ADDR_EXCEED_SIZE;
	}

	size = write(pBasicDB->pDBFile, pBasicDB->HeaderBuf, FILE_HEADER_SIZE);
	if(size != FILE_HEADER_SIZE)
	{
		LOGD("%s write data error", __FUNCTION__);
		return DATABASE_ERR_WRITE;
	}

	list_for_each_entry(pitem, &pBasicDB->item_list, item_head)
	{
		if(pitem->index == index)
		{
			bFound = TRUE;
			break;
		}
	}

	if(bFound == TRUE)
	{
		list_del(&pitem->item_head);
		free(pitem);
		LOGD("Delete %d record success", index);
	}

	LOGD("Cann't find %d record", index);
	return DATABASE_SUCCESS;
}

static int32 DeInit(PDATABASE pDataBase)
{
    BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
    struct self_item *pitem, *ptmp;
    struct list_head *pnext;

    if(pBasicDB != NULL && pBasicDB->pDBFile >= 0)
    {
        close(pBasicDB->pDBFile);
    }


    for (pnext = pBasicDB->item_list.next;	\
    	pnext != &(pBasicDB->item_list); )
    {
    	pitem = list_entry(pnext, typeof(*pitem), item_head);
    	pnext = pitem->item_head.next;
//    	LOGD("free %d record", pitem->index);

    	free(pitem);
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

    if(file != NULL)
    {
    	path = file;
    }

    pDataBase->pFileName = path;

    if((pFile = open(path, O_RDWR)) < 0)
    {
        LOGD("open database file %s failed", path);
        return DATABASE_ERR_NO_FILE;
    }

    INIT_LIST_HEAD(&pInfo->item_list);
    pInfo->pDBFile = pFile;
    pInfo->HeaderSize = FILE_HEADER_SIZE;
    pthread_mutex_init(&pDataBase->mutex, NULL);
    

    pDataBase->extra = pInfo;
    pDataBase->nSizePerRecord = SIZE_PER_BASICDB_RECORD;
    pDataBase->nSearchRange = DEFAULT_SEARCH_RANGE;

    do
    {
        if(CheckDataBase(pDataBase, &(pDataBase->nTotalRecord)) != DATABASE_SUCCESS)
        {
            ret = DATABASE_ERR_CHECK;
            break;
        }

        LOGD("self point db has %d record, version %d", pDataBase->nTotalRecord, pDataBase->nVersion);

        return ret;
    }while(0);

    close(pInfo->pDBFile);
    free(pInfo);
    return ret;
}

static int32 GetFreeIndex(PDATABASE pDataBase)
{
	int i, j;
	BASICDBTYPE *pBasicDB = (BASICDBTYPE *)pDataBase->extra;
	uint8 *pHeaderbuf = pBasicDB->HeaderBuf;

	for(i = 0; i < pBasicDB->HeaderSize; i++)
	{
		for (j = 0; j < 8; j++)
		{
			if((pHeaderbuf[i] & (1 << j)) == 0)
			{
				LOGD("selfpoint get free index %d", i * 8 + j);
				return (i * 8 + j);
			}
		}
	}

	return -1;
}

static DataBaseType gSelfDatabase =
{
    .Init = Init,
    .DeInit = DeInit,
    .ReadRecord = ReadOneRecord,
    .ParseRecord = ParseRecord,
    .Search = Search,
    .SaveRecord = SaveRecord,
    .DelRecord = DelRecord,
    .GetFreeIndex = GetFreeIndex,
    .EncodeRecord = DecodeRecord,
    .DecodeRecord = DecodeRecord,
};

DataBaseType *pSelfDataBase = &gSelfDatabase;
