package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.FlashTest;


public class FlashTestTest extends ActivityInstrumentationTestCase2<FlashTest> {

    private FlashTest mActivity;

    public FlashTestTest(String name) {
        super(FlashTest.class);
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