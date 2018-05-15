package gn.com.android.mmitest.item;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import gn.com.android.mmitest.utils.DswLog;

public class GnReflectionMethods {
    static final String TAG = "GnReflectionMethods";

    private String mClassName;
    private String mMethodName;
    private Class[] mParamTypes;
    private Object[] mParams;
    private String mConstantName;
    private Object mResult;

    private Class clazzClass;

    public GnReflectionMethods(String className, String methodName,
                               Class[] paramTypes, Object[] params) {
        this.mClassName = className;
        this.mMethodName = methodName;
        this.mParamTypes = paramTypes;
        this.mParams = params;
    }

    public GnReflectionMethods(Class className, String methodName,
                               Class[] paramTypes, Object[] params) {
        this.clazzClass = className;
        this.mMethodName = methodName;
        this.mParamTypes = paramTypes;
        this.mParams = params;
    }

    public Object getInvokeResult(Context context) {
        try {
            Class classType = Class.forName(mClassName);
            Constructor cs = classType.getConstructor(Context.class);
            Object instance = cs.newInstance(context);
            Method method = classType.getMethod(mMethodName, mParamTypes);
            mResult = method.invoke(instance, mParams);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.d(TAG, " Oops, getInvokeResult exeception=" + e.getMessage());
        }
        return mResult;
    }

    public Object getInvokeResult1(Context context) {
        try {
            Class classType = Class.forName(mClassName);
            Method method = classType.getMethod(mMethodName, mParamTypes);
            mResult = method.invoke(classType, mParams);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.d(TAG, " Oops, getInvokeResult exeception=" + e.getMessage());
        }
        return mResult;

    }

    public Object getInvokeResult1() {
        try {
            Method method = clazzClass.getMethod(mMethodName, mParamTypes);
            mResult = method.invoke(clazzClass, mParams);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.d(TAG, " Oops, getInvokeResult exeception=" + e.getMessage());
        }
        return mResult;

    }

}
