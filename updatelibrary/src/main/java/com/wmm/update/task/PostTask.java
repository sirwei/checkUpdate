package com.wmm.update.task;

import com.wmm.update.request.IRequest;
import com.wmm.update.request.PostRequest;

import java.util.Map;


public class PostTask extends BaseTask<PostTask> {
    public PostTask(String url) {
        super(url);
    }

    public PostTask(String url, Map<String, String> params) {
        super(url, params);
    }

    @Override
    public void execute() {
        IRequest request = new PostRequest(mUrl, mParams, mCallback);
        request.request();
    }
}
