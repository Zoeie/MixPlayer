package com.zoe.android.exoplayer2.upstream;

import java.io.IOException;


/**
 * author zoe
 * created 2020/7/17 10:11
 */

public class BufferEmptyException extends IOException {

    public BufferEmptyException() {
    }

    public BufferEmptyException(String message) {
        super(message);
    }
}
