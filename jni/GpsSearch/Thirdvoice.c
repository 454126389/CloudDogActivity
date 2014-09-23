#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <pthread.h>
#include <math.h>
#include <string.h>
#include "config.h"
#include "common.h"
#include "_string.h"
#include "List.h"
#include "GpsData.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "DataBase.h"
#include "thirdvoicetable.h"
#include "AlarmProcess.h"
#include "EnterExitDataBase.h"
#include "PointSearch.h"
#define __THIRDVOICE_C__
#include "thirdvoice.h"

#ifdef _CUSTOMMAIN_EXTRA_POINT_
unsigned int retTmp;
#endif /* ifdef _CUSTOMMAIN_EXTRA_POINT_ */

uint8 *RemoveString(const uint8 *src, uint8 *sub)
{
	uint8 *p = _strstr(src, sub);
	uint32 pos;
	uint8 *ret;

	if(p == NULL)
	{
		return _strdup(src);
	}
	ret = malloc(_strlen(src) - _strlen(sub) + 1);

	pos = p - src;
	memcpy(ret, src, pos);
	_strcpy(ret + pos, p + _strlen(sub));

	return ret;
}

uint8 *InsertBeforeString(const uint8 *src, uint8 *sub, uint8 *des)
{
	uint8 *p, *ret;
	uint32 len = _strlen(src) + _strlen(des) + 1;
	uint32 pos;

	p = _strstr(src, sub);

	if(p == NULL)
	{
		return _strdup(src);
	}

	ret = (uint8 *)malloc(len);
	pos = p - src;
	memcpy(ret, src, pos);
	memcpy(ret + pos, des, _strlen(des));
	pos = pos + _strlen(des);
	_strcpy(ret + pos, p);

	return ret;
}

uint8 *InsertAfterString(const uint8 *src, uint8 *sub, uint8 *des)
{
	uint8 *p, *ret;
	uint32 len = _strlen(src) + _strlen(des) + 1;
	uint32 pos;

	p = _strstr(src, sub);

	if(p == NULL)
	{
		return _strdup(src);
	}

	ret = (uint8 *)malloc(len);
	pos = p - src + _strlen(sub);
	memcpy(ret, src, pos);
	memcpy(ret + pos, des, _strlen(des));
	pos = pos + _strlen(des);
	_strcpy(ret + pos, p + _strlen(sub));

	return ret;
}

uint8 *ReplaceString(uint8 *src, uint8 *sub, uint8 *des)
{
	uint8 *p = _strstr(src, sub);

	if(p != NULL)
	{
		memcpy(p, des, _strlen(sub));
	}
	return src;
}

uint8 *InsertAfterRemove(const uint8 *src, uint8 *rem, uint8 *ins)
{
	uint32 len_rem = _strlen(rem);
	uint32 len_ins = _strlen(ins);
	uint8 *p = NULL;
	uint8 *ret;
	uint32 len = _strlen(src) - len_rem + len_ins + 1;
	uint32 pos;

	p = _strstr(src, rem);
	if(p == NULL)
	{
		return _strdup(src);
	}

	ret = malloc(len);
	pos = p - src;
	memcpy(ret, src, pos);
	memcpy(ret + pos, ins, len_ins);
	pos += len_ins;
	_strcpy(ret + pos, p + len_rem);

	return ret;
}

//if dec bit above bits, the bits is the really bits. otherwise the left bits will filled with space.
uint8 *Dec2String(uint32 dec, uint8 bits)
{
	uint8 buf[10];
	uint8 *p = malloc(4);
	int i;
	memset(buf, ' ', 9);
	buf[9] = 0;

	for(i = 8; i >= 0; i--)
	{
		if(dec == 0)
		{
			break;
		}
		buf[i] = (dec % 10) + '0';
		dec /= 10;
	}

	i = i + 1;
	if((9 - i) < bits)
	{
		i = (bits <= 9) ? (9 - bits) : 0;
	}

	return _strdup(buf + i);
}

/**
 * return: the voice string
 * flag: resver for later
 *
 * if can't get the voice data, will length returned is NULL
 */
uint8 *GetCommonVoiceData(uint32 index, uint32 flag)
{
	uint8 *p = NULL;

	p = _strdup(specialSentenceArray[index]);

	return p;
}

