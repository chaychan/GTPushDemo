package com.chaychan.pushdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.chaychan.pushdemo.R;

/**
 * 订单详情页
 */
public class OrderDetailActivity extends AppCompatActivity {

    public static final String ORDER_ID = "orderId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        TextView tv = (TextView) findViewById(R.id.tv);

        String productId = getIntent().getStringExtra(ORDER_ID);
        //订单详情页数据通过获取到对应的订单id访问接口获取，这里只是简单的演示下数据的传递
        tv.setText(String.format("订单id为: %s 的详情页",productId));
    }

}
