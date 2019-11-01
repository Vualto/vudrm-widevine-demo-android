/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vualto.vudrm.widevinedemo.view.activities;

import android.app.Notification;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.vualto.vudrm.widevinedemo.R;

/**
 * Created by Adam Gerber on 29/04/2019.
 * <p>
 * Copyright © 2019 Vualto Ltd. All rights reserved.
 */

/** A service for downloading media. */
public class DemoDownloadService extends DownloadService {

  private static final String CHANNEL_ID = "download_channel";
  private static final int JOB_ID = 1;
  private static final int FOREGROUND_NOTIFICATION_ID = 1;
  public static String TAG="MediaDownloadService";
  private static final int MESSAGE_PROGRESS = 0;

  public DemoDownloadService() {
    super(
        FOREGROUND_NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        CHANNEL_ID,
        R.string.exo_download_notification_channel_name);
  }

  @Override
  protected DownloadManager getDownloadManager() {
    return ((com.vualto.vudrm.widevinedemo.App)getApplication( )).getDownloadManager( );
  }

  @Override
  protected PlatformScheduler getScheduler() {
    return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
  }

    @Override
    protected Notification getForegroundNotification(TaskState[] taskStates) {
    float totalPercentage = 0;
    int downloadTaskCount = 0;
    boolean allDownloadPercentagesUnknown = true;
    boolean haveDownloadedBytes = false;
    boolean haveDownloadTasks = false;
    boolean haveRemoveTasks = false;
    for (TaskState taskState : taskStates) {
        if (taskState.state != TaskState.STATE_STARTED
                && taskState.state != TaskState.STATE_COMPLETED) {
            continue;
        }
        if (taskState.action.isRemoveAction) {
            haveRemoveTasks = true;
            continue;
        }
        haveDownloadTasks = true;
        if (taskState.downloadPercentage != C.PERCENTAGE_UNSET) {
            allDownloadPercentagesUnknown = false;
            totalPercentage += taskState.downloadPercentage;
        }
        haveDownloadedBytes |= taskState.downloadedBytes > 0;
        downloadTaskCount++;
    }

    int progress = 0;
    boolean indeterminate = true;
    if (haveDownloadTasks) {
        progress = (int) (totalPercentage / downloadTaskCount);
        indeterminate = allDownloadPercentagesUnknown && haveDownloadedBytes;
        Intent intent = new Intent("download-progress");
        intent.putExtra("download progress", "" + progress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    return DownloadNotificationUtil.buildProgressNotification(
        /* context= */ this,
        R.drawable.exo_controls_play,
        CHANNEL_ID,
        /* contentIntent= */ null,
        /* message= */ null,
        taskStates);
}

  @Override
  protected void onTaskStateChanged(TaskState taskState) {
    if (taskState.action.isRemoveAction) {
      return;
    }
    Notification notification = null;
    if (taskState.state == TaskState.STATE_COMPLETED) {
      notification =
          DownloadNotificationUtil.buildDownloadCompletedNotification(
              /* context= */ this,
              R.drawable.exo_controls_play,
              CHANNEL_ID,
              /* contentIntent= */ null,
              Util.fromUtf8Bytes(taskState.action.data));
    } else if (taskState.state == TaskState.STATE_FAILED) {
      notification =
          DownloadNotificationUtil.buildDownloadFailedNotification(
              /* context= */ this,
              R.drawable.exo_controls_play,
              CHANNEL_ID,
              /* contentIntent= */ null,
              Util.fromUtf8Bytes(taskState.action.data));
    }
    int notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId;
    NotificationUtil.setNotification(this, notificationId, notification);
  }
}
