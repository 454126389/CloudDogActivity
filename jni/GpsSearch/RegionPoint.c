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

typedef struct {
	int32 last_lat;
	int32 last_lon;
	uint32 distance;
	uint32 point_cnt;
	uint32 trip_distance;
	uint32 speed_sum;
	uint32 speed;
	struct gps_listener listener;
	struct display_node *display;
}REGION_SPEED_TYPE;

static REGION_SPEED_TYPE regionDat;

static uint8 gps_data_change(void *arg)
{
	GPSDATA_TYPE *data = (GPSDATA_TYPE *)arg;
	uint32 average = 0;

	if(data->glatitude == regionDat.last_lat && data->glongitude == regionDat.last_lon)
	{
		return 0;
	}

	regionDat.trip_distance += CalculateDistance(data->glatitude, data->glongitude, regionDat.last_lat, regionDat.last_lon, NULL);
	regionDat.speed_sum += data->gspeed;
	regionDat.point_cnt += 1;
	regionDat.last_lat = data->glatitude;
	regionDat.last_lon = data->glongitude;

	if(regionDat.trip_distance >= regionDat.distance)
	{
		//should unregister this listener
		LOGD("region end by distance");
		regionDat.listener.listener = NULL;
		RemoveDisplayByNode(&regionDat.display);
		return 1;
	}

	average = regionDat.speed_sum / regionDat.point_cnt;
	if(average > (regionDat.speed - 2))
	{
		uint32 diff = average - regionDat.speed + 2;
		uint8 cnt = 0;

		if(diff > 30)
		{
			cnt = 2;
		}
		else if(diff > 20)
		{
			cnt = 3;
		}
		else if(diff > 10)
		{
			cnt = 4;
		}
		else
		{
			cnt = 5;
		}

		if(regionDat.point_cnt % (15 * cnt) == 0)
		{
			VoicePlayCommon(speed_over_alarm_voice, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);
		}
		else
		{
			if(regionDat.point_cnt % cnt == 0)
			{
				VoicePlayCommon(ding_voice, TTS_PLAY_LEVEL_IGNORE, VOICE_DATA_FLAG_NULL);
			}
		}
	}

	return 0;
}

static uint8 rs_enter(ALARM_POINT_NODE *pPoint)
{
	DataBase_Record_Format *pRecord = pPoint->DBRecord;

	pPoint->status = ALARM_POINT_STATUS_CHECK_SPEED;

	VoicePlayPoint(pPoint, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);

	pPoint->extra = (void *)NotifyDisplayByPoint(pPoint);

	//regionDat.display = NotifyDisplayByPoint(pPoint);
	return 0;
}


static uint8 rs_leave(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	if(flag == 1)
	{
		RemoveDisplayByNode((struct display_node **)&(pPoint->extra));
		return 0;
	}

	DataBase_Record_Format *pRecord = pPoint->DBRecord;

	pPoint->status = ALARM_POINT_STATUS_EXIT;

	if(regionDat.listener.listener != NULL)
	{
		LOGD("region end by override");
		UnRegisterGpsdataListener(&regionDat.listener);
	}

	regionDat.last_lat = pRecord->ep_lat;
	regionDat.last_lon = pRecord->ep_lon;

	regionDat.speed = pRecord->speed_limit;
	regionDat.distance = pRecord->avg_spd_dist * 1000;
	regionDat.point_cnt = 0;
	regionDat.trip_distance = 0;
	regionDat.speed_sum = 0;
	regionDat.listener.listener = gps_data_change;
	RegisterGpsdataListener(&regionDat.listener);

	pPoint->cur2ep_dist = 0;
	RefreshDisplayByNode(pPoint, (struct display_node *)pPoint->extra);

	regionDat.display = (struct display_node *)pPoint->extra;

	return 0;
}

static void rs_timer(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	uint32 voice_alarm_range_start, voice_alarm_range_end;

	if(flag == 0)
	{
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

			pPoint->flag.flag1.cnt = 4;
			VoicePlayCommon(speed_over_alarm_voice, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_NULL);
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

	RefreshDisplayByNode(pPoint, (struct display_node *)pPoint->extra);
}

POINT_PROCESS_TYPE gRegionStartPoint =
{
	rs_enter,
	rs_leave,
	rs_timer,
	NULL
};

static uint8 re_enter(ALARM_POINT_NODE *pPoint)
{
	if(regionDat.display != NULL)
	{
		RemoveDisplayByNode(&regionDat.display);
	}

	rs_enter(pPoint);

	if(regionDat.listener.listener != NULL)
	{
		LOGD("region end by point");
		UnRegisterGpsdataListener(&regionDat.listener);
		regionDat.listener.listener = NULL;
	}

	return 0;
}

static uint8 re_leave(ALARM_POINT_NODE *pPoint, uint8 flag)
{
	RemoveDisplayByNode((struct display_node **)&pPoint->extra);

	if(flag == 1)
	{
		return 0;
	}

	pPoint->status = ALARM_POINT_STATUS_EXIT;
	pPoint->extra = 0;

	VoicePlayCommon(camera_voice, TTS_PLAY_LEVEL_MID, VOICE_DATA_FLAG_NULL);

	return 0;
}

POINT_PROCESS_TYPE gRegionEndPoint =
{
	re_enter,
	re_leave,
	rs_timer,
	NULL
};
