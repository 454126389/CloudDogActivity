#ifndef _THIRDVOICETABLE_
#define _THIRDVOICETABLE_

#define welcome_voice           0x00 //��ӭ��
#define GPS_ok_voice            0x01 //ף��;ƽ��
#define opp_sfp_set_ok_voice    0x02 //�����Խ����趨���
#define return_done_voice       0x03 //�ָ�ԭ���趨
#define now_timeup_voice        0x04 //���㱨ʱ
#define pict_mode_voice         0x05 //����ϵͳģʽ
#define safe_mode_voice         0x06 //��ȫģʽ
#define sys_update_voice        0x07 //ϵͳ����ģʽ
    #ifdef  _CUSTOM_PCM_RUSSIA_
#define sfp_set_ok_voice        0x45 //�Խ����趨���
#define rsfp_set_ok_voice       0x08 //�����Խ����趨���
    #else // ifdef  _CUSTOM_PCM_RUSSIA_
#define sfp_set_ok_voice        0x08 //�Խ����趨���
    #endif // ifdef  _CUSTOM_PCM_RUSSIA_
#define sfp_del_ok_voice        0x09 //�Խ���ɾ�����
#define sfp_re_st_voice         0x0A //�����Խ������
#define x_band_voice            0x0B //x band
#define ku_band_voice           0x0C //ku band
#define k_band_voice            0x0D //k band
#define ka_band_voice           0x0E //ka band
#define laser_voice             0x0F //laser
#define di_voice                0x10 //��
#define ding_voice              0x11 //��
#define ltime_drive_voice       0x12 //��ʱ���ʻ
#define speed_over_alarm_voice  0x13 //���ѳ�����
#define battery_low_alarm_voice 0x14 //��ƿ��ѹ�Ѳ���
#define ding_dong_voice         0x15 //����
#define di_ri_ring_voice        0x17 //������
#define mode00_music_sp_voice   0x18 //mode00 start point
#define mode01_music_sp_voice   0x19 //mode01 start point
#define sfp_music_sp_voice      0x1A //self point start point
#define di_di_voice             0x1B //�ǵ�
#define camera_voice            0x1C //camera
#define sfp_voice               0x1D //ǰ��Ϊ�Խ���
#define near_point_voice        0x1E //Ŀ���Ѿ��ӽ�
#define postion_voice           0x1F //��������λ��
#define postion_lat_voice       0x20 //γ��
#define postion_lon_voice       0x21 //����
#define postion_mal_voice       0x22 //�߶�
#define now_time_st_voice       0x23 //����ʱ��
#define now_date_voice          0x24 //������
#define now_time_voice          0x25 //ʱ����
#define drive_dir_voice         0x26 //��ʻ����
#define max_speed_rec_voice     0x27 //���� ��� �ó� ʱ�� 9 �� 1 10 1 ����
#define trip_dist_voice         0x2A //���� �ۼ� �ó� ���� 60 9 �� 9 ǧ 9 �� 50 9 ����
#define total_dist_voice        0x29 //���� �ܼ� �ó� ����	70 9 �� 9 ǧ 9 �� 1 10 9 ����

#define GPS_signal_voice        0x28 //����Ѷ��
#define battery_vol_voice       0x2B //��ƿ��ѹ
#define rd_mute_set_voice       0x2C //�ٶ��趨
#define set_speed_voice         0x2E //120����
#define cruise_speed_voice      0x2F //�����趨
#define drive_mode_voice        0x30 //��ʻģʽ�趨
#define drive_all_voice         0x31 //ȫ��λ
#define drive_lo_voice          0x32 //����
#define drive_hi_voice          0x33 //����
#define speed_re_voice          0x34 //�ٶ�΢��
#define set_speed_re_voice      0x35 //10����
#define voice_alarm_set_voice   0x36 //�������������趨
#define voice_alarm_mode_voice  0x37 //��������
#define voice_mode_voice        0x38 //����
#define alarm_mode_voice        0x39 //��������
#define music_mode_voice        0x3A //���� ���� ģʽ
#define sfp_del_sel_voice       0x3B //
#define zone_set_voice          0x3C //ʱ������
#define unit_set_voice          0x3D //�ٶȵ�λѡ��
#define km_unit_voice           0x3E //���ﵥλ
#define mile_unit_voice         0x3F //Ӣ�ﵥλ
#define node_unit_voice         0x40 //���� ���� ��λ
#define welcome_sel_voice       0x42 //���� ��ӭ�� ѡ�� �趨
#define long_welcome_voice      0x43 //���� ���� ��ӭ��
#define short_welcome_voice     0x44 //���� ��� ��ӭ��
#define drive_ver_voice         0x46 //���� Ŀǰ ���� �汾 0 0 0 1
#define db_ver_voice            0x47 //���� Ŀǰ ����� �汾 0 0 0 1
#define voice_ver_voice         0x48 //���� Ŀǰ ���� �汾 0 0 0 1
#define now_set_voice           0x4A //���� Ŀǰ �趨
#define pict_speed_mode_voice   0x4B //���� ���� ϵͳ ���� ģʽ
#define safe_speed_mode_voice   0x4C //���� ��ȫ ��ʻ ���� ģʽ
#define sfp_voice_speed_limit   0x4D //���� �Խ�����������
#define zone_re_voice           0x4E //��19��59��
#define light_st_set_voice      0x4F //����ʱ���趨
#define dim_st_set_voice        0x50 //΢��ʱ���趨
#define dim_light_time_voice    0x51 //����7��
#define light_st_voice          0x52 //��������
#define dim_st_voice            0x53 //΢������
#define dim_light_off_voice     0x54 //����΢���ر�
#define rd_set_voice            0x55 //����ϵͳ�趨
#define rd_on_voice             0x56 //����ϵͳ����
#define rd_off_voice            0x57 //����ϵͳ�ر�
#define rd_sent_set_voice       0x58 //�״�Ѷ������趨
#define rd_offline_voice        0x59 //�����δ����

