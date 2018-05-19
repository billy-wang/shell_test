package com.gionee.autommi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/*Gionee zhangke 20160321 modify for CR01652479 begin*/
import com.focaltech.tp.test.FT_Test;
import com.focaltech.tp.test.FT_Test_FT5X46;
import com.focaltech.tp.test.FT_Test_FT6X36;
import com.focaltech.tp.test.FT_Test_FT5X36;
import com.focaltech.tp.test.FT_Test_FT5822;
import com.focaltech.tp.test.FT_Test_FT8606;
import com.focaltech.tp.test.FT_Test_FT8716;

import android.os.Environment;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/*Gionee zhangke 20160321 modify for CR01652479 end*/

public class TouchPadPrivTest extends BaseActivity {
	private static final String TAG = "TouchPadPrivTest";
    /*Gionee zhangke 20160321 modify for CR01652479 begin*/
    private  FT_Test m_Test = null;
    /*Gionee zhangke 20160321 modify for CR01652479 end*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		//Gionee zhangke 20160128 modify for CR01632976 start
		((AutoMMI) getApplication()).recordResult(TAG, "", "0");
        //Gionee <GN_MMI><zhangke><2016-10-20> add for 12257 begin
        createTpDataFolder();
        //Gionee <GN_MMI><zhangke><2016-10-20> add for 12257 end
		doTpPrivTest();
		//Gionee zhangke 20160128 modify for CR01632976 end
	}

	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		Log.e(TAG, "onStop");
	}

        public String getTpResult() {
            String tpResult = null;
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20171018> modify for ID 239941 begin
            //  /sys/devices/platform/tp_wake_switch/factory_check
            //  /sys/bus/platform/devices/tp_wake_switch/factory_check 
            String mFileName = "/sys/devices/platform/tp_wake_switch/factory_check";
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20171018> modify for ID 239941 begin
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader br = null;
		try {
			try {
				File currentFilePath = new File(mFileName);
				if (currentFilePath.exists()) {
					fileInputStream = new FileInputStream(currentFilePath);
					inputStreamReader = new InputStreamReader(fileInputStream);
					br = new BufferedReader(inputStreamReader);
					String data = null;
					while ((data = br.readLine()) != null) {
						tpResult = data;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if (inputStreamReader != null) {
					inputStreamReader.close();
				}
				if (br != null) {
					br.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tpResult;
	}

	//Gionee zhangke 20160128 add for CR01632976 start
	private int mTimes = 0;
	private void doTpPrivTest(){
		try {
			/*Gionee zhangke 20160321 modify for CR01652479 begin*/
			boolean bFTTP = isFTTP();
			Log.i(TAG, "bFTTP="+bFTTP);
			if(bFTTP){
				int result_FT = getTPResultfor_FT();
				Log.i(TAG, "result_FT="+result_FT);
				if (result_FT == 0) {
					Log.e(TAG, "FT result == 1 " );
					((AutoMMI) getApplication()).recordResult(TAG, "", "1");
				} else {
					Log.e(TAG, "FT result other" );
					if(mTimes == 0){
						mTimes++;
						try{
							Thread.sleep(1000);
						}catch(Exception e){
							
						}
						doTpPrivTest();
					}else{
						((AutoMMI) getApplication()).recordResult(TAG, "", "0");
					}

				}
			} else {
				String result = getTpResult();
				Log.e(TAG, "result Number = " + result);
				if (result.equals("1")) {
					Log.e(TAG, "result == 1 ");
					((AutoMMI) getApplication()).recordResult(TAG, "", "1");
				} else {
					Log.e(TAG, "result other");
					if(mTimes == 0){
						mTimes++;
						try{
							Thread.sleep(1000);
						}catch(Exception e){
							
						}
						doTpPrivTest();
					}else{
						((AutoMMI) getApplication()).recordResult(TAG, "", "0");
					}
				}

			}
			/*Gionee zhangke 20160321 modify for CR01652479 end*/

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Gionee zhangke 20160128 add for CR01632976 end

	/*Gionee zhangke 20160321 modify for CR01652479 begin */
	private boolean isFTTP(){
		String mFileName = "/sys/devices/platform/tp_wake_switch/manufacturer";
		String result = null;
		boolean isFT = false;
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader br = null;

		try {
			File FilePath = new File(mFileName);
			if (FilePath.exists()) {
				fileInputStream = new FileInputStream(FilePath);
				inputStreamReader = new InputStreamReader(fileInputStream);
				br = new BufferedReader(inputStreamReader);
				String data = null;
				while ((data = br.readLine()) != null) {
					result = data;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreamQuiet(fileInputStream);
			closeReaderQuiet(inputStreamReader);
			closeReaderQuiet(br);
		}

		if (result	== null ) {
			isFT = false;
		} else if (result.equals("FT")) {
			isFT = true;
		} else {
			isFT = false;
		}

		return isFT;
	}

	public int getTPResultfor_FT(){
		int m_iTestResult = -1;
		boolean m_bDevice = false;
		String path  = "/system/etc/Conf_MultipleTest";

		initFTTestData();
		m_bDevice = m_Test.initDevice();

		if (!m_bDevice){
			Log.e(TAG,"m_bDevice="+m_bDevice);
			return m_iTestResult;
		}

		int iVID = m_Test.readReg(0xA8);
		String VidStr = String.format("_0x%02x.ini", iVID);

		Log.e(TAG,"Config patch="+path+VidStr);
		try {
			File FilePath = new File(path+VidStr);
			if (FilePath.exists()) {
				m_Test.loadConfig( path+VidStr);
			}else{
				m_Test.loadConfig( path+".ini");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		m_Test.createReport(Environment.getExternalStorageDirectory().getPath());
		try {
			m_iTestResult = m_Test.startTestTP();
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_Test.closeReport();
		m_Test.releaseDevice();
		return m_iTestResult;
	}


	private void initFTTestData(){
		String path  = "/system/etc/Conf_MultipleTest.ini";
		String result = null;
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader br = null;

		try {
			File FilePath = new File(path);
			if (FilePath.exists()) {
				fileInputStream = new FileInputStream(FilePath);
				inputStreamReader = new InputStreamReader(fileInputStream);
				br = new BufferedReader(inputStreamReader);
				String data = null;
				while ((data = br.readLine()) != null) {
					if (data.contains("IC_Type")){
						result = data.split("=")[1].substring(0);
						break;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreamQuiet(fileInputStream);
			closeReaderQuiet(inputStreamReader);
			closeReaderQuiet(br);
		}

		switch(result){
			case "FT5X36":
				m_Test = new FT_Test_FT5X36();
				break ;
			case "FT5X46":
				m_Test = new FT_Test_FT5X46();
				break ;
			case "FT6X36":
			case "FT3X07":
				m_Test = new FT_Test_FT6X36();
				break;
			case "FT5822":
				m_Test = new FT_Test_FT5822();
				break;
			case "FT8606":
				m_Test = new FT_Test_FT8606();
				break;
			/*Gionee huangjianqiang */
			case "FT8716":
				m_Test = new FT_Test_FT8716();
				break;
			default:
				Log.e(TAG, "initFTTestData new null ");
				m_Test = null;
				break;
		}
		return ;
	}

    void closeStreamQuiet(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream = null;
            }
        }
    }

    void closeReaderQuiet(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reader = null;
            }
        }
    }

	/*Gionee zhangke 20160321 modify for CR01652479 end*/

    private void createTpDataFolder(){
        File tpDataFolder = new File("/mnt/sdcard/tpdata/");
        if (!tpDataFolder.exists()) {
            try {
                tpDataFolder.mkdir(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
