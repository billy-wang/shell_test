package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.FingerDiscern;


public class FingerDiscernTest extends ActivityInstrumentationTestCase2<FingerDiscern> {

    private FingerDiscern mActivity;

    public FingerDiscernTest(String name) {
        super(FingerDiscern.class);
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