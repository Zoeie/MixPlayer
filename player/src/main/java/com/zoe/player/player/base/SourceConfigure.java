package com.zoe.player.player.base;

/**
 * author zoe
 * created 2019/4/25 14:08
 */

public class SourceConfigure {

    private boolean cache;//是否需要本地缓存

    private String playUrl;//当前的播放链接

    private String subtitleUrl = "";//字幕链接

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
}
