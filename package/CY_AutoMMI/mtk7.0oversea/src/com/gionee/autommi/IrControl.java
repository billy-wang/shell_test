package com.gionee.autommi;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 红外线测试工具类
 */
public class IrControl {

    private Object objir = null;
    private Method method_transmit;
    private Method method_stop;
    private Method method_receive;
    private Method method_receive_init;
    private Method method_receive_is_ready;

    private static final int BIT0 = 11;
    private static final int BIT1 = 34;

    public IrControl(Context ctx) {
        try {
            this.objir = ctx.getSystemService("remoteir");
            this.method_transmit = objir.getClass().getMethod("transmit", new Class[] { byte[].class, int.class });
            this.method_receive = objir.getClass().getMethod("receive", new Class[] { byte[].class, int.class });
            this.method_receive_init = objir.getClass().getMethod("receive_init", null);
            this.method_receive_is_ready = objir.getClass().getMethod("receive_is_ready", null);
            this.method_stop = objir.getClass().getMethod("cancelTransmit", null);
        } catch (Exception e) {
        }

        if (objir == null)
            Log.d("RemoteIr", "No RemoteIR");
        if (method_transmit == null)
            Log.d("RemoteIr", "No Transmit Method");
        if (method_stop == null)
            Log.d("RemoteIr", "No Stop Method");
        if (method_receive == null)
            Log.d("RemoteIr", "No received Method");
        if (method_receive_init == null)
            Log.d("RemoteIr", "No receive_init Method");
        if (method_receive_is_ready == null)
            Log.d("RemoteIr", "receive_is_ready");
        // if(method_stop == null) Log.d("RemoteIr", "No Stop Method");
    }

    // Build IR buffer for I2C transfer
    private byte[] buildBuffer(int[] frame) {
        int frequency = frame[0];

        int size = ((frame.length - 1) * 2) + 6;
        byte[] buffer = new byte[size];
        int idx = 0;

        buffer[idx++] = (byte) 0x80;
        buffer[idx++] = (byte) ((frequency >> 16) & 0xff);
        buffer[idx++] = (byte) ((frequency >> 8) & 0xff);
        buffer[idx++] = (byte) (frequency & 0xff);
        buffer[idx++] = 0x00;
        buffer[idx++] = 0x00;

        for (int i = 1; i < frame.length; i++) {
            buffer[idx++] = (byte) ((frame[i] >> 8) & 0xff);
            buffer[idx++] = (byte) (frame[i] & 0xff);
        }

        return buffer;
    }

    // Get IR pulse frame for 15 Bytes
    private int[] getFrames(byte[] data) {
        ArrayList<Integer> frameList = new ArrayList<Integer>();
        frameList.add(38000);
        frameList.add(114);
        frameList.add(114);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < 8; j++) {
                int bit = (data[i] >> j) & 0x01;
                if (bit == 0) {
                    frameList.add(BIT0);
                    frameList.add(BIT0);
                } else {
                    frameList.add(BIT0);
                    frameList.add(BIT1);
                }
            }
        }

        frameList.add(11);
        frameList.add(1140);

        int[] frames = new int[frameList.size()];
        for (int i = 0; i < frameList.size(); i++) {
            frames[i] = frameList.get(i);
        }

        return frames;
    }

    // Send IR signal
    public int sendData(byte[] data) {
        if (objir == null || method_transmit == null)
            return -1;

        int ret = 0;
        try {
            byte[] buffer = buildBuffer(getFrames(data));
            ret = (Integer) method_transmit.invoke(objir, buffer, buffer.length);
        } catch (Exception e) {
            Log.e("RemoteIr", e.toString());
        }

        return ret;
    }

    // Stop IR signal
    public int stopIR() {
        if (objir == null || method_transmit == null)
            return -1;

        int ret = 0;
        try {
            ret = (Integer) method_stop.invoke(objir);
        } catch (Exception e) {
            Log.e("RemoteIr", e.toString());
        }

        return ret;
    }

    public int Recevied_Init() {
        int ret = 0;
        try {
            ret = (Integer) method_receive_init.invoke(objir);
        } catch (Exception e) {
            Log.e("RemoteIr recevied init", e.toString());
        }
        // ret= method_receive_init
        return ret;
    }

    // return 1, received is read
    public int ReceivedIsRead() {
        int ret = 0;

        try {
            ret = (Integer) method_receive_is_ready.invoke(objir);
        } catch (Exception e) {
            Log.e("RemoteIr", e.toString());
        }

        return ret;
    }

    public int RecviedIR(byte[] buffer, int i32MaxLength) {
        int ret = 0;

        try {
            ret = (Integer) method_receive.invoke(objir, buffer, i32MaxLength);
        } catch (Exception e) {
            Log.e("RemoteIr", e.toString());
        }

        return ret;
    }

    // return is
    // int[0] is carrier frequenct
    // int [1]--int[n] is data
    public int[] ReaciveDataAnlazy(byte[] buffer, int i32Length) {
        // int[]
        int i32Freq = 0;
        int i;
        int i32Tmp;
        int tme = 0;
        int chk1;
        int chk2;

        i32Freq = buffer[3];
        i32Freq &= 0xFF;
        i32Freq <<= 8;
        tme = buffer[4];
        tme &= 0xFF;
        i32Freq |= tme;
        i32Freq &= 0xFFff;
        i32Freq <<= 8;
        tme = buffer[5];
        tme &= 0xFF;
        i32Freq |= tme;// buffer[5];
        i32Freq &= 0xFFffff;
        tme = buffer[1];
        tme &= 0xFF;
        i32Length = tme;// buffer[1];
        if (buffer[0] != 0x00) {
            Log.e("DataAna ", "packet no err");
            return new int[0];
        }
        if (i32Length < 30) {
            Log.e("DataAna ", "Data Length err");
            return new int[0];
        }

        chk1 = buffer[i32Length];
        chk1 &= 0x00FF;
        Log.e("DataAna Checksum H : ", "" + chk1);
        chk1 <<= 8;
        tme = buffer[i32Length + 1];
        tme &= 0x00FF;

        Log.e("DataAna Checksum L : ", "" + tme);
        chk1 += tme;

        chk2 = 0;
        for (i = 0; i < (i32Length + 2); i++)
            Log.e("DataAna", "" + buffer[i]);

        for (i = 0; i < (i32Length); i++) {
            tme = buffer[i];
            tme &= 0x00FF;
            chk2 += tme;
        }
        if (chk2 != chk1) {
            Log.e("DataAna checksum", "chk1!=chk2");
            return new int[0];
        }
        ArrayList<Integer> frameList = new ArrayList<Integer>();
        frameList.add(i32Freq);

        for (i = 6; i < i32Length; i += 2) {
            i32Tmp = buffer[i];
            i32Tmp &= 0xFF;
            i32Tmp <<= 8;
            tme = buffer[1 + i];
            tme &= 0xFF;
            i32Tmp |= tme;// buffer[1+i];
            i32Tmp &= 0xFFFF;
            frameList.add(i32Tmp);
        }

        int[] frames = new int[frameList.size()];
        for (i = 0; i < frameList.size(); i++) {
            frames[i] = frameList.get(i);
        }
        return frames;
    }
}
