package com.zoe.player.player.ijkplayer;

import android.content.Context;

import com.zoe.player.player.base.IPlayerFactory;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.base.Player;

public class IJKPlayerFactory implements IPlayerFactory {

    @Override
    public Player getPlayer(Context context, PlayListener listener, PlayConfigure configure) {
        IJKPlayerConfigure ijkPlayerConfigure;
        if(configure instanceof IJKPlayerConfigure) {
            ijkPlayerConfigure = (IJKPlayerConfigure) configure;
        } else {
            ijkPlayerConfigure = new IJKPlayerConfigure(configure.getSurfaceView());
        }
        return new IJKPlayerPlayer(context, listener, ijkPlayerConfigure);
    }

}
