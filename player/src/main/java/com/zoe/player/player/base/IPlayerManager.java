package com.zoe.player.player.base;

import android.content.Context;

/**
 * author zoe
 * created 2019/4/25 13:38
 */

public interface IPlayerManager {

    Player buildPlayer(Context context, PlayListener listener, int type, PlayConfigure configure);

}
