package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.FlashTest2;


public class FlashTest2Test extends ActivityInstrumentationTestCase2<FlashTest2> {

    private FlashTest2 mActivity;

    public FlashTest2Test(String name) {
        super(FlashTest2.class);
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