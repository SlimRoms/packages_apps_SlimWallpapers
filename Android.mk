LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULES_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Performance

LOCAL_PROGUARD_FLAGS := proguard.flags

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

include $(BUILD_PACKAGE)
