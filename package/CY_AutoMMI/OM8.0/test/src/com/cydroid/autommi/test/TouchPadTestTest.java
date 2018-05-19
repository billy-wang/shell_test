package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.TouchPadTest;


public class TouchPadTestTest extends ActivityInstrumentationTestCase2<TouchPadTest> {

    private TouchPadTest mActivity;

    public TouchPadTestTest(String name) {
        super(TouchPadTest.class);
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