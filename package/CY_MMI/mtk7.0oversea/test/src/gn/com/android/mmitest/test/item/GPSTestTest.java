package gn.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import gn.com.android.mmitest.item.GPSTest;


public class GPSTestTest extends ActivityInstrumentationTestCase2<GPSTest> {

    private GPSTest mActivity;

    public GPSTestTest(String name) {
        super(GPSTest.class);
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