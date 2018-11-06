package cy.com.android.mmitest.test;


import static android.test.ViewAsserts.assertOnScreen;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Button;
import cy.com.android.mmitest.CyMMITest;

public class GnMMITestTest extends ActivityInstrumentationTestCase2<CyMMITest> {

    private CyMMITest mActivity;

    private Button quitBtn;
    private ListView lView;

    public GnMMITestTest(String name) {
        super(CyMMITest.class);
        setName(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();

      //  quitBtn = (Button) mActivity.findViewById(cy.com.android.mmitest.R.id.quit_btn);
      //  lView = (ListView) mActivity.findViewById(cy.com.android.mmitest.R.id.main_listview);

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

   // public final void testFieldsOnScreen() {
   //     final Window window = mActivity.getWindow();
   //     final View origin = window.getDecorView();

   //     assertOnScreen(origin, quitBtn);
    //    assertOnScreen(origin, lView);
    //}

    public  void testPreconditions() {
        assertNotNull(mActivity);
    }
}