package com.zoe.player.player.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
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

public class MediaPlayerPlayer implements Player, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, SubtitleEngine.OnSubtitleChangeListener, SubtitleEngine.OnSubtitlePreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "MediaPlayerPlayer";
    private Context mContext;
    private PlayListener mPlayListener;
    private MediaPlayerConfigure mConfigure;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mHolder;
    private Handler handler;
    private SourceConfigure mSourceConfigure;
    private int mPercent = -1;
    private SubtitleEngine mSubtitleEngine;

    MediaPlayerPlayer(Context context, PlayListener playListener, MediaPlayerConfigure mediaPlayerPlayerConfigure) {
        mContext = context;
        mPlayListener = playListener;
        mConfigure = mediaPlayerPlayerConfigure;
        handler = new Handler();
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
            mMediaPlayer.setDataSource(configure.getPlayUrl());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seekTo(long ms) {
        mMediaPlayer.seekTo((int) ms);
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
        handler.removeCallbacks(progressAction);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PlaybackParams playbackParams = mMediaPlayer.getPlaybackParams();
                playbackParams.setSpeed(speed);
                mMediaPlayer.setPlaybackParams(playbackParams);
            }
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
        if (mPlayListener != null) {
            mPlayListener.onPlayError(new Exception("MediaPlayer exception"), what);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mPercent = percent;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mPlayListener != null) {
            mPlayListener.onSeekProcessed();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayListener != null) {
            mPlayListener.onPlayEnd();
        }
    }

    private void executeProgress() {
        long position = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
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
}
