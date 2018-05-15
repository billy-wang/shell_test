package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.FingerPrintsTest;


public class FingerPrintsTestTest extends ActivityInstrumentationTestCase2<FingerPrintsTest> {

    private FingerPrintsTest mActivity;

    public FingerPrintsTestTest(String name) {
        super(FingerPrintsTest.class);
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