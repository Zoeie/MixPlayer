package com.zoe.player.player.mediaplayer;

import android.content.Context;

import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.IPlayerFactory;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.exo.ExoConfigure;

public class MediaPlayerFactory implements IPlayerFactory {

    @Override
    public Player getPlayer(Context context, PlayListener listener, PlayConfigure configure) {
        MediaPlayerConfigure playerConfigure;
        if (configure instanceof MediaPlayerConfigure) {
            playerConfigure = (MediaPlayerConfigure) configure;
        } else {
            playerConfigure = new MediaPlayerConfigure(configure.getSurfaceView());
        }
        return new MediaPlayerPlayer(context, listener, playerConfigure);
    }

}
