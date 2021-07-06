package com.zoe.playdemo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView surfaceView;
    private Player iPlayer;
    private boolean flag;
    private RelativeLayout rlMain;
    private float speed = 1;
    private static final String VIDEO_URL = "https://r4---sn-i3belney.googlevideo.com/videoplayback?expire=1625575014&ei=BvrjYKjkDYivgAOvnZ6ADw&ip=103.243.94.251&id=o-AGEJ_G0McJHPFDq_PJMnJnK4thPaARDMBCATBzVonvc6&itag=137&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&mh=bO&mm=31%2C26&mn=sn-i3belney%2Csn-5uaezn6y&ms=au%2Conr&mv=m&mvi=4&pl=24&pcm2=yes&initcwndbps=1270000&vprv=1&mime=video%2Fmp4&ns=kGIW4MnkOXUFuiGNEBKZIAYG&gir=yes&clen=75696792&dur=186.019&lmt=1617411458598186&mt=1625553161&fvip=4&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=6316222&n=UmzRYLh9LueOEK&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cpcm2%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRAIgfyKULW5m59m2Q1g2fgNlctuQi8kBiauhn7wJ5LLFDrUCIDtdu6e07AQIvF-5D_L7qeJQSK5UJrTh5Io_oaOWpHKZ&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAIFrgRITpBrpc11Xrve8T60JG0-e8uJsqxAMuKrN7IFZAiEA70v19C-zs6qmfmf_jCL8KVcTRc28Vm7lKVz0WkMpCxA%3D&ratebypass=yes";//视频地址
    private static final String AUDIO_URL = "https://r4---sn-i3belney.googlevideo.com/videoplayback?expire=1625575014&ei=BvrjYKjkDYivgAOvnZ6ADw&ip=103.243.94.251&id=o-AGEJ_G0McJHPFDq_PJMnJnK4thPaARDMBCATBzVonvc6&itag=251&source=youtube&requiressl=yes&mh=bO&mm=31%2C26&mn=sn-i3belney%2Csn-5uaezn6y&ms=au%2Conr&mv=m&mvi=4&pl=24&pcm2=yes&initcwndbps=1270000&vprv=1&mime=audio%2Fwebm&ns=kGIW4MnkOXUFuiGNEBKZIAYG&gir=yes&clen=2853478&dur=186.041&lmt=1617411396036170&mt=1625553161&fvip=4&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=6311222&n=UmzRYLh9LueOEK&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cpcm2%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIge4iqv8-NCJZOzYsYGSKe_f08Z2-LSA7ftnHHGCze1dACIQDDVCwIFCcQQt0xAOeaKkuNryWlUnw044WECL1chZXSMA%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAIFrgRITpBrpc11Xrve8T60JG0-e8uJsqxAMuKrN7IFZAiEA70v19C-zs6qmfmf_jCL8KVcTRc28Vm7lKVz0WkMpCxA%3D&ratebypass=yes";//音频地址
    private static final String SUBTITLE_URL1 = "http://img.lemmovie.com/sub/1_cnen.srt";//普通点播
    private static final String SUBTITLE_URL2 = "http://img.lemmovie.com/sub/seal.team.s04e01_my.srt";//普通点播
    private MySeekBar seekBar;
    private TextView tvPassTime;
    private TextView tvBufferTime;
    private boolean isDragging;
    private EditText etPath;
    private TextView tvDuration;
    private String[] mPaths;
    private int pathIndex = 0;
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
        etPath = findViewById(R.id.et_path);
        etPath.setText(VIDEO_URL);

        Button btnSwitch = findViewById(R.id.btn_screen_switch);
        btnSwitch.setOnClickListener(this);

        Button btnSubtitle = findViewById(R.id.btn_subtitle_switch);
        btnSubtitle.setOnClickListener(this);

        Button btnSpeed = findViewById(R.id.btn_speed_switch);
        btnSpeed.setOnClickListener(this);

        Button btnPause = findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(this);

        Button btnChange = findViewById(R.id.change_source);
        btnChange.setOnClickListener(this);

        Button btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        btnPlay.requestFocus();
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
                            LogUtil.i("duration:" + duration + ",seekPos:" + seekPos);
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
                LogUtil.i("duration:" + duration + ",seekPos:" + seekPos);
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
                LogUtil.d("onBufferingEnd time:" + (System.currentTimeMillis() - bufferTime));
            }

            @Override
            public void onSeekProcessed() {
                LogUtil.d("onSeekProcessed");
            }

            @Override
            public void onProgress() {
                if (isDragging) return;
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
                LogUtil.e("onPlayError：" + e.getMessage() + ",errorCode:" + errorCode);
            }

            @Override
            public void onSubtitleChanged(SubtitleData subtitle) {
                if (subtitle == null) {
                    LogUtil.d("onSubtitleChanged:" + null);
                } else {
                    LogUtil.d("onSubtitleChanged：" + subtitle.getContent());
                }
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }
        }, PlayConstant.EXO_PLAYER, new PlayConfigure(surfaceView, 1, true));
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
                if (iPlayer.isPlaying()) {
                    iPlayer.pause();
                } else {
                    iPlayer.start();
                }
                break;
            case R.id.btn_subtitle_switch:
                iPlayer.switchSubtitle(1);
                break;
            case R.id.btn_speed_switch:
                speed += 0.5f;
                if (speed > 6) {
                    speed = 1;
                }
                LogUtil.d("speed is :" + speed);
                iPlayer.switchSpeed(speed);
                break;
            case R.id.change_source:
                pathIndex++;
                pathIndex %= mPaths.length;
                iPlayer.play(new SourceConfigure(mPaths[pathIndex]));
                break;
            case R.id.btn_play:
                startPlay();
                break;
        }
    }

    private void startPlay() {
        String path = etPath.getText().toString();
//        List<String> subtitleList = new ArrayList<>();
//        subtitleList.add(SUBTITLE_URL1);
        List<String> mediaList = new ArrayList<>();
        mediaList.add(VIDEO_URL);
        mediaList.add(AUDIO_URL);
        SourceConfigure configure = new SourceConfigure(mediaList);
        //SourceConfigure configure = new SourceConfigure(VIDEO_URL/*,null,"127.0.0.1",9050,Proxy.Type.SOCKS*/);
        //configure.setStartPosition(100 * 1000);
        iPlayer.play(configure);
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
        if (flag) {
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
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
