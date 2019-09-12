package com.zoe.player.player.ijkplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zoe.player.player.PlayConstant;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.SourceConfigure;
import com.zoe.player.player.base.SubtitleData;
import com.zoe.player.player.module.VideoFormat;
import com.zoe.player.player.subtitle.DefaultSubtitleEngine;
import com.zoe.player.player.subtitle.SubtitleEngine;
import com.zoe.player.player.subtitle.model.Subtitle;

import java.io.IOException;
import java.util.List;

import androidx.annotation.Nullable;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaFormat;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

public class IJKPlayerPlayer implements Player, SurfaceHolder.Callback, SubtitleEngine.OnSubtitlePreparedListener, SubtitleEngine.OnSubtitleChangeListener {
    private static final String TAG = "IJKPlayerPlayer";
    private PlayListener mPlayListener;

    private Context mContext;

    private IJKPlayerConfigure mConfigure;
    private boolean mEnableMediaCodec = true;
    private SurfaceHolder mHolder;
    private IjkMediaPlayer ijkMediaPlayer;
    private int mPercent = -1;
    private Handler handler;
    private DefaultSubtitleEngine mSubtitleEngine;
    private SourceConfigure mSourceConfigure;//当前的播放信息

    public IJKPlayerPlayer(Context context, PlayListener playListener, IJKPlayerConfigure ijkPlayerConfigure) {
        mContext = context;
        mPlayListener = playListener;
        mConfigure = ijkPlayerConfigure;
        handler = new Handler();
        buildPlayer();
    }

    @Override
    public void buildPlayer() {
        ijkMediaPlayer = createPlayer();
        initSurfaceView();
    }

    private void initSurfaceView() {
        SurfaceView surfaceView = mConfigure.getSurfaceView();
        if (surfaceView == null) {
            throw new NullPointerException("Need render surface view");
        }
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
    }

    //创建一个新的player
    private IjkMediaPlayer createPlayer() {
        IjkMediaPlayer player = new IjkMediaPlayer();
        setOption(player);
        player.setVolume(1.0f, 1.0f);
        player.setScreenOnWhilePlaying(true);

        setEnableMediaCodec(player, mEnableMediaCodec);
        return player;
    }

    /**
     * 设置播放器配置参数
     * @param player
     */
    private void  setOption(IjkMediaPlayer player){
        if (player==null) return;
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);//使能opensles功能（音频）,默认值0

        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5); //丢帧  是在视频帧处理不过来的时候丢弃一些帧达到同步的效果
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);//准备好之后自动播放

        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1);

        //设置是否开启环路过滤。0：开启，画面质量高，解码开销大。48：关闭，画面质量差点，解码开销小
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 100); //最小加载100帧才显示
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 0);//设置为不精准seek，1为精准seek

        //设置缓存
        //        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_file_path","sdcard/a.temp");
        //        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "cache_map_path","sdcard/b.temp");
        //        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "parse_cache_map", 1);
        //        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "auto_save_map", 1);

        //m3u8本地播放问题
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,hls");

        //解决m3u8文件拖动问题 比如:一个3个多少小时的音频文件，开始播放几秒中，然后拖动到2小时左右的时间，要loading 10分钟
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");// 设置seekTo能够快速seek到指定位置并播放
        //播放前的探测Size，默认是1M, 改小一点会出画面更快
//        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 1);

        //设置播放前的探测时间 1,达到首屏秒开效果
//        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzeduration",1);

        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"reconnect",3); //播放重连次数
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"packet-buffering",0); //开启/关闭 缓冲
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"infbuf",1); //是否无限读
        //ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);//清空dns
    }




    //设置ijkplayer的监听
    private void setListener(final IMediaPlayer player){
        player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Log.d(TAG, "onPrepared: ");
                if(mPlayListener != null) {
                    mPlayListener.onPlayPrepared();
                }
                executeProgress();
                start();
                setSubtitle();
            }
        });
        player.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                if(mPlayListener != null) {
                    mPlayListener.onVideoSizeChanged(width, height);
                }
            }
        });
        player.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                IJKPlayerPlayer.this.mPercent = percent;
            }
        });
        player.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                Log.e(TAG, "onSeekComplete: " );
                if(mPlayListener != null) {
                    mPlayListener.onSeekProcessed();
                }
            }
        });
        player.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Log.d(TAG, "onCompletion: ");
                if(mPlayListener != null) {
                    mPlayListener.onPlayEnd();
                }
            }
        });
        player.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                Log.e("IJKPLayer", "what:" + what + ",extra:" + extra);
                if (mPlayListener != null) {
                    mPlayListener.onPlayError(new Exception("IJKPlayer exception"), what);
                }
                return true;
            }
        });
        player.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
