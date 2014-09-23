#ifndef __USER_SETTING_H__
#define __USER_SETTING_H__

#ifdef __USER_SETTING_C__
#define __USER_SETTING_DEC__
#else
#define __USER_SETTING_DEC__ extern
#endif

//GPS time----------------------------------
typedef struct
{
    char time_hour;
    char time_min;
    char time_sec;
}GPS_TIME;
//-----------------------------------------

//GPS date---------------------------------
typedef struct
{
    unsigned int year;
    unsigned char month;
    unsigned char day;
}GPS_DATE;

typedef struct
{
    unsigned char use_flag;    //已设定否?
    char pic_safe_mode;        //0->safe spd, 1->camera spd, 2->fix camera spd, 3->safe, 4->camera, 5->fix camera
    signed char unit_set;            //0->公里, 1->英里, 2->海浬
    char city_mode_set;        //0->全区, 1->市区, 2->高速

    char time_zone_flag;       //时区--小时
    char time_zone_min_flag;   //时区--分
    char speed_re_flag;        //速度重建
    char voice_long_flag;      //0->长语句, 1->短语句
    //unsigned char max_speed_mem; //最高车速
    char time_24_12_sw;        //0->24制, 1->12制
    char cruise_speed;         //巡航速度(用于收集座标使用20->80m, 30->150m, 40~60->300m, 70~80->500m, 90->700m, 100~120->1000m)
    //char over_speed_alarm;     //超速警示(> 警告)
    char voice_mode;           //0->语音模式, 1->音乐模式, 2->静音模式
    char RD_mute_set;          //0->OFF(静音),1~15->10~150公里/时以下雷达静音
    unsigned char voice_vol_tmp; //音量
    char rd_sensitivity;       //雷达感度 0->超高, 1->高 , 2->中, 3->低
    char auto_light_time;      //自动明亮时间
    char auto_dim_time;        //自动微暗时间
    char rd_on_off_set;        //雷达开关 0->on & offline alarm, 1->on no offline alarm, 2->off

    char GPS_on_off_set;        //GPS开关 0->on , 1->off
    char special_voice_set;    //special voice 0->off 1->on
    char x_band_set_control;   //0->off, other->on
    char ku_band_set_control;  //0->off, other->on
    char ka_band_set_control;  //0->off, other->on
    char k_band_set_control;   //0->off, other->on
    char laser_set_control;    //0->off, other->on
    char radar_light_time;     //fix radar start work time
    char radar_dim_time;       //fix radar not work time
    char int_camera_control;   //inter camera control 0->off, 1->on
    char rd_mute_auto_control; //rd mute auto 0->on, other->off

    unsigned long total_distance; //总计旅程
    unsigned long trip_distance; //累计旅程
    unsigned int max_speed_mem; //最高车速//20090216change
    GPS_DATE max_spd_date;     //最高车速日期
    GPS_TIME max_spd_time;     //最高车速时间
}setting_data;

__USER_SETTING_DEC__ setting_data user_setting;
__USER_SETTING_DEC__ void FillSetting(char *buf);
#endif
