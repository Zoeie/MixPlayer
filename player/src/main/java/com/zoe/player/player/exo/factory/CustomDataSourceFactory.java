package com.zoe.player.player.exo.factory;

import android.content.Context;

import com.zoe.android.exoplayer2.upstream.DataSource;
import com.zoe.android.exoplayer2.upstream.DefaultDataSource;
import com.zoe.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.zoe.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.zoe.android.exoplayer2.upstream.TransferListener;

import androidx.annotation.Nullable;

import java.net.Proxy;


/**
 * author zoe
 * created 2019/5/16 17:45
 */

public class CustomDataSourceFactory
        implements DataSource.Factory {

    private final Context context;
    private final @Nullable TransferListener listener;
    private final DataSource.Factory baseDataSourceFactory;
    /**
     * 是否设置代理
     */
    public boolean setProxy = false;

    /**
     * 代理地址
     */
    public String proxyUrl;

    /**
     * 代理端口
     */
    public int proxyPort;

    /**
     * 代理方式
     */
    public Proxy.Type proxyType = Proxy.Type.HTTP;

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     */
    public CustomDataSourceFactory(Context context, String userAgent) {
        this(context, userAgent, /* listener= */ null);
    }

    /**
     * @param context A context.
     * @param userAgent The User-Agent string that should be used.
     * @param listener An optional listener.
     */
    public CustomDataSourceFactory(
            Context context, String userAgent, @Nullable TransferListener listener) {
        this(context, listener, new DefaultHttpDataSourceFactory(userAgent, listener,
                //用于支持重定向
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true));
    }

    public CustomDataSourceFactory(
            Context context, String userAgent, @Nullable TransferListener listener,
            String proxyUrl, int proxyPort, Proxy.Type proxyType) {
        this(context, listener, new DefaultHttpDataSourceFactory(userAgent, listener));
        this.setProxy = true;
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
        this.proxyType = proxyType;
    }

    /**
     * @param context A context.
     * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
     *     for {@link DefaultDataSource}.
     * @see DefaultDataSource#DefaultDataSource(Context, TransferListener, DataSource)
     */
    public CustomDataSourceFactory(Context context, DataSource.Factory baseDataSourceFactory) {
        this(context, /* listener= */ null, baseDataSourceFactory);
    }

    /**
     * @param context A context.
     * @param listener An optional listener.
     * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
     *     for {@link DefaultDataSource}.
     * @see DefaultDataSource#DefaultDataSource(Context, TransferListener, DataSource)
     */
    public CustomDataSourceFactory(
            Context context,
            @Nullable TransferListener listener,
            DataSource.Factory baseDataSourceFactory) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.baseDataSourceFactory = baseDataSourceFactory;
    }

    @Override
    public CustomDataSource createDataSource() {
        CustomDataSource dataSource;
        if(setProxy) {
            dataSource = new CustomDataSource(context, baseDataSourceFactory.createDataSource()
                    , proxyUrl, proxyPort, proxyType);
        } else {
            dataSource =
                    new CustomDataSource(context, baseDataSourceFactory.createDataSource());
        }
        if (listener != null) {
            dataSource.addTransferListener(listener);
        }
        return dataSource;
    }
}
