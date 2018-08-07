package com.wmm.update.imageloader;
import android.widget.ImageView;
public interface ImageLoader {
    /**
     * 从网络加载图片
     *
     * @param view     the ImageView
     * @param imageUrl 图片URL
     */
    void loadImage(ImageView view, String imageUrl);
}
