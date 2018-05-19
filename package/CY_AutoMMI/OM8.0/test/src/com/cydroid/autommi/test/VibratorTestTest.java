package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.VibratorTest;


public class VibratorTestTest extends ActivityInstrumentationTestCase2<VibratorTest> {

    private VibratorTest mActivity;

    public VibratorTestTest(String name) {
        super(VibratorTest.class);
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