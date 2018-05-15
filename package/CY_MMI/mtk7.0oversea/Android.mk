#ifeq ("$(GN_MMI_TEST_SUPPORT)","yes")
ifeq ($(findstring 7, $(PLATFORM_VERSION)), 7)
$(warning "PLATFORM_VERSION=$(PLATFORM_VERSION)")
ifeq ($(word 1,$(VCHECK_NUMS)),7)
$(warning "VCHECK_NUMS=$(VCHECK_NUMS)")
endif

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := GN_MMI
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src)\
	src/com/android/fmradio/IFmRadioService.aidl \
	src/com/mediatek/fmradio/IFmRadioService.aidl \
	src/com/mediatek/fmradio/IFmRadioServiceCallback.aidl \
        src/com/fingerprints/service/IAuthenticator.aidl \
        src/com/fingerprints/service/IFingerprintSensorTest.aidl \
        src/com/fingerprints/service/IFingerprintSensorTestListener.aidl \
        src/com/fingerprints/service/IFingerprintService.aidl \
        src/com/fingerprints/service/IFingerprintServiceEngineering.aidl \
        src/com/fingerprints/service/IImageSubscriptionListener.aidl \
        src/com/fingerprints/service/ILegacyFingerprint.aidl \
        src/com/fingerprints/service/IUserIdValidListener.aidl \
        src/com/fingerprints/service/IVerifyUserListener.aidl

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := tp tpTest ZCalib
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_STATIC_JAVA_LIBRARIES += emma
LOCAL_AAPT_FLAGS += --debug-mode
LOCAL_JACK_ENABLED := disabled
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 end

# Gionee <Oversea_Bug> <chengq> <20161219> add for ID 50212 begin
#LOCAL_JNI_SHARED_LIBRARIES := libL
# Gionee <Oversea_Bug> <chengq> <20161219> add for ID 50212 end
#LOCAL_JNI_SHARED_LIBRARIES := libL libfp_gf_mp
PRODUCT_COPY_FILES += $(foreach file, $(notdir $(wildcard $(LOCAL_PATH)/system/etc/*.xml)), $(LOCAL_PATH)/system/etc/$(file):system/etc/$(file))

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#Gionee <Oversea_Bug> <chengq> <20170316> modify for ID 85574 begin
LOCAL_MULTILIB := both
LOCAL_PREBUILT_JNI_LIBS += @lib/arm64-v8a/libL.so
LOCAL_PREBUILT_JNI_LIBS += @lib/armeabi/libL.so
#Gionee <Oversea_Bug> <chengq> <20170316> modify for ID 85574 end
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tp:libs/com.focaltech.tp.comm.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += tpTest:libs/com.focaltech.tp.test.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += ZCalib:libs/ZCalib.jar
#LOCAL_MODULE_TAGS := optional
#LOCAL_PREBUILT_LIBS := libL:libs/arm64-v8a/libL.so
# Gionee <Oversea_Bug> <chengq> <20161219> add for ID 50212 begin
#LOCAL_PREBUILT_LIBS := libL:libs/arm64-v8a/libL.so
# Gionee <Oversea_Bug> <chengq> <20161219> add for ID 50212 end
#LOCAL_PREBUILT_LIBS +=libfp_gf_mp:libs/arm64-v8a/libfp_gf_mp.so

include $(BUILD_MULTI_PREBUILT)
#endif

#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
endif
