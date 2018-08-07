package com.wmm.update.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wmm.update.Check;
import com.wmm.update.CheckUpdateOption;
import com.wmm.update.R;
import com.wmm.update.callback.DownloadCallback;
import com.wmm.update.imageloader.ImageLoader;
import com.wmm.update.service.DownloadService;
import com.wmm.update.utils.AppUtil;
import com.wmm.update.utils.LogUtils;

import java.io.File;

/**
 * Created by 魏明明 on 2018/8/7
 * Author email: 15867237430@163.com
 * Motto :以前想着怎么致富，现在老子要脱贫
 * Effect:
 */
public class InternalDialog extends Dialog implements DownloadCallback {
    private CheckUpdateOption mOption;
    private ImageLoader mLoader;
    private ImageView checkUpdateImage;
    private TextView checkUpdateVersionCode;
    private TextView checkUpdateVersionSize;
    private TextView checkUpdateVersionLog;
    private TextView checkUpdateNegative;
    private TextView checkUpdatePositive;
    private ProgressBar checkUpdateProgressBar;
    private Activity mActivity;
    private boolean isDownloadComplete;
    private File mApk;
    private CheckUpdateDialog mCheckUpdateDialog;
    private long timeRange;//时间间隔

    InternalDialog(@NonNull Context context, CheckUpdateDialog checkUpdateDialog) {
        super(context, R.style.checkUpdateDialogStyle);
        mActivity = (Activity) context;
        mCheckUpdateDialog = checkUpdateDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_check_update_layout, null);
        setContentView(layout);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        checkUpdateImage =  findViewById(R.id.check_update_image);
        checkUpdateVersionCode =  findViewById(R.id.check_update_version_code);
        checkUpdateVersionSize = findViewById(R.id.check_update_version_size);
        checkUpdateVersionLog =  findViewById(R.id.check_update_version_log);
        checkUpdateProgressBar =  findViewById(R.id.check_update_progress);
        checkUpdateNegative =  findViewById(R.id.check_update_negative);
        checkUpdatePositive =  findViewById(R.id.check_update_positive);
    }

    private void initEvent() {
        //暂不更新 or 退出应用
        checkUpdateNegative.setOnClickListener(v -> dismissIfNeeded());
        //立即更新
        checkUpdatePositive.setOnClickListener(v -> {
            //防抖动,两次点击间隔小于500ms都return;
            if (System.currentTimeMillis() - timeRange < 500) {
                return;
            }
            timeRange = System.currentTimeMillis();

            if (isDownloadComplete) {
                AppUtil.installApk(getContext(), mApk);
                return;
            }
            if (mActivity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permissionStatus = ActivityCompat.checkSelfPermission(mActivity, Manifest.permission_group.STORAGE);
                    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                        mCheckUpdateDialog.requestPermissions(new String[]{Manifest.permission.
                                WRITE_EXTERNAL_STORAGE}, 0x007);
                    }else {
                        downloadInBackgroundIfNeeded();
                    }
                } else {
                    downloadInBackgroundIfNeeded();
                }
            } else {
                downloadInBackgroundIfNeeded();
            }
        });
    }

    void downloadInBackgroundIfNeeded() {
        if (mOption.isForceUpdate()) {
            checkUpdatePositive.setClickable(false);
            Check.download(mOption.getNewAppUrl(), mOption.getFilePath(), mOption.getFileName())
                    .callback(this).execute();
        } else {
            Intent intent = new Intent(mActivity, DownloadService.class);
            intent.putExtra("CheckUpdateOption", mOption);
            getContext().startService(intent);
            dismiss();
        }
    }

    void dismissIfNeeded() {
        if (mOption.isForceUpdate()) {
            System.exit(0);
            Process.killProcess(Process.myPid());
        } else {
            dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        if (!TextUtils.isEmpty(mOption.getImageUrl()) && mLoader != null) {
            mLoader.loadImage(checkUpdateImage, mOption.getImageUrl());
        } else if (mOption.getImageResId() != 0 && mLoader != null) {
            checkUpdateImage.setImageResource(mOption.getImageResId());
        }
        if (mOption.isForceUpdate()) {
            setCancelable(false);
            checkUpdateNegative.setText("退出应用");
        }
        if (TextUtils.isEmpty(mOption.getNewAppVersionName())) {
            checkUpdateVersionCode.setVisibility(View.GONE);
        } else {
            checkUpdateVersionCode.setText("版本号：" + mOption.getNewAppVersionName());
        }
        if (mOption.getNewAppSize() == 0) {
            checkUpdateVersionSize.setVisibility(View.GONE);
        } else {
            checkUpdateVersionSize.setText("新版大小：" + mOption.getNewAppSize() + "M");
        }
        if (TextUtils.isEmpty(mOption.getNewAppUpdateDesc())) {
            checkUpdateVersionLog.setVisibility(View.GONE);
        } else {
            checkUpdateVersionLog.setText(mOption.getNewAppUpdateDesc());
        }
    }


    void apply(CheckUpdateOption option) {
        if (option == null) throw new NullPointerException("option==null");
        mOption = option;
    }

    @Override
    public void checkUpdateStart() {
        checkUpdatePositive.setText("正在下载...");
    }

    @Override
    public void checkUpdateFailure(Throwable t) {
        LogUtils.e(t);
        checkUpdatePositive.setText("下载失败");
        checkUpdatePositive.setClickable(true);
    }

    @Override
    public void checkUpdateFinish() {
        checkUpdatePositive.setClickable(true);
    }

    @Override
    public void downloadProgress(long currentLength, long totalLength) {
        checkUpdateProgressBar.setVisibility(View.VISIBLE);
        checkUpdateProgressBar.setMax((int) totalLength);
        checkUpdateProgressBar.setProgress((int) currentLength);
    }

    @Override
    public void downloadSuccess(File apk) {
        mApk = apk;
        checkUpdatePositive.setText("点击安装");
        isDownloadComplete = true;
        checkUpdatePositive.setClickable(true);
        AppUtil.installApk(getContext(), apk);
    }

    void setImageLoader(ImageLoader loader) {
        mLoader = loader;
    }

    @Override
    public void show() {
        super.show();
        resizeWidthAndHeight();
    }

    private void resizeWidthAndHeight() {
        Window window = getWindow();
        if (window == null) return;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = dp2px(260);//宽高固定为260dp
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    private int dp2px(float dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

}
