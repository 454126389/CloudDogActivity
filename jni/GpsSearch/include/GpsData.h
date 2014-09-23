#ifndef __GPSDATA_h__
#define __GPSDATA_h__

#ifdef __GPSDATA_C__
#define __GPSDATA_DEC__
#else
#define __GPSDATA_DEC__ extern
#endif

typedef struct 
{
    int32 year;
	uint8 month;
	uint8 day;
    uint8 hour;
	uint8 minute;
	uint8 second;
	int32 glatitude;
	int32 glongitude;
	int32 gspeed;		                //speed
	int32 gcursor;	                    //cursor
}GPSDATA_TYPE;

typedef struct
{
	int32 latitude;
	int32 longitude;
}GPS_SIMPLE_POINT;

__GPSDATA_DEC__ GPSDATA_TYPE *pCurGpsPoint;

__GPSDATA_DEC__ void GpsDataInit(void);
__GPSDATA_DEC__ void GpsDataDeInit(void);
__GPSDATA_DEC__ uint8 CompareCursor(int32 SrcCursor, int32 DesCursor, int32 CursorRange);
__GPSDATA_DEC__ double CalculateDistance(int32 FromLatitude, int32 FromLongitude, int32 ToLatitude, int32 ToLongitude, int32 *cursor);
__GPSDATA_DEC__ double CalculateCursor(int32 FromLatitude, int32 FromLongitude, int32 ToLatitude, int32 ToLongitude);
__GPSDATA_DEC__ uint8 IsNight(void);

#define GET_CUR_GPS_POINT_CURSOR()                      (pCurGpsPoint->gcursor)
#define GET_CUR_GPS_POINT_LATITUDE()                    (pCurGpsPoint->glatitude)
#define GET_CUR_GPS_POINT_LONGITUDE()                   (pCurGpsPoint->glongitude)
#define GetCurSpeed()									(pCurGpsPoint->gspeed)
#endif