#define rdg_set_voice           0x5A //radarϵͳ�趨
#define rdg_on_voice            0x5B //radraϵͳ����
#define rdg_off_voice           0x5C //radarϵͳ�ر�

//#define sys_update_ok_voice     0x5B//���� ϵͳ ���� ���
//#define voice_update_ok_voice   0x5C//���� ���� ���� ���
//#define db_update_ok_voice      0x5D//���� ����� ���� ���

//#define xband_alarm_voice       0x5A
//#define kaband_alarm_voice      0x5B
//#define kband_alarm_voice       0x5C
#define kuband_alarm_voice      0x5D
#define laser_alarm_voice       0x5E

#define trip_dist_del_voice     0x5F //���� �ۼ� �ó� ���� ɾ�� ���
#define total_dist_del_voice    0x60 //���� �ܼ� �ó� ���� ɾ�� ���
#define max_speed_del_voice     0x61 //���� ��� �ó� ʱ�� ɾ�� ���

#define radar_mode_voice        0x62 //���� �״� ģʽ

#define radar_near_voice        0x63 //���� ǰ���� ���� �������� �� С�� ��ʻ

#define rd_sent_set_voice_sph   0x64 //���� Ŀǰ �״� Ѷ�� ��� �趨 ����
#define rd_sent_set_voice_hi    0x65
#define rd_sent_set_voice_med   0x66
#define rd_sent_set_voice_lo    0x67

#define GPS_signal_voice_hi     0x68 //���� ���� ���� Ѷ�� ����
#define GPS_signal_voice_med    0x69
#define GPS_signal_voice_lo     0x6A

#define cruise_only_mode_voice  0x6B //���� ���� ģʽ

#define fcamera_spd_mode_voice  0x6C //���� �̶� ���� ϵͳ ���� ģʽ
#define fcamera_mode_voice      0x6D //���� �̶� ���� ϵͳ ģʽ

#define update_db_alarm_voice   0x6E //���������ϵͳ

#define radar_connected_voice   0x6F

#define now_time00_voice        0x70 //���� ʱ�� ��ҹ 12 �� ��
#define now_time01_voice        0x71 //���� ʱ�� �賿 1 �� ��
#define now_time02_voice        0x72 //���� ʱ�� �賿 �� �� ��
#define now_time03_voice        0x73 //���� ʱ�� �賿 3 �� ��
#define now_time04_voice        0x74 //���� ʱ�� �賿 4 �� ��
#define now_time05_voice        0x75 //���� ʱ�� �賿 5 �� ��
#define now_time06_voice        0x76 //���� ʱ�� ���� 6 �� ��
#define now_time07_voice        0x77 //���� ʱ�� ���� 7 �� ��
#define now_time08_voice        0x78 //���� ʱ�� ���� 8 �� ��
#define now_time09_voice        0x79 //���� ʱ�� ���� 9 �� ��
#define now_time10_voice        0x7A //���� ʱ�� ���� 10 �� ��
#define now_time11_voice        0x7B //���� ʱ�� ���� 11 �� ��
#define now_time12_voice        0x7C //���� ʱ�� ���� 12 �� ��
#define now_time13_voice        0x7D //���� ʱ�� ���� 1 �� ��
#define now_time14_voice        0x7E //���� ʱ�� ���� �� �� ��
#define now_time15_voice        0x7F //���� ʱ�� ���� 3 �� ��
#define now_time16_voice        0x80 //���� ʱ�� ���� 4 �� ��
#define now_time17_voice        0x81 //���� ʱ�� ���� 5 �� ��
#define now_time18_voice        0x82 //���� ʱ�� ���� 6 �� ��
#define now_time19_voice        0x83 //���� ʱ�� ���� 7 �� ��
#define now_time20_voice        0x84 //���� ʱ�� ���� 8 �� ��
#define now_time21_voice        0x85 //���� ʱ�� ���� 9 �� ��
#define now_time22_voice        0x86 //���� ʱ�� ���� 10 �� ��
#define now_time23_voice        0x87 //���� ʱ�� ���� 11 �� ��

