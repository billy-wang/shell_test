
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.app.Notification;
import android.app.NotificationManager;

public class ColorTest extends Activity implements OnClickListener {
    private View mColorView;

    private int mCount;

    private Handler mColorHandler;

    private Runnable mColorRunnable;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final int NOTIFICATION_ID = 0;
    private static final int RED = 0xFFFF0000; // red
    private static final int GREEN = 0xFF00FF00;// green
    private static final int BLUE = 0xFF0000FF; // blue
    //Gionee <GN_MMI><lifielong><20161026> modify for 11834begin
    private static final int BLACK = 0xFF000000;//BLACK
    //Gionee <GN_MMI><lifeilong><20161026> modify for 11834 end
    private static final String TAG = "ColorTest";
    NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        mColorHandler = new Handler();
        mColorView = new View(this);
        mColorView.setBackgroundColor(Color.RED);
        setContentView(mColorView);
        showNotification(RED, 1, 0);
    }

    private void showNotification(int color, int on, int off) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
                Activity.NOTIFICATION_SERVICE);
        Notification notice = new Notification.Builder(this).setSmallIcon(R.drawable.icon).setContentTitle("LCD Test")
                .build();
        notice.ledARGB = color;
        notice.ledOnMS = on;
        notice.ledOffMS = off;
        notice.flags |= Notification.FLAG_SHOW_LIGHTS;
        Log.d(TAG,
                "mmicolor" + Integer.toHexString(notice.ledARGB) + "on--" + notice.ledOnMS + "off--" + notice.ledOffMS);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, notice);
        } catch (Exception e) {
            Log.e(TAG, "mNotificationManager.notify error=" + e.getMessage());
        }
    }

    private void showNotification1() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        // if (true == TestUtils.mIsAutoMode) {
        // mCount = 0;
        // mColorHandler.postDelayed(mColorRunnable, 1000);
        // }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            Log.e(TAG, "mCount == " + mCount);
            switch (mCount) {
            case 0:
                mColorView.setBackgroundColor(Color.GREEN);
                showNotification(GREEN, 1, 0);
                mCount++;
                break;
            case 1:
                mColorView.setBackgroundColor(Color.BLUE);
                showNotification(BLUE, 1, 0);
                mCount++;
                break;
            case 2:
                mColorView.setBackgroundColor(Color.BLACK);
		//Gionee <GN_MMI><lifeilong><20161026> modify for 11834 begin
                //showNotification1();
                showNotification(BLACK,1,0);
                //Gionee <GN_MMI><lifeilong><20161026> modify for 11834 end
                mCount++;
                break;
            case 3:
                mColorView.setBackgroundColor(Color.WHITE);
                mCount++;
                break;
            case 4:
                mCount++;
                if (true == TestUtils.mIsAutoMode || true == TestUtils.mIsAutoMode_2
                        || true == TestUtils.mIsAutoMode_3) {
                    ColorTest.this.setContentView(R.layout.common_textview);
                    mRightBtn = (Button) ColorTest.this.findViewById(R.id.right_btn);
                    mRightBtn.setOnClickListener(ColorTest.this);
                    mRightBtn.setEnabled(true);
                    mWrongBtn = (Button) ColorTest.this.findViewById(R.id.wrong_btn);
                    mWrongBtn.setOnClickListener(ColorTest.this);
                    mRestartBtn = (Button) findViewById(R.id.restart_btn);
                    mRestartBtn.setOnClickListener(this);
                } else {
                    finish();
                }
		//Gionee <GN_MMI><lifeilong><20161026> modify for 11834 begin
		showNotification1();
		//Gionee <GN_MMI><lifeilong><20161026> modify for 11834 end
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }

        case R.id.wrong_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.wrongPress(TAG, this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.restart(this, TAG);
            break;
        }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
