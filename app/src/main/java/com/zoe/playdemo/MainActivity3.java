package com.zoe.playdemo;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

/**
 * author zoe
 * created 2020/8/20 16:58
 * desc
 */

public class MainActivity3 extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private static final String VIDEO_URL = "/sdcard/SomeoneGreat.mkv";//普通点播

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_3);

        playerView = findViewById(R.id.video_view);
        initializePlayer();
    }

    private void initializePlayer() {
        if (player==null){
            player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            playerView.setPlayer(player);
        }
        Uri uri = Uri.parse(VIDEO_URL);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, false, true);
        player.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri) {
       return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, "userAgent", new DefaultBandwidthMeter()))
               .setExtractorsFactory(new DefaultExtractorsFactory())
               .createMediaSource(uri);
    }

    private int mode = AspectRatioFrameLayout.RESIZE_MODE_FIT;

    public void onClick(View view) {
        if (mode > AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
            mode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        }
        Log.e("xxx","mode:"+mode);
        playerView.setResizeMode(mode);
        mode++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null) {
            player.release();
        }
    }
}
