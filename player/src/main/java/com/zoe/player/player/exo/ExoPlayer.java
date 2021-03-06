package com.zoe.player.player.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zoe.android.exoplayer2.C;
import com.zoe.android.exoplayer2.ExoPlaybackException;
import com.zoe.android.exoplayer2.Format;
import com.zoe.android.exoplayer2.PlaybackParameters;
import com.zoe.android.exoplayer2.SimpleExoPlayer;
import com.zoe.android.exoplayer2.Timeline;
import com.zoe.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.zoe.android.exoplayer2.source.ExtractorMediaSource;
import com.zoe.android.exoplayer2.source.MediaSource;
import com.zoe.android.exoplayer2.source.MergingMediaSource;
import com.zoe.android.exoplayer2.source.SingleSampleMediaSource;
import com.zoe.android.exoplayer2.source.TrackGroupArray;
import com.zoe.android.exoplayer2.source.hls.HlsMediaSource;
import com.zoe.android.exoplayer2.text.Cue;
import com.zoe.android.exoplayer2.text.TextOutput;
import com.zoe.android.exoplayer2.trackselection.TrackSelectionArray;
import com.zoe.android.exoplayer2.upstream.DataSource;
import com.zoe.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.zoe.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.zoe.android.exoplayer2.util.MimeTypes;
import com.zoe.android.exoplayer2.util.Util;
import com.zoe.android.exoplayer2.video.VideoListener;
import com.zoe.player.player.PlayConstant;
import com.zoe.player.player.base.PlayListener;
import com.zoe.player.player.base.Player;
import com.zoe.player.player.base.SourceConfigure;
import com.zoe.player.player.base.SubtitleData;
import com.zoe.player.player.exo.error.MyLoadErrorHandlingPolicy;
import com.zoe.player.player.exo.factory.CustomDataSourceFactory;
import com.zoe.player.player.module.VideoFormat;

import java.util.List;

/**
 * author zoe
 * created 2019/4/25 13:40
 */

public class ExoPlayer implements Player {

    private static final String TAG = "ExoPlayer";

    private ExoPlayerHelper exoPlayerHelper;
    private Context mContext;
    private PlayListener mPlayListener;
    private SimpleExoPlayer exoPlayer;
    private ExoConfigure mExoConfigure;
    private boolean isPrepared = false;
    private boolean isBuffering = false;
    private Handler handler;
    private static String DEFAULT_CACHE_PATH = "sdcard/exo"; //默认的缓存路径
    private static final int MAX_FILE_SIZE = 25 * 1024 * 1024;
    private SourceConfigure mCurrentPlayInfo=null;//当前播放节目的信息
    private String userAgent;

    public ExoPlayer(Context context, PlayListener playListener, ExoConfigure exoConfigure) {
        exoPlayerHelper = new ExoPlayerHelper();
        handler = new Handler(Looper.myLooper());
        mContext = context;
        mPlayListener = playListener;
        mExoConfigure = exoConfigure;
        buildPlayer();
    }

    @Override
    public void buildPlayer() {
        exoPlayer = exoPlayerHelper.buildExoPlayer(mContext, mExoConfigure);
        exoPlayer.addVideoListener(videoListener);
        exoPlayer.addListener(eventListener);
        exoPlayer.addTextOutput(mOutput);
        userAgent = Util.getUserAgent(mContext, "ExoPlayer");
    }

