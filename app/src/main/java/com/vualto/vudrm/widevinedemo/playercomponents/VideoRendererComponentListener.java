package com.vualto.vudrm.widevinedemo.playercomponents;

import android.view.Surface;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

/**
 * Created by davidwork on 25/11/2016.
 * <p>
 * Copyright (c) 2016 Vualto Ltd. All rights reserved.
 */
public class VideoRendererComponentListener implements VideoRendererEventListener {

    private AspectRatioChangeListener _listener;

    public VideoRendererComponentListener() {
        this(null);
    }

    public VideoRendererComponentListener(AspectRatioChangeListener listener) {
        _listener = listener;
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if (_listener == null) return;
        _listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }

    public interface AspectRatioChangeListener {
        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio);
    }
}
