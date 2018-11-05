#ifeq ("$(GN_AUTO_MMI_SUPPORT)", "yes")
ifeq ($(findstring 8, $(PLATFORM_VERSION)), 8)
$(warning "PLATFORM_VERSION=$(PLATFORM_VERSION)")
ifeq ($(word 1,$(VCHECK_NUMS)),8)
$(warning "VCHECK_NUMS=$(VCHECK_NUMS)")
endif

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/android/fmradio/IFmRadioService.aidl \
	src/com/mediatek/fmradio/IFmRadioService.aidl \
	src/com/mediatek/fmradio/IFmRadioServiceCallback.aidl \
    src/android/hardware/fingerprint/ICyFingerprintServiceReceiver.aidl\
        src/tv/peel/service/smartir/IReceiveCallback.aidl \
        src/tv/peel/service/smartir/ISmartIrService.aidl \
        src/tv/peel/service/smartir/ITransmitCallback.aidl

#LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_PACKAGE_NAME := AutoMMI
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_MULTILIB := both
LOCAL_STATIC_JAVA_LIBRARIES := tp tpTest
LOCAL_STATIC_JAVA_LIBRARIES += vendor.mediatek.hardware.nvram-V1.0-java-static

LOCAL_PROGUARD_ENABLED := disabled
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
LOCAL_STATIC_JAVA_LIBRARIES += emma
LOCAL_AAPT_FLAGS += --debug-mode
LOCAL_JACK_ENABLED := disabled
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 end
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tp:libs/com.focaltech.tp.comm.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += tpTest:libs/com.focaltech.tp.test.jar

LOCAL_MODULE_TAGS := optional 

include $(BUILD_MULTI_PREBUILT)
#endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 end
endif
