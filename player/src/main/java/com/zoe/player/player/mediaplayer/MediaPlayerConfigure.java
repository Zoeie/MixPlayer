package com.zoe.player.player.mediaplayer;

import android.view.SurfaceView;

import com.zoe.player.player.base.PlayConfigure;

public class MediaPlayerConfigure extends PlayConfigure {

    public MediaPlayerConfigure(SurfaceView surfaceView) {
        super(surfaceView);
    }

    public MediaPlayerConfigure(SurfaceView surfaceView, int bufferFactor) {
        super(surfaceView, bufferFactor);
    }
}
