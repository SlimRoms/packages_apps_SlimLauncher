#
# Copyright (C) 2013 The Android Open Source Project
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

#
# Build app code.
#
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MANIFEST_FILE := source/AndroidManifest.xml

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-java-files-under, WallpaperPicker/src) \
    $(call all-proto-files-under, protos) \
    $(call all-java-files-under, source/src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/WallpaperPicker/res $(LOCAL_PATH)/res
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 android-support-v7-recyclerview

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/protos/
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_SDK_VERSION := current
LOCAL_PACKAGE_NAME := SlimLauncher
LOCAL_PRIVILEGED_MODULE := true
#LOCAL_CERTIFICATE := shared

LOCAL_OVERRIDES_PACKAGES := Home Launcher2

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
