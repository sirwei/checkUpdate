package com.wmm.update.request;
import com.wmm.update.callback.BaseCallback;
import com.wmm.update.constants.Const;

import java.util.Map;

public class PostRequest extends AbsStringRequest {

    public PostRequest(String url) {
        super(url);
    }

    public PostRequest(String url, Map<String, String> params) {
        super(url, params);
    }

    public PostRequest(String url, Map<String, String> params, BaseCallback callback) {
        super(url, params, callback);
    }

    @Override
    String requestMethod() {
        return Const.POST;
    }

}
