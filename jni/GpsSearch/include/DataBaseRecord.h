#ifndef __DATABASERECORD_H__
#define __DATABASERECORD_H__

#ifdef __DATABASERECORD_C__
#define __DATABASERECORD_DEC__
#else
#define __DATABASERECORD_DEC__ extern
#endif


#define picture_mode        				0		//camera mode, speed limited
#define safe_mode           				1		//saft mode. by school, extra or petrol
#define self_point_mode     				3       //save the point by the use self.
#define onepc_picture_mode  				4	//has curse
#define onepc_safe_mode    	 				5

#define onepnc_picture_mode 				8	//no curse
#define onepnc_safe_mode    				9

#define SIZE_PER_RECORD                     (16)

#define RECORD_SUCCESS                      (0)
#define RECORD_ERR_PARAMETER                (-1)
#define RECORD_ERR_EXCEED_RANGE             (-2)

typedef struct
{
    uint8 driver_mode;                      // driver mode
    uint8 speed_limit;                      // the speed limited
    uint8 voice_index;                      // (voice index)
    int16 ep_cursor;                        // (end point cursor)
    int16 sp_cursor;                        // (start point cursor)
    uint8 voice_page;
    uint8 sp2ep_cursor;                     // (start to end point way cursor)
    int32 ep_lat;                           // (end point lat)
    int32 ep_lon;                           // (end point lon)
    int32 sp_lat;                           // (start point latitude)
    int32 sp_lon;                           // (start point longitude)
    int32 sp_ep_lat;                        // (start point lat-end point lat)
    int32 sp_ep_lon;                        // (start point lon-end point lon)
    int32 cur2ep_dist;                      // (your point to end point distance)
    int32 sp2cur_dist;                      // (start point to your point distance)
    int32 sp2ep_dist;                       // (start to end point distance)
    //uint8 sep_cursor;                     // (start end point cursor)
    uint8 avg_spd_dist;                     // 
    int32 addrInFile;                       // address in the database file
    uint8 *ori_data;						// 16byte origin data
    void *pDatabase;
}DataBase_Record_Format;

#define LATITUDE_FROM_BUF(buf)              ((buf[5] << 16) | (buf[6] << 8) | buf[7])
#define LONGITUDE_FROM_BUF(buf)             ((buf[9] << 16) | (buf[10] << 8) | buf[11])

#define IS_IN_RANGE(val, range_min, range_max) (val >= range_min && val <= range_max)

#define DB_RECORD_DRIVER_MODE(pPoint)               ((pPoint)->driver_mode)//(((pPoint)->driver_mode & 0xF0) >> 4)
#define DB_RECORD_END_POINT_CURSOR(pPoint)          ((pPoint)->ep_cursor)//((pPoint)->ep_cursor * 2)
#define DB_RECORD_START_POINT_CURSOR(pPoint)        ((pPoint)->sp_cursor)//((pPoint)->sp_cursor * 2)
#define DB_RECORD_END_POINT_LATITUDE(pPoint)        ((pPoint)->ep_lat)
#define DB_RECORD_END_POINT_LONGITUDE(pPoint)       ((pPoint)->ep_lon)

typedef void (*SubmitRecord)(DataBase_Record_Format *pRecord);

#endif

