package com.hideep.zcalib.standalone.activity;

/*****************************************************************************
 * PressSensor.java - ZCalib Application Example Copyright (c) 2013-2017 HiDeep,
 * Incorporated. All rights reserved. Software License Agreement
 * 
 * HiDeep, Inc. is supplying this software for Gionee's own development on
 * Gionee's own application software solely and exclusively for Gionee's own
 * products that HiDeep's IC is used. HiDeep, Inc. is the owner of all
 * intellectual property rights in this software and the right is protected
 * under applicable laws.
 *****************************************************************************/
public class PressSensor {
    private int pressFiber = 2;
    private int circleSize = 50;
    private boolean isPress = false;
    private boolean isPressArr[] = { false, false };
    public static final int CIRCLE_MULTIPLE_SIZE = 700;

    public float getCircleSize(float size) {
        circleSize = (int) (size * CIRCLE_MULTIPLE_SIZE);
        return circleSize;
    }

    public void initPressFiber() {
        this.pressFiber = 1;
    }

    public int getPressFiber() {
        return pressFiber;
    }

    public boolean getPress() {
        return this.isPress;
    }

    public int getPressLenth() {
        return isPressArr.length;
    }

    public boolean getPress(int id) {
        if (isPressArr.length > id) {
            return isPressArr[id];
        }
        return false;
    }

    public void setPress(boolean isPress) {
        this.isPress = isPress;
    }

    public void setPress(int id, boolean isPress) {
        if (isPressArr.length > id) {
            isPressArr[id] = isPress;
        }
    }
}
