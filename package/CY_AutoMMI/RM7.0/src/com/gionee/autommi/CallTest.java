package com.gionee.autommi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;

public class CallTest extends BaseActivity implements OnClickListener{

    private boolean callFlag = false;
    private Intent it;
    private Button mRightBtn, mWrongBtn;
    private String TAG = "CallTest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);   
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        it = this.getIntent();
        if(it != null){
            callFlag=  it.getBooleanExtra("as", false);
            mRightBtn.setEnabled(true);
            mWrongBtn.setEnabled(true);
            ((AutoMMI)getApplication()).asResult(TAG, "", "2");
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
        callIntent.setData(Uri.parse("tel:112"));
        this.startActivity(callIntent);
        if(!callFlag){
            this.finish();
        }
    }
   
   @Override
   public void onClick(View v) {
           // TODO Auto-generated method stub
       switch (v.getId()) {
           case R.id.right_btn: {
               if(callFlag){
                   ((AutoMMI)getApplication()).asResult(TAG, "", "1");
                    CallTest.this.finish();
               }
               break;
           }
       
           case R.id.wrong_btn: {
               if(callFlag){
                   ((AutoMMI)getApplication()).asResult(TAG, "", "0");
                    CallTest.this.finish();
               }
               break;
           }
       }
   }
}
