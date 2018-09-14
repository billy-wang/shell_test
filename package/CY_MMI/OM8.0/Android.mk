#Chenyee <CY_BSP_MMI> <chengq> <20170919> add for CR 217352 begin
ifeq ("$(CY_APK_CY_MMI_SUPPORT)","yes")
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := CY_MMI
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
#PRODUCT_COPY_FILES += $(foreach file, $(notdir $(wildcard $(LOCAL_PATH)/system/etc/*.xml)), $(LOCAL_PATH)/system/etc/$(file):system/etc/$(file))
LOCAL_MULTILIB := both

include $(BUILD_PREBUILT)
endif
#Chenyee <CY_BSP_MMI> <chengq> <20170919> add for CR 217352 end
