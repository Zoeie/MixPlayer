package com.zoe.playdemo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zoe.playdemo.util.LogUtil;
import com.zoe.playdemo.view.MySeekBar;
import com.zoe.player.player.PlayConstant;
import com.zoe.player.player.PlayManager;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.SourceConfigure;
import com.zoe.player.player.base.SubtitleData;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    private SurfaceView surfaceView;
    private Player iPlayer;
    private static final String VIDEO_URL = "http://vod.lemmovie.com/vod/530517ec-1c6f-9966-2f74-e862c77f887d.m3u8";//普通点播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        surfaceView = findViewById(R.id.surface_view);
        Button btn = findViewById(R.id.btn_test);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterFullScreen1();
            }
        });
        initPlayer();
    }

    private void initPlayer() {
        PlayManager instance = PlayManager.getInstance();
        iPlayer = instance.buildPlayer(this, new PlayListener() {
            @Override
            public void onPlayPreparing() {

            }

            @Override
            public void onPlayPrepared() {

            }

            @Override
            public void onBufferingStart() {

            }

            @Override
            public void onBufferingEnd() {

            }

            @Override
            public void onSeekProcessed() {

            }

            @Override
            public void onProgress() {

            }

            @Override
            public void onPlayEnd() {

            }

            @Override
            public void onPlayError(Exception e, int errorCode) {

            }
        }, PlayConstant.EXO_PLAYER, new PlayConfigure(surfaceView, 1, true));
        SourceConfigure configure = new SourceConfigure(VIDEO_URL);
        iPlayer.play(configure);
    }

    private void enterFullScreen1() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        surfaceView.setLayoutParams(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        iPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        iPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iPlayer.release();
    }
}
