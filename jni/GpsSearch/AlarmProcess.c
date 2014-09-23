#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <string.h>
#include <pthread.h>

#include "common.h"
#include "config.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "DataBase.h"
#include "GpsData.h"
#include "PointSearch.h"
#include "ThirdVoiceTable.h"

#define __ALARMPROCESS_C__
#include "AlarmProcess.h"

#include "Display.h"
#include "thirdvoice.h"

static void RemoveAlarmPoint(ALARM_POINT_NODE *pPoint)
{
	list_del(&pPoint->list);
	RELEASE_ALARM_POINT(pPoint);
}

static void notifyMapShowPoint()
{
	uint8 buf[512];
	DataBase_Record_Format *pRecord;// = pPoint->DBRecord;
	ALARM_POINT_NODE *pPoint;
	uint cnt = 0;

	list_for_each_entry(pPoint, GetAlarmPointList(), list)
	{
		pRecord = pPoint->DBRecord;

		if(pPoint->cur2ep_dist > 500) {
			continue;
		}

		if(pPoint->status == ALARM_POINT_STATUS_INITIAL)
		{
			buf[cnt * 7 + 1] = 0;
		}
		else if(pPoint->status == ALARM_POINT_STATUS_CHECK_SPEED || pPoint->status == ALARM_POINT_STATUS_VOICE_SPEAK)
		{
			buf[cnt * 7 + 1] = 1;
		}
		else if(pPoint->status == ALARM_POINT_STATUS_EXIT)
		{
			buf[cnt * 7 + 1] = 2;
		}
		else if(pPoint->status == ALARM_POINT_STATUS_REMOVE)
		{
			buf[cnt * 7 + 1] = 3;
		}

		buf[cnt * 7 + 2] = pRecord->ep_lat >> 16;
		buf[cnt * 7 + 3] = pRecord->ep_lat >> 8;
		buf[cnt * 7 + 4] = pRecord->ep_lat;
		buf[cnt * 7 + 5] = pRecord->ep_lon >> 16;
		buf[cnt * 7 + 6] = pRecord->ep_lon >> 8;
		buf[cnt * 7 + 7] = pRecord->ep_lon;
		cnt++;

		if(cnt > 20) {
			break;
		}
	}
	buf[0] = cnt;

	NotifyUI(NOTIFY_UI_CODE_ALARM_POINT_STATUS, buf, cnt * 7 + 1);
}

static void RemoveNotInRangePoint(void)
{
	ALARM_POINT_NODE *pPoint;
	int32 lat_min, lon_min, lat_max, lon_max;
	struct list_head *pos;

	for(pos = GetAlarmPointList()->next; pos != GetAlarmPointList(); )
	{
		pPoint = list_entry(pos, ALARM_POINT_NODE, list);
		//LOGD("addr2 %08x, %08x", (uint32)pPoint, (uint32)(pPoint->DBRecord));
		pos = pos->next;

		if(pPoint->status == ALARM_POINT_STATUS_INITIAL)
		{
			pPoint->status = ALARM_POINT_STATUS_REMOVE;
			RemoveAlarmPoint(pPoint);
		}
	}
}

static void __inline TworPointGetRange(ALARM_POINT_NODE *pPoint, uint32 *sp2ep, int32 *sp2cur, int32 *cursor)
{
	if(pPoint->DBRecord->driver_mode == DRIVER_MODE_SELF_POINT)
	{
		*sp2ep = 200;
		*sp2cur = 100;
		*cursor = 30;
	}
	else
	{
		if(pPoint->DBRecord->speed_limit >= 90)
		{
			*sp2ep = 200;
			*sp2cur = 150;
			*cursor = 30;
		}
		else if(pPoint->DBRecord->speed_limit >= 60)
		{
			*sp2ep = 150;
			*sp2ep = 100;
			*cursor = 20;
		}
		else
		{
			*sp2ep = 100;
			*sp2cur = 50;
			*cursor = 10;
		}
	}
}

