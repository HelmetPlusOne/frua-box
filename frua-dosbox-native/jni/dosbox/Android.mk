LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := dosbox_core
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
	$(LOCAL_PATH)/../android/include \
	$(LOCAL_PATH)/../sdl/include 

LOCAL_SRC_FILES := $(shell cd $(LOCAL_PATH) && git ls-files "*.cpp")

include $(BUILD_STATIC_LIBRARY)
