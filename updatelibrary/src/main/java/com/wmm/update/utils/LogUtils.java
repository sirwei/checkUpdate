package com.wmm.update.utils;

import android.util.Log;

import com.wmm.update.Check;


/**
 * Created by qiangxi(任强强) on 2017/9/25.
 * 简易日志类
 */

public class LogUtils {
    /**
     * tag标签可通过#Check.debug(String tag, boolean debug)方法设置
     *
     * @param msg log信息
     */
    public static void e(String msg) {
        if (Check.isDebug) Log.e(Check.TAG_NAME, msg);
    }

    /**
     * 打印Throwable日志
     *
     * @param t the Throwable
     */
    public static void e(Throwable t) {
        if (t == null || !Check.isDebug) return;
        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("\nExceptionMsg：").append(t.getMessage());
        for (StackTraceElement element : stackTrace) {
            sb.append("\nClassName：").append(element.getClassName())
                    .append("\nMethodName：").append(element.getMethodName())
                    .append("\nLineNumber：").append(element.getLineNumber());
        }
        Log.e(Check.TAG_NAME, sb.toString());
    }


}