static uint8 ReCheckPoint(ALARM_POINT_NODE *pPoint)
{
	if(pPoint->status == ALARM_POINT_STATUS_INITIAL || pPoint->status == ALARM_POINT_STATUS_EXIT)
	{
		return 0;
	}

	if(CompareCursor(pPoint->DBRecord->sp_cursor, pPoint->sp2ep_cursor, 30))
	{
		if(CompareCursor(pPoint->DBRecord->sp_cursor, GET_CUR_GPS_POINT_CURSOR(), 80))
		{
			if((pPoint->sp2cur_dist + pPoint->cur2ep_dist) > (pPoint->sp2ep_dist + 150))
			{
				LOGD("ignore0(%d,%d,%d,%d) (%d,%d,%d), (%d,%d,%d), (%d,%d)", pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon, pPoint->DBRecord->sp_cursor, \
						pPoint->DBRecord->ep_cursor, GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), GET_CUR_GPS_POINT_CURSOR(), \
						pPoint->sp2cur_dist, pPoint->cur2ep_dist, pPoint->sp2ep_dist, pPoint->sp2cur_cursor, pPoint->cur2ep_cursor);
				if(pPoint->pProcess->leave != NULL)
				{
					pPoint->pProcess->leave(pPoint, 1);
				}
				pPoint->status = ALARM_POINT_STATUS_INITIAL;
			}
		}
		else
		{
			LOGD("ignore1(%d,%d,%d,%d) (%d,%d,%d), (%d,%d,%d)", pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon, pPoint->DBRecord->sp_cursor, \
					pPoint->DBRecord->ep_cursor, GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), GET_CUR_GPS_POINT_CURSOR(), \
					pPoint->sp2cur_dist, pPoint->cur2ep_dist, pPoint->sp2ep_dist);

			if(pPoint->pProcess->leave != NULL)
			{
				pPoint->pProcess->leave(pPoint, 1);
			}

			pPoint->status = ALARM_POINT_STATUS_INITIAL;
		}
	}
	else
	{
		if((pPoint->sp2cur_dist + pPoint->cur2ep_dist) > (pPoint->sp2ep_dist + 300))
		{
			LOGD("ignore2(%d,%d,%d,%d) (%d,%d,%d), (%d,%d,%d)", pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon, pPoint->DBRecord->sp_cursor, \
				pPoint->DBRecord->ep_cursor, GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), GET_CUR_GPS_POINT_CURSOR(), \
				pPoint->sp2cur_dist, pPoint->cur2ep_dist, pPoint->sp2ep_dist);

			if(pPoint->pProcess->leave != NULL)
			{
				pPoint->pProcess->leave(pPoint, 1);
			}

			pPoint->status = ALARM_POINT_STATUS_INITIAL;
		}
	}
	return 0;
}

static uint8 DoExitedPoint(ALARM_POINT_NODE *pPoint)
{
	uint32 cursor = pPoint->DBRecord->sp_cursor;
	uint32 range = 120;

	if(pPoint->status != ALARM_POINT_STATUS_EXIT)
	{
		return 0;
	}

	switch(pPoint->DBRecord->driver_mode)
	{
	case DRIVER_MODE_PICTURE:
	case DRIVER_MODE_SAFE:
	case DRIVER_MODE_SELF_POINT:
		break;

	case DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE:
		cursor = pPoint->DBRecord->ep_cursor;
		break;

	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE:
		break;
	}

	if(!CompareCursor(cursor, GET_CUR_GPS_POINT_CURSOR(), range))
	{
		LOGD("exit to init 1");
		pPoint->status = ALARM_POINT_STATUS_INITIAL;
	}
	else
	{
		if((pPoint->sp2cur_dist + pPoint->cur2ep_dist) > (pPoint->sp2ep_dist + 100))
		{
			LOGD("exit to init 2");
			pPoint->status = ALARM_POINT_STATUS_INITIAL;
		}
	}

	return 0;
}

