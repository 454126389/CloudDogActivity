#ifndef __SEARCHPOINT_H__
#define __SEARCHPOINT_H__

#ifdef __SEARCHPOINT_C__
#define __SEARCHPOINT_DEC__
#else
#define __SEARCHPOINT_DEC__ extern
#endif

#define SEARCH_DOWN                                 (0)
#define SEARCH_UP                                   (1)

#define DEFAULT_SEARCH_RANGE                        (11)

#define SEARCH_SUCCESS                              (0)
#define SEARCH_ERR_NO_DATABASE                      (-1)
#define SEARCH_ERR_RECORD_READ_FAIL                 (-2)
#define SEARCH_ERR_RECORD_PARSE_FAIL                (-3)
#define SEARCH_ERR_REACH_HEAD                       (-4)
#define SEARCH_ERR_REACH_TAIL                       (-5)
#define SEARCH_ERR_NOT_FOUND                        (-6)

#define DRIVER_MODE_PICTURE                         (0)                 // camera mode, speed limited
#define DRIVER_MODE_SAFE                            (1)                 // saft mode. by school, extra or petrol
#define DRIVER_MODE_SELF_POINT                      (3)                 // save the point by the use self.
#define DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE     (4)                 // single point has cursor camera mode
#define DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE        (5)                 // single point has cursor safe mode
#define DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE   (8)                 // single point no cursor camera mode
#define DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE      (9)                 // single point no cursor safe mode


#define ALARM_POINT_STATUS_INVALID                  (-1)
#define ALARM_POINT_STATUS_NEED_CHECK               (0)
#define ALARM_POINT_STATUS_ACTIVE                   (1)
#define ALARM_POINT_STATUS_DONE                     (2)

#define RELEASE_ALARM_POINT(pPoint)         \
    {                                       \
        free(pPoint->DBRecord);             \
        free(pPoint);                       \
    }

__SEARCHPOINT_DEC__ struct list_head AlarmPointListHead;

static struct list_head __inline *GetAlarmPointList(void)
{
	return (&AlarmPointListHead);
}

__SEARCHPOINT_DEC__ int32 SearchPoint(int32 lat, int32 lon);
__SEARCHPOINT_DEC__ int32 SearchPointInit(void);
__SEARCHPOINT_DEC__ int32 SearchPointDeInit(void);

#endif

