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
LOCAL_MODULE    := serial
LOCAL_SRC_FILES := libserial.so
include $(PREBUILT_SHARED_LIBRARY)
else
LOCAL_MODULE    := serial
LOCAL_SRC_FILES := SerialPort.c
LOCAL_LDLIBS    := -llog -ldl
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH)
include $(BUILD_SHARED_LIBRARY)
endif