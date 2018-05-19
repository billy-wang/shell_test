package com.gionee.autommi;

import android.content.Context;
import android.util.Log;
import com.gionee.autommi.ReflectionTools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.io.IOException;
/**
 * Created by hunter on 6/15/17.
 */

public class OMAUtil {

    private static final String TAG = "OMAUtil";
    final private Context mContext;
    private final static String sSEServiceClassName = "org.simalliance.openmobileapi.SEService";
    private final static String sSEServiceCallBackClassName = "org.simalliance.openmobileapi.SEService$CallBack";
    private final static String sReader = "org.simalliance.openmobileapi.Reader";
    private final static String sSession = "org.simalliance.openmobileapi.Session";
    private final static String sChannel= "org.simalliance.openmobileapi.Channel";

    public OMAUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    public byte[] getCPLC() {
        try {
            ReflectionTools.getClass(sSEServiceClassName);
        } catch (Exception e) {
            Log.d(TAG , "class not found:" + sSEServiceClassName);
            return null;
        }
        Object channel = getBasicChannelWithId(getISDAID());
        if (channel == null) {
            Log.d(TAG ,  "channel null");
            return null;
        }

        byte[] rsp = null;
        try {
            rsp = (byte[])ReflectionTools.getMethod(sChannel, channel, "transmit", new Class[] {byte[].class},
                    new Object[] {getCPLCCommandAPDU()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ReflectionTools.getMethod(sChannel,channel,"close",null,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG," byte[] getCPLC = " + rsp);
        return rsp;
    }

    private byte[] getCPLCCommandAPDU() {
        return new byte[] {(byte) 0x80, (byte) 0xCA, (byte) 0x9F, (byte) 0x7F, (byte) 00};
    }

    private byte[] getISDAID() {
        return new byte[] {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x51, (byte) 0x00,
                (byte) 0x00, (byte) 0x00};
    }

    private Object getBasicChannelWithId(byte[] aid) {
        Object session = getSession();
        if (session == null) {
            Log.d(TAG, "session null");
            return null;
        }

        Object channel = null;

        try {
            channel = ReflectionTools.getMethod(sSession, session, "openBasicChannel", new Class[] {byte[].class},
                    new Object[] {aid});
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG," Object getBasicChannelWithId = " + channel);
        return channel;
    }

    private Object getSession() {
        Object reader = getReader();
        if (reader == null) {
            Log.d(TAG, "reader null");
            return null;
        }

        Object session = null;
        try {
            session = ReflectionTools.getMethod(sReader, reader, "openSession", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG," Object getSession = " + session);
        return session;
    }

    private Object getReader() {
        Object[] readers = getReaders();

        if (readers == null) {
            Log.d(TAG, "readers null");
            return null;
        }

        for (Object r : readers) {
            String name = null;
            try {
                name = (String) ReflectionTools.getMethod(sReader, r, "getName", null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (name == null)
                continue;

            if (name.contains("eSE")) {
                Log.d(TAG," Object getReader = " + name);
                return r;
            }
        }

        return null;
    }

    private Object[] getReaders() {
        Object seService1 = getSEService();
        if (seService1 == null) {
            return null;
        }

        try {
            Object[] readers = (Object[]) ReflectionTools.getMethod(sSEServiceClassName, seService1,
                    "getReaders", null, null);
            Log.d(TAG," Object[] getReaders = " + readers);
            return readers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object mSEService;

    private Object getSEService() {
        Object listener = ReflectionTools
                .getInterface(sSEServiceCallBackClassName, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("serviceConnected")) {
                            mSEService = args[0];
                        }
                        return null;
                    }
                });
        try {
            ReflectionTools.getClassInstance(sSEServiceClassName,
                    new Class[] {Context.class, ReflectionTools.getClass(sSEServiceCallBackClassName)},
                    new Object[] {mContext, listener});
        } catch (Exception e) {
            e.printStackTrace();
        }

        long l = System.currentTimeMillis();
        while (true) {
            if (this.mSEService == null) {
                if (System.currentTimeMillis() - l > 8000L) {
                    Log.d(TAG, "get service time out");
                    return null;
                }
            } else {
                return mSEService;
            }

            try {
                Thread.sleep(200L);
            } catch (Exception paramContext) {
                paramContext.printStackTrace();
            }
        }
    }
}
