package com.chaychan.pushdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chaychan.pushdemo.R;
import com.chaychan.pushdemo.global.Constants;
import com.chaychan.pushdemo.utils.PreUtils;
import com.igexin.sdk.PushManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPwd = (EditText) findViewById(R.id.et_pwd);
    }

    public void login(View view){
        String username = etUsername.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"用户名不能为空",Toast.LENGTH_SHORT);
            return;
        }
        if (TextUtils.isEmpty(pwd)){
            Toast.makeText(this,"密码不能为空",Toast.LENGTH_SHORT);
            return;
        }

        //执行登录逻辑，登录成功后，绑定推送的别名，可以绑定用户名为个推推送的别名
        PushManager.getInstance().bindAlias(this, username);//绑定推送别名

        PreUtils.putBoolean(this, Constants.IS_LOGIN,true);//设置为已登录
        PreUtils.putString(this,Constants.ACCOUNT,username);//保存账号，关于账号的保存，这里只是保存到sp作为演示，对于用户的信息，最好保存在数据库中

        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
