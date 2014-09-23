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
#include "Display.h"

static uint8 self_enter(ALARM_POINT_NODE *pPoint)
{
	DataBase_Record_Format *pRecord = pPoint->DBRecord;
	uint8 voice_index;

	pPoint->status = ALARM_POINT_STATUS_CHECK_SPEED;
	VoicePlayPoint(pPoint, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);

	RefreshDisplayByPoint(pPoint);

	return 0;
}

static uint8 self_leave(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	RemoveDisplayByPoint(pPoint);

	if(flag == 1)
	{
		//alnormal leave
		return 0;
	}

	pPoint->status = ALARM_POINT_STATUS_EXIT;

	VoicePlayCommon(sfp_music_sp_voice, TTS_PLAY_LEVEL_MID, VOICE_DATA_FLAG_NULL);

	return 0;
}

static void self_timer(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	uint32 voice_alarm_range_start, voice_alarm_range_end;

	if(flag == 0)
	{
		//flag 0: normal point, 1: nearest point
		return;
	}

	if(GetCurSpeed() > pPoint->DBRecord->speed_limit)
	{
		if(pPoint->DBRecord->speed_limit > 70)
		{
			voice_alarm_range_start = 300;
			voice_alarm_range_end = 500;
		}
		else
		{
			voice_alarm_range_start = 150;
			voice_alarm_range_end = 300;
		}

		if(pPoint->cur2ep_dist > voice_alarm_range_start && pPoint->cur2ep_dist < voice_alarm_range_end
				&& !(pPoint->flag.flag1.flag & ALARM_POINT_EXTRA_FLAG_SPEED_OVER_ALARM))
		{
			//don't alarm the speed, only voice alarm once
			pPoint->flag.flag1.cnt = 0;
			VoicePlayCommon(near_point_voice, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);
			pPoint->flag.flag1.flag |= ALARM_POINT_EXTRA_FLAG_SPEED_OVER_ALARM;
		}
		else
		{
			if(pPoint->flag.flag1.cnt > 0 && (--(pPoint->flag.flag1.cnt)) == 0)
			{
				VoicePlayCommon(ding_voice, TTS_PLAY_LEVEL_IGNORE, VOICE_DATA_FLAG_NULL);
				if(pPoint->cur2ep_dist >= voice_alarm_range_end)
				{
					pPoint->flag.flag1.cnt = 2;
				}
				else
				{
					pPoint->flag.flag1.cnt = 1;
				}
			}
		}
	}

	RefreshDisplayByPoint(pPoint);
}

POINT_PROCESS_TYPE gSelfPoint =
{
	self_enter,
	self_leave,
	self_timer,
	NULL
};
