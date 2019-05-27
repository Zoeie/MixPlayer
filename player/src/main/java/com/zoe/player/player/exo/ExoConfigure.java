package com.zoe.player.player.exo;

import android.view.SurfaceView;

import com.zoe.player.player.base.PlayConfigure;

/**
 * author zoe
 * created 2019/4/25 13:43
 */

public class ExoConfigure extends PlayConfigure {

    public ExoConfigure(SurfaceView surfaceView) {
        super(surfaceView);
    }

    public ExoConfigure(SurfaceView surfaceView, int bufferFactor) {
        super(surfaceView, bufferFactor);
    }
}
