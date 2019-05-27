package com.zoe.player.player.base;

/**
 * author zoe
 * created 2019/5/9 10:37
 */

public class SubtitleData {

    private int region; // 字幕位置 (0:底部 1:顶部)
    private String content; //字幕内容

    public SubtitleData(int region, String content) {
        this.region = region;
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
