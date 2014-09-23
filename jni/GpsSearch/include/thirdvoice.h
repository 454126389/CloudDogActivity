#ifndef __THIRDVOICE_H__
#define __THIRDVOICE_H__

#ifdef __THIRDVOICE_C__
#define __THIRDVOICE_DEC__
#else
#define __THIRDVOICE_DEC__ extern
#endif

#define VOICE_DATA_FLAG_NULL								0
#define VOICE_DATA_FLAG_TTS_IN_THREAD						1

#define vtb_numb0   										0
#define vtb_numb1   										1
#define vtb_numb2   										2
#define vtb_numb3  	 										3
#define vtb_numb4   										4
#define vtb_numb5   										5
#define vtb_numb6   										6
#define vtb_numb7   										7
#define vtb_numb8   										8
#define vtb_numb9   										9
#define vtb_numbA   										10
#define vtb_numbB   										11
#define vtb_numbC   										12
#define vtb_numbD   										13
#define vtb_numbE   										14
#define vtb_numbF   										15

#define special_voice_mode  								15

#define TTS_PLAY_LEVEL_IMMEDIATELY				(0)
#define TTS_PLAY_LEVEL_HIGH						(1)
#define TTS_PLAY_LEVEL_MID						(2)
#define TTS_PLAY_LEVEL_LOW						(3)
#define TTS_PLAY_LEVEL_IGNORE					(4)

void VoicePlayCommon(uint32 index, uint8 level, uint32 flag);
void VoicePlayPoint(ALARM_POINT_NODE *pAlarmPoint, uint8 level, uint32 flag);

#endif
