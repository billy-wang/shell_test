package com.gionee.autommi;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.StatFs;
import android.content.Intent;
import com.gionee.autommi.TestUtils;

public class SdCardTest extends BaseActivity {
        private StorageManager storageManager;
        public final static String TAG = "SdCardTest";
        private boolean flag = false;
        private String EMULATED = "emulated";
        private String temp = "";
        private String temp1 = "";
	@Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            Intent intent = getIntent();
            if(intent != null){
                flag = intent.getBooleanExtra("as", false);
            }
            Log.d(TAG,"flag = " + flag);
            storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume[] volumes = storageManager.getVolumeList();
            String info = "";
            for(int i = 0; i < volumes.length; i++){
                Log.d(TAG, "volumes["+i+"]="+volumes[i]);
                String path = volumes[i].getPath();
                String state = storageManager.getVolumeState(path);//mounted or removed
                if(path.contains("storage")){
                    StatFs stat = new StatFs(path);
                    int allVol = (int) (((long)stat.getBlockCount()*stat.getBlockSize())/(1024*1024));
                    int avaiableVol = (int)(((long)stat.getAvailableBlocks()*stat.getBlockSize())/(1024*1024));
                    if(flag){
                        if(!path.contains(EMULATED)){
                            temp = path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
                        } else {
                            info += path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
                        }
                    } else {
                        info += path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
                    }
                }
                Log.d(TAG, path + " : " + state + "  ,  temp = " + temp);
            }
            if(flag){
                temp1 = info;
                info = temp + temp1;
            }
            Log.i(TAG, "info="+info);
            info = info.substring(0, info.length()-1);
            if(flag){
                ((AutoMMI)getApplication()).asResult(TAG, info, "1");
            } else {
                ((AutoMMI)getApplication()).recordResult(TAG, info, "2");
            }
            Toast.makeText(this, info.replace("|", "\n"), Toast.LENGTH_LONG).show();
	}


	@Override
	protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop();
            this.finish();
	}
}
