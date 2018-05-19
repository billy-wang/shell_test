package com.cydroid.autommi;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.os.RemoteException;
import android.os.Message;
import android.widget.TextView;
import com.cydroid.util.DswLog;
import com.cydroid.util.SystemUtil;

public class FlagDouJia extends BaseActivity {

    public static final String TAG = "FlagDouJia";
    private int presult;
    private static final int SN_LENGTH = 500;
    private static final int MMI_DJ_FLAG = 499;
    private static byte[] mSnByteArray = new byte[SN_LENGTH];
    private TextView item;
    private TextView resultTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        item =  (TextView) findViewById(R.id.item);
        resultTitle =  (TextView) findViewById(R.id.result);
        item.setText("写入：");

        presult = this.getIntent().getIntExtra("DoJFlag", -1);
    }



    @Override
    protected void onStart() {
        super.onStart();
        uiHandler.sendEmptyMessage(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    private void startWriteFlag() {
        //read nvram
        if (!readProductionInfo(SN_LENGTH))
            return;
        //replace nvram doujia flag

        String flag = presult == 1 ? "P" : "F";
        mSnByteArray = SystemUtil.getNewSN(MMI_DJ_FLAG, flag, mSnByteArray);

        //write Nvram flag
        SystemUtil.writeToNvramInfo(mSnByteArray,SN_LENGTH);

        //send message
        uiHandler.sendEmptyMessage(1);
    }

    private boolean isWritePass(int snLength) {
        byte[] productInfoBuff = SystemUtil.readINvramInfo(snLength);

        if (productInfoBuff != null && productInfoBuff.length > MMI_DJ_FLAG) {
            DswLog.d(TAG, "FlagDoujia: isWritePass[499]=" + productInfoBuff[MMI_DJ_FLAG]);
            if ('P' == productInfoBuff[MMI_DJ_FLAG])
                return true;
        }
        return false;
    }

    private boolean readProductionInfo(int snLength) {
        try {
            //read nvram
            System.arraycopy(SystemUtil.readINvramInfo(snLength), 0, mSnByteArray, 0, snLength);
            String snNumber = new String(mSnByteArray);
            if (snNumber == null || snNumber.isEmpty()) {
                DswLog.v(TAG, "updateSN oldSn is null or empty!");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!isWritePass(SN_LENGTH)) {
                        DswLog.d(TAG, "write FlagDoujia Failed");
                        return;
                    }
                    resultTitle.setText(R.string.success);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
                    break;
                case 0:
                    resultTitle.setText(R.string.fail);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
                    startWriteFlag();
                    break;

            }
        }
    };
}
