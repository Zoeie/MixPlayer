package com.zoe.player.player.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
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

public class MediaPlayerPlayer implements Player, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, SubtitleEngine.OnSubtitleChangeListener, SubtitleEngine.OnSubtitlePreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String  TAG           = "MediaPlayerPlayer";
    private Context              mContext;
    private PlayListener         mPlayListener;
    private MediaPlayerConfigure mConfigure;
    private MediaPlayer          mMediaPlayer;
    private SurfaceHolder        mHolder;
    private Handler              mHandler;
    private SourceConfigure      mSourceConfigure;
    private int                  mPercent      = -1;
    private SubtitleEngine       mSubtitleEngine;
    private int                  mSeekPosition =-1;//记录每次seek完成后的position
    public static final int      CODE_TIMEOUT = -10000;//播放超时
    public  int                  mTimeout = 30 * 1000;//播放超时时间
    public static final int MSG_CHECK_BUFFER=100;//对比进度是否应该显示缓冲

    MediaPlayerPlayer(Context context, PlayListener playListener, MediaPlayerConfigure mediaPlayerPlayerConfigure) {
        mContext = context;
        mPlayListener = playListener;
        mConfigure = mediaPlayerPlayerConfigure;
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_CHECK_BUFFER:
                        long currentPosition = getCurrentPosition();
                        if (currentPosition>0&&currentPosition==mSeekPosition){
                            Log.d(TAG, "executeProgress:onBufferingStart ");
                            mPlayListener.onBufferingStart();
                        }else if (currentPosition>0&&currentPosition>mSeekPosition){
                            mPlayListener.onBufferingEnd();
                            Log.d(TAG, "executeProgress:onBufferingEnd ");
                            mSeekPosition=-1;
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        buildPlayer();
    }

    @Override
    public void buildPlayer() {
        initSurfaceViewHolder();
        createMediaPlayer();
    }

    private void initSurfaceViewHolder() {
        SurfaceView surfaceView = mConfigure.getSurfaceView();
        if (surfaceView == null) {
            throw new NullPointerException("Need render surface view");
        }
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
    }

    private void createMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void play(SourceConfigure configure) {
        if (configure == null || TextUtils.isEmpty(configure.getPlayUrl())) {
            Log.e(TAG, "播放配置不能为空");
            return;
        }
        mSourceConfigure = configure;
        if (mPlayListener != null) {
            mPlayListener.onPlayPreparing();
        }
        mMediaPlayer.reset();
        try {
            startTimeOutBuffer();
            mMediaPlayer.setDataSource(configure.getPlayUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seekTo(long ms) {
        mMediaPlayer.seekTo((int) ms);
        Log.i(TAG, "seekTo: "+ms);
    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
    }

    @Override
    public void release() {
        cancelTimeOutBuffer();
        mHandler.removeCallbacks(progressAction);
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public long getDuration() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public long getBufferedPosition() {
        return (long) (mPercent * (1.0f) / 100 * getDuration());
    }

    @Override
    public int getBufferedPercentage() {
        return mPercent;
    }

    @Override
    public int getVideoWidth() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getVideoWidth();
        }
        return -1;
    }

    @Override
    public int getVideoHeight() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getVideoHeight();
        }
        return -1;
    }

    @Override
    public VideoFormat getVideoFormat() {
        VideoFormat videoFormat = new VideoFormat();
        if(mMediaPlayer == null) {
            return videoFormat;
        }
        videoFormat.videoWidth = getVideoWidth();
        videoFormat.videoHeight = getVideoHeight();
        MediaPlayer.TrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
        if(trackInfo != null && trackInfo.length > 0) {
            for (MediaPlayer.TrackInfo track : trackInfo) {
                if (track.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                    MediaFormat format = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        format = track.getFormat();
                    }
                    if(format == null) return videoFormat;
                    int tileW = format.getInteger(MediaFormat.KEY_TILE_WIDTH);
                    int tileH = format.getInteger(MediaFormat.KEY_TILE_HEIGHT);
                    videoFormat.videoWidth = tileW;
                    videoFormat.videoHeight = tileH;
                    return videoFormat;
                }
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
        if(mMediaPlayer != null) {
            //不支持切换倍速
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.setDisplay(mHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        cancelTimeOutBuffer();
        if (mPlayListener != null) {
            mPlayListener.onPlayPrepared();
        }
        mMediaPlayer.start();
        executeProgress();
        setSubtitle();
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

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: what:"+what);
        cancelTimeOutBuffer();
        if (mPlayListener != null) {
            mPlayListener.onPlayError(new Exception("MediaPlayer exception"), what);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "onBufferingUpdate: percent:"+percent);
        mPercent = percent;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onInfo: what:"+what+","+isPlaying());
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "onInfo: MEDIA_INFO_BUFFERING_START");
                if (mPlayListener != null) {
                    mPlayListener.onBufferingStart();
                }
                startTimeOutBuffer();
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.d(TAG, "onInfo: MEDIA_INFO_VIDEO_RENDERING_START");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
                if (mPlayListener != null) {
                    mPlayListener.onBufferingEnd();
                }
                cancelTimeOutBuffer();
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://视频编码过于复杂，解码器无法足够快的解码出帧
                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING://音视频错乱传输，视频跟音频不同步
                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING");
                break;
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.e(TAG, "UNKNOWN...");
                break;
        }
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mPlayListener != null) {
            mPlayListener.onSeekProcessed();
            mSeekPosition=mp.getCurrentPosition();
            Log.i(TAG, "onSeekComplete: "+mSeekPosition+"-"+getCurrentPosition());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        cancelTimeOutBuffer();
        if (mPlayListener != null) {
            mPlayListener.onPlayEnd();
        }
    }

    private void executeProgress() {
        long position = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
        // Remove scheduled updates.
        mHandler.removeCallbacks(progressAction);
        long delayMs = PlayConstant.PROGRESS_INTERVAL - (position % PlayConstant.PROGRESS_INTERVAL);
        if (delayMs < 200) {
            delayMs += PlayConstant.PROGRESS_INTERVAL;
        }
        if (mPlayListener != null) {
            mPlayListener.onProgress();
            if (mSeekPosition!=-1){
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_BUFFER,1500);
            }
        }
        mHandler.postDelayed(progressAction, delayMs);
    }

    private final Runnable progressAction = this::executeProgress;

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

    @Override
    public void onSubtitlePrepared(@Nullable List<Subtitle> subtitles) {
        if(mSubtitleEngine != null) {
            mSubtitleEngine.start();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if(mPlayListener != null) {
            mPlayListener.onVideoSizeChanged(width, height);
        }
    }


    /**
     * 启动播放超时监听
     */
    protected void startTimeOutBuffer() {
        if (mHandler !=null){
            mHandler.postDelayed(mTimeOutRunnable, mTimeout);
        }

    }

    /**
     * 取消播放超时监听
     */
    protected void cancelTimeOutBuffer() {
        if (mHandler !=null){
            mHandler.removeCallbacks(mTimeOutRunnable);
        }
        mSeekPosition=-1;
    }

    /**
     * 播放超时
     */
    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayListener != null) {
                Log.w(TAG, "run: play Timeout");
                mPlayListener.onPlayError(new Exception("play Timeout"),CODE_TIMEOUT);
            }
        }
    };


}
