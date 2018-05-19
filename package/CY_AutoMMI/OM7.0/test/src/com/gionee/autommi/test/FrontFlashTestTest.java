package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.FrontFlashTest;


public class FrontFlashTestTest extends ActivityInstrumentationTestCase2<FrontFlashTest> {

    private FrontFlashTest mActivity;

    public FrontFlashTestTest(String name) {
        super(FrontFlashTest.class);
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