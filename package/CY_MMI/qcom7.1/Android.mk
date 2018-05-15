#ifeq ("$(GN_MMI_TEST_SUPPORT)","yes")
ifeq ($(findstring 7.1, $(PLATFORM_VERSION)), 7.1)
$(warning "PLATFORM_VERSION=$(PLATFORM_VERSION)")
ifeq ($(word 1,$(VCHECK_NUMS)),7)
$(warning "VCHECK_NUMS=$(VCHECK_NUMS)")
endif

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src)\
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

PRODUCT_COPY_FILES += $(foreach file, $(notdir $(wildcard $(LOCAL_PATH)/system/etc/*.xml)), $(LOCAL_PATH)/system/etc/$(file):system/etc/$(file)) 

LOCAL_PACKAGE_NAME := GN_MMI
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
##################################################
#引入第三方jar方法
#include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libarity:arity-2.1.2.jar

#include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
endif
