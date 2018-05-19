package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.SensorsCalTest;


public class SensorsCalTestTest extends ActivityInstrumentationTestCase2<SensorsCalTest> {

    private SensorsCalTest mActivity;

    public SensorsCalTestTest(String name) {
        super(SensorsCalTest.class);
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