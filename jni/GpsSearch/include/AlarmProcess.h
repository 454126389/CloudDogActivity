#ifndef __ALARMPROCESS_H__
#define __ALARMPROCESS_H__

#ifdef __ALARMPROCESS_C__
#define __ALARMPROCESS_DEC__
#else
#define __ALARMPROCESS_DEC__ extern
#endif

typedef struct _ALARM_POINT_NODE
{
	struct list_head list;
    DataBase_Record_Format *DBRecord;
    int32 cur2ep_dist;
    int32 sp2cur_dist;
    int32 sp2ep_dist;
    int32 sp2ep_cursor;
    int32 sp2cur_cursor;
    int32 cur2ep_cursor;
    uint32 status;
    POINT_PROCESS_TYPE *pProcess;
    void *extra;
    union {
    	uint32 flag0;
    	struct {
    		uint16 flag;
    		uint16 cnt;
    	}flag1;
    }flag;
}ALARM_POINT_NODE;

#define ALARM_POINT_STATUS_INITIAL							(0)
#define ALARM_POINT_STATUS_VOICE_SPEAK						(1)
#define ALARM_POINT_STATUS_CHECK_SPEED						(2)
#define ALARM_POINT_STATUS_EXIT								(4)
#define ALARM_POINT_STATUS_REMOVE							(5)

#define ALARM_POINT_EXTRA_FLAG_SPEED_OVER_ALARM				(0x00000001)

__ALARMPROCESS_DEC__ uint8 AlarmProcess(void);
#endif
