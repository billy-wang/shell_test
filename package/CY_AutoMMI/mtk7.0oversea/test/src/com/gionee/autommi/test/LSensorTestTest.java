package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.LSensorTest;


public class LSensorTestTest extends ActivityInstrumentationTestCase2<LSensorTest> {

    private LSensorTest mActivity;

    public LSensorTestTest(String name) {
        super(LSensorTest.class);
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