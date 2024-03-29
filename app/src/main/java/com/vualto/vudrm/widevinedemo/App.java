package com.vualto.vudrm.widevinedemo;

import android.app.Application;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import com.vualto.vudrm.widevinedemo.view.activities.DownloadTracker;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class App extends Application {
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 1;

    protected String userAgent;

    private File downloadDirectory;
    private Cache downloadCache;
    private DownloadManager downloadManager;
    private com.vualto.vudrm.widevinedemo.view.activities.DownloadTracker downloadTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        userAgent = Util.getUserAgent( this, "VUDRMWidevine" );
    }

    public DataSource.Factory buildDataSourceFactory() {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(this, buildHttpDataSourceFactory());
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

//    /** Returns whether extension renderers should be used. */
//    public boolean useExtensionRenderers() {
//        return "withExtensions".equals(BuildConfig.FLAVOR);
//    }

    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return downloadManager;
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager();
        return downloadTracker;
    }

    /** inits DownloadManager. */

    private synchronized void initDownloadManager( )
    {
        if( downloadManager == null )
        {
            DownloaderConstructorHelper downloaderConstructorHelper = new DownloaderConstructorHelper( getDownloadCache( ), buildHttpDataSourceFactory( ) );
            downloadManager = new DownloadManager( downloaderConstructorHelper, MAX_SIMULTANEOUS_DOWNLOADS, DownloadManager.DEFAULT_MIN_RETRY_COUNT, new File( getDownloadDirectory( ), DOWNLOAD_ACTION_FILE ) );
            downloadTracker = new DownloadTracker(
                    /* context= */ this, buildDataSourceFactory( ), new File( getDownloadDirectory( ), DOWNLOAD_TRACKER_ACTION_FILE ));
            downloadManager.addListener(downloadTracker);
        }
    }

    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }
}