#define rd_mute_auto_set_voice  0x88
#define rd_mute_auto_on_voice   0x89
#define rd_mute_auto_off_voice  0x8A

#define fix_camera_pt_voice             0x8B
#define mobile_camera_pt_voice          0x8C
#define possible_mobile_camera_pt_voice 0x8D



#define xband_set_voice         0x90 //x band setting
#define xband_on_voice          0x91 //x band on
#define xband_off_voice         0x92 //x band off

#define kuband_set_voice        0x93 //ku band setting
#define kuband_on_voice         0x94 //ku band on
#define kuband_off_voice        0x95 //ku band off

#define kaband_set_voice        0x96 //ka band setting
#define kaband_on_voice         0x97 //ka band on
#define kaband_off_voice        0x98 //ka band off

#define kband_set_voice         0x99 //k band setting
#define kband_on_voice          0x9A //k band on
#define kband_off_voice         0x9B //k band off

#define laser_set_voice         0x9C //laser setting
#define laser_on_voice          0x9D //laser on
#define laser_off_voice         0x9E //laser off


#define sd_card_out_voice       0xA0
#define recing_voice            0xA1
#define force_recing_voice      0xA2
#define force_rec_end_voice     0xA3
#define sd_card_full_voice      0xA4
#define auto_frec_st_voice      0xA5
#define auto_frec_end_voice     0xA6
#define sd_cant_read_voice      0xA7
#define sd_not_enough_voice     0xA8
#define sd_only_10_voice        0xA9
#define sd_only_20_voice        0xAA
#define sd_only_30_voice        0xAB
#define two_cam_rec_voice       0xAC
#define front_cam_rec_voice     0xAD
#define rear_cam_had_off_voice  0xAE
#define g_sensor_fail_voice     0xAF
#define gps_cant_rec_voice      0xB0
#define video_cant_rec_voice    0xB1
#define rear_cam_set_voice      0xB2
#define rear_cam_on_voice       0xB3
#define rear_cam_off_voice      0xB4

#define safe_mode_speed_alarm_id 0x80 //safe mode have speedlimit start addr
#define proc_bg_vsnumb 			3 //5            //for bengo voice used

#define _CUSTOMMAIN_TTS_REPLACE_

#define MAX_NAME_TABLE_NUMBER		270
//#define MAX_CLIP_COUNT_TWO			50
#define MAX_CUSTOM_NAME_LENGTH		40
#ifndef _CUSTOMMAIN_TTS_REPLACE_
#define SENTENCE_LENGTH				16
#define MAX_SENTENCE_TABLE			8
#else
#define SENTENCE_LENGTH				80
#define MAX_SENTENCE_TABLE			16//8
#endif

#ifdef _CUSTOMMAIN_PCM_VOICE_
#define PCM_SENTENCE_LENGTH				16
#define MAX_PCM_SENTENCE_TABLE			8
#endif

#define TTS_SENTENCE_LENGTH			80
#define MAX_TTS_SENTENCE_TABLE		16//8

#define _FF_		0xffff//none data

//static VOICE_INDEX_STRUCT voiceIndexTab;

typedef enum
{
	C_S_WELCOME,
	C_S_NAME,
	C_S_GLOBAL,
}VOICE_CUSTOM_INDEX;

typedef struct
{
	unsigned char name[MAX_CUSTOM_NAME_LENGTH];
	unsigned int position;
	unsigned int size;//byte
}VOICE_LIB_TABLE;

typedef struct
{
	unsigned char count;//how many
	unsigned char pointer; //which one is the current
	VOICE_LIB_TABLE array[SENTENCE_LENGTH];//the voice sentence array, current is 16
}VOICE_GROUP;

