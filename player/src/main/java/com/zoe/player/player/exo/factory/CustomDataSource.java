package com.zoe.player.player.exo.factory;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSchemeDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.zoe.player.player.util.M3u8ParseUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;


/**
 * author zoe
 * created 2019/5/16 17:46
 */

public class CustomDataSource implements DataSource {

    private static final String TAG = "CustomDataSource";

    private static final String SCHEME_ASSET = "asset";
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_RTMP = "rtmp";
    private static final String SCHEME_RAW = RawResourceDataSource.RAW_RESOURCE_SCHEME;

    private final Context                context;
    private final List<TransferListener> transferListeners;
    private final DataSource             baseDataSource;

    // Lazily initialized.
    private @Nullable DataSource fileDataSource;
    private @Nullable DataSource assetDataSource;
    private @Nullable DataSource contentDataSource;
    private @Nullable DataSource rtmpDataSource;
    private @Nullable DataSource dataSchemeDataSource;
    private @Nullable DataSource rawResourceDataSource;

    private @Nullable DataSource dataSource;

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param userAgent The User-Agent to use when requesting remote data.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public CustomDataSource(Context context, String userAgent, boolean allowCrossProtocolRedirects) {
        this(
                context,
                userAgent,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                allowCrossProtocolRedirects);
    }

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param userAgent The User-Agent to use when requesting remote data.
     * @param connectTimeoutMillis The connection timeout that should be used when requesting remote
     *     data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis The read timeout that should be used when requesting remote data, in
     *     milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     */
    public CustomDataSource(
            Context context,
            String userAgent,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean allowCrossProtocolRedirects) {
        this(
                context,
                new DefaultHttpDataSource(
                        userAgent,
                        /* contentTypePredicate= */ null,
                        connectTimeoutMillis,
                        readTimeoutMillis,
                        allowCrossProtocolRedirects,
                        /* defaultRequestProperties= */ null));
    }

    /**
     * Constructs a new instance that delegates to a provided {@link DataSource} for URI schemes other
     * than file, asset and content.
     *
     * @param context A context.
     * @param baseDataSource A {@link DataSource} to use for URI schemes other than file, asset and
     *     content. This {@link DataSource} should normally support at least http(s).
     */
    public CustomDataSource(Context context, DataSource baseDataSource) {
        this.context = context.getApplicationContext();
        this.baseDataSource = Assertions.checkNotNull(baseDataSource);
        transferListeners = new ArrayList<>();
    }

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param userAgent The User-Agent to use when requesting remote data.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     * @deprecated Use {@link #CustomDataSource(Context, String, boolean)} and {@link
     *     #addTransferListener(TransferListener)}.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public CustomDataSource(
            Context context,
            @Nullable TransferListener listener,
            String userAgent,
            boolean allowCrossProtocolRedirects) {
        this(context, listener, userAgent, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
             DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, allowCrossProtocolRedirects);
    }

