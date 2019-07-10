package com.zoe.player.player.exo;

import android.content.Context;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.video.DummySurface;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * author zoe
 * created 2019/4/25 13:42
 */

public class ExoPlayerHelper {

    public SimpleExoPlayer buildExoPlayer(Context context, ExoConfigure configure) {
        handleSSLHandshake();
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * configure.getBufferFactor(),
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * configure.getBufferFactor(),
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                .createDefaultLoadControl();
        //EXTENSION_RENDERER_MODE_ON：MediaCodecAudioRenderer不支持的格式，使用FfmpegAudioRenderer来进行播放
        //EXTENSION_RENDERER_MODE_PREFER:令FfmpegAudioRenderer的优先级高于MediaCodecAudioRenderer不支持的格式
        //播放器默认支持拓展，当MediaCodecAudioRenderer不支持的格式，使用FfmpegAudioRenderer
        DefaultRenderersFactory factory = new DefaultRenderersFactory(context, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        //2.创建ExoPlayer
        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(context,factory, trackSelector, loadControl);

        //增加log日志分析
        exoPlayer.addAnalyticsListener(new EventLogger(trackSelector));
        //主页解决切换SurfaceView  因为MediaCodec一定要关联一个SurfaceView,在Attach/Detach时，SurfaceView会销毁再创建
        //方式一：禁用或者开启，渲染的方式
        configure.getSurfaceView().getHolder().addCallback(new SurfaceManager1(exoPlayer,trackSelector));
        //方式二：在SurfaceView进行销毁再创建时，给MediaCodec一个DummySurface，防止出错。 暂不生效(ExoPlayer issue#2703 #677)
        //configure.getSurfaceView().getHolder().addCallback(new SurfaceManager2(exoPlayer, context));
        return exoPlayer;
    }

    private static final class SurfaceManager1 implements SurfaceHolder.Callback {

        private final SimpleExoPlayer player;
        private final DefaultTrackSelector trackSelector;

        SurfaceManager1(SimpleExoPlayer player, DefaultTrackSelector trackSelector) {
            this.player = player;
            this.trackSelector = trackSelector;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setVideoSurface(holder.getSurface());
            trackSelector.setRendererDisabled(0, false);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Do nothing.
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            player.setVideoSurface(null);
            trackSelector.setRendererDisabled(0, true);
        }
    }

    private static final class SurfaceManager2 implements SurfaceHolder.Callback {
        private final SimpleExoPlayer player;
        private DummySurface dummySurface;
        private Context context;

        public SurfaceManager2(SimpleExoPlayer player, Context context) {
            this.player = player;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setVideoSurface(holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Do nothing.
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (dummySurface == null) {
                dummySurface = DummySurface.newInstanceV17(context, DummySurface.isSecureSupported(context));
            }
            player.setVideoSurface(dummySurface);
        }

        public void release() {
            if (dummySurface != null) {
                dummySurface.release();
                dummySurface = null;
            }
        }

    }

    /**
     * 处理https非CA证书信任问题
     */
    private void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }


}
