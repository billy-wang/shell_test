
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
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
//Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
import gn.com.android.mmitest.TestUtils;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Message;
//Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end
import android.content.Intent;

public class ColorTest extends Activity implements OnClickListener {
    private View mColorView;

    private int mCount;

    private Handler mColorHandler;

    private Runnable mColorRunnable;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final int NOTIFICATION_ID = 0;
    private static final int RED = 0xFFFF0000; //red
    private static final int GREEN = 0xFF00FF00;//green
    private static final int BLUE = 0xFF0000FF; //blue
    private static final String TAG = "ColorTest";
    NotificationManager mNotificationManager;

    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
    private static final String NODE_TYPE_LED_RED_BRIGHTNESS = "NODE_TYPE_LED_RED_BRIGHTNESS";
    private static final String NODE_TYPE_LED_BLUE_BRIGHTNESS = "NODE_TYPE_LED_BLUE_BRIGHTNESS";
    private static final String NODE_TYPE_LED_GREEN_BRIGHTNESS = "NODE_TYPE_LED_GREEN_BRIGHTNESS";
    private Intent lastBatteryData;

    private int level;
    private int plugged;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end

    private boolean colorFlag = false;
    private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20151215 modify for CR01609753 end
        mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        mColorHandler = new Handler();
        mColorView = new View(this);
        mColorView.setBackgroundColor(Color.RED);
        setContentView(mColorView);
        it = this.getIntent();
        if(it != null){
            colorFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"colorFlag = " + colorFlag); 
        if(colorFlag){
            TestUtils.asResult(TAG,"","2");
        }
        //Gionee zhangxiaowei 20130523 add for CR00818649 start
        //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
        showNotification(RED, 1, 0);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end
        //Gionee zhangxiaowei 20130523 add for CR00818649 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170328> modify for ID 94767 begin
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.e(TAG,"registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));");
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 98181 begin
        //redOpen();
        uiHandler.sendEmptyMessage(0);
        uiHandler.sendEmptyMessageDelayed(1,100);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 98181 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170328> modify for ID 94767 end

    }

    //Gionee zhangxiaowei 20130523 add for CR00818649 start
    private void showNotification(int color, int on, int off) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        Notification notice = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("LCD Test")
                //Gionee <GN_BSP_MMI><lifeilong><20170207> modify for ID 65975 begin
                .setDefaults(Notification.DEFAULT_LIGHTS)
                //Gionee <GN_BSP_MMI> <lifeilong> <20170207> modify for ID 68703 begin
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0})
                //Gionee <GN_BSP_MMI> <lifeilong> <20170207> modify for ID 68703 end
                //Gionee <GN_BSP_MMI><lifeilong><20170207> modify for ID 65975 end
                .build();
        notice.ledARGB = color;
        notice.ledOnMS = on;
        notice.ledOffMS = off;
        notice.flags |= Notification.FLAG_SHOW_LIGHTS;
        Log.d(TAG, "zhangxiaowei mmicolor" + Integer.toHexString(notice.ledARGB) + "on--" + notice.ledOnMS + "off--" + notice.ledOffMS);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, notice);
        } catch (Exception e) {
            Log.e(TAG, "mNotificationManager.notify error=" + e.getMessage());
        }
    }

    private void showNotification1() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
    private void ledDown(){
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_RED_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,0);

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170624>  for ID 161083 begin
    private void redOpen(){
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_RED_BRIGHTNESS,255);
    }
    private void blueOpen(){
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_RED_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,255);
    }
    private void greenOpen(){
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_RED_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,255);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170624>  for ID 161083 end
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;
            exBatInfo(intent);
        }

    };
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    exBatInfo(lastBatteryData);
                    this.sendEmptyMessageDelayed(0, 10);
                    break;
          //Gionee <GN_BSP_MMI> <lifeilong> <20170327> modify for ID 94767 begin
                case 1:
                     redOpen();
                     break;
                case 2:
                     greenOpen();
                     break;
                case 3:
                     blueOpen();
                     break;
          //Gionee <GN_BSP_MMI> <lifeilong> <20170327> modify for ID 94767 end

            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end

    // Gionee zhangxiaowei 20130615 add for CR00825877  end


    //Gionee zhangxiaowei 20130615 add for CR00825877  start

    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
    private void exBatInfo(Intent intent) {
        // TODO Auto-generated method stub
        if (null == intent)
            return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            level = intent.getIntExtra("level", 0);
            plugged = intent.getIntExtra("plugged", 0);
        }
        //Log.e(TAG,"plugged = " + plugged + " , level = " + level);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end



    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"onStart");
        //Gionee <GN_BSP_MMI> <lifeilong> <20170426> modify for ID 122607 begin
