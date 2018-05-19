package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.ScreenBrightnessTest;


public class ScreenBrightnessTestTest extends ActivityInstrumentationTestCase2<ScreenBrightnessTest> {

    private ScreenBrightnessTest mActivity;

    public ScreenBrightnessTestTest(String name) {
        super(ScreenBrightnessTest.class);
        setName(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPreconditions() {
        assertNotNull(mActivity);
    }

}