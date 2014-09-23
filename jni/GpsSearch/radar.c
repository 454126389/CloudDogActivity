#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "config.h"
#include "common.h"
#include "List.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "thirdvoicetable.h"
#include "AlarmProcess.h"
#include "user_setting.h"
#include "thirdvoice.h"
#include "Mcu.h"

#define __RADAR_C__
#include "radar.h"

static uint8 radar_sensitivity[][RADAR_NULL] =
{
	//X		KU		K		KA		Laser
	{1, 	1, 		1, 		1, 		1},					// super
	{2, 	2, 		2, 		2, 		2},					// high
	{3, 	3, 		3, 		3, 		3},					// middle
	{4, 	4, 		4, 		4, 		4},					// low
};

typedef struct
{
	int8 last_radar_type;
	int cnt;
}RADAR_INFO;

static RADAR_INFO radar_info;

static int GetCurRadarTypeMaxCnt(int type, int sensitivity)
{
	return radar_sensitivity[sensitivity][type];
}

static int GetSensitivity(void)
{
	return user_setting.rd_sensitivity;
}

static int GetCurRadarIsEnable(int cur_type)
{
	int ret = 0;

	//for test
	return 1;

	switch(cur_type)
	{
	case RADAR_X_BAND:
		ret = user_setting.x_band_set_control;
		break;

	case RADAR_KU_BAND:
		ret = user_setting.ku_band_set_control;
		break;

	case RADAR_K_BAND:
		ret = user_setting.k_band_set_control;
		break;

	case RADAR_KA_BAND:
		ret = user_setting.ka_band_set_control;
		break;

	case RADAR_LASER:
		ret = user_setting.laser_set_control;
		break;
	}

	return ret;
}

static void radar_process(MCU_PROTOCAL_INFO *protocal, uint8 *buf, uint32 size)
{
	int8 radar_type = RADAR_NULL;
	uint8 radar_value, radar_voice_index;
	uint8 par[3];

	par[2] = RADAR_ALARM_BY_HARDWARE;

	if(size < 6)
	{
		LOGD("radar receive data length too shord %d", size);
		return;
	}
	LOGD("radar receive len(%d) %02X, %02X, %02X, %02X, %02X, %02X", size, buf[0], buf[1], buf[2], buf[3], buf[4], buf[5]);
	do
	{
		if(buf[0] != RD_CODE_HEADER0 || buf[1] != RD_CODE_HEADER1 || buf[2] != RD_CODE_HEADER2)
		{
			break;
		}

		if(buf[3] == RD_CODE_OFF)
		{
			radar_info.cnt = 0;
			par[0] = RADAR_STOP;				//OFF
			NotifyUIInThread(NOTIFY_UI_CODE_RADAR_WARNNING, par, 3);
		}
		else if(buf[3] == RD_CODE_BAND)
		{
			if(buf[4] == RD_CODE_HEART0 && buf[5] == RD_CODE_HEART1)
			{
				//heart
				if(GetSensitivity() == 0)
				{
					radar_info.cnt = 0;
				}

				if(radar_info.last_radar_type != RADAR_NULL)
				{
					radar_info.last_radar_type = RADAR_NULL;
					par[0] = RADAR_STOP;				//OFF
					NotifyUIInThread(NOTIFY_UI_CODE_RADAR_WARNNING, par, 3);
				}
			}
			else
			{
				if(buf[5] == RD_CODE_WEAK || buf[5] == RD_CODE_MIDDLE || buf[5] == RD_CODE_STRONG)
				{
					switch((buf[4] >> 4) & 0x0F)
					{
					case 4:
					case 5:
						radar_type = RADAR_X_BAND;
						radar_voice_index = x_band_voice;
						break;

					case 6:
					case 7:
						radar_type = RADAR_KU_BAND;
						radar_voice_index = ku_band_voice;
						break;

					case 10:
					case 11:
						radar_type = RADAR_K_BAND;
						radar_voice_index = k_band_voice;
						break;

					case 12:
					case 13:
						radar_type = RADAR_KA_BAND;
						radar_voice_index = ka_band_voice;
						break;

					default:
						break;
					}

					radar_value = (buf[5] == RD_CODE_STRONG) ? RADAR_LEVEL_HIGH : ((buf[6] == RD_CODE_MIDDLE) ? RADAR_LEVEL_MIDDLE : RADAR_LEVEL_LOW);
				}
			}
		}
		else if(buf[3] == RD_CODE_RASER)
		{
			//laser
			if(buf[4] != RD_CODE_RASER_HEADER0 || buf[5] != RD_CODE_RASER_HEADER1)
			{
				break;
			}
			radar_value = 0;
			radar_type = RADAR_LASER;
			radar_voice_index = laser_voice;
		}
		LOGD("type %d, level %d", radar_type, radar_value);

		if(radar_type != RADAR_NULL && GetCurRadarIsEnable(radar_type))
		{
			if(radar_info.last_radar_type != radar_type)
			{
				radar_info.last_radar_type = radar_type;
				radar_info.cnt = 0;
			}

			radar_info.cnt ++;

			if(radar_info.cnt == GetCurRadarTypeMaxCnt(radar_type, GetSensitivity()))
			{
				LOGD("voice speech %d", radar_voice_index);
				// only notify once
				//voice_assemb_out_weng(radar_voice_index, 0, 0, 0, special_voice_mode, vtb_numbF, NULL, IDLE_MODE, TTS_PLAY_LEVEL_HIGH);
				VoicePlayCommon(radar_voice_index, TTS_PLAY_LEVEL_HIGH, VOICE_DATA_FLAG_TTS_IN_THREAD);
				par[0] = radar_value;
				par[1] = radar_type;

				LOGD("notify to ui");
				NotifyUIInThread(NOTIFY_UI_CODE_RADAR_WARNNING, par, 3);
			}
		}
	} while(0);
}

static MCU_PROTOCAL_INFO radar_protocal =
{
	NULL,						//init
	radar_process,				//process
	NULL,						//deinit
	NULL,						//send
	NULL						//extra
};

void radar_init(void)
{
	radar_info.cnt = 0;
	radar_info.last_radar_type = RADAR_NULL;

	if(registerProtocal(PROTOCAL_RADAR, &radar_protocal) == -1)
	{
		LOGD("radar register fail");
	}
}

void radar_deinit(void)
{
	unRegisterProtocal(PROTOCAL_RADAR);
}