#ifndef _CUSTOMMAIN_TTS_REPLACE_
typedef struct
{
	kal_uint8 msgflag;
	kal_uint8 vPointer;//current voice out pointer
	kal_uint8 count;//how many clips 
	kal_uint16 table[MAX_SENTENCE_TABLE][SENTENCE_LENGTH];//the sentence matrix
}CUSTOM_VOICE_ARRAY;
#else
typedef struct
{
	unsigned char msgflag;
	unsigned char ttsInLoop;
	unsigned char vPointer;//current voice out pointer
	unsigned char count;//how many clips
	unsigned char table[MAX_TTS_SENTENCE_TABLE][TTS_SENTENCE_LENGTH];//the sentence matrix
}CUSTOM_VOICE_ARRAY;
#endif

#ifdef _CUSTOMMAIN_PCM_VOICE_
typedef struct
{
	kal_uint8 msgflag;
	kal_uint8 vPointer;//current voice out pointer
	kal_uint8 count;//how many clips 
	kal_uint16 table[MAX_PCM_SENTENCE_TABLE][PCM_SENTENCE_LENGTH];//the sentence matrix
}CUSTOM_PCM_VOICE_ARRAY;
#endif

#ifdef _CUSTOMMAIN_TTS_VOICE_
typedef struct
{
	kal_uint8 msgflag;
	kal_uint8 vPointer;//current voice out pointer
	kal_uint8 count;//how many clips 
	kal_uint8 table[MAX_TTS_SENTENCE_TABLE][TTS_SENTENCE_LENGTH];//the sentence matrix
}CUSTOM_TTS_VOICE_ARRAY;
#endif
#if 0
typedef struct
{
	kal_uint8 count;
	kal_uint8 pointer;
	kal_uint8 going;
	VOICE_GROUP array[7];
}VOICE_GROUP_ARRAY;
#endif
//same name add UNIQUE to head

