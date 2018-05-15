/**
 * 
 */
package gn.com.android.mmitest.test.launch;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Base class for all launch performance Instrumentation classes.
 * 
 * @author diego
 */
public class LaunchPerformanceBase extends Instrumentation {
    public static final String TAG = "LaunchBase";

    protected Bundle mResults;
    protected Intent mIntent;

    /**
     * Constructor.
     */
    public LaunchPerformanceBase() {
        Log.v(TAG, "LaunchBase create");
        mResults = new Bundle();
        mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setAutomaticPerformanceSnapshots();
    }
   
    /**
     * Launches intent {@link #mIntent}, and waits for idle before
     * returning.
     */
    protected void LaunchApp() {
        startActivitySync(mIntent);
        waitForIdleSync();
    }

	@Override
	public void finish(int resultCode, Bundle results) {
		Log.v(TAG, "Test reults = " + results);
		super.finish(resultCode, results);
	}  

}
