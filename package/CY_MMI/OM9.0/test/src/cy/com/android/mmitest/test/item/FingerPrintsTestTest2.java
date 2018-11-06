package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.FingerPrintsTest2;


public class FingerPrintsTestTest2 extends ActivityInstrumentationTestCase2<FingerPrintsTest2> {

    private FingerPrintsTest2 mActivity;

    public FingerPrintsTestTest2(String name) {
        super(FingerPrintsTest2.class);
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