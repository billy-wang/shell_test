package com.cydroid.autommi.test;


import com.cydroid.autommi.AutoMMI;
import android.test.ApplicationTestCase;

public class AutoMMITest extends ApplicationTestCase<AutoMMI> {


    private AutoMMI mApplication;


    public AutoMMITest() {
        this("GnMMITestApplicationTest");
    }


    public AutoMMITest(String name) {
        super(AutoMMI.class);
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