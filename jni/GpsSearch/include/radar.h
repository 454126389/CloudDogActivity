#ifndef __RADAR_H__
#define __RADAR_H__

#ifdef __RADAR_C__
#define __RADAR_DEC__
#else
#define __RADAR_DEC__ extern
#endif

#define RD_CODE_HEADER0 							0xD3
#define RD_CODE_HEADER1 							0x4D
#define RD_CODE_HEADER2 							0x34
#define RD_CODE_BAND 								0x9A

#define RD_CODE_HEART0  							0x69
#define RD_CODE_HEART1   							0xA6

#define RD_CODE_WEAK  								0x36
#define RD_CODE_MIDDLE  							0x34
#define RD_CODE_STRONG  							0x24

#define RD_CODE_RASER  								0xD2
#define RD_CODE_RASER_HEADER0  						0x49
#define RD_CODE_RASER_HEADER1  						0x24

#define RD_CODE_OFF									0xFF

typedef enum
{
	RADAR_X_BAND,
	RADAR_KU_BAND,
	RADAR_K_BAND,
	RADAR_KA_BAND,
	RADAR_LASER,
	RADAR_NULL
}RADAR_TYPE;

typedef enum
{
	RADAR_LEVEL_HIGH						= 1,
	RADAR_LEVEL_MIDDLE,
	RADAR_LEVEL_LOW,

	RADAR_STOP								= 0xFF
}RADAR_LEVEL_TYPE;

#define RADAR_ALARM_BY_HARDWARE						0
#define RADAR_ALARM_BY_SIMULATE						1

__RADAR_DEC__ void radar_init(void);
__RADAR_DEC__ void radar_deinit(void);

#endif
