package com.zoe.player.player;

import android.content.Context;

import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.IPlayerFactory;
import com.zoe.player.player.base.IPlayerManager;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.exo.ExoPlayerFactory;
import com.zoe.player.player.ijkplayer.IJKPlayerFactory;
import com.zoe.player.player.mediaplayer.MediaPlayerFactory;

/**
 * author zoe
 * created 2019/4/25 13:57
 */

public class PlayManager implements IPlayerManager {

    private static PlayManager manager;
    private int mPlayerType=PlayConstant.IJK_PLAYER;


    private PlayManager() {
    }

    public static PlayManager getInstance() {
        if (manager == null) {
            manager = new PlayManager();
        }
        return manager;
    }

    @Override
    public Player buildPlayer(Context context, PlayListener listener, int type, PlayConfigure configure) {
        this.mPlayerType=type;
        IPlayerFactory factory = new IJKPlayerFactory(); //默认为IJK
        if (type == PlayConstant.EXO_PLAYER) {
            factory = new ExoPlayerFactory();
        } else if (type == PlayConstant.MEDIA_PLAYER) {
            factory = new MediaPlayerFactory();
        } else if (type == PlayConstant.IJK_PLAYER) {
            factory = new IJKPlayerFactory();
        }
        return factory.getPlayer(context, listener, configure);
    }

    /**
     * 获取当前播放器的type
     * @return
     */
    public int getPlayerType() {
        return mPlayerType;
    }

}
