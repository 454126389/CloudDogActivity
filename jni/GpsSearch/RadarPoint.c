#include <stdio.h>
#include <fcntl.h>
#include <pthread.h>
#include "config.h"
#include "common.h"
#include "List.h"
#include "GpsData.h"
#include "GpsdataListener.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "AlarmProcess.h"
#include "thirdvoice.h"
#include "ThirdVoiceTable.h"
#include "PointSearch.h"
#include "Display.h"
#include "radar.h"

#define RADAR_POINT_ALIVE_IN_TIME						1
#define RADAR_POINT_ALWAYS_ALIVE						0

typedef struct
{
	uint8 type;
	uint8 level;
	uint8 flag;
	uint8 pt_cnt;
}SIMULATE_RADAR_INFO;

static SIMULATE_RADAR_INFO info;

static uint8 inline RadarNotify(SIMULATE_RADAR_INFO *pInfo)
{
	uint8 par[3];

	par[0] = pInfo->level;
	par[1] = pInfo->type;
	par[2] = RADAR_ALARM_BY_SIMULATE;
	return NotifyUI(NOTIFY_UI_CODE_RADAR_WARNNING, par, 3);
}

static uint8 inline GetRadarTypeByPoint(ALARM_POINT_NODE *pPoint)
{
	uint8 ret = RADAR_NULL;
	switch(pPoint->DBRecord->voice_index)
	{
	case 0xB3:
	case 0xBB:
		return RADAR_K_BAND;

	case 0xB4:
	case 0xBC:
		return RADAR_KA_BAND;

	case 0xB5:
	case 0xBD:
		return RADAR_KU_BAND;

	case 0xB6:
	case 0xBE:
		return RADAR_X_BAND;

	case 0xB7:
	case 0xBF:
		return RADAR_LASER;
	}

	return RADAR_NULL;
}

static uint8 radar_enter(ALARM_POINT_NODE *pPoint)
{
	pPoint->status = ALARM_POINT_STATUS_CHECK_SPEED;

	info.level = RADAR_LEVEL_HIGH;
	info.type = GetRadarTypeByPoint(pPoint);

	if(pPoint->DBRecord->voice_index >= 0xBB && pPoint->DBRecord->voice_index < 0xC0)
	{
		info.flag = RADAR_POINT_ALIVE_IN_TIME;
	}

	RadarNotify(&info);
	info.pt_cnt += 1;

	return 0;
}

static uint8 radar_leave(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	pPoint->status = ALARM_POINT_STATUS_EXIT;

	info.pt_cnt -= 1;
	if(info.pt_cnt == 0)
	{
		info.level = RADAR_STOP;
		RadarNotify(&info);
	}

	return 0;
}

static void radar_timer(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	return;
}

POINT_PROCESS_TYPE gRadarPoint =
{
	radar_enter,
	radar_leave,
	radar_timer,
	NULL
};