    @Override
    public boolean isPlaying() {
        if (exoPlayer == null) {
            Log.e(TAG, "exo player is null");
            return false;
        }
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void play(SourceConfigure configure) {
        if (configure == null || configure.getMediaUrlList().size() == 0) {
            Log.e(TAG, "至少需要有一个播放的媒体资源");
            return;
        }
        mCurrentPlayInfo = configure;
        //Prepare the player with the source.
        isPrepared = false;
        if (mPlayListener != null) {
            mPlayListener.onPlayPreparing();
        }
        //TODO 开启缓存待开发 (有坑)
        exoPlayer.prepare(getMediaSource(configure));
        exoPlayer.setPlayWhenReady(true);
        int startPosition = configure.getStartPosition();
        if (startPosition>=0){
            exoPlayer.seekTo(startPosition);
        }
    }

    private MediaSource getMediaSource(SourceConfigure configure) {
        //添加媒体资源
        List<String> mediaList = configure.getMediaUrlList();
        //添加字幕
        List<String> subtitleList = configure.getSubtitleList();
        Log.d(TAG, "media size:" + mediaList.size() + ",subtitle size:" + subtitleList.size());
        MediaSource[] mediaSources = new MediaSource[mediaList.size() + subtitleList.size()];
        for (int i = 0; i < mediaList.size(); i++) {
            mediaSources[i] = getMediaSource(mediaList.get(i), configure);
        }
        for (int i = 0; i < subtitleList.size(); i++) {
            mediaSources[i + mediaList.size()] = getSubtitleSource(subtitleList.get(i));
        }
        return new MergingMediaSource(mediaSources);
    }

    private MediaSource getMediaSource(String mediaUrl, SourceConfigure configure) {
        MediaSource mediaSource;
        int contentType = inferContentType(mediaUrl);
        DataSource.Factory factory;
        //测量播放过程中的带宽。 如果不需要，可以为null。
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        if (contentType == C.TYPE_HLS) {
            if (configure.setProxy) {
                factory = new CustomDataSourceFactory(mContext, userAgent, bandwidthMeter,
                        configure.proxyUrl, configure.proxyPort, configure.proxyType);
            } else {
                factory = new CustomDataSourceFactory(mContext, userAgent, bandwidthMeter);//针对HLS，自定义ts地址的加密以及解密
            }
        } else {
            factory = new DefaultDataSourceFactory(mContext, userAgent, bandwidthMeter);//非HLS，使用默认的Exo原生资源工厂
        }
        if (contentType == C.TYPE_HLS) {
            mediaSource = new HlsMediaSource.Factory(factory).setLoadErrorHandlingPolicy(new MyLoadErrorHandlingPolicy())
                    .createMediaSource(Uri.parse(mediaUrl));
        } else {
            mediaSource = new ExtractorMediaSource.Factory(factory)
                    .setExtractorsFactory(new DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(mediaUrl));
        }
        return mediaSource;
    }

    @C.ContentType
    public static int inferContentType(String fileName) {
        fileName = Util.toLowerInvariant(fileName);
        if (fileName.endsWith(".mpd")) {
            return C.TYPE_DASH;
        } else if (fileName.contains(".m3u8")) {
            return C.TYPE_HLS;
        } else if (fileName.endsWith(".ism") || fileName.endsWith(".isml")
                || fileName.endsWith(".ism/manifest") || fileName.endsWith(".isml/manifest")) {
            return C.TYPE_SS;
        } else {
            return C.TYPE_OTHER;
        }
    }

    @NonNull
    private MediaSource getSubtitleSource(String subtitleUrl) {
        String mimeType;
        if (subtitleUrl.endsWith(".srt")) {
            mimeType = MimeTypes.APPLICATION_SUBRIP;
        } else if (subtitleUrl.endsWith(".ass") || subtitleUrl.endsWith(".ssa")) {
            mimeType = MimeTypes.TEXT_SSA;
        } else {
            mimeType = MimeTypes.APPLICATION_SUBRIP;
        }
        Format subtitleFormat = Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                mimeType, // The mime type. Must be set correctly.
                C.SELECTION_FLAG_DEFAULT, // Selection flags for the track.
                getSubtitleLanguage(subtitleUrl)); // The subtitle language. May be null.
        DataSource.Factory factory = new DefaultDataSourceFactory(mContext, userAgent);
        return new SingleSampleMediaSource.Factory(factory)
                .createMediaSource(Uri.parse(subtitleUrl), subtitleFormat, C.TIME_UNSET);
    }

    //通过链接判断是什么语言的字幕
    private String getSubtitleLanguage(String subtitleUrl) {
        if (TextUtils.isEmpty(subtitleUrl)) return null;
        int crossIndex = subtitleUrl.lastIndexOf("_");
        int dotIndex = subtitleUrl.lastIndexOf('.');
        int startIndex = crossIndex + 1;
        int endIndex = dotIndex;
        if (startIndex > endIndex) {
            endIndex = startIndex;
        }
        return subtitleUrl.substring(startIndex, endIndex);//截取拓展名前面最后的两个字符，作为字幕的语言
    }

    private final TextOutput mOutput = new TextOutput() {
        @Override
        public void onCues(List<Cue> cues) {
            if (cues != null && cues.size() > 0) {
                if (mPlayListener != null) {
                    Cue cue = cues.get(0);
                    int lineAnchor = cue.lineAnchor;
                    int positionAnchor = cue.positionAnchor;
                    String text = cue.text.toString();
                    SubtitleData data = new SubtitleData(lineAnchor, positionAnchor, text);
                    mPlayListener.onSubtitleChanged(data);
                }
            } else {
                if (mPlayListener != null) {
                    mPlayListener.onSubtitleChanged(null);
                }
            }
        }
    };

    private final VideoListener videoListener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            if(mPlayListener != null) {
                mPlayListener.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onRenderedFirstFrame() {

        }
    };

    private final com.zoe.android.exoplayer2.Player.EventListener eventListener = new com.zoe.android.exoplayer2.Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case com.zoe.android.exoplayer2.Player.STATE_IDLE:

