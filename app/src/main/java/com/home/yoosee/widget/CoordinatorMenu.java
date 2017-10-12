package com.home.yoosee.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by best on 2017/1/18.
 */

public class CoordinatorMenu extends FrameLayout {
    private static final String TAG = "DragViewGroup";
    private final int mScreenWidth;
    private final int mScreenHeight;

    private View mMenuView;
    private MainView mMainView;

    private ViewDragHelper mViewDragHelper;

    private static final int MENU_CLOSED = 1;
    private static final int MENU_OPENED = 2;
    private int mMenuState = MENU_CLOSED;

    private int mDragOrientation;
    private static final int LEFT_TO_RIGHT = 3;
    private static final int RIGHT_TO_LEFT = 4;

    private static final float SPRING_BACK_VELOCITY = 1500;
    private static final int SPRING_BACK_DISTANCE = 80;
    private int mSpringBackDistance;

    private static final int MENU_MARGIN_RIGHT = 64;
    private int mMenuWidth;

    private static final int MENU_OFFSET = 128;
    private int mMenuOffset;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private static final String DEFAULT_SHADOW_OPACITY = "00";
    private String mShadowOpacity = DEFAULT_SHADOW_OPACITY;

    public CoordinatorMenu(Context context) {
        this(context, null);
    }

    public CoordinatorMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatorMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = getResources().getDisplayMetrics().density;//屏幕密度
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        mSpringBackDistance = (int) (SPRING_BACK_DISTANCE * density + 0.5f);
        System.out.println("CoordinatorMenu5 = " + mSpringBackDistance);

        mMenuOffset = (int) (MENU_OFFSET * density + 0.5f);
        System.out.println("CoordinatorMenu6 = " + mMenuOffset);

        mMenuWidth = mScreenWidth - (int) (MENU_MARGIN_RIGHT * density + 0.5f);
        System.out.println("CoordinatorMenu7 = " + mMenuWidth);

        mViewDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, new CoordinatorCallback());
    }

    private class CoordinatorCallback extends ViewDragHelper.Callback {
        /**
         * 五:处理部分Callback回调
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //侧滑菜单默认是关闭的,用户必定只能先触摸的到上层的主界面
            System.out.println("tryCaptureView = " + pointerId);
            return mMainView == child || mMenuView == child;//告诉ViewDragHelper对哪个子View进行拖动滑动
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            if (capturedChild == mMenuView) {
                System.out.println("onViewCaptured = " + activePointerId);
                mViewDragHelper.captureChildView(mMainView, activePointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {//进行水平方向滑动
            if (left < 0) {
                left = 0;//初始位置是屏幕的左边缘
            } else if (left > mMenuWidth) {
                left = mMenuWidth;//最远的距离就是菜单栏完全展开后的menu的宽度
            }
            System.out.println("clampViewPositionHorizontal = " + left);
            return left;//通常返回left即可，left指代此view的左边缘的位置
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.e(TAG, "onViewReleased: xvel: " + xvel);
            if (mDragOrientation == LEFT_TO_RIGHT) {//从左向右滑
                System.out.println("onViewReleased = " + LEFT_TO_RIGHT);
                if (xvel > SPRING_BACK_VELOCITY || mMainView.getLeft() > mSpringBackDistance) {//小于设定的距离
                    openMenu();//打开菜单
                } else {
                    closeMenu();//否则关闭菜单
                }
            } else if (mDragOrientation == RIGHT_TO_LEFT) {//从右向左滑
                System.out.println("onViewReleased = " + RIGHT_TO_LEFT);
                if (xvel < -SPRING_BACK_VELOCITY || mMainView.getLeft() < mMenuWidth - mSpringBackDistance) {//小于设定的距离
                    closeMenu();//关闭菜单
                } else {
                    openMenu();//否则打开菜单
                }
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.d(TAG, "onViewPositionChanged: dx:" + dx);
            //首先判断滑动的方向： 代表距离上一个滑动时间间隔后的滑动距离
            if (dx > 0) {//正
                System.out.println("onViewPositionChanged = " + dx);
                mDragOrientation = LEFT_TO_RIGHT;//从左往右
            } else if (dx < 0) {//负
                System.out.println("onViewPositionChanged = " + dx);
                mDragOrientation = RIGHT_TO_LEFT;//从右往左
            }
            float scale = (float) (mMenuWidth - mMenuOffset) / (float) mMenuWidth;
            System.out.println("onViewPositionChanged = " + scale);
            int menuLeft = left - ((int) (scale * left) + mMenuOffset);
            System.out.println("onViewPositionChanged = " + menuLeft);
            mMenuView.layout(menuLeft, mMenuView.getTop(), menuLeft + mMenuWidth, mMenuView.getBottom());

            //其中这个mShadowOpacity是随main的位置变化而变化的：
            float showing = (float) (mScreenWidth - left) / (float) mScreenWidth;
            int hex = 255 - Math.round(showing * 255);
            if (hex < 16) {
                mShadowOpacity = "0" + Integer.toHexString(hex);
                System.out.println("onViewPositionChanged = " + mShadowOpacity);
            } else {
                System.out.println("onViewPositionChanged = " + mShadowOpacity);
                mShadowOpacity = Integer.toHexString(hex);
            }
        }
    }

    /**
     * 一:确定目标及方向
     * 加载布局后拿到两个子View
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        System.out.println("onDetachedFromWindow = ");
        mMenuView = getChildAt(0);//第一个子View在底层，作为menu
        mMainView = (MainView) getChildAt(1);//第二个子View在上层，作为main
        mMainView.setParent(this);
    }

    /**
     * 三:然后拦截触摸事件，交给我们的主角ViewDragHelper处理：
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        System.out.println("onInterceptTouchEvent = 拦截");
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递给ViewDragHelper，此操作必不可少
        mViewDragHelper.processTouchEvent(event);
        System.out.println("onTouchEvent");
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        System.out.println("onDraw");
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        MarginLayoutParams menuParams = (MarginLayoutParams) mMenuView.getLayoutParams();
        menuParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuParams);
        if (mMenuState == MENU_OPENED) {//判断菜单的状态为打开的话
            System.out.println("onLayout = " + MENU_OPENED);
            //保持打开的位置
            mMenuView.layout(0, 0, mMenuWidth, bottom);
            mMainView.layout(mMenuWidth, 0, mMenuWidth + mScreenWidth, bottom);
            return;
        }
        mMenuView.layout(-mMenuOffset, top, mMenuWidth - mMenuOffset, bottom);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int restoreCount = canvas.save();//保存画布当前的剪裁信息

        final int height = getHeight();
        System.out.println("drawChild = " + height);
        final int clipLeft = 0;
        int clipRight = mMainView.getLeft();
        System.out.println("drawChild = " + clipRight);
        if (child == mMenuView) {
            canvas.clipRect(clipLeft, 0, clipRight, height);//剪裁显示的区域
        }

        boolean result = super.drawChild(canvas, child, drawingTime);//绘制当前view

        //恢复画布之前保存的剪裁信息    以正常绘制之后的view
        canvas.restoreToCount(restoreCount);

        int shadowLeft = mMainView.getLeft();
        Log.d(TAG, "drawChild: shadowLeft: " + shadowLeft);
        final Paint shadowPaint = new Paint();
        Log.d(TAG, "drawChild: mShadowOpacity: " + mShadowOpacity);
        shadowPaint.setColor(Color.parseColor("#" + mShadowOpacity + "777777"));
        shadowPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(shadowLeft, 0, mScreenWidth, mScreenHeight, shadowPaint);
        return result;
    }

    /**
     * 四:处理computeScroll方法：
     * <p>
     * 滑动过程中调用
     */
    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);//处理刷新，实现平滑移动
        }
        //获取菜单的状态
        if (mMainView.getLeft() == 0) {
            mMenuState = MENU_CLOSED;
            System.out.println("computeScroll = " + mMainView.getLeft());
        } else if (mMainView.getLeft() == mMenuWidth) {
            mMenuState = MENU_OPENED;
            System.out.println("computeScroll = " + mMainView.getLeft());
        }
    }

    public void openMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, mMenuWidth, 0);
        ViewCompat.postInvalidateOnAnimation(CoordinatorMenu.this);
    }

    public void closeMenu() {
        mViewDragHelper.smoothSlideViewTo(mMainView, 0, 0);
        ViewCompat.postInvalidateOnAnimation(CoordinatorMenu.this);
    }

    public boolean isOpened() {
        return mMenuState == MENU_OPENED;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);
        ss.menuState = mMenuState;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.menuState == MENU_OPENED) {
            openMenu();
        }
    }

    private static class SavedState extends AbsSavedState {
        int menuState;

        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            menuState = in.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(menuState);
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        System.out.println("createFromParcel = " + loader);
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        System.out.println("newArray = " + size);
                        return new SavedState[size];
                    }
                });
    }
}