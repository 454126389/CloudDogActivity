#ifndef __ENTEREXITDATABASE_H__
#define __ENTEREXITDATABASE_H__

#ifdef __ENTEREXITDATABASE_C__
#define __ENTEREXITDATABASE_DEC__
#else
#define __ENTEREXITDATABASE_DEC__ extern
#endif

#define EXTRA_RECORD_SIZE           						32
#define EXTRA_RECORD_INDEX          						6
#define EXTRA_RECORD_LON          	  						3
#define EXTRA_VOICE_START           						10
#define EXTRA_MAX_LEN               						22

#define EXTRA_VOICE_SUCCESS									(0)
#define EXTRA_VOICE_NO_FILE									(-1)
#define EXTRA_VOICE_FILE_READ_ERROR							(-2)
#define EXTRA_VOICE_FILE_SIZE_ERROR							(-3)
#define EXTRA_VOICE_FILE_TAIL_ERROR							(-4)
#define EXTRA_VOICE_FILE_DATA_ERROR							(-5)
#define EXTRA_VOICE_SEARCH_ERROR							(-6)
#define EXTRA_VOICE_SEARCH_LAT_ERROR						(-7)

#define EXTRA_VOICE_SEARCH_SUCCESS							(0)

typedef struct _EXTRA_VOICE_INFO
{
	char fileName[64];
	int hFile;
	uint32 nTotalRecord;
	uint32 nVersion;
	uint32 status;
	uint32 nStartLat;
	uint32 nEndLat;
	uint32 nStartLon;
	uint32 nEndLon;
	uint32 nSizePerRecord;
	uint8 key[EXTRA_RECORD_SIZE];
	uint32 (*read)(struct _EXTRA_VOICE_INFO *pInfo, uint32 index, uint8 *buf);
}EXTRA_VOICE_INFO;

__ENTEREXITDATABASE_DEC__ uint32 InitExtraVoiceFile(EXTRA_VOICE_INFO *pInfo);
__ENTEREXITDATABASE_DEC__ int32 ExtraVoiceSearch(int32 lat, int32 lon, uint32 index, uint8 **str);
__ENTEREXITDATABASE_DEC__ unsigned int CustomExtraVoiceInitial(void);
__ENTEREXITDATABASE_DEC__ void CustomExtraVoiceDeInit(void);
__ENTEREXITDATABASE_DEC__ int32 ExtraVoiceUpdate(const char *path);
__ENTEREXITDATABASE_DEC__ EXTRA_VOICE_INFO *pBasicVoice;
#endif
