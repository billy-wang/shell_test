package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.BackFlashlightCalTest;


public class BackFlashlightCalTestTest extends ActivityInstrumentationTestCase2<BackFlashlightCalTest> {

    private BackFlashlightCalTest mActivity;

    public BackFlashlightCalTestTest(String name) {
        super(BackFlashlightCalTest.class);
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