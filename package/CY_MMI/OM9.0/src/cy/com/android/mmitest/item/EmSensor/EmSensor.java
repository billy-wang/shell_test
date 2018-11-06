package cy.com.android.mmitest.item.EmSensor;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;
import cy.com.android.mmitest.utils.DswLog;

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
            DswLog.d(TAG, "mClassName:" + mClass.getName());
        } catch (Exception e) {
            DswLog.i(TAG, "can not use engineermode doCalibration then use mmimode");
            try {
                //Chenyee <CY_Sensor> <tanbotao> <20180704> modify for CSW1707A-1412 begin
                Context pkgContext = mContext.createPackageContext(
                        "com.mmi.mode", Context.CONTEXT_INCLUDE_CODE
                                | Context.CONTEXT_IGNORE_SECURITY);
                mClass = pkgContext.getClassLoader().loadClass(
                        "com.mmi.mode.sensor.EmSensor");
                DswLog.d(TAG, "mClassName:" + mClass.getName());
                //Chenyee <CY_Sensor> <tanbotao> <20180704> modify for CSW1707A-1412 end
            } catch (Exception error) {
                DswLog.i(TAG, "can not use mmimode doCalibration");
                error.printStackTrace();
            }
        }
    }


    public int doGsensorCalibration() {
        int ret = RET_ERROR;
        try {
            Method method = mClass.getDeclaredMethod("doGsensorCalibration", int.class);
            ret = (int) method.invoke(mClass, TOLERANCE_20);
        } catch (Exception e) {
        } finally {
            DswLog.d(TAG, "doGsensorCalibration: result = " + ret);
        }
        return ret;
    }

    /**
     * Calibrate Gyroscope
     *
     * @return int
     */
    public int doGyroscopeCalibration() {
        int ret = RET_ERROR;
        try {
            Method method = mClass.getDeclaredMethod("doGyroscopeCalibration", int.class);
            ret = (int) method.invoke(mClass, TOLERANCE_20);
        } catch (Exception e) {
        } finally {
            DswLog.d(TAG, "doGyroscopeCalibration: result = " + ret);
        }
        return ret;
    }

    /**
     * Get Psensor's High threshold
     *
     * @return int
     * @see {@link getPsensorThreshold}
     */
    public int getPsensorHighThreshold() {
        getPsensorThreshold(mResults);
        return mResults[0];
    }

    /**
     * Get Psensor's Low threshold
     *
     * @return int
     * @see {@link getPsensorThreshold}
     */
    public int getPsensorLowThreshold() {
        getPsensorThreshold(mResults);
        return mResults[1];
    }

    /**
     * Calibration psensor
     *
     * @return int
     */
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
            DswLog.d(TAG, Log.getStackTraceString(e));
        } finally {
            DswLog.d(TAG, "doPsensorCalibration: result = " + result);
        }
        return result;
    }

    /**
     * Get data from psensor
     *
     * @return int
     */
    public int getPsensorData() {
        int result = -1;
        try {
            Method method = mClass.getDeclaredMethod("getPsensorData");
            result = (int) method.invoke(mClass);
        } catch (Exception e) {
        } finally {
            DswLog.d(TAG, "getPsensorData: result = " + result);
        }
        return result;
    }

    private int getPsensorThreshold(int[] result) {
        int ret = -1;
        try {
            Method method = mClass.getDeclaredMethod("getPsensorThreshold",
                    int[].class);
            ret = (int) method.invoke(mClass, new Object[]{result});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DswLog.d(TAG, "getPsensorThreshold: ret = " + ret + ". Threshold:" + result[0]
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
            DswLog.d(TAG, "getPsensorMaxValue: result = " + result);
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
            DswLog.d(TAG, "getPsensorMinValue: result = " + result);
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
            DswLog.v(TAG, "calculatePsensorMaxValue: result = " + result);
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
            DswLog.v(TAG, "calculatePsensorMinValue: result = " + result);
        }
        return result;
    }
}
