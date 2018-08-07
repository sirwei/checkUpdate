package com.wmm.check.update;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.wmm.update.Check;
import com.wmm.update.CheckUpdateOption;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        findViewById(R.id.bt_bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Check.show(MainActivity.this, update(false));
            }
        });
        findViewById(R.id.bt_bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Check.show(MainActivity.this, update(true));
            }
        });
    }

    public CheckUpdateOption update(boolean isForce) {
        return new CheckUpdateOption.Builder().setAppName("测试Apk")
                .setFileName("测试Apk")
                .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/checkupdatelib")
                .setIsForceUpdate(isForce)
                .setNewAppUpdateDesc("1、全新改版，更换新的样式 " + "\n" + "2、添加快递工具的功能，快递入库、出库和查询 " + "\n" + "3、添加快递员对接审核 " + "\n" + "4、添加账户查看以及提现的功能")
                .setNewAppUrl("http://res.dounikaixin.cn/sk-apk/dounikaixin-sk-3.1.5.apk")
                .setNewAppVersionName("1.00")
                .setNewAppSize((float) 23.00)
                .setNotificationSuccessContent("下载成功，点击安装")
                .setNotificationFailureContent("下载失败，点击重新下载")
                .setNotificationIconResId(R.mipmap.ic_launcher)
                .setNotificationTitle("发现新的版本")
                .build();
    }
}
