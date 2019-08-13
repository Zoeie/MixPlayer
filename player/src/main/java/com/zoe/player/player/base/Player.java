package com.zoe.player.player.base;

import com.zoe.player.player.module.VideoFormat;

/**
 * author zoe
 * created 2019/4/25 13:40
 *
 * 该接口用于对外与UI进行交互
 */

public interface Player {

    void buildPlayer();

    boolean isPlaying();

    void play(SourceConfigure configure);

    void seekTo(long ms);

    void start();

    void pause();

    void stop();

    void release();

    long getCurrentPosition();

    long getDuration();

    long getBufferedPosition();

    int getBufferedPercentage();

    int getVideoWidth();

    int getVideoHeight();

    //视频格式信息
    VideoFormat getVideoFormat();

    /**
     * 获取当前的相关播放信息
     * @return
     */
    SourceConfigure getCurrentPlayInfo();

    /**
     * 切换字幕
     * @param index 需要切换字幕的索引
     */
    void switchSubtitle(int index);

    void switchSpeed(float speed);
}
