package com.zoe.player.player.exo.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.upstream.cache.ContentMetadata;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.NavigableSet;

/**
 * author zoe
 * created 2019/5/21 18:13
 */

public class CacheManager {

    private static final int MAX_CACHE_SIZE = 500 * 1024 * 1024;

    @SuppressLint("StaticFieldLeak")
    private static CacheManager manager;
    private Context context;
    private SimpleCache mCache;
    private String url;
    private File cacheDir;

    private CacheManager(Context context, String url,File cacheDir) {
        this.context = context;
        this.url = url;
        this.cacheDir = cacheDir;
        initCache();
    }

    public static CacheManager getInstance(Context context, String url,File cacheDir) {
        if (manager == null) {
            manager = new CacheManager(context, url, cacheDir);
        }
        return manager;
    }

    private void initCache() {
        String dirs = context.getCacheDir().getAbsolutePath();
        if (cacheDir != null) {
            dirs = cacheDir.getAbsolutePath();
        }
        if (mCache == null) {
            String path = dirs + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE));
            }
        }
    }

    public Cache getCache() {
        return mCache;
    }

//    public boolean hasCache() {
//        return resolveCacheState(mCache, url);
//    }

//    public void release() {
//        if (mCache != null) {
//            mCache.release();
//        }
//    }


    /**
     * Cache需要release之后才能clear
     *
     * @param url
     */
    public void clearCache(String url) {
        try {
            Cache cache = mCache;
            if (!TextUtils.isEmpty(url)) {
                if (cache != null) {
                    CacheUtil.remove(cache, CacheUtil.generateKey(Uri.parse(url)));
                }
            } else {
                if (cache != null) {
                    for (String key : cache.getKeys()) {
                        CacheUtil.remove(cache, key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据缓存块判断是否缓存成功
     *
     * @param cache
     */
//    private static boolean resolveCacheState(Cache cache, String url) {
//        boolean isCache = true;
//        if (!TextUtils.isEmpty(url)) {
//            String key = CacheUtil.generateKey(Uri.parse(url));
//            if (!TextUtils.isEmpty(key)) {
//                NavigableSet<CacheSpan> cachedSpans = cache.getCachedSpans(key);
//                if (cachedSpans.size() == 0) {
//                    isCache = false;
//                } else {
//                    long contentLength = ContentMetadata.getContentLength(cache.getContentMetadata(key));
//                    ;
//                    long currentLength = 0;
//                    for (CacheSpan cachedSpan : cachedSpans) {
//                        currentLength += cache.getCachedLength(key, cachedSpan.position, cachedSpan.length);
//                    }
//                    isCache = currentLength >= contentLength;
//                }
//            } else {
//                isCache = false;
//            }
//        }
//        return isCache;
//    }

}
