#ifndef __DATABASE_H__
#define __DATABASE_H__

#ifdef __DATABASE_C__
#define __DATABASE_DEC__
#else
#define __DATABASE_DEC__ extern
#endif

#define PASSWORD_LEN									16

#define DATABASE_SUCCESS                        (0)
#define DATABASE_ERR_NO_FILE                    (-1)
#define DATABASE_ERR_OVER_READ                  (-2)        //read some data, reach the end of file
#define DATABASE_ERR_CHECK                      (-3)
#define DATABASE_ERR_ADDR_EXCEED_SIZE           (-4)        //address exceed the file size
#define DATABASE_ERR_NOT_ENOUGH_DATA            (-5)        //address exceed the file size
#define DATABASE_ERR_WRITE						(-6)

typedef struct _DATABASE* PDATABASE;

typedef struct _DATABASE
{
    const char *pFileName;
    /* the total number of record */
    int32 nTotalRecord;
    /* database version */
    int32 nVersion;
    /* the address that current read */
    int32 nCurAddr;
    /* address for first/last record in the database */
    int32 nFirstRecordAddr;
    int32 nLastRecordAddr;

    int32 nSearchRange;
    int32 nSizePerRecord;

    struct list_head db_head;
    pthread_mutex_t mutex;

    //if has occured error, return the err code. otherwise return the DATABASE_SUCCESS
    //if return error, will register database fail
    int32 (*Init)(PDATABASE pDataBase, const char *file);
    int32 (*DeInit)(PDATABASE pDataBase);
    int32 (*ReadRecord)(PDATABASE pDataBase, int32 index, uint8 *buf);
    int32 (*ParseRecord)(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord);
    int32 (*EncodeRecord)(PDATABASE pDataBase, int32 index, uint8 *buf);
    int32 (*DecodeRecord)(PDATABASE pDataBase, int32 index, uint8 *buf);
    int32 (*Search)(PDATABASE pDataBase, int32 latitude, int32 longitude, SubmitRecord pSubmit);
    int32 (*SaveRecord)(PDATABASE pDataBase, int32 index, uint8 *buf);
    int32 (*DelRecord)(PDATABASE pDataBase, int32 index);
    int32 (*GetFreeIndex)(PDATABASE pDataBse);
    int32 (*Update)(PDATABASE pDataBase, const char *path);
    void *extra;
}DataBaseType;

__DATABASE_DEC__ int32 CommonParseRecord(PDATABASE pDataBase, uint8 *buf, DataBase_Record_Format *pRecord);
__DATABASE_DEC__ unsigned char DecodeKeyGenerate(char *FKey, char *PKey, char *decode);
__DATABASE_DEC__ int32 DataBaseRegister(PDATABASE pDataBase);
__DATABASE_DEC__ int32 DataBaseUnRegister(PDATABASE pDataBase);
__DATABASE_DEC__ int32 DataBaseInit(void);
__DATABASE_DEC__ int32 DataBaseDeInit(void);
__DATABASE_DEC__ pthread_mutex_t DataBaseMutex;
__DATABASE_DEC__ struct list_head DataBaseList;

#endif
