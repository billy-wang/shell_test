package com.gionee.autommi;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.StatFs;

public class SdCardTest extends BaseActivity {
	private StorageManager storageManager;
	public final static String TAG = "SdCardTest";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = storageManager.getVolumeList();
		String info = "";
		for(int i = 0; i < volumes.length; i++){
            //Gionee zhangke modify for CR01606671 start
			Log.d(TAG, "volumes["+i+"]="+volumes[i]);
			String path = volumes[i].getPath();
			String state = storageManager.getVolumeState(path);//mounted or removed
			//if((path.contains("sdcard"))||(path.contains("emulated"))) {
            if(path.contains("storage")){
				StatFs stat = new StatFs(path);
				int allVol = (int) (((long)stat.getBlockCount()*stat.getBlockSize())/(1024*1024));
				int avaiableVol = (int)(((long)stat.getAvailableBlocks()*stat.getBlockSize())/(1024*1024));
				info += path + ":" + state + ":" + allVol + ":" + avaiableVol + "|";
			}
			Log.d(TAG, path + " : " + state);
             //Log.d(TAG, ""+volumes[i]);
            Log.i(TAG, "info="+info);
            //Gionee zhangke modify for CR01606671 start
		}
		info = info.substring(0, info.length()-1);
		((AutoMMI)getApplication()).recordResult(TAG, info, "2");
		Toast.makeText(this, info.replace("|", "\n"), Toast.LENGTH_LONG).show();
		
	

		
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
	
}
