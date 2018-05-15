package gn.com.android.mmitest;

import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.content.Context;
import android.app.Activity;
import gn.com.android.mmitest.R;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.KeyEvent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import amigo.app.AmigoProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;

import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;
import gn.com.android.mmitest.GnMMITest;
import android.os.SystemProperties;
import android.app.StatusBarManager;

public class ChooseLanguages extends Activity  {
    private static final String TAG = "ChooseLanguages";
    private Context mContext;
    private String mLanguagesKey = "forSale";
    private StatusBarManager sbm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.activity_dialog);
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT | StatusBarManager.DISABLE_EXPAND);
    }
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }
    @Override
    protected void onResume() {
    // TODO Auto-generated method stub
        super.onResume();
        final boolean gnoverseaflag = SystemProperties.get("ro.gn.oversea.product").equals("yes");
        final Intent intent = new Intent(ChooseLanguages.this,GnMMITest.class);
        if(!gnoverseaflag){
            Log.i(TAG,"gnoverseaflag  =  " + gnoverseaflag);
            intent.putExtra(mLanguagesKey,false);
            startActivity(intent);
            ChooseLanguages.this.finish(); 
        } else {
            SystemProperties.set("persist.radio.dispatchAllKey", "true");
            AlertDialog.Builder builder = new AlertDialog.Builder(ChooseLanguages.this);
            builder.setTitle(R.string.chooseLanguages).setPositiveButton(R.string.english, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "choose english");
                    intent.putExtra(mLanguagesKey,true);
                    startActivity(intent);
                    ChooseLanguages.this.finish(); 
                }
            }).setNegativeButton(R.string.Chinese, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "choose Chinese");
                    intent.putExtra(mLanguagesKey,false);
                    startActivity(intent);
                    ChooseLanguages.this.finish();
                }
            }).setCancelable(false).show();
        }
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sbm.disable(StatusBarManager.DISABLE_NONE);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

}