    /**
     * Constructs a new instance, optionally configured to follow cross-protocol redirects.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param userAgent The User-Agent to use when requesting remote data.
     * @param connectTimeoutMillis The connection timeout that should be used when requesting remote
     *     data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis The read timeout that should be used when requesting remote data, in
     *     milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled when fetching remote data.
     * @deprecated Use {@link #CustomDataSource(Context, String, int, int, boolean)} and {@link
     *     #addTransferListener(TransferListener)}.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public CustomDataSource(
            Context context,
            @Nullable TransferListener listener,
            String userAgent,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean allowCrossProtocolRedirects) {
        this(
                context,
                listener,
                new DefaultHttpDataSource(
                        userAgent,
                        /* contentTypePredicate= */ null,
                        listener,
                        connectTimeoutMillis,
                        readTimeoutMillis,
                        allowCrossProtocolRedirects,
                        /* defaultRequestProperties= */ null));
    }

    /**
     * Constructs a new instance that delegates to a provided {@link DataSource} for URI schemes other
     * than file, asset and content.
     *
     * @param context A context.
     * @param listener An optional listener.
     * @param baseDataSource A {@link DataSource} to use for URI schemes other than file, asset and
     *     content. This {@link DataSource} should normally support at least http(s).
     * @deprecated Use {@link #CustomDataSource(Context, DataSource)} and {@link
     *     #addTransferListener(TransferListener)}.
     */
    @Deprecated
    public CustomDataSource(
            Context context, @Nullable TransferListener listener, DataSource baseDataSource) {
        this(context, baseDataSource);
        if (listener != null) {
            transferListeners.add(listener);
        }
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {
        baseDataSource.addTransferListener(transferListener);
        transferListeners.add(transferListener);
        maybeAddListenerToDataSource(fileDataSource, transferListener);
        maybeAddListenerToDataSource(assetDataSource, transferListener);
        maybeAddListenerToDataSource(contentDataSource, transferListener);
        maybeAddListenerToDataSource(rtmpDataSource, transferListener);
        maybeAddListenerToDataSource(dataSchemeDataSource, transferListener);
        maybeAddListenerToDataSource(rawResourceDataSource, transferListener);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Assertions.checkState(dataSource == null);
        String ts = dataSpec.uri.toString();
        DataSpec myDataSpec=null;
        if (!TextUtils.isEmpty(ts) && !ts.contains(".m3u8")
                && dataSpec.uri.getPath() != null
                && dataSpec.uri.getLastPathSegment() != null) {
            String path = dataSpec.uri.getPath();
            String lastPathSegment = dataSpec.uri.getLastPathSegment();
            path = path.replace(lastPathSegment, "");
            if (!ts.contains(".ts")) {//加密数据
                try {
                    String result = M3u8ParseUtil.parse(lastPathSegment);
                    if(result != null && !TextUtils.isEmpty(result) && result.contains(".ts")) {
                        if (result.startsWith("http")){//绝对路径
                            myDataSpec = new DataSpec(Uri.parse(result));
                            android.util.Log.i(TAG, "open: absolutePath");
                        }else {
                            String authority = dataSpec.uri.getAuthority();
                            String subPath = authority + path + result;
                            android.util.Log.d(TAG, "open():---authority:" + authority + ",path:" + path + ",relativePath:" + result);
                            String newPath = dataSpec.uri.getScheme() + "://" + subPath;
                            myDataSpec = new DataSpec(Uri.parse(newPath));
                            android.util.Log.i(TAG, "open: encrypt data--newPath:" + newPath);
                        }


                        /*if (result.startsWith("..")) {//ts流的链接为相对路径
                            String authority = dataSpec.uri.getAuthority();
                            String subPath = authority + path + result;
                            android.util.Log.d(TAG, "open():---authority:" + authority + ",path:" + path + ",relativePath:" + result);
                            String newPath = dataSpec.uri.getScheme() + "://" + subPath;
                            myDataSpec = new DataSpec(Uri.parse(newPath));
                            android.util.Log.i(TAG, "open: encrypt data--newPath:" + newPath);
                        } else {//绝对路径
                            myDataSpec = new DataSpec(Uri.parse(result));
                            android.util.Log.i(TAG, "open: absolutePath");
                        }*/
                    }
                } catch (NumberFormatException e) {
                    android.util.Log.e(TAG, "Number Format Exception" + ts);
                }
            } else {//非加密数据，ts流的链接为相对路径时播放器会自动拼接好回调到这边
                android.util.Log.d(TAG, "open: general data：" + ts);
            }
        }

        // Choose the correct source for the scheme.
        String scheme =myDataSpec!=null?myDataSpec.uri.getScheme():dataSpec.uri.getScheme();
        if (Util.isLocalFileUri(dataSpec.uri)) {
            String uriPath = dataSpec.uri.getPath();
            if (uriPath != null && uriPath.startsWith("/android_asset/")) {
                dataSource = getAssetDataSource();
            } else {
                dataSource = getFileDataSource();
            }
        } else if (SCHEME_ASSET.equals(scheme)) {
            dataSource = getAssetDataSource();
        } else if (SCHEME_CONTENT.equals(scheme)) {
            dataSource = getContentDataSource();
        } else if (SCHEME_RTMP.equals(scheme)) {
            dataSource = getRtmpDataSource();
        } else if (DataSchemeDataSource.SCHEME_DATA.equals(scheme)) {
            dataSource = getDataSchemeDataSource();
        } else if (SCHEME_RAW.equals(scheme)) {
            dataSource = getRawResourceDataSource();
        } else {
            dataSource = baseDataSource;
        }
        // Open the source and return.
        return dataSource.open(myDataSpec!=null?myDataSpec:dataSpec);
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return Assertions.checkNotNull(dataSource).read(buffer, offset, readLength);
    }

    @Override
    public @Nullable Uri getUri() {
        return dataSource == null ? null : dataSource.getUri();
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return dataSource == null ? Collections.emptyMap() : dataSource.getResponseHeaders();
    }

    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            try {
                dataSource.close();
            } finally {
                dataSource = null;
            }
        }
    }

    private DataSource getFileDataSource() {
        if (fileDataSource == null) {
            fileDataSource = new FileDataSource();
            addListenersToDataSource(fileDataSource);
        }
        return fileDataSource;
    }

    private DataSource getAssetDataSource() {
        if (assetDataSource == null) {
            assetDataSource = new AssetDataSource(context);
            addListenersToDataSource(assetDataSource);
        }
        return assetDataSource;
    }

    private DataSource getContentDataSource() {
        if (contentDataSource == null) {
            contentDataSource = new ContentDataSource(context);
            addListenersToDataSource(contentDataSource);
        }
        return contentDataSource;
    }

    private DataSource getRtmpDataSource() {
        if (rtmpDataSource == null) {
            try {
                // LINT.IfChange
                Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.rtmp.RtmpDataSource");
                rtmpDataSource = (DataSource) clazz.getConstructor().newInstance();
                // LINT.ThenChange(../../../../../../../../proguard-rules.txt)
                addListenersToDataSource(rtmpDataSource);
            } catch (ClassNotFoundException e) {
                // Expected if the app was built without the RTMP extension.
                Log.w(TAG, "Attempting to play RTMP stream without depending on the RTMP extension");
            } catch (Exception e) {
                // The RTMP extension is present, but instantiation failed.
                throw new RuntimeException("Error instantiating RTMP extension", e);
            }
            if (rtmpDataSource == null) {
                rtmpDataSource = baseDataSource;
            }
        }
        return rtmpDataSource;
    }

    private DataSource getDataSchemeDataSource() {
        if (dataSchemeDataSource == null) {
            dataSchemeDataSource = new DataSchemeDataSource();
            addListenersToDataSource(dataSchemeDataSource);
        }
        return dataSchemeDataSource;
    }

    private DataSource getRawResourceDataSource() {
        if (rawResourceDataSource == null) {
            rawResourceDataSource = new RawResourceDataSource(context);
            addListenersToDataSource(rawResourceDataSource);
        }
        return rawResourceDataSource;
    }

    private void addListenersToDataSource(DataSource dataSource) {
        for (int i = 0; i < transferListeners.size(); i++) {
            dataSource.addTransferListener(transferListeners.get(i));
        }
    }

    private void maybeAddListenerToDataSource(
            @Nullable DataSource dataSource, TransferListener listener) {
        if (dataSource != null) {
            dataSource.addTransferListener(listener);
        }
    }
}
