package com.gionee.autommi;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;

public class blueTest extends BaseActivity {

	private static final String TAG = "blueTest";
	private View mColorView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "ritColor : ");
		mColorView = new View(this);
		mColorView.setBackgroundColor(Color.BLUE);
		setContentView(mColorView);
		closeAllLeds();
		openBlue();


	}
    protected void onStart(){
		super.onStart();

    }


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStop()");
		super.onStop();
		this.finish();
	}


}
