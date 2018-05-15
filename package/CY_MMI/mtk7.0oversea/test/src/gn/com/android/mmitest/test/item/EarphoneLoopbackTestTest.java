package gn.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import gn.com.android.mmitest.item.EarphoneLoopbackTest;


public class EarphoneLoopbackTestTest extends ActivityInstrumentationTestCase2<EarphoneLoopbackTest> {

    private EarphoneLoopbackTest mActivity;

    public EarphoneLoopbackTestTest(String name) {
        super(EarphoneLoopbackTest.class);
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