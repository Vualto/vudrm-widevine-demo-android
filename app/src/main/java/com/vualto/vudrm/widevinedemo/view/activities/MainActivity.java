package com.vualto.vudrm.widevinedemo.view.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.drm.DrmInfoRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.source.dash.DashUtil;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadAction;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.util.Util;

import com.vualto.vudrm.HttpKidSource;
import com.vualto.vudrm.widevinedemo.App;
import com.vualto.vudrm.widevinedemo.R;
import com.vualto.vudrm.widevine.AssetConfiguration;
import com.vualto.vudrm.widevine.OfflineAssetConfiguration;
import com.vualto.vudrm.widevine.WidevineCallback;
import com.vualto.vudrm.widevine.vudrm;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class MainActivity extends AppCompatActivity implements DownloadTracker.Listener {

    private TextInputLayout urlContainer, drmContainer, contentIDContainer;
    private TextView url, drm, id;

    private String streamUrl = "";
    private String drmToken = "";
    private String contentID = "";

    private com.vualto.vudrm.widevinedemo.view.activities.DownloadTracker downloadTracker;
    private OfflineLicenseHelper<FrameworkMediaCrypto> mOfflineLicenseHelper;
    private App _app;

    private final static String PREF_NAME = "VUDRMWideVine";
    private final static String OFFLINE_KEY_ID = "OFFLINE_KEY_ID";

    protected String userAgent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _app = (App) getApplication();
        userAgent = Util.getUserAgent( this, "VUDRMWidevine" );
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        bindUi();
        downloadTracker = _app.getDownloadTracker();

        try {
            DownloadService.start(this, com.vualto.vudrm.widevinedemo.view.activities.DemoDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, com.vualto.vudrm.widevinedemo.view.activities.DemoDownloadService.class);
        }

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "download-progress".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("download-progress"));

        updateUI();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setDefaults() {
        url.setText(streamUrl);
        drm.setText(drmToken);
        id.setText(contentID);
        updateDeleteButton(false);
        updateLaunchButton(true);
        updateDeleteButton(true);
    }

    private void bindUi() {
        url = findViewById(R.id.stream_url);
        drm = findViewById(R.id.drm_token);
        id = findViewById(R.id.content_id);

        urlContainer = findViewById(R.id.stream_url_container);
        drmContainer = findViewById(R.id.drm_token_container);
        contentIDContainer = findViewById(R.id.content_id_container);

        setDefaults();

        findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamUrl = ((TextView) findViewById(R.id.stream_url)).getText().toString();
                drmToken = ((TextView) findViewById(R.id.drm_token)).getText().toString();
                contentID = ((TextView) findViewById(R.id.content_id)).getText().toString();

                urlContainer.setError(null);
                drmContainer.setError(null);
                contentIDContainer.setError(null);

                if (streamUrl.isEmpty()) {
                    urlContainer.setError(getString(R.string.stream_url_validation));
                }

                if (drmToken.isEmpty()) {
                    drmContainer.setError(getString(R.string.drm_token_validation));
                }

                if (contentID.isEmpty()) {
                    contentIDContainer.setError(getString(R.string.content_id_validation));
                }

                if (streamUrl.isEmpty() || drmToken.isEmpty() || contentID.isEmpty()) {
                    return;
                }
                launchPlayer(streamUrl, drmToken);
            }
        });

        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamUrl = ((TextView) findViewById(R.id.stream_url)).getText().toString();
                drmToken = ((TextView) findViewById(R.id.drm_token)).getText().toString();
                contentID = ((TextView) findViewById(R.id.content_id)).getText().toString();
                downloadAsset(streamUrl, drmToken, contentID);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamUrl = ((TextView) findViewById(R.id.stream_url)).getText().toString();
                deleteAsset(streamUrl);
            }
        });
    }

    private void updateUI() {
        if (checkIfLocalAssetsExist(streamUrl)) {
            updateDownloadButton(false);
            updateDeleteButton(true);
            updateLaunchButton(true);
        } else {
            updateDownloadButton(true);
            updateDeleteButton(false);
        }
    }

    private void launchPlayer(String streamUrl, String drmToken) {
        Intent videoIntent = new Intent(this, SimpleExoPlayerActivity.class);
        videoIntent.putExtra(SimpleExoPlayerActivity.STREAM_URL_KEY, streamUrl);

        if (isNetworkAvailable()){
            try {
                AssetConfiguration assetConfiguration = new AssetConfiguration.Builder()
                        .tokenWith(drmToken)
                        .kidProviderWith(
                                new HttpKidSource(
                                        new URL(streamUrl)
                                )
                        )
                        .build();

                videoIntent.putExtra(SimpleExoPlayerActivity.ASSET_KEY, assetConfiguration);
                startActivity(videoIntent);
            } catch (MalformedURLException | InvalidObjectException e) {
                e.printStackTrace();
            }
        } else {
            try {
                OfflineAssetConfiguration assetConfiguration = new OfflineAssetConfiguration.Builder()
                        .kidProviderWith(
                                new HttpKidSource(
                                        new URL(streamUrl)
                                )
                        )
                        .build();

                videoIntent.putExtra(SimpleExoPlayerActivity.ASSET_KEY, assetConfiguration);
                startActivity(videoIntent);
            } catch (MalformedURLException | InvalidObjectException e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadLicense(String streamUrl) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // retrieve offline key from the local device storage
                    SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, 0);
                    // Create an assetConfiguration to acquire offline license
                    AssetConfiguration assetConfiguration = new AssetConfiguration.Builder()
                            .tokenWith(drmToken)
                            .kidProviderWith(
                                    new HttpKidSource(
                                            new URL(streamUrl)
                                    )
                            )
                            .build();

                    // Create DrmInfo to create drmInitData - required to load manifest
                    DrmInfoRequest drmInfo = new DrmInfoRequest(1,"dash+xml");
                    byte[] deviceData = SerializationUtils.serialize(drmInfo.toString());

                    JSONArray byteArray = new JSONArray();
                    for (byte dataChunk : deviceData) {
                        int unsignedInt = dataChunk & 0xFF;
                        byteArray.put(unsignedInt);
                    }

                    // Load the manifest
                    HttpDataSource dataSource = _app.buildHttpDataSourceFactory().createDataSource();
                    DashManifest dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(streamUrl));
                    DrmInitData drmInitData = DashUtil.loadDrmInitData(dataSource, dashManifest.getPeriod(0));

                    // Create VUDRM callback to populate the OfflineLicenseHelper
                    WidevineCallback callback = new WidevineCallback(assetConfiguration);

                    // Initialise our DRM with required scheme and callback
                    mOfflineLicenseHelper = new OfflineLicenseHelper<>(vudrm.widevineDRMSchemeUUID, FrameworkMediaDrm.newInstance(vudrm.widevineDRMSchemeUUID), callback, null);

                    // Call the OfflineLicenseHelper to download license to offlineLicenseKeySetId byte array
                    byte[] offlineLicenseKeySetId = mOfflineLicenseHelper.downloadLicense(drmInitData);

                    // Check license validity
                    if (isLicenseValid(offlineLicenseKeySetId)) {
                        // License valid so save the license key locally
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(OFFLINE_KEY_ID, Base64.encodeToString(offlineLicenseKeySetId, Base64.DEFAULT));
                        editor.apply();
                    } else {
                        Log.d("QAActivity", "License Corrupted or expired");
                    }

                } catch (UnsupportedDrmException | IOException | InterruptedException | DrmSession.DrmSessionException e) {
                    Log.d("QAActivity", "Unsupported DRM exception: " + e);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void downloadAsset(String streamUrl, String drmToken, String contentID) {

        // Disable Download button to stop duplicate downloads
        Button downloadButton = (Button) findViewById(R.id.download);
        downloadButton.setEnabled(false);

        // Download our license
        // Note: it may be preferable to return a boolean from this call,
        // and only download the asset if the license has been acquired and saved.
        downloadLicense(streamUrl);

        // Call our DownloadTracker and let it go to work
        Uri streamUri = Uri.parse(streamUrl);
        downloadTracker.toggleDownload(this, contentID, streamUri, ".mpd");
    }

    private void updateLaunchButton (Boolean enabled) {
        Button launchButton = (Button) findViewById(R.id.launch);
        launchButton.setEnabled(enabled);
    }

    private void updateDeleteButton (Boolean enabled) {
        Button deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setEnabled(enabled);
    }

    private void updateDownloadButton (Boolean enabled) {
        Button downloadButton = (Button) findViewById(R.id.download);
        downloadButton.setEnabled(enabled);
    }

    public void updateProgress (int progress) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(progress);
    }

    private void deleteAsset (String streamUrl) {
        // Create a download action to delete the associated download
        Uri StreamUri = Uri.parse(streamUrl);
        DownloadAction downloadAction = DashDownloadAction.createRemoveAction(StreamUri, null);
        DownloadService.startWithAction(this, com.vualto.vudrm.widevinedemo.view.activities.DemoDownloadService.class, downloadAction, true);
        updateDownloadButton(true);
        updateDeleteButton(false);
    }

    @Override
    public void onDownloadsChanged() {
        // Intercept Download Tracker onDownloadsChanged notification here
        Log.d("QAActivity","Download Tracker onDownloadsChanged notification");
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "download-progress" is broadcast.
    // It will come from the associated download service (DemoDownloadService)

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("download progress");
            Log.d("receiver", "Download progress = " + message +"%");
            int b;
            b=Integer.parseInt(message);
            if (b > 0) {
                updateProgress(b);
            }
            if (b >= 100){
                updateUI();
            }
        }
    };


    public boolean checkIfLocalAssetsExist(String streamUrl){
        // Checks downloadTracker if offline asset for stream URI has already been downloaded
        Uri StreamUri = Uri.parse(streamUrl);
        return downloadTracker.isDownloaded(StreamUri);
    }

    /**
     * Check license validity
     * @param license byte[]
     * @return The license validity
     */
    private boolean isLicenseValid(byte[] license)
    {
        if (mOfflineLicenseHelper != null)
        {
            try
            {
                // get license duration
                Pair<Long, Long> licenseDurationRemainingSec = mOfflineLicenseHelper.getLicenseDurationRemainingSec(license);
                long licenseDuration = licenseDurationRemainingSec.first;

                Log.d("QAActivity", "License is valid for another " + licenseDuration + " seconds.");
                return licenseDuration > 0;
            }
            catch (DrmSession.DrmSessionException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return false;
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
