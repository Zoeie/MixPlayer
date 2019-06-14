package com.zoe.player.player.base;

/**
 * author zoe
 * created 2019/5/9 10:37
 */

public class SubtitleData {

    private int lineAnchor = AnchorType.TYPE_UNSET; //文字的垂直位置(ANCHOR_TYPE_START、ANCHOR_TYPE_MIDDLE、ANCHOR_TYPE_END分别对应上、中、下)
    private int positionAnchor = AnchorType.TYPE_UNSET; //文字的水平位置(ANCHOR_TYPE_START、ANCHOR_TYPE_MIDDLE、ANCHOR_TYPE_END分别对左、中、右)
    private String content; //字幕内容

    public SubtitleData(String content) {
        this.content = content;
    }

    public SubtitleData(int lineAnchor, int positionAnchor, String content) {
        this.lineAnchor = convertLineAnchor(lineAnchor);
        this.positionAnchor = convertPositionAnchor(positionAnchor);
        this.content = content;
    }

    private int convertLineAnchor(int lineAnchor) {
        if(lineAnchor == AnchorType.ANCHOR_TYPE_START) {
            return AnchorType.ANCHOR_TYPE_START;
        } else if(lineAnchor == AnchorType.ANCHOR_TYPE_MIDDLE) {
            return AnchorType.ANCHOR_TYPE_MIDDLE;
        } else if(lineAnchor == AnchorType.ANCHOR_TYPE_END) {
            return AnchorType.ANCHOR_TYPE_END;
        } else {
            return AnchorType.TYPE_UNSET;
        }
    }

    private int convertPositionAnchor(int positionAnchor) {
        if(positionAnchor == AnchorType.ANCHOR_TYPE_START) {
            return AnchorType.ANCHOR_TYPE_START;
        } else if(positionAnchor == AnchorType.ANCHOR_TYPE_MIDDLE) {
            return AnchorType.ANCHOR_TYPE_MIDDLE;
        } else if(positionAnchor == AnchorType.ANCHOR_TYPE_END) {
            return AnchorType.ANCHOR_TYPE_END;
        } else {
            return AnchorType.TYPE_UNSET;
        }
    }

    public int getLineAnchor() {
        return lineAnchor;
    }

    public int getPositionAnchor() {
        return positionAnchor;
    }

    public String getContent() {
        return content;
    }

    public static class AnchorType {
        public static final int TYPE_UNSET = Integer.MIN_VALUE; //未知
        public static final int ANCHOR_TYPE_START = 0;
        public static final int ANCHOR_TYPE_MIDDLE = 1;
        public static final int ANCHOR_TYPE_END = 2;
    }
}
