package com.zoe.player.player.ijkplayer;

import android.view.SurfaceView;

import com.zoe.player.player.base.PlayConfigure;

public class IJKPlayerConfigure extends PlayConfigure {

    public IJKPlayerConfigure(SurfaceView surfaceView) {
        super(surfaceView);
    }

    public IJKPlayerConfigure(SurfaceView surfaceView, int bufferFactor) {
        super(surfaceView, bufferFactor);
    }
}
