package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import java.util.Timer;
import java.util.TimerTask;
//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end

public class redTest extends BaseActivity {

	private static final String TAG = "redTest";
    private View mColorView;
    private Timer mTimer;
	private static final int DELAY_TIME = 250;
	//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
    private Intent lastBatteryData;
    private int level;
    private int plugged;
	//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "ritColor : ");
        mColorView = new View(this);
		mColorView.setBackgroundColor(Color.RED);
        setContentView(mColorView);
		//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
        uiHandler.sendEmptyMessage(0);
        closeAllLeds();
        openRed();
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end
	}

	//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;
            exBatInfo(intent);
        }

    };
	
    private void exBatInfo(Intent intent) {
        // TODO Auto-generated method stub
        if (null == intent)
            return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            level = intent.getIntExtra("level", 0);
            plugged = intent.getIntExtra("plugged", 0);
        }
    }
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    exBatInfo(lastBatteryData);
                    this.sendEmptyMessageDelayed(0, 500);
                    break;
            }
        }
    };
    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end
    protected void onStart(){
		super.onStart();

    }
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170505> modify for ID 130787 begin
        this.unregisterReceiver(mBroadcastReceiver);
        uiHandler.removeMessages(0);
        closeAllLeds();
		if(plugged != 0){
			if(level == 100){
				openGreen();
			}else if (level < 15){
				openRed();
			}else {
			   openBlue();
			}
		}else {
			   closeAllLeds();
		}
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170505> modify for ID 130787 end
		this.finish();
	}
}
