package com.vualto.studiodrm.widevinedemo;

/**
 * Created by Adam Gerber on 15/03/2022.
 * Copyright © 2022 JW Player. All rights reserved.
 * */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.vualto.studiodrm.HttpKidSource;
import com.vualto.studiodrm.widevine.AssetConfiguration;
import com.vualto.studiodrm.widevine.WidevineCallback;
import java.net.URL;
import java.util.Iterator;

public class StudiodrmHelper {
    private static final String TAG = "StudioDrmHelper";

    // Builds and returns a DrmSessionManager with the Studio DRM callback
    public static DrmSessionManager getStudioDrmSessionManager(String streamUri, String token)
            throws Exception {
        AssetConfiguration assetConfiguration = new AssetConfiguration.Builder()
                .tokenWith(token)
                .kidProviderWith(
                        new HttpKidSource(new URL(streamUri))
                ).build();
        MediaDrmCallback mediaDrmCallback = new WidevineCallback(assetConfiguration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            return new DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                    .setMultiSession(/* check individual assets for that */ false)
                    .build(mediaDrmCallback);

        }
        throw new Exception("unsupported android build version");
    }

    // Checks for vudrm.tech or drm.technology presence in the license URL
    public static Boolean useSdk(Context context, Uri uri) {
        return !uri.toString().contains("token=") && isStudioDrm(uri) && isNetworkAvailable(context);
    }

    // Checks for vudrm.tech or drm.technology presence in the license URL
    private static Boolean isStudioDrm(Uri uri) {
        String dns = uri.getAuthority();
        assert dns != null;
        return dns.contains("vudrm.tech") || dns.contains("drm.technology");
    }

    public static String getToken (MediaItem mediaItem) {
        String studioDRMToken = "";
        Iterator tokenIterator = mediaItem.playbackProperties.drmConfiguration.requestHeaders.keySet().iterator();
        while(tokenIterator.hasNext()) {
            String key = (String) tokenIterator.next();
            studioDRMToken = (String) mediaItem.playbackProperties.drmConfiguration.requestHeaders.get(key);
        }
        return studioDRMToken;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
