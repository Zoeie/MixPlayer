package com.zoe.player.player.base;

/**
 * author zoe
 * created 2019/4/25 14:20
 */

public interface PlayListener {

    //正在准备
    void onPlayPreparing();

    //准备完成
    void onPlayPrepared();

    //缓冲开始
    void onBufferingStart();

    //缓冲结束
    void onBufferingEnd();

    //seek结束
    void onSeekProcessed();

    //进度变化
    void onProgress();

    //播放结束
    void onPlayEnd();

    //播放错误码
    void onPlayError(Exception e, int errorCode);

    //字幕变更
    default void onSubtitleChanged(SubtitleData subtitle){}

    //视频宽高
    default void onVideoSizeChanged(int width, int height){}
}
