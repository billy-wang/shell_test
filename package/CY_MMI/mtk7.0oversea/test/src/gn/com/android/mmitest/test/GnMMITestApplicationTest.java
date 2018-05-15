package gn.com.android.mmitest.test;


import gn.com.android.mmitest.GnMMITestApplication;
import android.test.ApplicationTestCase;

public class GnMMITestApplicationTest extends ApplicationTestCase<GnMMITestApplication> {


    private GnMMITestApplication mApplication;


    public GnMMITestApplicationTest() {
        this("GnMMITestApplicationTest");
    }


    public GnMMITestApplicationTest(String name) {
        super(GnMMITestApplication.class);
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