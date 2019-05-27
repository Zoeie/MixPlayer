package com.zoe.player.player.base;

import android.view.SurfaceView;

public class PlayConfigure {

    private SurfaceView surfaceView; //渲染的目标View
    private int bufferFactor = 1;//缓冲缩放系数(实际缓冲 = 播放器默认的缓冲值 * bufferFactor)

    public PlayConfigure(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public PlayConfigure(SurfaceView surfaceView, int bufferFactor) {
        this.surfaceView = surfaceView;
        this.bufferFactor = bufferFactor;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public int getBufferFactor() {
        return bufferFactor;
    }
}
