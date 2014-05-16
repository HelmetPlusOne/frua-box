LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := dosbox_android
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -Wall

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/../dosbox/include \
	$(LOCAL_PATH)/../sdl/include 

LOCAL_LDLIBS := -ljnigraphics -llog

LOCAL_SRC_FILES := $(shell cd $(LOCAL_PATH) && git ls-files "*.c")

LOCAL_STATIC_LIBRARIES := dosbox_core

include $(BUILD_SHARED_LIBRARY)

