package com.moreno.fartbomb.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RatingBar;

public class VerticalRatingBar extends RatingBar {
    private int x, y, z, w;

    public VerticalRatingBar(Context context) {
        super(context);
    }

    public VerticalRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
    }

    @Override
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
        this.x = w;
        this.y = h;
        this.z = oldw;
        this.w = oldh;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setSelected(true);
            setPressed(true);
            break;
        case MotionEvent.ACTION_MOVE:
            setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            break;
        case MotionEvent.ACTION_UP:
            setSelected(false);
            setPressed(false);
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ProgressBar#setProgress(int)
     */
    @Override
    public synchronized void setProgress(int progress) {

        if (progress >= 0) {
            super.setProgress(progress);
        } else {
            super.setProgress(0);
        }
        onSizeChanged(x, y, z, w);

    }

}
