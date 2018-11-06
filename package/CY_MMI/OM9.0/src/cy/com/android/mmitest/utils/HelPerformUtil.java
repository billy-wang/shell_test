package cy.com.android.mmitest.utils;

import android.os.Handler;
import cy.com.android.mmitest.bean.OnPerformListen;
import cy.com.android.mmitest.utils.DswLog;

public class HelPerformUtil {
    public static final int delayTime = 100;
    private static final String TAG = "HelPerformUtil";
    private static final HelPerformUtil ourInstance = new HelPerformUtil();

    public static HelPerformUtil getInstance() {
        return ourInstance;
    }

    private Handler handler = new Handler();

    private OnPerformListen onPerformListen;

    private TRunnable mTRunnable = new TRunnable();

    private HelPerformUtil() {
    }


    public void unregisterPerformListen() {
        HelPerformUtil.this.onPerformListen = null;

    }

    public void performDelayed(OnPerformListen onPerformListen,int delaytime) {
        mTRunnable.setOnPerformListen(onPerformListen);
        handler.postDelayed(mTRunnable,delaytime);
    }

    public class TRunnable implements Runnable {

        private OnPerformListen  onPerformListen;

        public void setOnPerformListen(OnPerformListen onPerformListen) {
            TRunnable.this.onPerformListen = onPerformListen;
        }


        @Override
        public void run() {
            HelPerformUtil.this.onPerformListen = TRunnable.this.onPerformListen;
            TRunnable.this.onPerformListen = null;

            if(HelPerformUtil.this.onPerformListen != null) {
                HelPerformUtil.this.onPerformListen.OnButtonPerform();
            }
        }
    }

    public void onDestroy() {
        onPerformListen = null;
        mTRunnable.setOnPerformListen(null);
        handler.removeCallbacks(mTRunnable);
    }
}