typedef enum
{
#ifdef _CUSTOMMAIN_PCM_RUSSIA_VOICE_
	//		 0						1						2						3						4
		A_PERCENTAGE, 			ACTION_APPROACHINGPOI,	ACTION_BRIGHTDIMMODEOFF,	ACTION_BRIGHTMODEON,	ACTION_CHECKBAR,
	ACTION_DIMMODEON,	ACTION_ENTERINGHIGHWAY, ACTION_ENTERINGHIGHWAY1,	ACTION_ENTERINGHIGHWAY2,ACTION_ENTERINGOVERPASS,
	ACTION_ENTERINGTUNNEL,	ACTION_EXITHIGHWAY, ACTION_EXITHIGHWAY1,		ACTION_EXITSLOWLANE,	ACTION_EXITSLOWLANE1,
	ACTION_NOTURNLEFT,	ACTION_NOTURNRIGHT, 	ACTION_NOUTURN, 	ACTION_PLEASETURNONHEADLIGHT,	ACTION_RECORDING,
	ACTION_SLOWDOWN,	ALARM_DIL,				ALARM_DINGDONG, 			ALARM_DIRIRING, 		ALARM_DIS,
	ALARM_DONG, 		ALARM_SPMOD0MUSIC,		ALARM_SPMOD1MUSIC,			ALARM_SPMUSIC,			BAND_KABAND,
	BAND_KABAND1,		BAND_KBAND, 			BAND_KBAND1,				BAND_KUBAND,			BAND_KUBAND1,
	BAND_LASERBAND, BAND_LASERBAND1,			BAND_XBAND, 				BAND_XBAND1,			BLANK,
	BUIDING_MARKETZONE,BUIDING_SHOPINGMALL, 	BUIDING_STATEGOVERNMENT,	BUIDING_TUNNEL, 		BUIDING_WILDANIMALAREA,
	BUILDING_AHIGHWAY,BUILDING_AIRPORT, 		BUILDING_COUNTYHALL,	BUILDING_DISTRICTOFFICE,	BUILDING_EXPRESSWAYEXIT,
	BUILDING_GASSTATION,BUILDING_HIGHWAYEXIT,	BUILDING_HOSPITAL,		BUILDING_KANGAROOAREA,		BUILDING_OVERPASS,
	BUILDING_OVERPASS1,BUILDING_POIAREA,		BUILDING_POIAREA1,		BUILDING_POLICESTATION, 	BUILDING_PROVINCEGOVERNMENT,
	BUILDING_RAILROAD,BUILDING_RESTAREA,		BUILDING_SCHOOLZONE,	BUILDING_SITYHALL,			BUILDING_TOLLSTATION,
	BUILDING_TRAINSTATION,DIR_AHEAD,			DIR_OPPOSITEDIRECTION,		NUMB_10,				NUMB_100,
	NUMB_110,			NUMB_120,				NUMB_130,					NUMB_140,				NUMB_15,
	NUMB_150,			NUMB_20,				NUMB_25,					NUMB_30,				NUMB_35,
	NUMB_40,			NUMB_45,				NUMB_50,					NUMB_55,				NUMB_60,
	NUMB_65,			NUMB_70,				NUMB_75,					NUMB_80,				NUMB_90,
	RADAR_STRELKA_WENG_3DB,SAFE_ALCOHOLATE, 	SAFE_ATTENTIONOVERSPEED,SAFE_AVERAGESPEEDCAMERAZONE,SAFE_BLINDINGSNOW,
	SAFE_BUMPYLANE,SAFE_BUSONLYLANE,			SAFE_CAMERA,				SAFE_CAMERA1,			SAFE_CAMERA2,
	SAFE_CAMERAMOD,SAFE_CAMERASPEEDLIMITMOD,	SAFE_CAMERASPEEDLIMITMOD1,SAFE_CAUTION, 			SAFE_CAUTIONRADARDISCONNECTED,
	SAFE_CHECKPOINT,SAFE_CHECKUNDERLIMIT,		SAFE_DOWNLOADMOD,		SAFE_DRIVECAREFULLY,		SAFE_DRIVECAREFULLY1,
	SAFE_DRIVINGMOD,SAFE_FASTLANE,				SAFE_HEAVYFOG,			SAFE_LANDSIDEZONE,			SAFE_NOPASSZONE,
	SAFE_OVERSPEEDING,SAFE_RADARDISCONNECTED,	SAFE_REDLIGHT,			SAFE_RESTFORLONGTIMEDRIVE,	SAFE_ROCKSLIDE,
	SAFE_SAFETYDRIVING,SAFE_SAFETYDRIVINGMOD,	SAFE_SAFETYSPEEDLIMITMOD,SAFE_SAFETYSPEEDLIMITMOD1, SAFE_SAFETYSPEEDLIMITMOD2,
	SAFE_SHARPCURVE,SAFE_SPEEDLIMIT,	SAFE_SPEEDLIMITMOD, 			SAFE_STRONGWINDZONE,		SAFE_TRAFFICSURVEILLANCE,
	SAFE_WARNINGMOD,STATE_ATTUNNELEXIT, STATE_ATUNDERGROUNDEXIT,		STATE_ATUNDERGROUNDEXIT1,	STATE_ENDOFSECTION,
	STATE_NOPARKINGLOT,STATE_OPPOSITEDIRSAVEOK,STATE_POIDELETEDOK,		STATE_POISAVEOK,			STATE_RECORDINGACTIVATED,
	STATE_RECORDINGEND,STATE_SDCARDFULL,		STATE_STOREDOK, 		STATE_UNDERBRIDGE,			STATE_UNDERGROUNG,
	SWITCH_OFF, 		SWITCH_OFF1,			SWITCH_OFF2,				SWITCH_ON,				SWITCH_ON1,
	SWITCH_ON2, 		TIME_10AM,				TIME_11AM,					TIME_12AM,				TIME_12PM,
	TIME_13PM,			TIME_14PM,				TIME_15PM,					TIME_16PM,				TIME_17PM,
	TIME_18PM,			TIME_19PM,				TIME_1AM,					TIME_20PM,				TIME_21PM,
	TIME_22PM,			TIME_23PM,				TIME_2AM,					TIME_3AM,				TIME_4AM,
	TIME_5AM,			TIME_6AM,				TIME_7AM,					TIME_8AM,				TIME_9AM,
	TURN_LEFT,			TURN_RIGHT, 			UNIT_KMH,					UNIT_MPH,				WELCOME_GPSUP,
	WELCOME_NICETRIP,	WORD_AAC,				WORD_AAC1,					WORD_AREA,				WORD_AUDIO,
	WORD_AUTOSCAN,		WORD_BLACKSPOT, 		WORD_BRIGHT,				WORD_BRIGHTMOD, 		WORD_CANNOTREADSDCARD,
	WORD_CITY,			WORD_CITY1, 			WORD_CITY2, 			WORD_COUNTYBOUNDARY,		WORD_CURRENTPOSITION,
	WORD_CURRENTPOSITION1,WORD_CURRENTTIME, 	WORD_CURRENTTIME1,		WORD_CURRENTVOLTAGE,		WORD_CURRENTVOLTAGE1,
	WORD_DEFAULT,		WORD_DEFAULT1,			WORD_DIM,				WORD_DIMMOD,				WORD_DOWNTOWN,
	WORD_DUALCAMERARECORDING,WORD_FIXED,		WORD_FRONTCAMERARECORDING,WORD_GMOITONRECORDINGEND,WORD_GOOD,
	WORD_GPSLOGGERFAIL,WORD_GSENSORRECORDING,	WORD_GSENSORRECORDINGFAIL,WORD_HIGH,				WORD_HIGHWAY,
	WORD_HIGHWAY1,WORD_LESSUSEABLECAPACITYFORSD,WORD_LOW,					WORD_MIDDLE,		WORD_MOBILE,
	WORD_MOBLE1,		WORD_MODE,				WORD_MOTIONRECORDING,		WORD_NORMAL,			WORD_OVERSPEED,
	WORD_OVERSPEED1,	WORD_POIDELETE, 		WORD_POOR,					WORD_POP,				WORD_POP1,
	WORD_POSSIBLE,		WORD_PROVINCEBOUNDARY,	WORD_RADARMOD,				WORD_RADARSYSTEM,		WORD_REARCAMERA,
	WORD_REARCAMERA1,	WORD_REARCAMERAOFF, 	WORD_REARCAMERAON,		WORD_REARCAMERASETTING, 	WORD_RECORDINGFAIL,
	WORD_SATELLITECINDITION,WORD_SATELLITECONDITION,WORD_SATELLITECONDITION1,WORD_SDCAPACITYLESSTHAN,WORD_SDCAPACITYLESSTHAN10,
	WORD_SDCAPACITYLESSTHAN20,WORD_SDCAPACITYLESSTHAN30,WORD_SDCARDNOTINSERTED,WORD_SENSITIVITY,	WORD_SITYBOUNDARY,
	WORD_SMARTMUTE, 	WORD_SMARTMUTE1,		WORD_SMARTMUTE2,		WORD_SPEEDADUSTMENT,		WORD_STATEBOUNDARY,WORD_SUPER,
	WORD_TIMEZONE,		WORD_UNIT,				WORD_VOICE, 			WORD_WARNINGGPSLOGGERFAIL,	WORD_WARNINGGSENSORRECORDINGFAIL,
	WORD_WARNINGREACORDINGFAILED,
#else
// 0							//1						//2						//3							//4						//5						//6						//7							//8						//9
	ADDRESS_QU,				ADDRESS_SHEN, 			ADDRESS_SHI, 			ADDRESS_XIAN,  				ADDRESS_XIANG,  		ADDRESS_ZHENG, 		ADDRESS_ZHENGFU, 		ALARM_A,  					ALARM_B,  				ALARM_C, // 0-9
	ALARM_D, 					ALARM_DONG_1, 			ALARM_DONG_2, 			ALARM_DONG_3,  			ALARM_DU_L,  			ALARM_DU_S, 			ALARM_E, 				BAND_K,  					BAND_KA,  				BAND_KU, // 10-19
	BAND_LAYSER, 				BAND_X, 				DATE_D, 				DATE_M,  					DATE_Y,  				DIM_MINGLIANG, 		DIM_WEIAN, 			DIR_E_LON,  				UNIQUE_DIR_E,  			DIR_N_LAT, // 20-29
	UNIQUE_DIR_N, 				DIR_S_LAT, 				UNIQUE_DIR_S, 			DIR_W_LON,  				UNIQUE_DIR_W,  		LEVEL_HIGH, 			LEVEL_LOW, 				LEVEL_MID,  					MATH_JIA,  				MATH_JIAN, // 30-39
	NUM_0, 						NUM_1, 					NUM_2, 					NUM_3,  					NUM_4,  				NUM_5, 					NUM_6, 					NUM_7,  					NUM_8,  				NUM_9, // 40-49
	NUM_10, 					NUM_11, 				NUM_12, 				NUM_20,  					NUM_30,  				NUM_40, 				NUM_50, 				NUM_60,  					NUM_70,  				NUM_80, // 50-59
	NUM_90, 					NUM_100, 				NUM_110, 				NUM_120,  					NUM_130,  				NUM_140, 				NUM_150, 				NUM_160,  					NUM_170,  				NUM_180, // 60-69
	NUM_BAI, 					NUM_DIAN, 				NUM_QIAN, 				NUM_WAN,  					SAFE_ANQUANCHEJU,  	SAFE_AOTUDIANBO, 		SAFE_BIANJINGKOUAN, 	SAFE_CHANGTUQICHEZHAN,	SAFE_CHAOCHE,  		SAFE_CHUKOU, // 70-79 //
	SAFE_DITIEZHAN, 			SAFE_DIXIADAO, 		SAFE_DONGJIXUE, 		SAFE_DUANAI,  				SAFE_DULUNMATOU,  	SAFE_DUOWU, 			SAFE_GAOTIEZHANG, 	SAFE_GONGANJU,  			SAFE_HOUCHEZHAN,  	SAFE_HUOYUNZHAN, // 80-89
	SAFE_JIANCHAZHAN, 		SAFE_JIANLISHUO, 		SAFE_JIAYOUZHAN, 		SAFE_JICHANG,  				SAFE_JICHA,  			SAFE_JIEYUNZHAN, 		SAFE_JINCHANGCHUMO, 	SAFE_JINGCHAJU,  			SAFE_JINGGAOPAI,  		SAFE_JIXIAPO, // 90-99
	SAFE_JIZHUANWAN, 			SAFE_JUNSHIGUANZHIQU,SAFE_KEYUNZHAN, 		SAFE_LUDUAN,  				SAFE_LUKOU,  			SAFE_LUOSHI, 			SAFE_QIANGFENG, 		SAFE_RUKOU,  				SAFE_SHANGCHANG,  	SAFE_SHOUFEIZHAN, // 100-109
	SAFE_SHUIDAO, 				SAFE_TIELUPINGJIAODAO,SAFE_TINGCHECHANG, 	SAFE_XIUXIZHANG,  			SAFE_XUEXIAO,  			SAFE_YIYUAN, 			SAFE_YIZHAOSHI, 		SAFE_YOULEQU,  			SAFE_YUEXIAN,  			START_GLOBAL, // 110-119
	START_NAME, 				START_SAFEBAND, 		START_STARTING, 		START_WELCOME, 	 		START_ZHUPINGAN,  		TIME_LINGCHEN, 		TIME_WANSHANG, 		TIME_WUYIE,  				TIME_XIAWU,  			TIME_ZHAOSHANG, // 120-129
	TIME_ZHENG, 				TIME_ZHONGWU, 		UNIQUE_TIME, 			TURN_HUIZHUAN,  			TURN_YOUZHUAN2,  		TURN_YOUZHUAN, 		TURN_ZHOUZHUAN, 		TURN_ZHOUZHUAN2,  		UNIT_DANWEI,  			UNIT_DU, // 130-139
	UNIT_FENG, 					UNIT_FUTE, 				UNIT_GONGCHI, 			UNIT_GONGLI,  				UNIT_MA,  				UNIT_MIAO, 				UNIT_YINGCHI, 			UNIT_YINGLI,  				WORD_ANQUAN,  		WORD_BENGLUDUAN, // 140-149
	WORD_BIANHUANCHEDAO, 	WORD_BULIANG, 			WORD_CAMERA, 			WORD_CHANGSHIJIANJIASHI, WORD_CHAOGAO,  		WORD_CHAOSHU, 		WORD_CHESHU, 			WORD_CHUANGRED,  			WORD_DANXIANDAO,  	WORD_DAOLU, // 150-159
	WORD_DIANPINGDIANYA, 	WORD_DIANYABUZHU, 	WORD_DINGSHU, 		WORD_FEIFUYUANCHANG,  	WORD_FUDAO,  			WORD_GANDU, 			WORD_GAOJIA, 			WORD_GAOSHU,  				WORD_GENGXING,  		WORD_GONGCHEZHUANYONGDAO, // 160-169
	WORD_GONGLU, 				WORD_GUANBI, 			WORD_GUDING,			WORD_GUODAO,  			WORD_HAIBAGAODU,  	WORD_HAIPINGMIAN, 	WORD_JIANCHADIANPING,WORD_JIANSHUMANXING,  	WORD_JIAOJIECHU,  		WORD_JIAOLIUDAO, // 170-179
	WORD_JIAOTONGGUANZHIQU, WORD_JIASHI, 			WORD_JIESHU, 			WORD_JINGRU,  				WORD_JINGSHI,  			WORD_JINGZHI, 			WORD_JIUCHE, 			WORD_KAITOUDENG,  		WORD_KUAICHEDAO,  	WORD_KUAISHU, // 180-189
	WORD_LAYSER, 				WORD_LIANG, 			WORD_LIANGHAO, 		WORD_LIANXU,  				WORD_LINGSHITINGCHE,  WORD_LIUDONG, 		WORD_MANCHEDAO, 		WORD_MOSHI,  				WORD_MUBIAOJIEJING,	WORD_MUQIAN, // 190-199
	WORD_NEI, 					WORD_NIXIANG, 			WORD_PINGJUN, 			WORD_PINGMIAN,  			WORD_PUTONG,  			WORD_QIANFANG, 		WORD_QIANFANGWEI, 	WORD_QIANFANGYOU,  		WORD_QIAOSHANG,  		WORD_QIAOXIA, // 200-209
	WORD_QIDIAN, 				WORD_QIDONG, 			WORD_QING, 			WORD_QUANFANGWEI,  		WORD_QUANTIANHOU,  	WORD_QUDING, 			WORD_RADAR, 			WORD_SHANCHU,  			WORD_SHANG,  			WORD_SHEDING, // 210-219
	WORD_SHEYING, 				WORD_SHIQU, 			WORD_SHIQUZONE, 		WORD_SHIWAIWEILIAN, 		WORD_SHUDU,  			WORD_SHUOZHAIWEIZHI,	WORD_TIAOJIANG, 		WORD_TIAOSHENG,  			WORD_TIAOZHENG,  		WORD_WANCHENG, // 220-229
	WORD_WEI, 					WORD_WEIBAOCHI, 		WORD_WEIGUI, 			WORD_WEIXING,  			WORD_WUXIAN,  			WORD_XIAN, 			WORD_XIANG, 			WORD_XIANGZHENG,  		WORD_XIANSHIDUAN,  	WORD_XIANSHU, // 230-239
	WORD_XIANZHAI, 			WORD_XIAOXING, 		WORD_XINGHAO, 		WORD_XINGSHIFANGXIANG,  	WORD_XITONG,  			WORD_XIUXIYIXIA, 		WORD_XUANZHE, 		WORD_YICHAOSHU,  			WORD_YIGUIDINGXINGSHI,WORD_YISHUXINGSHI, // 240-249
	WORD_YOU, 					WORD_YUYING, 			WORD_ZHADAO, 			WORD_ZHAOXIANG,  			WORD_ZHENCHE,  		WORD_ZHIJIAN, 			WORD_ZHUDAO, 			WORD_ZHUOBIAN,  			UNIQUE_NULL0,  			UNIQUE_NULL1, // 250-259 
	#endif
}NAME_INDEX_NUMBER;


