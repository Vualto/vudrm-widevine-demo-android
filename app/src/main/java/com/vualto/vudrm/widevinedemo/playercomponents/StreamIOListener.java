package com.vualto.vudrm.widevinedemo.playercomponents;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;

import java.io.IOException;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

public class StreamIOListener implements AdaptiveMediaSourceEventListener {

    @Override
    public void onDownstreamFormatChanged( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.MediaLoadData mediaLoadData )
    {

    }

    @Override
    public void onLoadCanceled( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData )
    {

    }

    @Override
    public void onLoadCompleted( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData )
    {

    }

    @Override
    public void onLoadError( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled )
    {

    }

    @Override
    public void onLoadStarted( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData )
    {

    }

    @Override
    public void onMediaPeriodCreated( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId )
    {

    }

    @Override
    public void onMediaPeriodReleased( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId )
    {

    }

    @Override
    public void onReadingStarted( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId )
    {

    }

    @Override
    public void onUpstreamDiscarded( int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.MediaLoadData mediaLoadData )
    {

    }
}
