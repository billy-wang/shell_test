package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import com.gionee.util.DswLog;
import android.view.View;
import android.graphics.Color;

public class GrayTest extends BaseActivity {

	private static final String TAG = "GrayTest";
    private View mColorView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DswLog.i(TAG, "onCreate ");
        mColorView = new View(this);
		mColorView.setBackgroundColor(0xFF7F7F7F);
        setContentView(mColorView);
	}

	protected void closeAllLeds() {

	}	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		DswLog.i(TAG, "onStop()");
		this.finish();
	}
}
