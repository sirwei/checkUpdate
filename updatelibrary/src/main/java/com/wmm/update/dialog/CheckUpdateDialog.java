package com.wmm.update.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.wmm.update.imageloader.ImageLoader;


public class CheckUpdateDialog extends BaseDialogFragment {
    private InternalDialog mDialog;
    private ImageLoader mLoader;

    @Override
    public InternalDialog getDialog() {
        return mDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new InternalDialog(mActivity, this).apply(mOption).setImageLoader(mLoader);
        return mDialog;
    }

    @Override
    protected void agreeStoragePermission() {
        mDialog.downloadInBackgroundIfNeeded();
    }

    @Override
    protected void rejectStoragePermission() {
        try {
            mDialog.dismissIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImageLoader(ImageLoader loader) {
        mLoader = loader;
    }
}

