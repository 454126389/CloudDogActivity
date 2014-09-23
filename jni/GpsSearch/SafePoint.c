#include <stdio.h>
#include <fcntl.h>
#include <pthread.h>
#include "config.h"
#include "common.h"
#include "List.h"
#include "GpsData.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "AlarmProcess.h"
#include "thirdvoice.h"
#include "ThirdVoiceTable.h"
#include "PointSearch.h"

static uint8 safe_enter(ALARM_POINT_NODE *pPoint)
{
	uint8 voice_flag;
	DataBase_Record_Format *pRecord = pPoint->DBRecord;

	pPoint->status = ALARM_POINT_STATUS_EXIT;

	VoicePlayPoint(pPoint, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);

	return 0;
}

POINT_PROCESS_TYPE gSafePointProcess =
{
	.enter = safe_enter,
};
