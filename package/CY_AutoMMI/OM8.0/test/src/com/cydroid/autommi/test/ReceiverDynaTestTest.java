package com.cydroid.autommi.test;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import com.cydroid.autommi.ReceiverDynaTest;


public class ReceiverDynaTestTest extends ActivityInstrumentationTestCase2<ReceiverDynaTest> {

    private ReceiverDynaTest mActivity;

    public ReceiverDynaTestTest(String name) {
        super(ReceiverDynaTest.class);
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