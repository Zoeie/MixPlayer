package com.zoe.player.player.exo.factory;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Util;
import com.zoe.player.R;

/**
 * author zoe
 * created 2019/5/21 17:52
 */

public class CacheDataSourceFactory implements DataSource.Factory {
    private final DefaultDataSourceFactory defaultDatasourceFactory;
    private final long maxFileSize;
    private final Cache cache;

    public CacheDataSourceFactory(Context context, long maxFileSize, Cache cache) {
        super();
        this.maxFileSize = maxFileSize;
        this.cache = cache;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        defaultDatasourceFactory = new DefaultDataSourceFactory(context,
                bandwidthMeter,
                new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
    }

    @Override
    public DataSource createDataSource() {
        return new CacheDataSource(cache, defaultDatasourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(cache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }
}
