package gn.com.android.mmitest.item.EmSensor;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class EmSensor {
	private static final String TAG = "MMITest-EmSensor";
	public static final int RET_ERROR = 0;
	public static final int RET_SUCCESS = 1;
	private static final int TOLERANCE_20 = 2;
	
	private Class<?> mClass = null;
	private int[] mResults = new int[2];
	private Context mContext;
	private static EmSensor sInstance = null;
	
	public static EmSensor getInstance(Context context) {
		if (sInstance == null)
			sInstance = new EmSensor(context);
		return sInstance;
	}
	
	private EmSensor(Context context) {
		mContext = context;
		initClassLoader();
		getPsensorThreshold(mResults);
	}
	
	private void initClassLoader() {
		try {
			Context pkgContext = mContext.createPackageContext(
					"com.mediatek.engineermode", Context.CONTEXT_INCLUDE_CODE
							| Context.CONTEXT_IGNORE_SECURITY);
			mClass = pkgContext.getClassLoader().loadClass(
					"com.mediatek.engineermode.sensor.EmSensor");
			Log.v(TAG, "mClassName:" + mClass.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Calibrate Gyroscope
	 * @return int
	 * */
	public int doGyroscopeCalibration() {
		int ret = RET_ERROR;
		try {
			Method method = mClass.getDeclaredMethod("doGyroscopeCalibration",int.class);
			ret = (int) method.invoke(mClass, TOLERANCE_20);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "doGyroscopeCalibration: result = " + ret);
		}
		return ret;
	}
	/**
	 * Get Psensor's High threshold
	 * @return int
	 * @see {@link getPsensorThreshold}
	 * */
	public int getPsensorHighThreshold() {
		getPsensorThreshold(mResults);
		return mResults[0];
	}
	
	/**
	 * Get Psensor's Low threshold
	 * @return int
	 * @see {@link getPsensorThreshold}
	 * */
	public int getPsensorLowThreshold() {
		getPsensorThreshold(mResults);
		return mResults[1];
	}
	
	/**
	 * Calibration psensor
	 * @return int
	 * */
	public int doPsensorCalibration() {
		int min = getPsensorMinValue();
		int max = getPsensorMaxValue();
		int result = doPsensorCalibration(min, max);
		return result;
	}
	
	private int doPsensorCalibration(int min, int max) {
		int result = RET_ERROR;
		try {
			Method method = mClass.getDeclaredMethod("doPsensorCalibration",
					int.class, int.class);
			result = (int) method.invoke(mClass, min, max);
		} catch (Exception e) {
			Log.v(TAG, Log.getStackTraceString(e));
		} finally {
			Log.v(TAG, "doPsensorCalibration: result = " + result);
		}
		return result;
	}
	
	/**
	 * Get data from psensor
	 * @return int
	 * */
	public int getPsensorData() {
		int result = -1;
		try {
			Method method = mClass.getDeclaredMethod("getPsensorData");
			result = (int) method.invoke(mClass);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "getPsensorData: result = " + result);
		}
		return result;
	}
	
	private int getPsensorThreshold(int[] result) {
		int ret = -1;
		try {
			Method method = mClass.getDeclaredMethod("getPsensorThreshold",
					int[].class);
			ret = (int) method.invoke(mClass, new Object[] { result });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Log.v(TAG, "getPsensorThreshold: ret = " + ret +". Threshold:"+ result[0]
					+ " " + result[1]);
		}
		return ret;
	}

	private int getPsensorMaxValue() {
		int result = -1;
		try {
			Method method = mClass.getDeclaredMethod("getPsensorMaxValue");
			result = (int) method.invoke(mClass);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "getPsensorMaxValue: result = " + result);
		}
		return result;
	}

	private int getPsensorMinValue() {
		int result = -1;
		try {
			Method method = mClass.getDeclaredMethod("getPsensorMinValue");
			result = (int) method.invoke(mClass);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "getPsensorMinValue: result = " + result);
		}
		return result;
	}

	private int calculatePsensorMaxValue() {
		int result = -1;
		try {
			Method method = mClass.getDeclaredMethod("calculatePsensorMaxValue");
			result = (int) method.invoke(mClass);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "calculatePsensorMaxValue: result = " + result);
		}
		return result;
	}

	private int calculatePsensorMinValue() {
		int result = -1;
		try {
			Method method = mClass.getDeclaredMethod("calculatePsensorMinValue");
			result = (int) method.invoke(mClass);
		} catch (Exception e) {
		} finally {
			Log.v(TAG, "calculatePsensorMinValue: result = " + result);
		}
		return result;
	}
}
