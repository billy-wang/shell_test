package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.gionee.util.DswLog;
import android.view.View;
import android.graphics.Color;

public class greenTest extends BaseActivity {

	private static final String TAG = "greenTest";
    private View mColorView;
    //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DswLog.i(TAG, "ritColor : ");
        mColorView = new View(this);
		mColorView.setBackgroundColor(Color.GREEN);
        setContentView(mColorView);

		showNotification();
        openLed(GREEN);
	}

	@Override
	protected void onPause() {
		closeAllLeds();
		try{
			Thread.sleep(50);
		}catch(Exception e){
			DswLog.d(TAG,"onStart Thread.sleep() Error");
		}
		super.onPause();
	}
    //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 end

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();	
		DswLog.i(TAG, "onStop()");
		this.finish();
	}
}
