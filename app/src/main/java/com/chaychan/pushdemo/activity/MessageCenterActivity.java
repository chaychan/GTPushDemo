package com.chaychan.pushdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chaychan.pushdemo.R;

/**
 * 消息中心页面
 */
public class MessageCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);

        //消息中心没有数据传递过来，只是用于简单演示点击通知后打开对应的activity
    }

}
