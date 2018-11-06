package cy.com.android.mmitest.test.item;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import cy.com.android.mmitest.item.BluetoothTest;


public class BluetoothTestTest extends ActivityInstrumentationTestCase2<BluetoothTest> {

    private BluetoothTest mActivity;

    public BluetoothTestTest(String name) {
        super(BluetoothTest.class);
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