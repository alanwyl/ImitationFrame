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
    private int x, y, dx, dy;

    private float lastX, lastY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.getDrawable() == null) return false;

        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = v.getX();
                    lastY = v.getY();
                    x = (int)event.getRawX();
                    y = (int)event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    dx = (int)event.getRawX() - x;
                    dy = (int)event.getRawY() - y;
                    v.setX(lastX + dx);
                    v.setY(lastY + dy);
                    break;
            }

        } else if (event.getPointerCount() == 2) {
            MotionEvent.PointerCoords p = new MotionEvent.PointerCoords();
            event.getPointerCoords(0, p);

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