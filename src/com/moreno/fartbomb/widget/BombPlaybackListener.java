package com.moreno.fartbomb.widget;

import java.io.File;

import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;

public class BombPlaybackListener implements OnCompletionListener {
    private final File tmpFile;

    public BombPlaybackListener(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        tmpFile.delete();
        mediaPlayer.release();
    }

}
