package gn.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import gn.com.android.mmitest.item.GyroscopeTest;


public class GyroscopeTestTest extends ActivityInstrumentationTestCase2<GyroscopeTest> {

    private GyroscopeTest mActivity;

    public GyroscopeTestTest(String name) {
        super(GyroscopeTest.class);
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