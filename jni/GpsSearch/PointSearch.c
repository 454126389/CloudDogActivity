#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <string.h>
#include <pthread.h>

#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "DataBase.h"
#include "GpsData.h"
#include "AlarmProcess.h"
#define __SEARCHPOINT_C__

#include "PointSearch.h"



int32 SearchPointInit(void)
{
    INIT_LIST_HEAD(&AlarmPointListHead);
    return 0;
}

int32 SearchPointDeInit(void)
{
    ALARM_POINT_NODE *pPoint;
    struct list_head *pos;

    for(pos = GetAlarmPointList()->next; pos != GetAlarmPointList(); )
	{
		pPoint = list_entry(pos, ALARM_POINT_NODE, list);
		pos = pos->next;
		list_del(&pPoint->list);
		RELEASE_ALARM_POINT(pPoint);
	}

    return 0;
}

uint8 IsValidAlarmPoint(DataBase_Record_Format *pRecord)
{
    uint8 IsStartCursorInRange, IsEndCursorInRange;
    int32 Cur2EndCursor;
    
    switch( DB_RECORD_DRIVER_MODE(pRecord) )
    {
        case DRIVER_MODE_PICTURE:
        case DRIVER_MODE_SAFE:
        case DRIVER_MODE_SELF_POINT:
            IsStartCursorInRange = CompareCursor(GET_CUR_GPS_POINT_CURSOR(), DB_RECORD_START_POINT_CURSOR(pRecord), 60);
            IsEndCursorInRange = CompareCursor(GET_CUR_GPS_POINT_CURSOR(), DB_RECORD_END_POINT_CURSOR(pRecord), 60);
            return (IsStartCursorInRange || IsEndCursorInRange);

        case DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE:
        case DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE:
            IsEndCursorInRange = CompareCursor(GET_CUR_GPS_POINT_CURSOR(), DB_RECORD_END_POINT_CURSOR(pRecord), 60);
            return IsEndCursorInRange;

        case DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE:
        case DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE:
            Cur2EndCursor = CalculateCursor(GET_CUR_GPS_POINT_LATITUDE(), GET_CUR_GPS_POINT_LONGITUDE(),
                                            DB_RECORD_END_POINT_LATITUDE(pRecord), DB_RECORD_END_POINT_LONGITUDE(pRecord));
            IsEndCursorInRange = CompareCursor(Cur2EndCursor, DB_RECORD_END_POINT_CURSOR(pRecord), 60);
            return IsEndCursorInRange;
            
    }

    return FALSE;
}

uint8 CheckSameAlarmPoint(DataBase_Record_Format *pRecord)
{
	int i;
#if 0
	for(i = 0; i < proc_db_range_numb; i++)
	{
		if(DB_RECORD_END_POINT_LATITUDE(pRecord) == data_addr_tmp[i].ep_lat
			&& DB_RECORD_END_POINT_LONGITUDE(pRecord) == data_addr_tmp[i].ep_lon
			&& pRecord->ep_cursor == data_addr_tmp[i].ep_cursor
			&& pRecord->sp_cursor == data_addr_tmp[i].sp_cursor)
		{
			return TRUE;
		}
	}
#else
	ALARM_POINT_NODE *pPoint;
	DataBase_Record_Format *p;
	list_for_each_entry(pPoint, &AlarmPointListHead, list)
	{
		p = pPoint->DBRecord;
		if(pRecord->ep_lat == p->ep_lat && pRecord->ep_lon == p->ep_lon
				&& pRecord->ep_cursor == p->ep_cursor && pRecord->sp_cursor == p->sp_cursor)
		{
			return TRUE;
		}
	}
#endif
	return FALSE;
}

void AddAlarmRecord(DataBase_Record_Format *pRecord)
{
    ALARM_POINT_NODE *pPoint;
    int flag = 0;

    //outsideVar.mp_numb = 0;
    if(pRecord != NULL)
    {
        if(IsValidAlarmPoint(pRecord))
        {
        	if(CheckSameAlarmPoint(pRecord) == FALSE)
        	{
				//LOGD("ADD: %d %d %d at %s", pRecord->ep_lat, pRecord->ep_lon, pRecord->addrInFile, ((PDATABASE)pRecord->pDatabase)->pFileName);
				pPoint = (ALARM_POINT_NODE *)malloc(sizeof(ALARM_POINT_NODE));
				memset(pPoint, 0, sizeof(ALARM_POINT_NODE));
				pPoint->DBRecord = pRecord;
				list_add_tail(&pPoint->list, &AlarmPointListHead);

				if(pRecord->driver_mode == DRIVER_MODE_SAFE || pRecord->driver_mode == DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE \
						|| pRecord->driver_mode == DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE)
				{
					pPoint->pProcess = &gSafePointProcess;
				}
				else if(pRecord->driver_mode == DRIVER_MODE_SELF_POINT)
				{
					pPoint->pProcess = &gSelfPoint;
				}
				else
				{
					if(pRecord->voice_index >= 0xD8 && pRecord->voice_index <= 0xDF)
					{
						if(pRecord->voice_index % 2)
						{
							pPoint->pProcess = &gRegionEndPoint;
						}
						else
						{
							pPoint->pProcess = &gRegionStartPoint;
						}
					}
					else if((pRecord->voice_index >= 0xB3 && pRecord->voice_index < 0xB8)
							|| (pRecord->voice_index >= 0xBB && pRecord->voice_index < 0xC0))
					{
						pPoint->pProcess = &gRadarPoint;
					}
					else
					{
						pPoint->pProcess = &gCommonPoint;
					}
				}

				return;
        	}
        }

        free(pRecord);
    }
}

int32 SearchPoint(int32 lat, int32 lon)
{
    int32 ret = SEARCH_SUCCESS;
    DataBaseType *pDB;
    //struct timeval    time_start, time_end;
    //struct timezone     tz;
    //int32 dtime;
    
    //gettimeofday(&time_start, &tz);
    pthread_mutex_lock(&DataBaseMutex);
    list_for_each_entry(pDB, &DataBaseList, db_head)
    {
        if(pDB->Search != NULL)
        {
            if(pthread_mutex_trylock(&pDB->mutex) == EBUSY)
            {
            	LOGD("%s, try to lock, return busy", pDB->pFileName);
            	continue;
            }

            //LOGD("start search %s(%d, %d)", pDB->pFileName, lat, lon);
            ret = pDB->Search(pDB, lat, lon, AddAlarmRecord);
            //LOGD("search finish");

            pthread_mutex_unlock(&pDB->mutex);
        }
    }
    pthread_mutex_unlock(&DataBaseMutex);
    //gettimeofday(&time_end, &tz);
	//dtime = (time_end.tv_sec - time_start.tv_sec) * 1000000 + time_end.tv_usec - time_start.tv_usec;
	//LOGD("Search lat(%d), lon(%d), time %dus", lat, lon, dtime);

    return ret;
}
