package com.cydroid.autommi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.view.View;
import android.graphics.Color;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
import com.cydroid.autommi.TestUtils;

public class ColorTest extends BaseActivity {

    private static final String TAG = "ColorTest";
    private View mColorView;
    private static final String NODE_TYPE_LED_RED_BRIGHTNESS = "NODE_TYPE_LED_RED_BRIGHTNESS";
    private static final String NODE_TYPE_LED_BLUE_BRIGHTNESS = "NODE_TYPE_LED_BLUE_BRIGHTNESS";
    private static final String NODE_TYPE_LED_GREEN_BRIGHTNESS = "NODE_TYPE_LED_GREEN_BRIGHTNESS";
    private static final int ACTION_MSG_RED = 0;
    private static final int ACTION_MSG_GREEN = 1;
    private static final int ACTION_MSG_BLUE = 2;


    private int level;
    private int plugged;
    private boolean isLEDLight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.i(TAG, "AutoMMI ColorTest");
        String action = getIntent().getAction();
        mColorView = new View(this);
        if ("com.gionee.autommi.red".equals(action)) {
            mColorView.setBackgroundColor(Color.RED);
            isLEDLight = true;
            uiHandler.sendMessageDelayed(uiHandler.obtainMessage(ACTION_MSG_RED),100);
        }else if ("com.gionee.autommi.green".equals(action)) {
            mColorView.setBackgroundColor(Color.GREEN);
            isLEDLight = true;
            uiHandler.sendMessageDelayed(uiHandler.obtainMessage(ACTION_MSG_GREEN),100);
        }else if ("com.gionee.autommi.blue".equals(action)) {
            mColorView.setBackgroundColor(Color.BLUE);
            isLEDLight = true;
            uiHandler.sendMessageDelayed(uiHandler.obtainMessage(ACTION_MSG_BLUE),100);
        }else if ("com.gionee.autommi.white".equals(action)) {
            mColorView.setBackgroundColor(Color.WHITE);
        }else if ("com.gionee.autommi.blank".equals(action)) {
            mColorView.setBackgroundColor(Color.BLACK);
        }else if ("com.gionee.autommi.gray".equals(action)) {
            mColorView.setBackgroundColor(0xFF7F7F7F);
        }

        if ("com.gionee.autommi.gray2".equals(action)) {
            setContentView(R.layout.color_test);
        }else {
            setContentView(mColorView);
        }
    }

    @Override
    public void onResume() {
        if (isLEDLight) {
            registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        DswLog.e(TAG,"registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isLEDLight) {
            this.unregisterReceiver(mBroadcastReceiver);
            uiHandler.removeCallbacksAndMessages(null);
            reSetLedLight();
            isLEDLight = false;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DswLog.i(TAG, "onStop()");
        this.finish();
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170321> modify for ID 85901 begin
    private void ledDown(){
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_RED_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,0);
        TestUtils.writeNodeState(ColorTest.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,0);

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170624> add for ID 161083 begin
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
    //Gionee <GN_BSP_MMI> <lifeilong> <20170624> add for ID 161083 end

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            exBatInfo(intent);
        }

    };

    private void exBatInfo(Intent intent) {
        String action = intent.getAction();
        level = intent.getIntExtra("level", 0);
        plugged = intent.getIntExtra("plugged", 0);
        DswLog.e(TAG,"plugged = " + plugged + " , level = " + level);
    }

    private void reSetLedLight() {
        if(plugged != 0){
            if(level == 100){
                greenOpen();
            }else if (level < 15){
                redOpen();
            }else {
                blueOpen();
            }
        }
    }

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_MSG_RED:
                    redOpen();
                    break;
                case ACTION_MSG_GREEN:
                    greenOpen();
                    break;
                case ACTION_MSG_BLUE:
                    blueOpen();
                    break;
            }
        }
    };
}
