package com.zoe.playdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * author zoe
 * created 2019/5/23 11:24
 */

public class MySeekBar extends AppCompatSeekBar {
    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEnabled()) {
            int increment = 1;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_MINUS:
                    increment = -increment;
                    // fallthrough
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_PLUS:
                case KeyEvent.KEYCODE_EQUALS:
                    //用于解决TV端存在二级进度时，在同时Progress和SecondaryProgress时，出现Thumb先滑到SecondaryProgress，
                    // 然后再回弹到Progress的界面Bug
                    setProgress(getProgress() + increment);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
