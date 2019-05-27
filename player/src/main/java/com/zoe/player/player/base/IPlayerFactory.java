package com.zoe.player.player.base;

import android.content.Context;

/**
 * author zoe
 * created 2019/4/25 13:39
 */

public interface IPlayerFactory {

    Player getPlayer(Context context, PlayListener listener, PlayConfigure playConfigure);

}
