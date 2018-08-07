package com.wmm.update.task;

import com.wmm.update.request.GetRequest;
import com.wmm.update.request.IRequest;

import java.util.Map;

public class GetTask extends BaseTask<GetTask> {

    public GetTask(String url) {
        super(url);
    }

    public GetTask(String url, Map<String, String> params) {
        super(url, params);
    }

    @Override
    public void execute() {
        IRequest request = new GetRequest(mUrl, mParams, mCallback);
        request.request();
    }
}