//                int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频准备渲染
//                int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲
//                int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
//                int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频选择信息
//                int MEDIA_ERROR_SERVER_DIED = 100;//视频中断，一般是视频源异常或者不支持的视频类型。
//                int MEDIA_ERROR_IJK_PLAYER = -10000,//一般是视频源有问题或者数据格式不支持，比如音频不是AAC之类的
//                int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.d("IJKPLayer","MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.d("IJKPLayer","MEDIA_INFO_VIDEO_RENDERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        if(mPlayListener != null) {
                            mPlayListener.onBufferingStart();
                        }
                        Log.d("IJKPLayer","MEDIA_INFO_BUFFERING_START:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if(mPlayListener != null) {
                            mPlayListener.onBufferingEnd();
                        }
                        Log.d("IJKPLayer","MEDIA_INFO_BUFFERING_END:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        Log.d("IJKPLayer","MEDIA_INFO_NETWORK_BANDWIDTH: ");
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        Log.d("IJKPLayer","MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.d("IJKPLayer","MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        Log.d("IJKPLayer","MEDIA_INFO_METADATA_UPDATE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        Log.d("IJKPLayer","MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        Log.d("IJKPLayer","MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                        Log.d("IJKPLayer","MEDIA_INFO_VIDEO_ROTATION_CHANGED: ");
                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        Log.d("IJKPLayer","MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                }
                return true;
            }
        });
    }

    private void setSubtitle() {
        if(mSourceConfigure != null && mSourceConfigure.getSubtitleList() != null
                && mSourceConfigure.getSubtitleList().size() > 0 && !TextUtils.isEmpty(mSourceConfigure.getSubtitleList().get(0))) {
            createSubtitle();
            mSubtitleEngine.bindToMediaPlayer(this);
            mSubtitleEngine.setSubtitlePath(mSourceConfigure.getSubtitleList().get(0));
        }
    }

    private void createSubtitle() {
        mSubtitleEngine = new DefaultSubtitleEngine();
        mSubtitleEngine.setOnSubtitlePreparedListener(this);
        mSubtitleEngine.setOnSubtitleChangeListener(this);
    }

    //设置是否开启硬解码
    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value);//开启硬解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value);
    }

    public void setEnableMediaCodec(boolean isEnable){
        mEnableMediaCodec = isEnable;
    }

    @Override
    public boolean isPlaying() {
        if(ijkMediaPlayer != null) {
            return ijkMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void play(SourceConfigure configure) {
        if (configure == null || TextUtils.isEmpty(configure.getPlayUrl())) {
            Log.e(TAG, "播放配置不能为空");
            return;
        }
        if (mPlayListener != null) {
            mPlayListener.onPlayPreparing();
        }
        mSourceConfigure = configure;
        resetplayer();
        setListener(ijkMediaPlayer);
        try {
            if(mHolder != null) {
                ijkMediaPlayer.setDisplay(mHolder);
            }
            ijkMediaPlayer.setDataSource(mContext, Uri.parse(configure.getPlayUrl()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ijkMediaPlayer.prepareAsync();
    }

    private void resetplayer(){
        ijkMediaPlayer.reset();
        setOption(ijkMediaPlayer);
    }



    @Override
    public void seekTo(long ms) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.seekTo(ms);
        }
    }

    @Override
    public void start() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
        }
    }

    @Override
    public void release() {
        handler.removeCallbacks(progressAction);
        if(mSubtitleEngine != null) {
            mSubtitleEngine.destroy();
        }
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
            ijkMediaPlayer.release();
            ijkMediaPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public long getDuration() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public long getBufferedPosition() {
        if (ijkMediaPlayer != null) {
            return (long) (mPercent *(1.0f) / 100 * ijkMediaPlayer.getDuration());
        }
        return -1;
    }

    @Override
    public int getBufferedPercentage() {
        return mPercent;
    }

    @Override
    public int getVideoWidth() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getVideoWidth();
        }
        return -1;
    }

    @Override
    public int getVideoHeight() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getVideoHeight();
        }
        return -1;
    }

    @Override
    public VideoFormat getVideoFormat() {
        VideoFormat videoFormat = new VideoFormat();
        if(ijkMediaPlayer != null) {
            return videoFormat;
        }
        videoFormat.videoWidth = getVideoWidth();
        videoFormat.videoHeight = getVideoHeight();
        IjkTrackInfo[] trackInfo = ijkMediaPlayer.getTrackInfo();
        if(trackInfo != null && trackInfo.length > 0) {
            for (IjkTrackInfo ijkTrackInfo : trackInfo) {
                IMediaFormat format = ijkTrackInfo.getFormat();
                //TODO format.getString("key");
            }
        }
        return videoFormat;
    }

    @Override
    public SourceConfigure getCurrentPlayInfo() {
        return mSourceConfigure;
    }

    @Override
    public void switchSubtitle(int index) {

    }

    @Override
    public void switchSpeed(float speed) {
        if(ijkMediaPlayer != null) {
            ijkMediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        if(mHolder != null) {
            ijkMediaPlayer.setDisplay(mHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void executeProgress() {
        long position = ijkMediaPlayer == null ? 0 : ijkMediaPlayer.getCurrentPosition();
        // Remove scheduled updates.
        handler.removeCallbacks(progressAction);
        long delayMs = PlayConstant.PROGRESS_INTERVAL - (position % PlayConstant.PROGRESS_INTERVAL);
        if (delayMs < 200) {
            delayMs += PlayConstant.PROGRESS_INTERVAL;
        }
        if (mPlayListener != null) {
            mPlayListener.onProgress();
        }
        handler.postDelayed(progressAction, delayMs);
    }

    private final Runnable progressAction = new Runnable() {
        @Override
        public void run() {
            executeProgress();
        }
    };

    @Override
    public void onSubtitlePrepared(@Nullable List<Subtitle> subtitles) {
        if(mSubtitleEngine != null) {
            mSubtitleEngine.start();
        }
    }

    @Override
    public void onSubtitleChanged(@Nullable Subtitle subtitle) {
        if (subtitle == null) {
            if (mPlayListener != null) {
                mPlayListener.onSubtitleChanged(null);
            }
            return;
        }
        SubtitleData data = new SubtitleData(subtitle.content);
        if (mPlayListener != null) {
            mPlayListener.onSubtitleChanged(data);
        }
    }
}
