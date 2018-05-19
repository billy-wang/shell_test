#ifeq ("$(GN_AUTO_MMI_SUPPORT)", "yes")
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CERTIFICATE := platform

LOCAL_MODULE_TAGS := tests
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_JACK_ENABLED := disabled
LOCAL_JAVA_LIBRARIES := android.test.runner

LOCAL_STATIC_JAVA_LIBRARIES := easymock hamcrest-core \
	hamcrest-integration hamcrest-library robotium-solo

# Include all test java files.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := GN_MMITest
LOCAL_AAPT_FLAGS += --debug-mode
LOCAL_INSTRUMENTATION_FOR := AutoMMI
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	easymock:libs/easymock-2.5.2.jar \
	hamcrest-core:libs/hamcrest-core-1.2-android.jar \
	hamcrest-integration:libs/hamcrest-integration-1.2-android.jar \
	hamcrest-library:libs/hamcrest-library-1.2-android.jar \
	robotium-solo:libs/robotium-solo-5.2.1.jar
include $(BUILD_MULTI_PREBUILT)
#endif
