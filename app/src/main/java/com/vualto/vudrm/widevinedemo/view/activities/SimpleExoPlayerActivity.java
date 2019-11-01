package com.vualto.vudrm.widevinedemo.view.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import com.vualto.vudrm.widevinedemo.view.models.SourceItem;
import com.vualto.vudrm.HttpKidSource;
import com.vualto.vudrm.widevine.AssetConfiguration;
import com.vualto.vudrm.widevine.OfflineAssetConfiguration;
import com.vualto.vudrm.widevine.WidevineCallback;
import com.vualto.vudrm.widevine.data.KidProvider;
import com.vualto.vudrm.widevine.vudrm;
import com.vualto.vudrm.widevinedemo.App;
import com.vualto.vudrm.widevinedemo.BuildConfig;
import com.vualto.vudrm.widevinedemo.R;
import com.vualto.vudrm.widevinedemo.playercomponents.PlayerListener;
import com.vualto.vudrm.widevinedemo.playercomponents.StreamIOListener;

import java.io.InvalidObjectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class SimpleExoPlayerActivity extends AppCompatActivity {

//    private SimpleExoPlayer _exoPlayer;
//    private SimpleExoPlayerView _exoplayerView;
//    private App _app;
//    private String _streamURL;

    public final static String ASSET_KEY = "ASSET_KEY";
    public final static String STREAM_URL_KEY = "STREAM_URL_KEY";
    private SimpleExoPlayer _exoPlayer;
    private PlayerView _exoplayerView;
    private App _app;
    private DataSource.Factory dataSourceFactory;
    protected String userAgent;
    private final static String PREF_NAME = "VUDRMWideVine";
    private final static String OFFLINE_KEY_ID = "OFFLINE_KEY_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_exoplayer);
        _exoplayerView = findViewById(R.id.exoplayer_view);
        _app = (App) getApplication();
        userAgent = Util.getUserAgent( this, "VUDRMWidevine" );

        if (isNetworkAvailable()) {
            AssetConfiguration sourceItem = getAsset();
            if (sourceItem == null) return;
            _exoPlayer = createPlayer(sourceItem);
            _exoPlayer.addListener(new PlayerListener());
            _exoplayerView.setPlayer(_exoPlayer);

            dataSourceFactory = buildDataSourceFactory();
            MediaSource mediaSource = new DashMediaSource.Factory(dataSourceFactory)
                    .setManifestParser(
                            new FilteringManifestParser<>(new DashManifestParser(), getOfflineStreamKeys(Uri.parse(getIntent().getStringExtra(STREAM_URL_KEY)))))
                    .createMediaSource(Uri.parse(getIntent().getStringExtra(STREAM_URL_KEY)));

            _exoPlayer.setPlayWhenReady(true);
            _exoPlayer.prepare(mediaSource);

        } else {

            OfflineAssetConfiguration sourceItem = getOfflineAsset();
            if (sourceItem == null) return;
            _exoPlayer = createOfflinePlayer(sourceItem);
            _exoPlayer.addListener(new PlayerListener());
            _exoplayerView.setPlayer(_exoPlayer);

            dataSourceFactory = buildDataSourceFactory();
            MediaSource mediaSource = new DashMediaSource.Factory(dataSourceFactory)
                    .setManifestParser(
                            new FilteringManifestParser<>(new DashManifestParser(), getOfflineStreamKeys(Uri.parse(getIntent().getStringExtra(STREAM_URL_KEY)))))
                    .createMediaSource(Uri.parse(getIntent().getStringExtra(STREAM_URL_KEY)));

            _exoPlayer.setPlayWhenReady(true);
            _exoPlayer.prepare(mediaSource);

        }
    }

    private List<StreamKey> getOfflineStreamKeys(Uri uri) {
        return _app.getDownloadTracker().getOfflineStreamKeys(uri);
    }

    private AssetConfiguration getAsset() {
        if (!getIntent().hasExtra(STREAM_URL_KEY)) return null;

        if (getIntent().hasExtra(ASSET_KEY)) {
            return getIntent().getParcelableExtra(ASSET_KEY);
        } else if (getIntent().hasExtra(SourceItem.INTENT_KEY)) {
            return mappedAsset();
        } else {
            return null;
        }
    }

    private AssetConfiguration mappedAsset() {
        SourceItem item = getIntent().getParcelableExtra(SourceItem.INTENT_KEY);
        try {
            return new AssetConfiguration.Builder()
                    .tokenWith(item.drmToken)
                    .licenceUrlWith(item.licenseURL)
                    .kidProviderWith(
                            new HttpKidSource(
                                    new URL(getIntent().getStringExtra(STREAM_URL_KEY))
                            )
                    )
                    .build();
        } catch (MalformedURLException | InvalidObjectException e) {
            e.printStackTrace();
            throw new IllegalStateException("Invalid Asset"); //crash fast, not public facing
        }
    }

    private OfflineAssetConfiguration getOfflineAsset() {
        if (!getIntent().hasExtra(STREAM_URL_KEY)) return null;

        if (getIntent().hasExtra(ASSET_KEY)) {
            return getIntent().getParcelableExtra(ASSET_KEY);
        } else if (getIntent().hasExtra(SourceItem.INTENT_KEY)) {
            return offlineMappedAsset();
        } else {
            return null;
        }
    }

    private OfflineAssetConfiguration offlineMappedAsset() {
        SourceItem item = getIntent().getParcelableExtra(SourceItem.INTENT_KEY);
        try {
            return new OfflineAssetConfiguration.Builder()
                    .licenceUrlWith(item.licenseURL)
                    .kidProviderWith(
                            new HttpKidSource(
                                    new URL(getIntent().getStringExtra(STREAM_URL_KEY))
                            )
                    )
                    .build();
        } catch (MalformedURLException | InvalidObjectException e) {
            e.printStackTrace();
            throw new IllegalStateException("Invalid Asset"); //crash fast, not public facing
        }
    }

    private SimpleExoPlayer createPlayer(AssetConfiguration assetConfiguration) {
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmManager = getDRMPluginWith(assetConfiguration.getToken(),
                assetConfiguration.getKIDProvider(),
                assetConfiguration.getLicenseURL() == null ? BuildConfig.WIDEVINE_LICENSE_SERVER_URL : assetConfiguration.getLicenseURL().toString());
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(
                /*bufferDurationRequiredForQualityIncrease=*/1000,
                AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
                AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
                AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION);
        return ExoPlayerFactory.newSimpleInstance(this, renderersFactory, new DefaultTrackSelector(trackSelectionFactory), drmManager);
    }

    private SimpleExoPlayer createOfflinePlayer(OfflineAssetConfiguration assetConfiguration){
        try {

            SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, 0);
            String strLicenseEncoded = sharedPreferences.getString(OFFLINE_KEY_ID, null);
            if (strLicenseEncoded == null) {
                Log.d("SimpleExoPlayerActivity", "No offline license found. Operation aborted");
                return null;
            }

            byte[] offlineLicenseKeySetId = Base64.decode(strLicenseEncoded, Base64.DEFAULT);

            DefaultDrmSessionManager drmManager = buildOfflineDrmSessionManager(assetConfiguration.getLicenseURL() == null ? BuildConfig.WIDEVINE_LICENSE_SERVER_URL : assetConfiguration.getLicenseURL().toString(), offlineLicenseKeySetId);

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
            TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(
                    /*bufferDurationRequiredForQualityIncrease=*/1000,
                    AdaptiveTrackSelection.DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
                    AdaptiveTrackSelection.DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
                    AdaptiveTrackSelection.DEFAULT_BANDWIDTH_FRACTION);
            return ExoPlayerFactory.newSimpleInstance(this, renderersFactory, new DefaultTrackSelector(trackSelectionFactory), drmManager);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> getDRMPluginWith(String drmToken, KidProvider KID, String licenseURL) {
        AssetConfiguration assetConfiguration;

        try {
            assetConfiguration = new AssetConfiguration.Builder()
                    .tokenWith(drmToken)
                    .kidProviderWith(KID)
                    .licenceUrlWith(licenseURL)
                    .build();
        } catch (MalformedURLException | InvalidObjectException e) {
            return null;
        }

        WidevineCallback callback = new WidevineCallback(assetConfiguration);
        try {
            return new DefaultDrmSessionManager<>(vudrm.widevineDRMSchemeUUID,
                    FrameworkMediaDrm.newInstance(vudrm.widevineDRMSchemeUUID),callback,null,false);
        } catch (UnsupportedDrmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build DRM manager with {@link DefaultDrmSessionManager MODE_PLAYBACK} for offline usage
     * @param licenseUrl
     * @param license
     * @return The drm session manager
     * @throws UnsupportedDrmException
     */
    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildOfflineDrmSessionManager(String licenseUrl, byte[] license) throws Exception
    {
        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        MediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, httpDataSourceFactory);

        DefaultDrmSessionManager<FrameworkMediaCrypto> defaultDrmSessionManager = DefaultDrmSessionManager.newWidevineInstance(drmCallback, null);
        defaultDrmSessionManager.setMode(DefaultDrmSessionManager.MODE_PLAYBACK, license);
        try {
            return defaultDrmSessionManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        _exoPlayer.release();
    }

    /** Returns a new DataSource factory. */
    private DataSource.Factory buildDataSourceFactory() {
        return _app.buildDataSourceFactory();
    }

    /**
     * Check network availability
     * @return The network availability
     */
    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
