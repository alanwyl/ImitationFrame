package hk.hku.cs.imitationframe;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

/**
 * An {@link AppCompatImageView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitImageView extends AppCompatImageView implements View.OnTouchListener {
    private static final String TAG = "AW.AutoFitImageView";
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private int windowWidth, windowHeight;

    public AutoFitImageView(Context context) {
        this(context, null);
    }

    public AutoFitImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);

        DisplayMetrics metrices = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrices);
        windowWidth = metrices.widthPixels;
        windowHeight = metrices.heightPixels;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

     */

    public void reset() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)getLayoutParams();
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;
        layoutParams.rightMargin = 0;

    }

    private static final float MOVEMENT_THRESHOLD = 5.0f;
    private float x, y, dx, dy, x0,y0, x1,y1;
    private int originalHeight, originalWidth;

    private float lastX, lastY;
    private double lastDistance;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.getDrawable() == null) return false;

        Log.d(TAG, "****** onTouch " + event.getPointerCount() + " - " + event.getActionMasked() + " " + MotionEvent.actionToString(event.getActionMasked()));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

        if (event.getPointerCount() <= 2) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    lastX = v.getX();
                    lastY = v.getY();

                    if (event.getPointerCount() == 1) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                    } else if (event.getPointerCount() == 2) {
                        int p0 = event.getPointerId(0);
                        int p1 = event.getPointerId(1);

                        x0 = (int) event.getX(p0);
                        y0 = (int) event.getY(p0);

                        x1 = (int) event.getX(p1);
                        y1 = (int) event.getY(p1);

                        x = (x0 + x1)/2.0f;
                        y = (y0 + y1)/2.0f;
                        lastDistance = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
                    }

                    originalHeight = layoutParams.height;
                    originalWidth = layoutParams.width;
                    if (originalHeight < 0) originalHeight = windowHeight;
                    if (originalWidth < 0) originalWidth = windowWidth;

                    Log.d(TAG, "********** original " + originalHeight + "x" + originalWidth);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        dx = (int)event.getRawX() - x;
                        dy = (int)event.getRawY() - y;

                        v.setX(lastX + dx);
                        v.setY(lastY + dy);

                        Log.d(TAG, "********** points (" + (int)event.getRawX() + "," + (int)event.getRawY() + ")");

                    } else if (event.getPointerCount() == 2) {
                        int p0 = event.getPointerId(0);
                        int p1 = event.getPointerId(1);

                        x0 = (int) event.getX(p0);
                        y0 = (int) event.getY(p0);

                        x1 = (int) event.getX(p1);
                        y1 = (int) event.getY(p1);

                        dx = (x0 + x1)/2.0f - x;
                        dy = (y0 + y1)/2.0f - y;

                        double distance = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
                        Log.d(TAG, "********** distance " + distance);
                        double ratio = distance / lastDistance;
                        Log.d(TAG, "********** ratio " + ratio);


                        layoutParams.height = (int)(originalHeight * ratio);
                        layoutParams.width = (int)(originalWidth * ratio);

                        Log.d(TAG, "********** size " + layoutParams.width + "x" + layoutParams.height);
                        Log.d(TAG, "********** points (" + x0 + "," + y0 + ") (" + x1 + "," + y1 + ")");

                        v.setX((x0 + x1)/2.0f - layoutParams.width/2.0f);
                        v.setY((y0 + y1)/2.0f - layoutParams.height/2.0f);

                        v.requestLayout();
                        v.invalidate();
                    }

                    Log.d(TAG, "********** new position (" + v.getX() + "," + v.getY() + ")");

//                    if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
//
//                    } else {
//                        Log.d(TAG, "********** ignored");
//                    }

//                    v.setX(0); v.setY(0); // just try resizing first
                    break;
            }

        } else {
            Log.d(TAG, "ignoring " + event.getPointerCount() + " touches");
            return false;
        }

        return true;
    }

    // for reference only
    private void rotate(float degree) {
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(0);
        rotateAnim.setFillAfter(true);
        startAnimation(rotateAnim);
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}