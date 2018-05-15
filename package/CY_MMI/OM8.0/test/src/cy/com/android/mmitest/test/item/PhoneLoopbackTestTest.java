package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.PhoneLoopbackTest;


public class PhoneLoopbackTestTest extends ActivityInstrumentationTestCase2<PhoneLoopbackTest> {

    private PhoneLoopbackTest mActivity;

    public PhoneLoopbackTestTest(String name) {
        super(PhoneLoopbackTest.class);
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