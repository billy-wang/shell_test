package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.GyroSensorTest;


public class GyroSensorTestTest extends ActivityInstrumentationTestCase2<GyroSensorTest> {

    private GyroSensorTest mActivity;

    public GyroSensorTestTest(String name) {
        super(GyroSensorTest.class);
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