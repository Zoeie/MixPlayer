package com.zoe.playdemo;

import android.os.Bundle;
import android.os.Environment;
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

import com.zoe.playdemo.util.LogUtil;
import com.zoe.playdemo.view.MySeekBar;
import com.zoe.player.player.PlayConstant;
import com.zoe.player.player.PlayManager;
import com.zoe.player.player.base.PlayConfigure;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.SourceConfigure;
import com.zoe.player.player.base.SubtitleData;

import java.io.File;
import java.net.Proxy;
import java.util.Formatter;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private Player iPlayer;
    private boolean flag;
    private RelativeLayout rlMain;
    private float speed = 1;

//        private static final String VIDEO_URL = "http://yfvod.lemmovie.com/vod/48F0ADF7096844F697FB0B5877029AA3/master.m3u8?pt=0";
//        private static final String VIDEO_URL = "http://yflive2.lemmovie.com/cdn/iptv-line.com:6969/live/my.longtv1-restream/izEUgF7l/2457.m3u8";
//        private static final String VIDEO_URL = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
//        private static final String VIDEO_URL = Environment.getExternalStorageDirectory()+ File.separator+"testData"+File.separator+"testdata.m3u8";;
//    private static final String VIDEO_URL = "http://10.11.89.80:8080/ts/media-playlist-test.m3u8";;
//        private static final String VIDEO_URL = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
//    private static final String VIDEO_URL = "https://cdn.letv-cdn.com/2018/12/20/n4s9XBjHUVWz7Bsh/playlist.m3u8";//ijk测试https
//    private static final String VIDEO_URL = "http://gz.long.tv:1977/live2/10.20.63.67:1977/live/afc.m3u8";//加密资源,ts解密后为相对路径
//    private static final String VIDEO_URL = "https://zy.512wx.com/20171130/EI2p4dYT/index.m3u8";
//    private static final String VIDEO_URL = "http://yfvod.lemmovie.com/vod/437332CF8EA1444B8A06FB923CEC9FDC/master.m3u8?pt=0";
//    private static final String VIDEO_URL = "https://www2.yuboyun.com/hls/2018/07/29/SqB3V9P4/playlist.m3u8";
//    private static final String VIDEO_URL = "http://cdn.lemmovie.com/live/1e54d0e5-7b1a-42b0-a4f0-26610627ee4c.m3u8";
//    private static final String VIDEO_URL = "http://10.11.89.80:8080/timeshift/a678c957-ef44-4c5d-a2d9-99003bab05d5/master.m3u8";//时移;
//    private static final String VIDEO_URL = "http://ts.lemmovie.com/a678c957-ef44-4c5d-a2d9-99003bab05d5/master.m3u8";//J2时移
//    private static final String VIDEO_URL = "http://vod.lemmovie.com/vod/0e549fe7-2acf-64b1-dcec-6353d87f17de0.m3u8";//时移 马来西亚 NTV7
//    private static final String VIDEO_URL = "http://10.20.63.222:1977/hls/3_VN_FOX_MOVIES_HD.m3u8";//exo显示第一帧卡住
//    private static final String VIDEO_URL = "http://10.20.63.222:1977/hls/6_World_Match_RU.m3u8";
//    private static final String VIDEO_URL = "http://ts.lemmovie.com/ab2ffad6-acc9-4595-acf3-467626c22fb5/master.m3u8";//AOD时移
//    private static final String VIDEO_URL = "http://ts.lemmovie.com/68cd46a7-3bf1-4406-add7-5474a1d49948/master.m3u8";
    private static final String VIDEO_URL = "http://10.20.63.116:81/lvod/2020/03/00b7272f-ace7-9726-4049-67895d3def04/master.m3u8";//普通点播
    private MySeekBar seekBar;
    private TextView            tvPassTime;
    private TextView            tvBufferTime;
    private boolean             isDragging;
    private TextView tvDuration;
    private String[] mPaths;
    private int pathIndex=0;
    private ProgressBar mPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surface_view);
        rlMain = findViewById(R.id.rl_main);

        tvPassTime = findViewById(R.id.tv_pass_time);
        tvBufferTime = findViewById(R.id.tv_buffer_time);
        tvDuration = findViewById(R.id.tv_duration);

        Button btnSwitch = findViewById(R.id.btn_screen_switch);
        btnSwitch.setOnClickListener(this);

        Button btnSubtitle = findViewById(R.id.btn_subtitle_switch);
        btnSubtitle.setOnClickListener(this);

        Button btnSpeed = findViewById(R.id.btn_speed_switch);
        btnSpeed.setOnClickListener(this);

        Button btnPlay = findViewById(R.id.btn_pause);
        btnPlay.setOnClickListener(this);

        Button btnChange = findViewById(R.id.change_source);
        btnChange.setOnClickListener(this);

        mPb = findViewById(R.id.pb);
        seekBar = findViewById(R.id.sb_progress);
        seekBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int action = event.getAction();
                switch (action) {
                    case KeyEvent.ACTION_DOWN:
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            isDragging = true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        isDragging = false;
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            int progress = seekBar.getProgress();
                            int duration = (int) iPlayer.getDuration();
                            long seekPos = (long) (progress * (1.0f) / 100 * iPlayer.getDuration());
                            iPlayer.seekTo(seekPos);
                            LogUtil.i("duration:"+duration+",seekPos:"+seekPos);
                        }
                        break;
                }
                return false;
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int duration = (int) iPlayer.getDuration();
                long seekPos = (long) (progress * (1.0f) / 100 * iPlayer.getDuration());
                LogUtil.i("duration:"+duration+",seekPos:"+seekPos);
                iPlayer.seekTo(seekPos);
            }
        });

        initPlayer();
    }

    private long bufferTime = System.currentTimeMillis();

    private void initPlayer() {
        PlayManager instance = PlayManager.getInstance();
        iPlayer = instance.buildPlayer(this, new PlayListener() {
            @Override
            public void onPlayPreparing() {
                LogUtil.d("onPlayPreparing");
            }

            @Override
            public void onPlayPrepared() {
                tvDuration.setText(String.format("总时长：%s", formatPlayTime(iPlayer.getDuration())));
                LogUtil.d("onPlayPrepared");
            }

            @Override
            public void onBufferingStart() {
                mPb.setVisibility(View.VISIBLE);
                bufferTime = System.currentTimeMillis();
                LogUtil.d("onBufferingStart");
            }

            @Override
            public void onBufferingEnd() {
                mPb.setVisibility(View.INVISIBLE);
                LogUtil.d("onBufferingEnd time:"+(System.currentTimeMillis() - bufferTime));
            }

            @Override
            public void onSeekProcessed() {
                LogUtil.d("onSeekProcessed");
            }

            @Override
            public void onProgress() {
                if(isDragging) return;
                long currentPos = iPlayer.getCurrentPosition();
                long bufferedPos = iPlayer.getBufferedPosition();
                long duration = iPlayer.getDuration();
                int progress = (int) (currentPos * 100 * (1.0f) / duration + 0.5f);
                int secondaryProgress = (int) (bufferedPos * 100 * (1.0f) / duration + 0.5f);
                seekBar.setProgress(progress);
                seekBar.setSecondaryProgress(secondaryProgress);
                tvPassTime.setText(String.format("当前进度：%s", formatPlayTime(currentPos)));
                tvBufferTime.setText(String.format("缓冲进度：%s", formatPlayTime(bufferedPos)));
//                LogUtil.i("duration:"+duration+",currentPos:"+currentPos+",bufferedPos:"+bufferedPos);
            }

            @Override
            public void onPlayEnd() {
                LogUtil.d("onPlayEnd");
            }

            @Override
            public void onPlayError(Exception e, int errorCode) {
                LogUtil.e("onPlayError："+e.getMessage()+",errorCode:"+errorCode);
            }

            @Override
            public void onSubtitleChanged(SubtitleData subtitle) {
                if(subtitle == null) {
                    LogUtil.d("onSubtitleChanged:"  + null);
                } else {
                    LogUtil.d("onSubtitleChanged："+subtitle.getContent());
                }
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }
        }, PlayConstant.EXO_PLAYER, new PlayConfigure(surfaceView,1,true));
       /* List<String> subtitleList = new ArrayList<>();
        subtitleList.add("http://img.lemmovie.com/sub/Game.of.Thrones.S08E01_cn.srt");
        subtitleList.add("http://img.lemmovie.com/sub/quanyou8_1_track3_en.srt");
        SourceConfigure configure = new SourceConfigure(VIDEO_URL,subtitleList);*/
        SourceConfigure configure = new SourceConfigure(VIDEO_URL/*,null,"127.0.0.1",9050,Proxy.Type.SOCKS*/);

        String path = Environment.getExternalStorageDirectory() + File.separator + "3.ts";
//        SourceConfigure configure = new SourceConfigure(path);
        iPlayer.play(configure);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_screen_switch:
                switchScreen();
                break;
            case R.id.btn_pause:
                if(iPlayer.isPlaying()) {
                    iPlayer.pause();
                } else {
                    iPlayer.start();
                }
                break;
            case R.id.btn_subtitle_switch:
//                iPlayer.switchSubtitle(1);
                break;
            case R.id.btn_speed_switch:
                speed += 0.5f;
                if(speed > 6) {
                    speed = 1;
                }
                LogUtil.d("speed is :" + speed);
                iPlayer.switchSpeed(speed);
                break;
            case R.id.change_source:
                pathIndex++;
                pathIndex%=mPaths.length;
                iPlayer.play(new SourceConfigure(mPaths[pathIndex]));
                break;
        }
    }

    private void switchScreen() {
        if (!flag) {
            enterFullScreen1();
        } else {
            exitFullScreen1();
        }
        flag = !flag;
    }

    @Override
    public void onBackPressed() {
        if(flag) {
            switchScreen();
        } else {
            super.onBackPressed();
        }
    }

    private void enterFullScreen1() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        surfaceView.setLayoutParams(params);
    }

    private void enterFullScreen() {
        removeViewFormParent(surfaceView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        ViewGroup contentView = findViewById(android.R.id.content);
        contentView.addView(surfaceView, params);
    }

    private void exitFullScreen1() {
        RelativeLayout.LayoutParams params  = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = (int) getResources().getDimension(R.dimen.dpi_480);
        params.height = (int) getResources().getDimension(R.dimen.dpi_270);
        surfaceView.setLayoutParams(params);
    }

    private void exitFullScreen() {
        removeViewFormParent(surfaceView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.dpi_480),
                (int) getResources().getDimension(R.dimen.dpi_270)
        );
        rlMain.addView(surfaceView, params);
    }

    public void removeViewFormParent(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    public static String formatPlayTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
