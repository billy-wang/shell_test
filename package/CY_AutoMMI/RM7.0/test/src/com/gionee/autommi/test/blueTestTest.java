package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.blueTest;


public class blueTestTest extends ActivityInstrumentationTestCase2<blueTest> {

    private blueTest mActivity;

    public blueTestTest(String name) {
        super(blueTest.class);
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