package hk.hku.cs.imitationframe;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * An {@link AppCompatImageView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitImageView extends AppCompatImageView implements View.OnTouchListener {
    private static final String TAG = "AW.AutoFitImageView";
    private int windowWidth, windowHeight;
    private int rotationDegree;

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

        setAlpha(0.5f);
        rotationDegree = 0;
    }

    public void reset() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)getLayoutParams();
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;
        layoutParams.rightMargin = 0;
        setX(0);
        setY(0);
        rotate(0);
        setImageMatrix(new Matrix());
    }

    private static final float MOVEMENT_THRESHOLD = 5.0f;
    private static final float ZOOM_THRESHOLD = 0.05f;

    private float x,y, dx,dy, x0,y0, x1,y1;
    private int originalHeight, originalWidth;

    private float lastX, lastY;
    private double lastDistance;
    private int lastPointerCount;

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

                        x0 = (int) event.getX(p0) + lastX;
                        y0 = (int) event.getY(p0) + lastY;

                        x1 = (int) event.getX(p1) + lastX;
                        y1 = (int) event.getY(p1) + lastY;

                        x = (x0 + x1)/2.0f;
                        y = (y0 + y1)/2.0f;
                        lastDistance = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
                    }

                    originalHeight = layoutParams.height;
                    originalWidth = layoutParams.width;
                    if (originalHeight < 0) originalHeight = windowHeight;
                    if (originalWidth < 0) originalWidth = windowWidth;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1 && lastPointerCount == 1) {
                        dx = (int)event.getRawX() - x;
                        dy = (int)event.getRawY() - y;
                        v.setX(lastX + dx);
                        v.setY(lastY + dy);

                    } else if (event.getPointerCount() == 2 && lastPointerCount == 2) {
                        x0 = (int) event.getX(0) + v.getX();
                        y0 = (int) event.getY(0) + v.getY();

                        x1 = (int) event.getX(1) + v.getX();
                        y1 = (int) event.getY(1) + v.getY();

                        dx = (x0 + x1)/2.0f - x;
                        dy = (y0 + y1)/2.0f - y;

                        double distance = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
                        double ratio = distance / lastDistance;

                        layoutParams.width = (int)(originalWidth * ratio);
                        layoutParams.height = (int)(originalHeight * ratio);

                        Matrix matrix = new Matrix();
                        ImageView imageView = (ImageView) v;
                        imageView.setScaleType(ScaleType.MATRIX);

                        if (dx >= MOVEMENT_THRESHOLD || dy >= MOVEMENT_THRESHOLD) {
                            matrix.postTranslate(dx, dy);
                        }
                        if (Math.abs(1-ratio) >= ZOOM_THRESHOLD) {
                            matrix.postScale((float)ratio, (float)ratio);
                        }

                        matrix.postRotate(rotationDegree);
                        imageView.setImageMatrix(matrix);

                        v.requestLayout();
                        v.invalidate();
                    }

                    lastPointerCount = event.getPointerCount();
                    break;
            }

        } else {
            Log.d(TAG, "ignoring " + event.getPointerCount() + " touches");
            return false;
        }

        return true;
    }

    public void rotate(int degree) {
        rotationDegree = degree;
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(0);
        rotateAnim.setFillAfter(true);
        startAnimation(rotateAnim);
    }

    public int getRotationDegree() {
        return rotationDegree;
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