package com.wmm.update.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
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

import com.wmm.update.BR;
import com.wmm.update.Check;
import com.wmm.update.CheckUpdateOption;
import com.wmm.update.R;
import com.wmm.update.callback.DownloadCallback;
import com.wmm.update.databinding.DialogCheckUpdateBinding;
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
    private Activity mActivity;
    private boolean isDownloadComplete;
    private File mApk;
    private CheckUpdateDialog mCheckUpdateDialog;
    private long timeRange;
    private DataHolder dataHolder = new DataHolder();
    private DialogCheckUpdateBinding binding;

    InternalDialog(@NonNull Context context, CheckUpdateDialog checkUpdateDialog) {
        super(context, R.style.checkUpdateDialogStyle);
        mActivity = (Activity) context;
        mCheckUpdateDialog = checkUpdateDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_check_update, null, false);
        binding.setVariable(BR.data, dataHolder);
        setContentView(binding.getRoot());
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        if (!TextUtils.isEmpty(mOption.getImageUrl()) && mLoader != null) {
            mLoader.loadImage(binding.checkUpdateImage, mOption.getImageUrl());
        } else if (mOption.getImageResId() != 0 && mLoader != null) {
            binding.checkUpdateImage.setImageResource(mOption.getImageResId());
        }
        if (mOption.isForceUpdate()) {
            setCancelable(false);
            dataHolder.tvNegativeUpdate.set("退出应用");
        }
        if (TextUtils.isEmpty(mOption.getNewAppVersionName())) {
            binding.checkUpdateVersionCode.setVisibility(View.GONE);
        } else {
            dataHolder.versionCode.set(mOption.getNewAppVersionName());
        }
        if (mOption.getNewAppSize() == 0) {
            binding.checkUpdateVersionSize.setVisibility(View.GONE);
        } else {
            dataHolder.versionSize.set(String.valueOf(mOption.getNewAppSize()));
        }
        if (TextUtils.isEmpty(mOption.getNewAppUpdateDesc())) {
            binding.checkUpdateVersionLog.setVisibility(View.GONE);
        } else {
            dataHolder.versionLog.set(mOption.getNewAppUpdateDesc());
        }
    }
    public void downloadInBackgroundIfNeeded() {
        if (mOption.isForceUpdate()) {
            binding.checkUpdatePositive.setClickable(false);
            Check.download(mOption.getNewAppUrl(), mOption.getFilePath(), mOption.getFileName())
                    .callback(this).execute();
        } else {
            Intent intent = new Intent(mActivity, DownloadService.class);
            intent.putExtra("CheckUpdateOption", mOption);
            getContext().startService(intent);
            dismiss();
        }
    }

    public void dismissIfNeeded() {
        if (mOption.isForceUpdate()) {
            System.exit(0);
            Process.killProcess(Process.myPid());
        } else {
            dismiss();
        }
    }

   public InternalDialog apply(CheckUpdateOption option) {
        if (option == null) throw new NullPointerException("option==null");
        mOption = option;
        return this;
    }

    @Override
    public void checkUpdateStart() {
        dataHolder.tvPositiveUpdate.set("正在下载...");
    }

    @Override
    public void checkUpdateFailure(Throwable t) {
        LogUtils.e(t);
        dataHolder.tvPositiveUpdate.set("下载失败");
        binding.checkUpdatePositive.setClickable(true);
    }

    @Override
    public void checkUpdateFinish() {
        binding.checkUpdatePositive.setClickable(true);
    }

    @Override
    public void downloadProgress(long currentLength, long totalLength) {
        binding.checkUpdateProgress.setVisibility(View.VISIBLE);
        binding.checkUpdateProgress.setMax((int) totalLength);
        binding.checkUpdateProgress.setProgress((int) currentLength);
    }

    @Override
    public void downloadSuccess(File apk) {
        mApk = apk;
        dataHolder.tvPositiveUpdate.set("点击安装");
        isDownloadComplete = true;
        binding.checkUpdatePositive.setClickable(true);
        AppUtil.installApk(getContext(), apk);
    }

   public InternalDialog setImageLoader(ImageLoader loader) {
        mLoader = loader;
        return this;
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


    public class DataHolder {
        public ObservableField<String> versionCode = new ObservableField<>("");
        public ObservableField<String> versionSize = new ObservableField<>("");
        public ObservableField<String> versionLog = new ObservableField<>("");
        public ObservableField<String> tvNegativeUpdate = new ObservableField<>("暂不更新");
        public ObservableField<String> tvPositiveUpdate = new ObservableField<>("立即更新");
        /**
         * 暂不更新
         */
        public View.OnClickListener negativeUpdate = v -> dismissIfNeeded();
        /**
         * 立即更新
         */
        public View.OnClickListener positiveUpdate = v -> {
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
                    } else {
                        downloadInBackgroundIfNeeded();
                    }
                } else {
                    downloadInBackgroundIfNeeded();
                }
            } else {
                downloadInBackgroundIfNeeded();
            }
        };
    }

}
