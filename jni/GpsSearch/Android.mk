#
# Copyright 2009 Cedric Priscal
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License. 
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

BUILD_CODE := 1

ifeq ($(BUILD_CODE), 0)
	LOCAL_MODULE    := dog
	LOCAL_SRC_FILES := libdog.so
	include $(PREBUILT_SHARED_LIBRARY)
else
	ifeq ($(BUILD_CODE), 2)
		LOCAL_MODULE    := dog
		LOCAL_SRC_FILES := jni_test.c thirdvoicetable.c
		LOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH)
		include $(BUILD_SHARED_LIBRARY)
	else
		LOCAL_MODULE    := dog
		LOCAL_SRC_FILES := thirdvoicetable.c Thirdvoice.c jni.c DataBase.c DataBaseRecord.c \
							GpsData.c PointSearch.c BasicDataBase.c user_setting.c UpdateDataBase.c SelfDataBase.c radar.c \
							AlarmProcess.c ComPoint.c SafePoint.c SelfPoint.c RegionPoint.c Display.c RadarPoint.c Fifo.c Mcu.c \
							EnterExitDataBase.c _string.c
		
		LOCAL_STATIC_LIBRARIES := libsql
		LOCAL_LDLIBS    := -llog -ldl
		
		LOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH) $(LOCAL_PATH)/../sqlite/
		include $(BUILD_SHARED_LIBRARY)
	endif
endif