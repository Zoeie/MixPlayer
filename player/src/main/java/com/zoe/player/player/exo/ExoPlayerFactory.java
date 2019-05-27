package com.zoe.player.player.exo;

import android.content.Context;

import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.IPlayerFactory;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;

/**
 * author zoe
 * created 2019/4/25 13:58
 */

public class ExoPlayerFactory implements IPlayerFactory {

    @Override
    public Player getPlayer(Context context, PlayListener listener, PlayConfigure configure) {
        ExoConfigure exoConfigure;
        if (configure instanceof ExoConfigure) {
            exoConfigure = (ExoConfigure) configure;
        } else {
            exoConfigure = new ExoConfigure(configure.getSurfaceView(), configure.getBufferFactor());
        }
        return new ExoPlayer(context, listener, exoConfigure);
    }

}
