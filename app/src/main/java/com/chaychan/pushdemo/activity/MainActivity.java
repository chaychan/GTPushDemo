package com.chaychan.pushdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.chaychan.pushdemo.R;
import com.chaychan.pushdemo.global.Constants;
import com.chaychan.pushdemo.global.PushConstants;
import com.chaychan.pushdemo.service.DemoIntentService;
import com.chaychan.pushdemo.service.DemoPushService;
import com.chaychan.pushdemo.utils.PreUtils;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.PushService;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pkgManager = getPackageManager();

        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        // read phone state用于获取 imei 设备信息
        boolean phoneSatePermission =
                pkgManager.checkPermission(android.Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
            requestPermission();
        } else {
            PushManager.getInstance().initialize(this.getApplicationContext(), DemoPushService.class);
        }

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), DemoIntentService.class);


        jumpToOtherPage();//做相应的跳转
    }

    /**
     * 如果有接收到传递过来的消息，则是点击通知打开的，做相应的跳转
     */
    private void jumpToOtherPage() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //如果bundle不为空，则是点击通知栏打开App进来的，获取传过来的数据
            int pageNum = bundle.getInt(PushConstants.PAGE_NUMBER, 0);
            String id = bundle.getString(PushConstants.CONTENT_ID, "");

            Intent destinationIntent = null;//目标intent
            switch (pageNum) {
                case PushConstants.PAGE_ORDER_DETAIL:
                    //订单详情页
                    destinationIntent = new Intent(this, OrderDetailActivity.class);
                    destinationIntent.putExtra(OrderDetailActivity.ORDER_ID, id);//传对应订单的id
                    break;
                case PushConstants.MESSAGE_CENTER_NOTIFY:
                    //消息中心页面
                    destinationIntent = new Intent(this, MessageCenterActivity.class);
                    break;
            }
            if (destinationIntent != null) {
                startActivity(destinationIntent);
            }
        }
    }

    public void logout(View view){
        //退出登录
        PreUtils.putBoolean(this, Constants.IS_LOGIN,false);

        String username = PreUtils.getString(this, Constants.ACCOUNT, "");
        if (!TextUtils.isEmpty(username)){
            PushManager.getInstance().unBindAlias(this,username,true);//解绑别名
        }

        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                PushManager.getInstance().initialize(this.getApplicationContext(), PushService.class);
            } else {
                PushManager.getInstance().initialize(this.getApplicationContext(), PushService.class);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
