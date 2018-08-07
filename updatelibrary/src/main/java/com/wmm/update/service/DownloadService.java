package com.wmm.update.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wmm.update.Check;
import com.wmm.update.CheckUpdateOption;
import com.wmm.update.callback.DownloadCallback;
import com.wmm.update.utils.AppUtil;
import com.wmm.update.utils.NotificationUtil;

import java.io.File;

public class DownloadService extends Service implements DownloadCallback {
    private CheckUpdateOption mOption;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        mOption = intent.getParcelableExtra("CheckUpdateOption");
        Check.download(mOption.getNewAppUrl(), mOption.getFilePath(), mOption.getFileName())
                .callback(this).execute();
        return START_STICKY;
    }

    @Override
    public void checkUpdateStart() {

    }

    @Override
    public void checkUpdateFailure(Throwable t) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("CheckUpdateOption", mOption);
        startService(intent);
        NotificationUtil.showDownloadFailureNotification(this, intent, mOption.getNotificationIconResId(),
                mOption.getNotificationTitle(), mOption.getNotificationFailureContent(), true);
    }

    @Override
    public void checkUpdateFinish() {

    }

    @Override
    public void downloadProgress(long currentLength, long totalLength) {
        NotificationUtil.showDownloadingNotification(this, (int) currentLength, (int) totalLength,
                mOption.getNotificationIconResId(), mOption.getNotificationTitle(), false);
    }

    @Override
    public void downloadSuccess(File apk) {
        AppUtil.installApk(this, apk);
        NotificationUtil.showDownloadSuccessNotification(this, apk, mOption.getNotificationIconResId(),
                mOption.getNotificationTitle(), mOption.getNotificationSuccessContent(), false);
    }
}
