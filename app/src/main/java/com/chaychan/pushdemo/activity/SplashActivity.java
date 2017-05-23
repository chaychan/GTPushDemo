package com.chaychan.pushdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.chaychan.pushdemo.R;
import com.chaychan.pushdemo.global.Constants;
import com.chaychan.pushdemo.utils.PreUtils;

public class SplashActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = null;
                if (PreUtils.getBoolean(SplashActivity.this, Constants.IS_LOGIN,false)){
                    //如果已经处于登录状态，直接进入主界面
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null){
                        //如果是App没开启，点击通知打开App，传递的bundle就可以到达MainActivity
                        intent.putExtras(bundle);
                    }
                }else{
                    //未登录，跳转登录界面
                    intent = new Intent(SplashActivity.this,LoginActivity.class);
                }
                startActivity(intent);
            }
        },3000);
    }
}
