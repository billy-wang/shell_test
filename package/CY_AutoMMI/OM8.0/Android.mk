#Chenyee <CY_BSP_MMI> <chengq> <20170919> add for CR 217352 begin
ifeq ("$(CY_APK_AutoMMI_SUPPORT)","yes")
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := AutoMMI
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)


include $(BUILD_PREBUILT)
endif
#Chenyee <CY_BSP_MMI> <chengq> <20170919> add for CR 217352 begin
