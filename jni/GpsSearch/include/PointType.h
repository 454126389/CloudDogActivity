#ifndef __POINT_TYPE_H__
#define __POINT_TYPE_H__

#ifdef __POINT_TYPE_C__
#define __POINT_TYPE_DEC__
#else
#define __POINT_TYPE_DEC__ extern
#endif

struct _ALARM_POINT_NODE;

typedef struct {
	uint8 (*enter)(struct _ALARM_POINT_NODE *pPoint);
	// flag 0: normal leave, 1: alnormal leave
	uint8 (*leave)(struct _ALARM_POINT_NODE *pPoint, uint8 flag);
	//1s timer, flag 0: normal, 1: nearest point
	void (*timer)(struct _ALARM_POINT_NODE *pPoint, uint8 flag);
	void *extra;
}POINT_PROCESS_TYPE;

extern POINT_PROCESS_TYPE gCommonPoint;
extern POINT_PROCESS_TYPE gSafePointProcess;
extern POINT_PROCESS_TYPE gRegionStartPoint;
extern POINT_PROCESS_TYPE gRegionEndPoint;
extern POINT_PROCESS_TYPE gRadarPoint;
extern POINT_PROCESS_TYPE gSelfPoint;
#endif