uint8 *GetSafeString(ALARM_POINT_NODE *pAlarmPoint, uint32 index, const uint8 *str)
{
	uint8 *ret;
	uint8 *ee_str = NULL;
	uint8 *p = NULL;

	switch(index)
	{
	case 0x20:
	case 0x25:
		if(IsNight())
		{
			ret = RemoveString(str, "请开头灯");
		}
		else
		{
			ret = _strdup(str);
		}
		break;

	case 0x10:
	case 0x14:
	case 0x11:
	case 0x48:
	case 0x33:
	case 0x34:
	case 0x35:
	case 0x36:
		if(ExtraVoiceSearch(pAlarmPoint->DBRecord->ep_lat, pAlarmPoint->DBRecord->ep_lon, index, &ee_str) == EXTRA_VOICE_SUCCESS)
		{
			if(index == 0x10 || index == 0x14)
			{
				p = malloc(_strlen(ee_str) + 5);
				memcpy(p, ee_str, _strlen(ee_str));
				_strcpy(p + _strlen(ee_str), "方向");
				ret = InsertAfterRemove(str, "为交流道", p);
			}
			else if(index == 0x11 || index == 0x48)
			{
				p = malloc(_strlen(ee_str) + 5);
				memcpy(p, ee_str, _strlen(ee_str));
				_strcpy(p + _strlen(ee_str), "方向");
				ret = InsertAfterRemove(str, "为系统交流道", p);
			}
			else if(index == 0x33 || index == 0x34)
			{
				ret = InsertBeforeString(str, "收费", ee_str);
			}
			else if(index == 0x35 || index == 0x36)
			{
				ret = InsertBeforeString(str, "休息", ee_str);
			}

			if(p != NULL)
			{
				free(p);
			}

			free(ee_str);
		}
		else
		{
			ret = _strdup(str);
		}
		break;

	default:
		ret = _strdup(str);
		break;
	}

	return ret;
}

uint8 *InsertDistanceToString(const uint8 *src, const uint8 *sub, uint32 distance)
{
	uint8 buf[13];
	uint8 *speed_str;
	uint8 *unit = "米";
	int32 pos, i;

	pos = 12 - strlen(unit);
	memset(buf, ' ', pos);
	_strcpy(&buf[pos], unit);

	for(i = (pos - 1); i >= 0; i--)
	{
		if(distance == 0)
		{
			break;
		}
		buf[i] = (distance % 10) + '0';
		distance /= 10;
	}

	i = i + 1;

	return InsertAfterString(src, sub, &buf[i]);
}

/**
 * return: the voice string
 * flag: resver for later
 *
 * if can't get the voice data, will length returned is NULL
 */
uint8 *GetVoiceDataByPoint(ALARM_POINT_NODE *pAlarmPoint, uint32 flag)
{
	uint8 *ret;
	const uint8 *p;
	uint8 *speed_str;
	uint32 dis = 0;
	uint32 index = pAlarmPoint->DBRecord->voice_index;

	switch(pAlarmPoint->DBRecord->driver_mode)
	{
	case DRIVER_MODE_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE:
	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE:
		dis = pAlarmPoint->cur2ep_dist;
		dis = dis - ((GetCurSpeed() > 80) ? 15 : 0);
		dis = dis - ((GetCurSpeed() > 40) ? 15 : 0);
		ret = InsertDistanceToString(pictureSentenceArray[index], "前方", dis);
		//ret = _strdup(pictureSentenceArray[index]);
		break;

	case DRIVER_MODE_SELF_POINT:
		dis = pAlarmPoint->cur2ep_dist;
		dis = dis - ((GetCurSpeed() > 80) ? 15 : 0);
		dis = dis - ((GetCurSpeed() > 40) ? 15 : 0);
		ret = InsertDistanceToString(specialSentenceArray[index], "前方", dis);
		//ret = _strdup(specialSentenceArray[index]);
		break;

	case DRIVER_MODE_SAFE:
	case DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE:
	case DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE:
		p = safeSentenceArray[index];
		ret = GetSafeString(pAlarmPoint, index, p);
		break;
	}

	speed_str = Dec2String(pAlarmPoint->DBRecord->speed_limit, 3);
	//LOGD("%s", speed_str);
	ReplaceString(ret, "***", speed_str);
	free(speed_str);

	return ret;
}

void VoicePlayCommon(uint32 index, uint8 level, uint32 flag)
{
	uint8 *p, *q;
	uint32 len;

	p = GetCommonVoiceData(index, flag);
	if(p == NULL)
	{
		return;
	}

	len = _strlen(p) + 1;
	q = (uint8 *)malloc(len + 1);
	*q = level;
	memcpy(q + 1, p, len);
	if(flag & VOICE_DATA_FLAG_TTS_IN_THREAD)
	{
		NotifyUIInThread(NOTIFY_UI_CODE_TTS_PLAY, q, len);
	}
	else
	{
		NotifyUI(NOTIFY_UI_CODE_TTS_PLAY, q, len);
	}
	free(q);
	free(p);
}

void VoicePlayPoint(ALARM_POINT_NODE *pAlarmPoint, uint8 level, uint32 flag)
{
	uint8 *p, *q;
	uint32 len;
	p = GetVoiceDataByPoint(pAlarmPoint, flag);
	if(p == NULL)
	{
		return;
	}

	len = _strlen(p) + 1;
	q = (uint8 *)malloc(len + 1);
	*q = level;
	memcpy(q + 1, p, len);
	if(flag & VOICE_DATA_FLAG_TTS_IN_THREAD)
	{
		NotifyUIInThread(NOTIFY_UI_CODE_TTS_PLAY, q, len);
	}
	else
	{
		NotifyUI(NOTIFY_UI_CODE_TTS_PLAY, q, len);
	}
	free(q);
	free(p);
}
