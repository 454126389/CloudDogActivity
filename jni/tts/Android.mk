# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := Aisound

BUILD_CODE := 1
ifeq ($(BUILD_CODE), 0)
LOCAL_SRC_FILES := libAisound.so
include $(PREBUILT_SHARED_LIBRARY)
else
LOCAL_LDLIBS := -L$(LOCAL_PATH)/Lib -laisound5 -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH)
LOCAL_SRC_FILES := AisoundJni.c CommonPCM.c
#测试时打开此行
#TARGET_CFLAGS += -D_DEBUG
TARGET_CFLAGS += -g
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)
endif

