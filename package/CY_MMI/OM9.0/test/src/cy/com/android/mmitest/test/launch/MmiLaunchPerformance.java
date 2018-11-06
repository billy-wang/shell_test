package cy.com.android.mmitest.test.launch;

import android.app.Activity;
import android.os.Bundle;

/**
 * Instrumentation class for {@link TemperatureConverterActivity} launch performance testing.
 */
public class MmiLaunchPerformance extends
        LaunchPerformanceBase {
    /**
     * Constructor.
     */
    public MmiLaunchPerformance() {
        super();
    }

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        mIntent.setClassName("cy.com.android.mmitest",
                 "cy.com.android.mmitest.CyMMITest");
        start();
    }

    /**
     * Calls LaunchApp and finish.
     */
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