static uint8 TwoPointCheckAlarm(ALARM_POINT_NODE *pPoint)
{
	DataBase_Record_Format *pRecord = pPoint->DBRecord;
	uint32 sp2ep_dist_range, sp2cur_dist_range;
	int32 cursor_range;

	pPoint->sp2cur_dist = CalculateDistance(pRecord->sp_lat, pRecord->sp_lon, GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), &pPoint->sp2cur_cursor);
	pPoint->cur2ep_dist = CalculateDistance(GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), pRecord->ep_lat, pRecord->ep_lon, &pPoint->cur2ep_cursor);
	//LOGD("(%d,%d)===>%d,%d", pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon, (uint32)pPoint->sp2cur_cursor, (uint32)pPoint->cur2ep_cursor);
	TworPointGetRange(pPoint, &sp2ep_dist_range, &sp2cur_dist_range, &cursor_range);
	do
	{
		if(pPoint->status != ALARM_POINT_STATUS_INITIAL)
		{
			break;
		}

		pPoint->sp2ep_dist = CalculateDistance(pRecord->sp_lat, pRecord->sp_lon, pRecord->ep_lat, pRecord->ep_lon, &pPoint->sp2ep_cursor);

		if((pPoint->sp2cur_dist + pPoint->cur2ep_dist) > (pPoint->sp2ep_dist + sp2ep_dist_range))
		{
			break;
		}

		//LOGD("sp2cur=%d,cur2ep=%d,sp2ep=%d,sp2ep_c=%d,sp2cur_c=%d,cur2ep_c=%d (%d,%d,%d,%d,%d,%d,%d,%d)", \
				pPoint->sp2cur_dist, pPoint->cur2ep_dist, pPoint->sp2ep_dist, pPoint->sp2ep_cursor, pPoint->sp2cur_cursor, pPoint->cur2ep_cursor, \
				pPoint->DBRecord->driver_mode, pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon, pPoint->DBRecord->sp_cursor, pPoint->DBRecord->ep_cursor, \
				pPoint->DBRecord->sp_lat, pPoint->DBRecord->sp_lon, GET_CUR_GPS_POINT_CURSOR());

		if(pPoint->cur2ep_dist >= pPoint->sp2ep_dist)
		{
			if(CompareCursor(pRecord->sp_cursor, GET_CUR_GPS_POINT_CURSOR(), 15) && (pPoint->sp2cur_dist <= sp2cur_dist_range))
			{
				LOGD("cur2ep_dist >= sp2ep_dist && sp_cursor - cur <= 15 && sp2cur_dist <= %d", sp2cur_dist_range);
				pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
			}
		}
		else
		{
			if(!CompareCursor(pPoint->cur2ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 30))
			{
				break;
			}

			if(CompareCursor(pRecord->sp_cursor, pPoint->sp2ep_cursor, cursor_range))
			{
				if(CompareCursor(pRecord->sp_cursor, pRecord->ep_cursor, 10))
				{
					if(CompareCursor(pRecord->sp_cursor, GET_CUR_GPS_POINT_CURSOR(), 15)
							&& CompareCursor(pRecord->sp_cursor, pPoint->sp2cur_cursor, 10))
					{
						LOGD("sp_cursor - sp2ep_cursor < %d && sp_cursor - ep_cursor < 10 && sp_cursor - cur < 15 && sp_cursor - sp2cur_cursor < 10", cursor_range);
						pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
					}
				}
				else
				{
					if(CompareCursor(pRecord->sp_cursor, GET_CUR_GPS_POINT_CURSOR(), 10))
					{
						LOGD("sp_cursor - sp2ep_cursor < %d && sp_cursor - ep_cursor > 10 && sp_cursor - cur < 10", cursor_range);
						pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
					}
				}
			}
			else
			{
				uint32 distance;
				uint16 cursor;

				distance = (pRecord->speed_limit < 90) ? 200 : 400;
				cursor = (pRecord->speed_limit < 90) ? 10 : 15;

				if(pPoint->cur2ep_dist <= distance)
				{
					if(CompareCursor(pRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 10)
							&& CompareCursor(pPoint->cur2ep_cursor, pRecord->ep_cursor, cursor))
					{
						LOGD("sp_cursor - sp2ep_cursor > %d && cur2ep_dist < %d && ep_cursor - cur < 10", cursor_range, distance);
						pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
					}
				}
			}
		}
	} while(0);

	return 0;
}

