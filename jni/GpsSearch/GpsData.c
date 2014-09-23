#include <stdio.h>
#include <math.h>
#include "common.h"
#include "List.h"
#define __GPSDATA_C__
#include "GpsData.h"

#define __GPSDATLISTENER_C__
#include "GpsdataListener.h"

//#define PI                                                  (31415926)

static GPSDATA_TYPE gCurGpsPoint;

void GpsDataInit(void)
{
    pCurGpsPoint = &gCurGpsPoint;
    INIT_LIST_HEAD(&gps_listener_list);
}

void GpsDataDeInit(void)
{
	struct list_head *pos;
	struct gps_listener *listener;

	for(pos = gps_listener_list.next; pos != &gps_listener_list; )
	{
		listener = list_entry(pos, struct gps_listener, node);
		pos = pos->next;
		UnRegisterGpsdataListener(listener);
	}
}

uint8 IsNight(void)
{
	return (pCurGpsPoint->hour >= 18 && pCurGpsPoint->hour <= 6);
}

uint8 CompareCursor(int32 SrcCursor, int32 DesCursor, int32 CursorRange)
{
    int32 diff = (SrcCursor > DesCursor) ? (SrcCursor - DesCursor) : (DesCursor - SrcCursor);

    if(diff >= 180)
    {
        return (CursorRange >= (360 - diff));
    }
    else
    {
        return (diff <= CursorRange);
    }
}


//return unit is m
double CalculateDistance(int32 FromLatitude, int32 FromLongitude, int32 ToLatitude, int32 ToLongitude, int32 *cursor)
{
    double fromlat, fromlon, tolat, tolon;
    double dist_lat, dist_lon;
    double dist;
    double tmp;

    fromlat = (double)(FromLatitude * M_PI) / 10000 / 180;
    fromlon = (double)(FromLongitude * M_PI) / 10000 / 180;
    tolat = (double)(ToLatitude * M_PI) / 10000 / 180;
    tolon = (double)(ToLongitude * M_PI) / 10000 / 180;

    fromlon = 0 - fromlon;
    tolon = 0 - tolon;

    if( ( fromlat + tolat == 0 ) && ( fabs(fromlon - tolon) == M_PI ))
    {
        return -1;
    }

    dist_lat = sin( (fromlat - tolat) / 2 );
    dist_lon = sin( (fromlon - tolon) / 2 );

    dist = asin( sqrt( (dist_lat * dist_lat) + cos(fromlat) * cos(tolat) * dist_lon * dist_lon ) );
    dist = 6378137 * dist * 2;

    if(cursor != NULL)
    {
    	/*
		 *  纬度一分约等于1852米
		 *  计算两点相差的纬度，单位为弧度
		 */
    	tmp = ( dist * M_PI ) / 180 / 60 / 1852;

		if( sin(tmp) * cos(fromlat) == 0)
		{
			return 0;
		}
		else
		{
			tmp = ( sin(tolat) - sin(fromlat) * cos(tmp) ) / ( sin(tmp) * cos(fromlat) );

			tmp = (tmp > 1) ? 1 : ((tmp < -1) ? -1 : tmp);

			if( tolon < fromlon )
			{
				tmp = (acos(tmp) * 180 / M_PI);
			}
			else
			{
				tmp = ((2 * M_PI - acos(tmp)) * 180 / M_PI);
			}
		}

		*cursor = tmp;
    }

    return dist;
}

//return unit is degree
double CalculateCursor(int32 FromLatitude, int32 FromLongitude, int32 ToLatitude, int32 ToLongitude)
{
    double fromlat, fromlon, tolat, tolon;
    double dist_lat, dist_lon;
    double dist;
    double cursor;

    fromlat = (double)(FromLatitude * M_PI) / 10000 / 180;
    fromlon = (double)(FromLongitude * M_PI) / 10000 / 180;
    tolat = (double)(ToLatitude * M_PI) / 10000 / 180;
    tolon = (double)(ToLongitude * M_PI) / 10000 / 180;

    dist = CalculateDistance(FromLatitude, FromLongitude, ToLatitude, ToLongitude, (int32 *)&cursor);

    return cursor;
}
