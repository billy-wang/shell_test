package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.OTGTest;


public class OTGTestTest extends ActivityInstrumentationTestCase2<OTGTest> {

    private OTGTest mActivity;

    public OTGTestTest(String name) {
        super(OTGTest.class);
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