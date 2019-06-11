package com.zoe.player.player.base;

/**
 * author zoe
 * created 2019/4/25 14:08
 */

public class SourceConfigure {

    private boolean cache;//是否需要本地缓存

    private String playUrl;//当前的播放链接

    private String subtitleUrl = "";//字幕链接

    /**
     * 起始播放位置,ms值，只针对exoplayer起作用
     * 主要是为了处理exoplayer再prepare之后才调用seek会先显示资源的第一帧的问题
     */
    private int startPosition=0;

    public SourceConfigure(String playUrl) {
        this.playUrl = playUrl;
    }

    public SourceConfigure(String playUrl, String subtitleUrl) {
        this.playUrl = playUrl;
        this.subtitleUrl = subtitleUrl;
    }


    public SourceConfigure(boolean cache, String playUrl, String subtitleUrl) {
        this.cache = cache;
        this.playUrl = playUrl;
        this.subtitleUrl = subtitleUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public String getSubtitleUrl() {
        return subtitleUrl;
    }

    public boolean isCache() {
        return cache;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getStartPosition() {
        return startPosition;
    }
}