                    break;
                case com.zoe.android.exoplayer2.Player.STATE_BUFFERING:
                    isBuffering = true;
                    if (mPlayListener != null) {
                        mPlayListener.onBufferingStart();
                    }
                    break;
                case com.zoe.android.exoplayer2.Player.STATE_READY:
                    if (!isPrepared) {
                        executeProgress();
                        if (mPlayListener != null) {
                            mPlayListener.onPlayPrepared();
                        }
                        isPrepared = true;
                    }
                    if (mPlayListener != null && isBuffering) {
                        mPlayListener.onBufferingEnd();
                        isBuffering = false;
                    }
                    break;
                case com.zoe.android.exoplayer2.Player.STATE_ENDED:
                    if (mPlayListener != null) {
                        mPlayListener.onPlayEnd();
                    }
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if(error != null) {
                if (mPlayListener != null) {
                    mPlayListener.onPlayError(error, error.type);
                }
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {
            if (mPlayListener != null) {
                mPlayListener.onSeekProcessed();
            }
        }
    };

    @Override
    public void seekTo(long ms) {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.seekTo(ms);
    }

    @Override
    public void start() {
        if (exoPlayer == null) {
            return;
        }
        setPlayPause(true);
    }

    @Override
    public void pause() {
        if (exoPlayer == null) {
            return;
        }
        setPlayPause(false);
    }

    @Override
    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
    }

    @Override
    public void release() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        if(exoPlayerHelper != null) {
            exoPlayerHelper.release();
            exoPlayerHelper = null;
        }
        handler.removeCallbacks(progressAction);
    }

    @Override
    public long getCurrentPosition() {
        if (exoPlayer == null) {
            return -1;
        }
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (exoPlayer == null) {
            return -1;
        }
        return exoPlayer.getDuration();
    }

    @Override
    public long getBufferedPosition() {
        if (exoPlayer == null) {
            return -1;
        }
        return exoPlayer.getBufferedPosition();
    }

    @Override
    public int getBufferedPercentage() {
        if (exoPlayer == null) {
            return -1;
        }
        return exoPlayer.getBufferedPercentage();
    }

    @Override
    public int getVideoWidth() {
        if (exoPlayer == null || exoPlayer.getVideoFormat() == null) {
            return -1;
        }
        return exoPlayer.getVideoFormat().width;
    }

    @Override
    public int getVideoHeight() {
        if (exoPlayer == null || exoPlayer.getVideoFormat() == null) {
            return -1;
        }
        return exoPlayer.getVideoFormat().height;
    }

    @Override
    public VideoFormat getVideoFormat() {
        VideoFormat videoFormat = new VideoFormat();
        if(exoPlayer == null) {
            return videoFormat;
        }
        videoFormat.videoWidth = getVideoWidth();
        videoFormat.videoHeight = getVideoHeight();
        videoFormat.pixelWidthHeightRatio = getPixelWidthHeightRatio();
        return videoFormat;
    }

    private float getPixelWidthHeightRatio() {
        if (exoPlayer == null || exoPlayer.getVideoFormat() == null) {
            return -1;
        }
        return exoPlayer.getVideoFormat().pixelWidthHeightRatio;
    }

    @Override
    public SourceConfigure getCurrentPlayInfo(){
        return mCurrentPlayInfo;
    }

    @Override
    public void switchSubtitle(int index) {
        if(exoPlayerHelper != null) {
            exoPlayerHelper.textTrackSelect(index);
        }
    }

    @Override
    public void setVolume(float audioVolume) {
        if (exoPlayer != null) {
            exoPlayer.setVolume(audioVolume);
        }
    }

    @Override
    public void switchSpeed(float speed) {
        if(exoPlayer != null) {
            exoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     *
     * @param play True if playback should be started
     */
    private void setPlayPause(boolean play) {
        exoPlayer.setPlayWhenReady(play);
    }

    private void executeProgress() {
        long position = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
        // Remove scheduled updates.
        handler.removeCallbacks(progressAction);
        // Schedule an update if necessary.
        int playbackState = exoPlayer == null ? com.zoe.android.exoplayer2.Player.STATE_IDLE : exoPlayer.getPlaybackState();
        if (playbackState != com.zoe.android.exoplayer2.Player.STATE_IDLE) {
            long delayMs;
            if (exoPlayer.getPlayWhenReady() && playbackState == com.zoe.android.exoplayer2.Player.STATE_READY) {
                delayMs = PlayConstant.PROGRESS_INTERVAL - (position % PlayConstant.PROGRESS_INTERVAL);
                if (delayMs < 200) {
                    delayMs += PlayConstant.PROGRESS_INTERVAL;
                }
            } else {
                delayMs = PlayConstant.PROGRESS_INTERVAL;
            }
            if (mPlayListener != null) {
                mPlayListener.onProgress();
            }
            handler.postDelayed(progressAction, delayMs);
        }
    }

    private final Runnable progressAction = new Runnable() {
        @Override
        public void run() {
            executeProgress();
        }
    };

    @Override
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
