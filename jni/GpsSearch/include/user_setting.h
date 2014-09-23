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
    unsigned char use_flag;    //���趨��?
    char pic_safe_mode;        //0->safe spd, 1->camera spd, 2->fix camera spd, 3->safe, 4->camera, 5->fix camera
    signed char unit_set;            //0->����, 1->Ӣ��, 2->����
    char city_mode_set;        //0->ȫ��, 1->����, 2->����

    char time_zone_flag;       //ʱ��--Сʱ
    char time_zone_min_flag;   //ʱ��--��
    char speed_re_flag;        //�ٶ��ؽ�
    char voice_long_flag;      //0->�����, 1->�����
    //unsigned char max_speed_mem; //��߳���
    char time_24_12_sw;        //0->24��, 1->12��
    char cruise_speed;         //Ѳ���ٶ�(�����ռ�����ʹ��20->80m, 30->150m, 40~60->300m, 70~80->500m, 90->700m, 100~120->1000m)
    //char over_speed_alarm;     //���پ�ʾ(> ����)
    char voice_mode;           //0->����ģʽ, 1->����ģʽ, 2->����ģʽ
    char RD_mute_set;          //0->OFF(����),1~15->10~150����/ʱ�����״ﾲ��
    unsigned char voice_vol_tmp; //����
    char rd_sensitivity;       //�״�ж� 0->����, 1->�� , 2->��, 3->��
    char auto_light_time;      //�Զ�����ʱ��
    char auto_dim_time;        //�Զ�΢��ʱ��
    char rd_on_off_set;        //�״￪�� 0->on & offline alarm, 1->on no offline alarm, 2->off

    char GPS_on_off_set;        //GPS���� 0->on , 1->off
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

    unsigned long total_distance; //�ܼ��ó�
    unsigned long trip_distance; //�ۼ��ó�
    unsigned int max_speed_mem; //��߳���//20090216change
    GPS_DATE max_spd_date;     //��߳�������
    GPS_TIME max_spd_time;     //��߳���ʱ��
}setting_data;

__USER_SETTING_DEC__ setting_data user_setting;
__USER_SETTING_DEC__ void FillSetting(char *buf);
#endif