//    redOpen();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170426> modify for ID 122607 end
//        if (true == TestUtils.mIsAutoMode) {
//            mCount = 0;
//            mColorHandler.postDelayed(mColorRunnable, 1000);
//        }
    }
	@Override
	public void onResume() {
            super.onResume();
            Log.e(TAG,"onResume");
	}
    //Gionee <GN_BSP_MMI> <lifeilong> <20170328> modify for ID 94767 begin


    //Gionee <GN_BSP_MMI> <lifeilong> <20170727> modify for ID 175395 begin
    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG,"onStop");
    //Gionee <GN_BSP_MMI> <lifeilong> <20170727> modify for ID 175395 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169287 begin
        try{
            this.unregisterReceiver(mBroadcastReceiver);
            uiHandler.removeMessages(0);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        if(colorFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }        
        //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169287 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170330> modify for ID 99812 begin
        /*if(plugged != 0){
            if(level == 100){
                greenOpen();
            }else if (level < 15){
                redOpen();
            }else {
                blueOpen();
            }
        }else {
            ledDown();
        }*/
        //Gionee <GN_BSP_MMI> <lifeilong> <20170330> modify for ID 99812 end
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170328> modify for ID 94767 end
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            Log.e("lich", "mCount == " + mCount);
            switch (mCount) {
                case 0:
                    mColorView.setBackgroundColor(Color.GREEN);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
                    greenOpen();
                    //uiHandler.sendEmptyMessageDelayed(2,50);
                    //Gionee zhangxiaowei 20130523 add for CR00818649 start
                    showNotification(GREEN, 1, 0);
                    //Gionee zhangxiaowei 20130523 add for CR00818649 end
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end
                    mCount++;
                    break;
                case 1:
                    mColorView.setBackgroundColor(Color.BLUE);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
                    blueOpen();
                    //uiHandler.sendEmptyMessageDelayed(3,50);
                    //Gionee zhangxiaowei 20130523 add for CR00818649 start
                    showNotification(BLUE, 1, 0);
                    //Gionee zhangxiaowei 20130523 add for CR00818649 end
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
                    mCount++;
                    break;
                // Gionee xiaolin 20121222 modify for CR00753039  start
                case 2:
                    mColorView.setBackgroundColor(Color.BLACK);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
                    ledDown();
                    //Gionee zhangxiaowei 20130523 add for CR00818649 start
                    showNotification1();
                    //Gionee zhangxiaowei 20130523 add for CR00818649 end
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end
                    mCount++;
                    break;
                case 3:
                    //Gionee xiaolin 20121106 add for CR00725238 start
                    mColorView.setBackgroundColor(Color.WHITE);
                    mCount++;
                    break;
                case 4:
                    //Gionee xiaolin 20121106 add for CR00725238 end
                    // Gionee xiaolin 20121222 modify for CR00753039  end
                    mCount++;
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
                    if(plugged != 0){
                        if(level == 100){
                            greenOpen();
                        }else if (level < 15){
                            redOpen();
                        }else {
                            blueOpen();
                        }
                    }
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 end
                    if (true == TestUtils.mIsAutoMode || true == TestUtils.mIsAutoMode_2 || true == colorFlag) {
                        ColorTest.this.setContentView(R.layout.common_textview);
                        mRightBtn = (Button) ColorTest.this.findViewById(R.id.right_btn);
                        mRightBtn.setOnClickListener(ColorTest.this);
                        mRightBtn.setEnabled(true);
                        mWrongBtn = (Button) ColorTest.this.findViewById(R.id.wrong_btn);
                        mWrongBtn.setOnClickListener(ColorTest.this);
                        mRestartBtn = (Button) findViewById(R.id.restart_btn);
                        if(colorFlag){
                            mRestartBtn.setVisibility(View.INVISIBLE);
                        }                        
                        mRestartBtn.setOnClickListener(this);
                    } else {
                        finish();
                    }
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
                if(colorFlag){
                    TestUtils.asResult(TAG,"","1");
                }                 
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(colorFlag){
                    TestUtils.asResult(TAG,"","0");
                }                 
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