static void __inline SinglePoingGetRange(ALARM_POINT_NODE *pPoint, uint32 *distance, int32 *cursor)
{
	if(pPoint->DBRecord->speed_limit >= 90)
	{
		*distance = 1000;
		*cursor = 15;
	}
	else if(pPoint->DBRecord->speed_limit >= 50)
	{
		*distance = 500;
		*cursor = 10;
	}
	else
	{
		*distance = 300;
		*cursor = 10;
	}
}

static uint8 SinglePointNoCursorCheckAlarm(ALARM_POINT_NODE *pPoint)
{
	DataBase_Record_Format *pRecord = pPoint->DBRecord;
	uint32 dist_range;
	int32 cursor_range;
	int32 cur2ep_cursor;
	uint32 cur2ep_distance;

	pPoint->cur2ep_dist = CalculateDistance(GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), pRecord->ep_lat, pRecord->sp_lon, &cur2ep_cursor);

	SinglePoingGetRange(pPoint, &dist_range, &cursor_range);

	do
	{
		if(pPoint->status != ALARM_POINT_STATUS_INITIAL)
		{
			break;
		}

		if(pPoint->cur2ep_dist > dist_range)
		{
			break;
		}

		if(CompareCursor(GET_CUR_GPS_POINT_CURSOR(), cur2ep_cursor, cursor_range))
		{
			pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
			pRecord->sp_lat = GET_CUR_GPS_POINT_LATITUDE();
			pRecord->sp_lon = GET_CUR_GPS_POINT_LONGITUDE();
			pRecord->sp2ep_dist = dist_range;
			pRecord->sp_cursor = cur2ep_cursor;
		}
	} while (0);

	return 0;
}

static uint8 SinglePointCursorCheckAlarm(ALARM_POINT_NODE *pPoint)
{
	DataBase_Record_Format *pRecord = pPoint->DBRecord;
	uint32 dist_range;
	int32 cursor_range;
	int32 cur2ep_cursor;
	uint32 cur2ep_distance;

	pPoint->cur2ep_dist = CalculateDistance(GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(), pRecord->ep_lat, pRecord->sp_lon, &cur2ep_cursor);

	SinglePoingGetRange(pPoint, &dist_range, &cursor_range);

	do
	{
		if(pPoint->status != ALARM_POINT_STATUS_INITIAL)
		{
			break;
		}

		if(pPoint->cur2ep_dist > dist_range)
		{
			break;
		}

		if(!CompareCursor(pRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 30))
		{
			break;
		}

		if(CompareCursor(pRecord->ep_cursor, cur2ep_cursor, cursor_range))
		{
			if(CompareCursor(pRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 15))
			{
				pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
				pRecord->sp_lat = GET_CUR_GPS_POINT_LATITUDE();
				pRecord->sp_lon = GET_CUR_GPS_POINT_LONGITUDE();
				pRecord->sp2ep_dist = dist_range;
			}
		}
		else
		{
			if(pPoint->cur2ep_dist <= 200)
			{
				if(CompareCursor(pRecord->ep_cursor, cur2ep_cursor, 10)
						&& CompareCursor(pRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 10))
				{
					pPoint->status = ALARM_POINT_STATUS_VOICE_SPEAK;
					pRecord->sp_lat = GET_CUR_GPS_POINT_LATITUDE();
					pRecord->sp_lon = GET_CUR_GPS_POINT_LONGITUDE();
					pRecord->sp2ep_dist = dist_range;
				}
			}
		}
	} while (0);

	return 0;
}

static uint8 CheckPointAlarmInRange(ALARM_POINT_NODE *pPoint)
{
	switch(pPoint->DBRecord->driver_mode)
	{
	case DRIVER_MODE_PICTURE:
	case DRIVER_MODE_SAFE:
	case DRIVER_MODE_SELF_POINT:
		return TwoPointCheckAlarm(pPoint);

	case DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE:
		return SinglePointCursorCheckAlarm(pPoint);

	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE:
		return SinglePointNoCursorCheckAlarm(pPoint);
	}

	LOGD("this point has not reconized driver mode %d", pPoint->DBRecord->driver_mode);
	return 0;
}

