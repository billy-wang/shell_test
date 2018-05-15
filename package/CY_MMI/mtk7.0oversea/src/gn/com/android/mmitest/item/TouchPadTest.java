package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
//Gionee zhangke 20151019 add for CR01571097 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end

public class TouchPadTest extends BaseActivity {
    private float mAverageHeight;
    private float mAverageWidth;
    private Handler mTouchHandler;
    private Runnable mTouchRunanable, mRestartRunnable;
    private boolean mIsRestart;
    private boolean isSendMessage = true;
    private static final String TAG = "TouchPadTest";
    private static final int RIGHT_MESSAGE = 0;
    private static final int WRONG_MESSAGE = 1;
    private static final int RESTART_MESSAGE = 2;

    private class TouchHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RIGHT_MESSAGE: {
                    Log.e(TAG, TAG + "zhangxiaoweirightpress handle message");
                    // mTouchHandler.removeCallbacks(mTouchRunanable);
                    TestUtils.rightPress(TAG, TouchPadTest.this);
                    break;
                }
                case WRONG_MESSAGE: {
                    Log.e(TAG, TAG + "   wrongpress handle message");
                    TestUtils.wrongPress(TAG, TouchPadTest.this);
                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120921 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120921 add for CR00693542 end
        setContentView(new TouchPadView(this));
        mTouchHandler = new TouchHandler();
        mTouchRunanable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.e(TAG, TAG + "   wrongpress run");
                mTouchHandler.sendEmptyMessage(WRONG_MESSAGE);
            }

        };
        //   mTouchHandler.postDelayed(mTouchRunanable, 60000);
    }

    public class TouchPadView extends View {

        private float mX, mY;
        private Path mPath;
        private Canvas mCanvas;
        private Bitmap mBitmap;
        private Paint mBitmapPaint;
        // Gionee xiaolin 20121113 modify for CR00730261 start
        private Paint mBackGroudPaint, mLinePaint, mPaint, mTestPaint;
        // Gionee xiaolin  20121113 modify for CR00730261 end
        private RectF mRf = new RectF();
        private float[] mVertBaseline = new float[16];
        private float[] mHorBaseline = new float[11];
        private static final float TOUCH_TOLERANCE = 4;
        private ArrayList mLeftList = new ArrayList<Integer>();
        private ArrayList mRightList = new ArrayList<Integer>();
        private ArrayList mTopList = new ArrayList<Integer>();
        private ArrayList mBottomList = new ArrayList<Integer>();
        // Gionee xiaolin  20121113 add for CR00730261 start
        private ArrayList<RectF> mTestRecs = new ArrayList<RectF>();
        private Set<Integer> mIdxTRecsBePassed = new HashSet<Integer>();
        // Gionee xiaolin   20121113 add for CR00730261 end

        public TouchPadView(Context context) {
            super(context);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBackGroudPaint = new Paint();
            mBackGroudPaint.setColor(R.color.test_blue);
            // Gionee xiaolin 20121113 add for CR00730261 start
            mTestPaint = new Paint();
            mTestPaint.setColor(R.color.test_blue);
            mTestPaint.setStyle(Style.STROKE);
            // Gionee xiaolin 20121113 add for CR00730261 end
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Style.STROKE);

            mLinePaint = new Paint();
            mLinePaint.setColor(Color.BLACK);
            // TODO Auto-generated constructor stub
        }

        private void setTestRcts() {
            // TODO Auto-generated method stub
            //Gionee zhangke 20151019 add for CR01571097 start 
            if (FeatureOption.GN_RW_GN_MMI_TP_CROSS_SUPPORT) {
                for (int i = 1; i < 9; i++) {
                    mTestRecs.add(new RectF(mHorBaseline[i], mVertBaseline[i + 2], mHorBaseline[i + 1], mVertBaseline[i + 3]));
                }

                for (int i = 1; i < 9; i++) {
                    mTestRecs.add(new RectF(mHorBaseline[i], mVertBaseline[9 + (2 - i)], mHorBaseline[i + 1], mVertBaseline[10 + (2 - i)]));
                }
            }
            if (FeatureOption.GN_RW_GN_MMI_TP_TEN_SUPPORT) {
                float hPivot = mHorBaseline[5];
                for (int i = 1; i < 14; i++) {
                    if (6 == i || 7 == i)
                        continue;
                    mTestRecs.add(new RectF(hPivot - mAverageWidth / 2, mVertBaseline[i], hPivot + mAverageWidth / 2, mVertBaseline[i + 1]));
                }

                float vHivot = mVertBaseline[7];
                for (int i = 1; i < 9; i++) {
                    if (4 == i || 5 == i)
                        continue;
                    mTestRecs.add(new RectF(mHorBaseline[i], vHivot - mAverageHeight / 2, mHorBaseline[i + 1], vHivot + mAverageHeight / 2));
                }
            }
            //Gionee zhangke 20151019 add for CR01571097 end
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (null == mBitmap) {
                mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
            }
            canvas.drawColor(Color.WHITE);
            if (0 == mAverageHeight) {
                mAverageWidth = getMeasuredWidth() / 10;
                mAverageHeight = getMeasuredHeight() / 15;
                for (int i = 0; i < 14; i++) {
                    mVertBaseline[i] = i * mAverageHeight;
                }
                mVertBaseline[14] = getMeasuredHeight() - mAverageHeight;
                mVertBaseline[15] = getMeasuredHeight();

                for (int i = 0; i < 9; i++) {
                    mHorBaseline[i] = i * mAverageWidth;
                }
                mHorBaseline[9] = getMeasuredWidth() - mAverageWidth;
                mHorBaseline[10] = getMeasuredWidth();
            }
            canvas.drawLine(0, mAverageHeight, getMeasuredWidth(), mAverageHeight, mLinePaint);
            for (int i = 0; i < 12; i++) {
                canvas.drawLine(0, mVertBaseline[i + 2], mAverageWidth, mVertBaseline[i + 2], mLinePaint);
                canvas.drawLine(getMeasuredWidth() - mAverageWidth, mVertBaseline[i + 2], getMeasuredWidth(), mVertBaseline[i + 2], mLinePaint);
            }
            canvas.drawLine(0, getMeasuredHeight() - mAverageHeight, getMeasuredWidth(), getMeasuredHeight() - mAverageHeight, mLinePaint);

            canvas.drawLine(mAverageWidth, 0, mAverageWidth, getMeasuredHeight(), mLinePaint);
            for (int i = 0; i < 7; i++) {
                canvas.drawLine(mHorBaseline[i + 2], 0, mHorBaseline[i + 2], mAverageHeight, mLinePaint);
                canvas.drawLine(mHorBaseline[i + 2], getMeasuredHeight() - mAverageHeight, mHorBaseline[i + 2], getMeasuredHeight(), mLinePaint);
            }
            canvas.drawLine(getMeasuredWidth() - mAverageWidth, 0, getMeasuredWidth() - mAverageWidth, getMeasuredHeight(), mLinePaint);

            if (false == mLeftList.isEmpty()) {
                for (int i = 0; i < mLeftList.size(); i++) {
                    mRf.set(0, mVertBaseline[((Integer) mLeftList.get(i)).intValue()],
                            mAverageWidth,
                            mVertBaseline[((Integer) mLeftList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (false == mRightList.isEmpty()) {
                for (int i = 0; i < mRightList.size(); i++) {
                    mRf.set(getMeasuredWidth() - mAverageWidth, mVertBaseline[((Integer) mRightList.get(i)).intValue()],
                            getMeasuredWidth(),
                            mVertBaseline[((Integer) mRightList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (false == mTopList.isEmpty()) {
                for (int i = 0; i < mTopList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mTopList.get(i)).intValue()], 0,
                            mHorBaseline[((Integer) mTopList.get(i)).intValue() + 1],
                            mAverageHeight);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (false == mBottomList.isEmpty()) {
                for (int i = 0; i < mBottomList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mBottomList.get(i)).intValue()], getMeasuredHeight() - mAverageHeight,
                            mHorBaseline[((Integer) mBottomList.get(i)).intValue() + 1],
                            getMeasuredHeight());
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            //Gionee <xiaolin><2013-04-17>  modify for CR00798303 start
            // Gionee xiaolin 20130115 delete for CR00730261 start
            // Gionee xiaolin 20121113 add for CR00730261 start
            Log.d(TAG, "*********mTestRecs.size() = " + mTestRecs.size());
            if (mTestRecs.size() == 0)
                setTestRcts();


            drawTestRects(canvas);
            for (int idx : mIdxTRecsBePassed) {
                canvas.drawRect(mTestRecs.get(idx), mBackGroudPaint);
            }
            // Gionee xiaolin 20121113 add for CR00730261 end
            // Gionee xiaolin 20130115 delete for CR00730261 end
            //Gionee <xiaolin><2013-04-17>  modify for CR00798303 end
            canvas.drawPath(mPath, mPaint);
        }

        // Gionee xiaolin 20121113 add for CR00730261 start
        private void drawTestRects(Canvas canvas) {
            // TODO Auto-generated method stub
            for (RectF rectf : mTestRecs) {
                canvas.drawRect(rectf, mTestPaint);
                Log.d(TAG, "drawRect : " + rectf);
                if (rectf.isEmpty())
                    Log.d(TAG, rectf + " is isEmpty!!!");
            }
        }

        // Gionee xiaolin 20121113 add for CR00730261 end
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            if (null != mCanvas) {
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                // kill this so we don't double draw
                mPath.reset();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();//是获取相对当前控件（View）的坐标
            float y = event.getY();
            // Gionee xiaolin 20121113 add for CR00730261  start
            Log.d(TAG, "event.getAction()=" + event.getAction() + ";event.getX()=" + x + " ,event.getY()=" + event.getY());
            for (RectF rf : mTestRecs) {
                if (rf.contains(x, y)) {
                    Log.d(TAG, rf + " contains " + "(" + x + " ," + y + ")");
                    int idx = mTestRecs.indexOf(rf);
                    mIdxTRecsBePassed.add(idx);
                    break;
                }
            }
            // Gionee xialin 20121113 add for CR00730261 end
            // Gionee xiaolin 20121018 modify for CR00715724 start
            if (x < mAverageWidth) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mLeftList.contains(i)) {
                    mLeftList.add(i);
                    Log.i(TAG, "mLeftList.add(i) = " + i);
                }
            } else if (x > mHorBaseline[9]) {
                int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                if (-1 != i && false == mRightList.contains(i)) {
                    mRightList.add(i);
                    Log.i(TAG, "mRightList.add(i) = " + i);
                }
            } else if (y < mAverageHeight) {
                int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mTopList.contains(i) && i != 9 && i != 0) {
                    mTopList.add(i);
                    Log.i(TAG, "mTopList.add(i) = " + i);
                }
            } else if (y > mVertBaseline[14]) {
                int i;
                i = gnBinarySearch(x, mHorBaseline, 0, 9);
                if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                    mBottomList.add(i);
                    Log.i(TAG, "mBottomList.add(i) = " + i);
                }
            }
            // Gionee xiaolin 20121018 modify for CR00715724 end


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e(TAG, TAG + " MotionEvent.ACTION_UP");
                    touch_up();
                    invalidate();
                    // Gionee xiaolin 20121113 modify for CR00730261 start
                    //if (46 + 33 == mLeftList.size() + mRightList.size() + mTopList.size()
                    //        + mBottomList.size() + mIdxTRecsBePassed.size()) {
                    //Gionee <xiaolin><2013-04-17>  modify for CR00798303 start
                    if (46 + mTestRecs.size() == mLeftList.size() + mRightList.size() + mTopList.size()
                            + mBottomList.size() + mIdxTRecsBePassed.size()) {
                        //Gionee <xiaolin><2013-04-17>  modify for CR00798303 end
                        // Gionee xiaolin 20121113 modify for CR00730261 end
                        if (isSendMessage) {
                            mTouchHandler.sendEmptyMessage(RIGHT_MESSAGE);
                            Log.e(TAG, TAG + " send right_message");
                            isSendMessage = false;
                        }
                    }
                    break;
            }
            return true;
        }

        public int gnBinarySearch(float elem, float[] array, int low, int high) {
            for (int i = 0; i < array.length - 1; i++) {
                if (elem >= array[i] && elem < array[i + 1]) {
                    return i;
                }
            }
            // Gionee xiaolin 20121018 modify for CR00715724 start
            return -1;
            // Gionee xiaolin 20121018 modify for CR00715724 end
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        return true;
    }
}
