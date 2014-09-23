#include <stdio.h>
#include "common.h"

#define __USER_SETTING_C__
#include "user_setting.h"

void FillSetting(char *buf)
{
	user_setting.use_flag = buf[ 0 ];
	user_setting.pic_safe_mode = buf[ 1 ];
	user_setting.unit_set = buf[ 2 ];
	user_setting.city_mode_set = buf[ 3 ];
	user_setting.time_zone_flag = buf[ 4 ];
	user_setting.time_zone_min_flag = buf[ 5 ];
	user_setting.speed_re_flag = buf[ 6 ];
	user_setting.voice_long_flag = buf[ 7 ];
	user_setting.time_24_12_sw = buf[ 8 ];
	user_setting.cruise_speed = buf[ 9 ];
	user_setting.voice_mode = buf[ 10 ];
	user_setting.RD_mute_set = buf[ 11 ];
	user_setting.voice_vol_tmp = buf[ 12 ];
	user_setting.rd_sensitivity = buf[ 13 ];
	user_setting.auto_light_time = buf[ 14 ];
	user_setting.auto_dim_time = buf[ 15 ];
	user_setting.rd_on_off_set = buf[ 16 ];
	user_setting.special_voice_set = buf[ 17 ];
	user_setting.x_band_set_control = buf[ 18 ];
	user_setting.ku_band_set_control = buf[ 19 ];
	user_setting.ka_band_set_control = buf[ 20 ];
	user_setting.k_band_set_control = buf[ 21 ];
	user_setting.laser_set_control = buf[ 22 ];
	user_setting.radar_light_time = buf[ 23 ];
	user_setting.radar_dim_time = buf[ 24 ];
	user_setting.int_camera_control = buf[ 25 ];
	user_setting.rd_mute_auto_control = buf[ 26 ];

	user_setting.total_distance = buf[ 27 ] + ( buf[ 28 ] << 8 ) + ( buf[ 29 ] << 16 ) + ( buf[ 30 ] << 24 );
	#if 0
	user_setting.trip_distance = buf[ 31 ] + ( buf[ 32 ] << 8 ) + ( buf[ 33 ] << 16 ) + ( buf[ 34 ] << 24 );
	#else /* if 0 */
	user_setting.trip_distance = 0;
	#endif /* if 0 */
	user_setting.max_speed_mem = buf[ 35 ] + ( buf[ 36 ] << 8 );

	user_setting.max_spd_date.year = buf[ 37 ] + ( buf[ 38 ] << 8 );
	user_setting.max_spd_date.month = buf[ 39 ];
	user_setting.max_spd_date.day = buf[ 40 ];

	user_setting.max_spd_time.time_hour = buf[ 41 ];
	user_setting.max_spd_time.time_min = buf[ 42 ];
	user_setting.max_spd_time.time_sec = buf[ 43 ];
	user_setting.GPS_on_off_set = buf[ 44 ];

	if( user_setting.time_24_12_sw == 0 )
	{
		user_setting.time_24_12_sw = 1; //change to 12
	}
}
