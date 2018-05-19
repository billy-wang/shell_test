package com.cydroid.autommi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import com.cydroid.util.DswLog;
import android.view.KeyEvent;
import android.widget.TextView;

public class MemoryTest extends BaseActivity {
	private final static String TAG = "MemoryTest";
	private final static String RAM_PATH = "/proc/meminfo";
	private final static String ROM_PATH = "/proc/partitions";
	private final static String STATE_SUCCESS = "1";

	TextView mTextView;
	Handler mHandler = new Handler();
	
	private String mRomInfo = null;
	private String mRamInfo = null;
	double[] array = new double []{
			0.5, 1, 1.5, 2,
			3, 4, 6, 8,
			10, 12, 14, 16,
			32, 64, 128, 256};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTextView = new TextView(this);
		setContentView(mTextView);
		mRamInfo = getRamInfo();
		mRomInfo = getRomInfo();
		//Gionee zhangke 20151216 modify for CR01610169 start
		DswLog.i(TAG, "mRamInfo = "+mRamInfo+";mRomInfo="+mRomInfo);
		//Gionee zhangke 20151216 modify for CR01610169 end
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				StringBuilder builder = new StringBuilder();
				builder.append("\n").append(getString(R.string.memory)).append(mRamInfo);
				builder.append("\n").append(getString(R.string.storage)).append(mRomInfo);
				mTextView.setText(builder.toString());
				mTextView.setPadding(40, 40, 20, 20);
				mTextView.setTextSize(18);
				mTextView.setLineSpacing(1.2f, 2);
			}
		});
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		((AutoMMI)getApplication()).recordResult(TAG, mRamInfo+"|"+mRomInfo, STATE_SUCCESS);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTextView = null;
	}

	String getRamInfo() {
		long memSize = 0;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(RAM_PATH);
			bufferedReader = new BufferedReader(fileReader);
			if (bufferedReader != null) {
				String line = bufferedReader.readLine();
				String[] arrays = line.split("\\s+");
				for (String value : arrays) {
					DswLog.v(TAG, "mem value: " + value);
				}
				memSize = Long.valueOf(arrays[1]).longValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeQuiet(bufferedReader);
			closeQuiet(fileReader);
		}
		//Gionee zhangke 20151216 modify for CR01610169 start
		DswLog.i(TAG, "memSize1="+memSize);
		double ram1 = (double)memSize/(1024*1024);
		String virturlValue = translateCapacity(ram1);
		DswLog.i(TAG, "ram1="+ram1+";getRamInfo="+virturlValue);
        //Gionee zhangke 20151216 modify for CR01610169 end
		return virturlValue;
	}

	String getRomInfo() {
		long romSize = 0;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
        //Gionee zhangke 20151216 modify for CR01610169 start
		try {
			fileReader = new FileReader(ROM_PATH);
			bufferedReader = new BufferedReader(fileReader);
			if (bufferedReader != null) {
				String line = null;
				// Line 1 is " major minor  #blocks  name "
				bufferedReader.readLine();
				// Line 2 is blank line.
				bufferedReader.readLine();
				boolean isAdd = false;
				while (!isAdd) {
					line = bufferedReader.readLine();
					DswLog.v(TAG, "line = "+line);
					if (line != null && line.contains("mmcblk0")) {
						String[] arrays = line.split("\\s+");
						romSize += Long.valueOf(arrays[3]).longValue();
						isAdd = true;
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		DswLog.i(TAG, "romSize1="+romSize);
		double rom1 = (double)romSize/(1024*1024);
		String virturlValue = translateCapacity(rom1);
		DswLog.i(TAG, "rom1="+rom1+";virturlValue="+virturlValue);
		//Gionee zhangke 20151216 modify for CR01610169 end
		return virturlValue;
	}

	void closeQuiet(Reader reader) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    //Gionee zhangke 20151203 delete for CR01603113 start
	/*@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}
    */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }
    //Gionee zhangke 20151203 delete for CR01603113 end

	private String translateCapacity(double capacity) {
		String result = "1G";

		if (capacity <=0) {
			return "ERROR";
		}
		for (int i=0; i < array.length; i++) {
			if (capacity <= array[i]) {
				result = array[i] + "G";
				break;
			}
		}
		return result;
	}

}
