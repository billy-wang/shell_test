package gn.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import gn.com.android.mmitest.item.ReceiverTest;


public class ReceiverTestTest extends ActivityInstrumentationTestCase2<ReceiverTest> {

    private ReceiverTest mActivity;

    public ReceiverTestTest(String name) {
        super(ReceiverTest.class);
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