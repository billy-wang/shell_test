#ifeq ("$(GN_AUTO_MMI_SUPPORT)", "yes")
ifeq ($(findstring 7.1, $(PLATFORM_VERSION)), 7.1)
$(warning "PLATFORM_VERSION=$(PLATFORM_VERSION)")
ifeq ($(word 1,$(VCHECK_NUMS)),7)
$(warning "VCHECK_NUMS=$(VCHECK_NUMS)")
endif

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
        src/com/caf/fmradio/IFMRadioService.aidl\
        src/com/caf/fmradio/IFMRadioServiceCallbacks.aidl\
        src/com/fingerprints/service/IAuthenticator.aidl \
        src/com/fingerprints/service/IFingerprintSensorTest.aidl \
        src/com/fingerprints/service/IFingerprintSensorTestListener.aidl \
        src/com/fingerprints/service/IFingerprintService.aidl \
        src/com/fingerprints/service/IFingerprintServiceEngineering.aidl \
        src/com/fingerprints/service/IImageSubscriptionListener.aidl \
        src/com/fingerprints/service/ILegacyFingerprint.aidl \
        src/com/fingerprints/service/IUserIdValidListener.aidl \
        src/com/fingerprints/service/IVerifyUserListener.aidl 

LOCAL_JAVA_LIBRARIES := qcnvitems qcrilhook telephony-common
LOCAL_PACKAGE_NAME := AutoMMI
LOCAL_CERTIFICATE := platform

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_MULTILIB := both
LOCAL_STATIC_JAVA_LIBRARIES := tp tpTest
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tp:libs/com.focaltech.tp.comm.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += tpTest:libs/com.focaltech.tp.test.jar

LOCAL_MODULE_TAGS := optional 

include $(BUILD_MULTI_PREBUILT)
#endif
endif
