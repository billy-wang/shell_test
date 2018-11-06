package cy.com.android.mmitest.test;


import cy.com.android.mmitest.CyMMITestApplication;
import android.test.ApplicationTestCase;

public class GnMMITestApplicationTest extends ApplicationTestCase<CyMMITestApplication> {


    private CyMMITestApplication mApplication;


    public GnMMITestApplicationTest() {
        this("GnMMITestApplicationTest");
    }


    public GnMMITestApplicationTest(String name) {
        super(CyMMITestApplication.class);
        setName(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        createApplication();
        mApplication = getApplication();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testPreconditions() {
        assertNotNull(mApplication);
    }

}