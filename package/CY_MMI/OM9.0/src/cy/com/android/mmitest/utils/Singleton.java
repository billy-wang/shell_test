package cy.com.android.mmitest.utils;

import android.content.Context;

import cy.com.android.mmitest.bean.OnGPSListenner;

/**
 * Created by qiang on 12/18/17.
 */
public class Singleton {
    private static Singleton instance = null;

    public boolean isGServiceBusy = false;

    private OnGPSListenner onGPSListenner;


    public void setOnGPSListenner(OnGPSListenner onGPSListenner) {
        this.onGPSListenner = onGPSListenner;
    }

    public OnGPSListenner getOnGPSListenner() {
        return onGPSListenner;
    }




    public Singleton() {}

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }






}
