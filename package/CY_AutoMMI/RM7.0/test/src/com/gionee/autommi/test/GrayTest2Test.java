package com.gionee.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.gionee.autommi.GrayTest2;


public class GrayTest2Test extends ActivityInstrumentationTestCase2<GrayTest2> {

    private GrayTest2 mActivity;

    public GrayTest2Test(String name) {
        super(GrayTest2.class);
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