package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.HeadSetLoopTest;


public class HeadSetLoopTestTest extends ActivityInstrumentationTestCase2<HeadSetLoopTest> {

    private HeadSetLoopTest mActivity;

    public HeadSetLoopTestTest(String name) {
        super(HeadSetLoopTest.class);
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