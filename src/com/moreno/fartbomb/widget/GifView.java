package com.moreno.fartbomb.widget;

import java.io.InputStream;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;

public class GifView extends View {
    private Movie movie;
    private InputStream stream;
    private long movieStart;

    public GifView(Context context, InputStream stream) {
        super(context);
        setInputStream(stream);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now = SystemClock.uptimeMillis();

        if (movieStart == 0) {
            movieStart = now;
        }

        final int relTime = (int) ((now - movieStart) % movie.duration());
        movie.setTime(relTime);
        movie.draw(canvas, 10, 10);
        this.invalidate();
    }

    public void setInputStream(InputStream stream) {
        this.stream = stream;
        this.movie = Movie.decodeStream(this.stream);
    }

}
