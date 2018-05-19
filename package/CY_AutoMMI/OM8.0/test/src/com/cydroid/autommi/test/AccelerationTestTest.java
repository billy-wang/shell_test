package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.AccelerationTest;


public class AccelerationTestTest extends ActivityInstrumentationTestCase2<AccelerationTest> {

    private AccelerationTest mActivity;

    public AccelerationTestTest(String name) {
        super(AccelerationTest.class);
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