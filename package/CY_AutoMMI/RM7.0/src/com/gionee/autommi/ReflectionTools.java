//Gionee <cls><2012-5-20> add for CR00802008 begin
package com.gionee.autommi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionTools {

    /**
     * 返回是否调用成功
     * 
     * @param className
     * @param methodName
     * @param parameterTypes
     * @param args
     *            是否有返回值
     * @return
     */
    public static Object getMethod(String className, String methodName, Class<?>[] parameterTypes,
            Object[] args) throws Exception {
        Class<?> classes = Class.forName(className);
        Object instance = classes.newInstance();
//        if (instance == null) {
//            throw new Exception("-----------反射获取类实例:" + className + "失败，返回");
//        }
        Method method = classes.getMethod(methodName, parameterTypes);
        Object roProductManufacturer = method.invoke(instance, args);
        return roProductManufacturer;
    }

    /**
     * 返回是否调用成功
     * 
     * @param className
     * @param instance
     * @param methodName
     * @param parameterTypes
     * @param args
     *            是否有返回值
     * @return
     */
    public static Object getMethod(String className, Object instance, String methodName,
            Class<?>[] parameterTypes, Object[] args) throws Exception {
        Class<?> classes = Class.forName(className);
        if (instance == null) {
            throw new Exception("-----------反射获取类实例:" + className + "失败，返回");
        }
        Method method = classes.getMethod(methodName, parameterTypes);
        Object roProductManufacturer = method.invoke(instance, args);
        return roProductManufacturer;
    }

    public static Object getStaticMethod(String className, String methodName, Class<?>[] parameterTypes,
            Object[] args) throws Exception {
        Class<?> c = Class.forName(className);
        Method m = c.getMethod(methodName, parameterTypes);
        return m.invoke(c, args);
    }

    public static <T> T getClassInstance(String className, Class<?> parameterTypes, Object args)
            throws Exception {
        Class<?> classes = Class.forName(className);
        Constructor<?> constructor = classes.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        T instance = (T) constructor.newInstance(args);
        return instance;
    }
    public static <T> T getClassInstance(String className, Class<?>[]  parameterTypes, Object[]  args)
            throws Exception {
        Class<?> classes = Class.forName(className);
        Constructor<?> constructor = classes.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        T instance = (T) constructor.newInstance(args);
        return instance;
    }

    public static Class<?> getClass(String className)
            throws Exception {
        Class<?> classes = Class.forName(className);
        return classes;
    }

    public static void updateStaticFieldValue(String className, String staticFieldName, boolean args) {
        try {
            Class<?> classes = Class.forName(className);
            Field method = classes.getField(staticFieldName);
            method.setBoolean(classes, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Object getInterface(String callbackClassName, InvocationHandler mHandler) {
        try {
            Class<?> mCallback = Class.forName(callbackClassName);
            Object mObj = Proxy.newProxyInstance(mHandler.getClass().getClassLoader(), new Class[] { mCallback }, mHandler);
            return mObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
//Gionee <cls><2012-5-20> add for CR00802008 end
