#ifeq ("$(GN_AUTO_MMI_SUPPORT)", "yes")
ifeq ($(findstring 7, $(PLATFORM_VERSION)), 7)
$(warning "PLATFORM_VERSION=$(PLATFORM_VERSION)")
ifeq ($(word 1,$(VCHECK_NUMS)),7)
$(warning "VCHECK_NUMS=$(VCHECK_NUMS)")
endif

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := telephony-common

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/android/fmradio/IFmRadioService.aidl \
        src/android/hardware/fingerprint/IGnFingerprintServiceReceiver.aidl \
        src/com/fingerprints/service/IAuthenticator.aidl \
        src/com/fingerprints/service/IFingerprintSensorTest.aidl \
        src/com/fingerprints/service/IFingerprintSensorTestListener.aidl \
        src/com/fingerprints/service/IFingerprintService.aidl \
        src/com/fingerprints/service/IFingerprintServiceEngineering.aidl \
        src/com/fingerprints/service/IImageSubscriptionListener.aidl \
        src/com/fingerprints/service/ILegacyFingerprint.aidl \
        src/com/fingerprints/service/IUserIdValidListener.aidl \
        src/com/fingerprints/service/IVerifyUserListener.aidl 

#LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_PACKAGE_NAME := GN_AutoMMI
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#Gionee <GN_BSP_MMI> <lifeilong> <20170516> add for ID 142159 begin
LOCAL_PRIVILEGED_MODULE := true
#Gionee <GN_BSP_MMI> <lifeilong> <20170516> add for ID 142159 end
LOCAL_MULTILIB := both
LOCAL_STATIC_JAVA_LIBRARIES := tp tpTest esemanager
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_STATIC_JAVA_LIBRARIES += emma
LOCAL_AAPT_FLAGS += --debug-mode
LOCAL_JACK_ENABLED := disabled
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 end
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tp:libs/com.focaltech.tp.comm.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += tpTest:libs/com.focaltech.tp.test.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += esemanager:libs/com.gionee.esemanager.jar
LOCAL_MODULE_TAGS := optional 

include $(BUILD_MULTI_PREBUILT)
#endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 begin
ifeq ($(GN_APK_AUTO_APK_TEST), yes)
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
#Gionee <GN_BSP_MMI> <chengq> <20170324> add for ID 91383 end
endif
