package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

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
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;

import android.os.Message;
import android.os.SystemProperties;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.os.Looper;
//Gionee zhangke 20151019 add for CR01571097 start
import cy.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end

public class TouchPadTest extends BaseActivity {
    private float mAverageHeight;
    private float mAverageWidth;

    private boolean mIsRestart;
    private boolean isSendMessage = true;
    private static final String TAG = "TouchPadTest";
    private static final int RIGHT_MESSAGE = 0;
    private static final int WRONG_MESSAGE = 1;
    private static final int RESTART_MESSAGE = 2;
    private static final int TOUCH_DOWN_MESSAGE = 3;
    private static final int TOUCH_MOVE_MESSAGE = 4;
    private static final int TOUCH_UP_MESSAGE = 35;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开CTP划线 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(new TouchPadView(this));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DswLog.d(TAG, "\n****************退出CTP划线 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    public class TouchPadView extends View {

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
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();//是获取相对当前控件（View）的坐标
            float y = event.getY();
            DswLog.d(TAG, "event.getAction()=" + event.getAction() + ";event.getX()=" + x + " ,event.getY()=" + event.getY());

            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            float h = mAverageWidth / 2;
            float v = mAverageHeight / 2;
            float cX, cY, dX, dY;
            cX = (x + mX) / 2;
            cY = (y + mY) / 2;
            dX = (cX + mX) / 2;
            dY = (cY + mY) / 2;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touch_start(x, y);

                if (x < mAverageWidth) {
                    int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                    if (-1 != i && !mLeftList.contains(i)) {
                        mLeftList.add(i);
                        DswLog.i(TAG, "mLeftList.add(i) = " + i + " x="+x +" y="+y);
                    }
                } else if (x > mHorBaseline[9]) {
                    int i = gnBinarySearch(y, mVertBaseline, 0, 14);
                    if (-1 != i && !mRightList.contains(i)) {
                        mRightList.add(i);
                        DswLog.i(TAG, "mRightList.add(i) = " + i + " x="+x +" y="+y);
                    }
                } else if (y < mAverageHeight) {
                    int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                    if (-1 != i && !mTopList.contains(i) && i != 9 && i != 0) {
                        mTopList.add(i);
                        DswLog.i(TAG, "mTopList.add(i) = " + i + " x="+x +" y="+y);
                    }
                } else if (y > mVertBaseline[14]) {
                    int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                    if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                        mBottomList.add(i);
                        DswLog.i(TAG, "mBottomList.add(i) = " + i + " x="+x +" y="+y);
                    }
                }else {
                    for (RectF rf : mTestRecs) {
                        if (rf.contains(x, y)) {
                            DswLog.d(TAG, rf + " contains " + "(" + x + " ," + y + ")");
                            int idx = mTestRecs.indexOf(rf);
                            mIdxTRecsBePassed.add(idx);
                            continue;
                        }
                    }

                }
            }else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                if (x < mAverageWidth) {
                    for (int a = 0; a < 3 && a * v <= dy * 1.5; a++) {
                        int i = gnBinarySearch(y - mY > 0 ? mY + a * v : mY - a * v, mVertBaseline, 0, 14);
                        if (-1 != i && !mLeftList.contains(i)) {
                            mLeftList.add(i);
                            DswLog.i(TAG, "mLeftList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }
                } else if (x > mHorBaseline[9]) {
                    for (int a = 0; a < 3 && a * v <= dy * 1.5; a++) {
                        int i = gnBinarySearch(y - mY > 0 ? mY + a * v : mY - a * v, mVertBaseline, 0, 14);
                        if (-1 != i && !mRightList.contains(i)) {
                            mRightList.add(i);
                            DswLog.i(TAG, "mRightList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }

                } else if (y < mAverageHeight) {
                    for (int a = 0; a < 3 && a * h <= dx * 1.5; a++) {
                        int i = gnBinarySearch(x - mX > 0 ? mX + a * h : mX - a * h, mHorBaseline, 0, 9);
                        if (-1 != i && !mTopList.contains(i) && i != 9 && i != 0) {
                            mTopList.add(i);
                            DswLog.i(TAG, "mTopList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }
                } else if (y > mVertBaseline[14]) {
                    for (int a = 0; a < 3 && a * h <= dx * 1.5; a++) {
                        int i = gnBinarySearch(x - mX > 0 ? mX + a * h : mX - a * h, mHorBaseline, 0, 9);
                        if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                            mBottomList.add(i);
                            DswLog.i(TAG, "mBottomList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }
                }else {
                    for (RectF rf : mTestRecs) {
                        if (rf.contains(x, y) || rf.contains(cX, cY) ||  rf.contains(dX, dY) || rf.contains((x + cX) / 2, (y + cY) / 2)) {
                            DswLog.d(TAG, rf + " contains " + "(" + x + " ," + y + ")");
                            int idx = mTestRecs.indexOf(rf);
                            mIdxTRecsBePassed.add(idx);
                            continue;
                        }
                    }

                }

                if (x < mAverageWidth || x > mHorBaseline[9]) {
                    if (y < mAverageHeight)  {
                        int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                        if (-1 != i && !mTopList.contains(i) && i != 9 && i != 0) {
                            mTopList.add(i);
                            DswLog.i(TAG, "#mTopList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }else if (y > mVertBaseline[14]) {
                        int i = gnBinarySearch(x, mHorBaseline, 0, 9);
                        if (-1 != i && false == mBottomList.contains(i) && i != 9 && i != 0) {
                            mBottomList.add(i);
                            DswLog.i(TAG, "#mBottomList.add(i) = " + i + " x="+x +" y="+y);
                        }
                    }
                }
                touch_move(x, y);
            }else if (event.getAction() == MotionEvent.ACTION_UP) {
                touch_up(x, y);
                if (46 + mTestRecs.size() == mLeftList.size() + mRightList.size() + mTopList.size()
                        + mBottomList.size() + mIdxTRecsBePassed.size()) {
                    if (isSendMessage) {
                        TestUtils.rightPress(TAG, TouchPadTest.this);
                        DswLog.e(TAG, TAG + " send right_message");
                        isSendMessage = false;
                        return true;
                    }
                }
            }

            invalidate();
            return true;
        }

        @Override
        public boolean isHardwareAccelerated() {
            return super.isHardwareAccelerated();
        }

        private void setTestRcts() {

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

        }

        private void drawTestRects(Canvas canvas) {

            for (RectF rectf : mTestRecs) {
                canvas.drawRect(rectf, mTestPaint);
                if (rectf.isEmpty())
                    DswLog.d(TAG, rectf + " is isEmpty!!!");
            }
        }

        private void initData() {
            //画边框分隔线
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

            //设置斜边框分隔线
            if (mTestRecs.size() == 0)
                setTestRcts();
        }

        private void initView(Canvas canvas) {
            //画中间黑色斜方块边框
            drawTestRects(canvas);

            //画四周黑色方块边框
            canvas.drawLine(0, mAverageHeight, getMeasuredWidth(), mAverageHeight, mLinePaint);
            canvas.drawLine(0, getMeasuredHeight() - mAverageHeight, getMeasuredWidth(), getMeasuredHeight() - mAverageHeight, mLinePaint);
            canvas.drawLine(mAverageWidth, 0, mAverageWidth, getMeasuredHeight(), mLinePaint);
            canvas.drawLine(getMeasuredWidth() - mAverageWidth, 0, getMeasuredWidth() - mAverageWidth, getMeasuredHeight(), mLinePaint);

            for (int i = 0; i < mVertBaseline.length; i++) {
                canvas.drawLine(0, mVertBaseline[i], mAverageWidth, mVertBaseline[i], mLinePaint);
                canvas.drawLine(getMeasuredWidth() - mAverageWidth, mVertBaseline[i], getMeasuredWidth(), mVertBaseline[i], mLinePaint);
            }

            for (int i = 0; i < mHorBaseline.length; i++) {
                canvas.drawLine(mHorBaseline[i], 0, mHorBaseline[i], mAverageHeight, mLinePaint);
                canvas.drawLine(mHorBaseline[i], getMeasuredHeight() - mAverageHeight, mHorBaseline[i], getMeasuredHeight(), mLinePaint);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {

            if (null == mBitmap) {
                mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);
                mCanvas = new Canvas(mBitmap);
                initData();
            }

            canvas.drawColor(Color.WHITE);

            drawRectView(canvas);

            for (int idx : mIdxTRecsBePassed) {
                canvas.drawRect(mTestRecs.get(idx), mBackGroudPaint);
            }

            initView(canvas);

            canvas.drawPath(mPath, mPaint);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }

        private void drawRectView(Canvas canvas) {
            if (!mLeftList.isEmpty()) {
                for (int i = 0; i < mLeftList.size(); i++) {
                    mRf.set(0, mVertBaseline[((Integer) mLeftList.get(i)).intValue()],
                            mAverageWidth,
                            mVertBaseline[((Integer) mLeftList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (!mRightList.isEmpty()) {
                for (int i = 0; i < mRightList.size(); i++) {
                    mRf.set(getMeasuredWidth() - mAverageWidth, mVertBaseline[((Integer) mRightList.get(i)).intValue()],
                            getMeasuredWidth(),
                            mVertBaseline[((Integer) mRightList.get(i)).intValue() + 1]);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (!mTopList.isEmpty()) {
                for (int i = 0; i < mTopList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mTopList.get(i)).intValue()], 0,
                            mHorBaseline[((Integer) mTopList.get(i)).intValue() + 1],
                            mAverageHeight);
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }

            if (!mBottomList.isEmpty()) {
                for (int i = 0; i < mBottomList.size(); i++) {
                    mRf.set(mHorBaseline[((Integer) mBottomList.get(i)).intValue()], getMeasuredHeight() - mAverageHeight,
                            mHorBaseline[((Integer) mBottomList.get(i)).intValue() + 1],
                            getMeasuredHeight());
                    canvas.drawRect(mRf, mBackGroudPaint);
                }
            }
 
        }


        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_up(float x, float y) {
            if (null != mCanvas) {
                mPath.lineTo(mX, mY);
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
            }
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

        public int gnBinarySearch(float elem, float[] array, int low, int high) {
            for (int i = 0; i < array.length - 1; i++) {
                if (elem >= array[i] && elem < array[i + 1]) {
                    return i;
                }
            }
            return -1;
        }

    }

}