static ALARM_POINT_NODE *GetNearestNeedPlayPoint(uint8 state)
{
	ALARM_POINT_NODE *pPoint;
	ALARM_POINT_NODE *pNearestPoint = NULL;

	list_for_each_entry(pPoint, GetAlarmPointList(), list)
	{
		if(pPoint->status == state)
		{
			if(pNearestPoint == NULL)
			{
				pNearestPoint = pPoint;
			}
			else
			{
				if(pPoint->cur2ep_dist < pNearestPoint->cur2ep_dist)
				{
					pNearestPoint = pPoint;
				}
			}
		}
	}

	return pNearestPoint;
}

static ALARM_POINT_NODE *GetNearestLeavePoint(void)
{
	ALARM_POINT_NODE *pPoint;
	ALARM_POINT_NODE *pNearestPoint = NULL;

	list_for_each_entry(pPoint, GetAlarmPointList(), list)
	{
		if(pPoint->status == ALARM_POINT_STATUS_CHECK_SPEED)
		{
			if(pNearestPoint == NULL)
			{
				pNearestPoint = pPoint;
			}
			else
			{
				if(pPoint->cur2ep_dist < pNearestPoint->cur2ep_dist)
				{
					pNearestPoint = pPoint;
				}
			}
		}
	}

	return pNearestPoint;
}

static void CheckLeaveAlarmPoint(ALARM_POINT_NODE *pPoint)
{
	uint32 ReCalDisBySpeed = 0;
	uint32 voice_index = 0;
	if(pPoint->cur2ep_dist > 150)
	{
		return;
	}

	ReCalDisBySpeed = (GetCurSpeed() * 1000 / 60 / 60);

	if(pPoint->DBRecord->driver_mode == DRIVER_MODE_SELF_POINT)
	{
		ReCalDisBySpeed += 10;
	}
	else
	{
		ReCalDisBySpeed += 20;
	}

	//LOGD("re=%d, sp2cur=%d, sp2ep=%d, cur2ep=%d, ep_c=%d, c=%d", ReCalDisBySpeed, pPoint->sp2cur_dist, pPoint->sp2ep_dist, pPoint->cur2ep_dist, \
			pPoint->DBRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR());

	if((pPoint->sp2cur_dist + ReCalDisBySpeed) >= pPoint->sp2ep_dist || (pPoint->cur2ep_dist <= ReCalDisBySpeed))
	{
		if(pPoint->DBRecord->driver_mode != DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE
			&& !CompareCursor(pPoint->DBRecord->ep_cursor, GET_CUR_GPS_POINT_CURSOR(), 30))
		{
			return;
		}

		if(pPoint->pProcess->leave != NULL)
		{
			pPoint->pProcess->leave(pPoint, 0);
		}
	}
}

uint8 AlarmProcess(void)
{
	ALARM_POINT_NODE *pPoint, *pTmpPoint;
	uint8 pt_cnt = 0;

	list_for_each_entry(pPoint, GetAlarmPointList(), list)
	{
		CheckPointAlarmInRange(pPoint);
		DoExitedPoint(pPoint);
		ReCheckPoint(pPoint);
	}

	pPoint = GetNearestNeedPlayPoint(ALARM_POINT_STATUS_VOICE_SPEAK);
	if(pPoint != NULL)
	{
		//LOGD("do point(mode=%d, voice=%x, speed=%d, page=%d, %d, %d)", pPoint->DBRecord->driver_mode, pPoint->DBRecord->voice_index, \
				pPoint->DBRecord->speed_limit, pPoint->DBRecord->voice_page, pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon);

		if(pPoint->pProcess->enter != NULL)
		{
			pPoint->pProcess->enter(pPoint);
		}
	}

	pTmpPoint = GetNearestLeavePoint();
	list_for_each_entry(pPoint, GetAlarmPointList(), list)
	{
		if(pPoint->status != ALARM_POINT_STATUS_CHECK_SPEED)
		{
			continue;
		}

		if(pPoint->pProcess->timer != NULL)
		{
			if(pTmpPoint == pPoint)
			{
				pPoint->pProcess->timer(pPoint, 1);
			}
			else
			{
				pPoint->pProcess->timer(pPoint, 0);
			}
		}

		CheckLeaveAlarmPoint(pPoint);
	}

	notifyMapShowPoint();
	RemoveNotInRangePoint();
	DisplayProcess();

	return 0;
}