#ifndef _THIRDVOICETABLE_C_
extern VOICE_GROUP VoiceGroup;
extern const VOICE_LIB_TABLE voiceLibTable[MAX_NAME_TABLE_NUMBER];
extern const unsigned char voiceCustomNameTable[MAX_NAME_TABLE_NUMBER][MAX_CUSTOM_NAME_LENGTH];
#ifndef _CUSTOMMAIN_TTS_REPLACE_
extern const unsigned short pictureSentenceArray[256][SENTENCE_LENGTH];
extern const unsigned short safeSentenceArray[256][SENTENCE_LENGTH];
extern const unsigned short specialSentenceArray[256][SENTENCE_LENGTH];
#else
extern const unsigned char pictureSentenceArray[256][SENTENCE_LENGTH];
extern const unsigned char safeSentenceArray[256][SENTENCE_LENGTH];
extern const unsigned char specialSentenceArray[256][SENTENCE_LENGTH];
extern const unsigned char FacTestSentenceArray[10][TTS_SENTENCE_LENGTH];
#endif
#ifdef _CUSTOMMAIN_PCM_RUSSIA_VOICE_
extern const unsigned char TestRussia[][MAX_CUSTOM_NAME_LENGTH];
extern const VOICE_LIB_TABLE voiceLibTableRussia[MAX_NAME_TABLE_NUMBER];
extern const unsigned char voiceCustomNameTableRussia[][MAX_CUSTOM_NAME_LENGTH];
extern const unsigned short specialPcmSentenceArrayRussia[][16];
extern const unsigned short picturePcmSentenceArrayRussia[][16];

