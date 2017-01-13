package com.moreno.fartbomb.widget;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public abstract class RightDrawableOnTouchListener implements OnTouchListener {
    Drawable drawable;
    private final int fuzz = 10;

    /**
     * @param keyword
     */
    public RightDrawableOnTouchListener(EditText view) {
        super();
        final Drawable[] drawables = view.getCompoundDrawables();
        if (drawables != null && drawables.length == 4) {
            this.drawable = drawables[2];
        }
    }

    public abstract boolean onDrawableTouch(final MotionEvent event);

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = drawable.getBounds();
            if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
                    && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
                return onDrawableTouch(event);
            }
        }
        return false;
    }

}