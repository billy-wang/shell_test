package com.gionee.autommi;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;

public class GrayTest2 extends BaseActivity {

	private static final String TAG = "GrayTest2";
    private View mColorView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "GrayTest2");
        setContentView(R.layout.color_test);
	}

	protected void closeAllLeds() {

	}	

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		Log.i(TAG, "onStop()");
		this.finish();
	}
}