#endif
#ifdef _CUSTOMMAIN_PCM_VOICE_
extern const unsigned short picturePcmSentenceArray[256][PCM_SENTENCE_LENGTH];
extern const unsigned short safePcmSentenceArray[256][PCM_SENTENCE_LENGTH];
extern const unsigned short specialPcmSentenceArray[256][PCM_SENTENCE_LENGTH];
#endif

#ifdef _CUSTOMMAIN_TTS_VOICE_
extern const unsigned char ttsPictureSentenceArray[256][TTS_SENTENCE_LENGTH];
extern const unsigned char ttsSafeSentenceArray[256][TTS_SENTENCE_LENGTH];
extern const unsigned char ttsSpecialSentenceArray[256][TTS_SENTENCE_LENGTH];

#ifdef _CUSTOMMAIN_NOTE_WAV_TTS_
extern const unsigned char wav16x16DingDong[31216];
extern const unsigned char wav16x16UpSpeed[19422];
#if 0
extern const unsigned char wav16x16Camera[14406];
#else
extern const unsigned char wav16x16Camera[20562];
#endif
extern const unsigned char wav16x16KaBand[28452];
extern const unsigned char wav16x16KBand[23888];
extern const unsigned char wav16x16KuBand[28464];
extern const unsigned char wav16x16LayserRadar[19012];
extern const unsigned char wav16x16XBand[24170];
extern const unsigned char wav16x16Radar[6100];
extern const unsigned char wav16x16Unavailable[11808];
#endif

#endif

#endif

#endif
