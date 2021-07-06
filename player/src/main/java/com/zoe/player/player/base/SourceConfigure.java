package com.zoe.player.player.base;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * author zoe
 * created 2019/4/25 14:08
 */

public class SourceConfigure {

    private boolean cache;//是否需要本地缓存

    private final List<String> mediaUrlList = new ArrayList<>();//媒体资源播放链接合集

    private final List<String> subtitleList = new ArrayList<>();//字幕链接

    /**
     * 是否设置代理
     */
    public boolean setProxy = false;

    /**
     * 代理地址
     */
    public String proxyUrl;

    /**
     * 代理端口
     */
    public int proxyPort;

    /**
     * 代理方式
     */
    public Proxy.Type proxyType = Proxy.Type.HTTP;

    /**
     * 起始播放位置,ms值，只针对exoplayer起作用
     * 主要是为了处理exoplayer再prepare之后才调用seek会先显示资源的第一帧的问题
     */
    private int startPosition = 0;

    public SourceConfigure(String playUrl) {
        this.mediaUrlList.add(playUrl);
        this.setProxy = false;
    }

    public SourceConfigure(List<String> mediaUrlList) {
        this.mediaUrlList.addAll(mediaUrlList);
        this.setProxy = false;
    }

    public SourceConfigure(String playUrl, List<String> subtitleList, String proxyUrl, int proxyPort, Proxy.Type proxyType) {
        this.mediaUrlList.add(playUrl);
        this.subtitleList.addAll(subtitleList);
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
        this.proxyType = proxyType;
        this.setProxy = true;
    }

    public SourceConfigure(String playUrl, List<String> subtitleList) {
        this.mediaUrlList.add(playUrl);
        this.subtitleList.addAll(subtitleList);
    }

    public SourceConfigure(boolean cache, String playUrl, List<String> subtitleList) {
        this.cache = cache;
        this.mediaUrlList.add(playUrl);
        this.subtitleList.addAll(subtitleList);
    }

    public List<String> getSubtitleList() {
        return subtitleList;
    }

    public boolean isCache() {
        return cache;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public boolean isSetProxy() {
        return setProxy;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public Proxy.Type getProxyType() {
        return proxyType;
    }

    public List<String> getMediaUrlList() {
        return mediaUrlList;
    }
}
