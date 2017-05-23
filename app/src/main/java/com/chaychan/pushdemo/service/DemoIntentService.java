package com.chaychan.pushdemo.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.chaychan.pushdemo.R;
import com.chaychan.pushdemo.global.PushConstants;
import com.chaychan.pushdemo.receiver.NotificationReceiver;
import com.chaychan.pushdemo.utils.NotificationUtils;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class DemoIntentService extends GTIntentService {

    private Handler handler = new Handler(Looper.getMainLooper());

    public DemoIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        byte[] payload = msg.getPayload();

        String message = new String(payload);
        Log.i(TAG, "onReceiveMessageData: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            int isNotification = jsonObject.optInt(PushConstants.IS_NOTIFICATION);
            if (isNotification == 1) {
                //属于通知类的透传消息
                showNotification(context, jsonObject);
            } else {
                //提醒消息
                dealNotifyMessage(context, jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据消息内容弹出通知框
     *
     * @param context
     * @param jsonObject
     */
    private void showNotification(Context context, JSONObject jsonObject) {
        String title = jsonObject.optString(PushConstants.TITLE);
        String content = jsonObject.optString(PushConstants.CONTENT);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            return;
        }

        int pageNum = jsonObject.optInt(PushConstants.PAGE_NUMBER);
        String contentId = jsonObject.optString(PushConstants.CONTENT_ID);

        //设置点击通知后是发送广播，传递对应的数据
        Intent broadcastIntent = new Intent(context, NotificationReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt(PushConstants.PAGE_NUMBER, pageNum);
        bundle.putString(PushConstants.CONTENT_ID, contentId);
        broadcastIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(context, NotificationUtils.getRandowReqCode(), broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtils.showIntentNotification(context, title, content, title, pendingIntent, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
    }

    /**
     * 处理提醒消息
     *
     * @param context
     * @param jsonObject
     */
    private void dealNotifyMessage(final Context context, JSONObject jsonObject) {
        int notifyType = jsonObject.optInt(PushConstants.NOTIFY_TYPE, PushConstants.MESSAGE_CENTER_NOTIFY);
        //判断提醒信息的类型，做相应的UI操作，由于此处处于IntentService中，做UI操作需要进行线程的切换，这里使用了handler的post()方法切换
        switch (notifyType) {
            case PushConstants.MESSAGE_CENTER_NOTIFY:
                //消息中心的提醒
                handler.post(new Runnable() {
                    public void run() {
                       /* if (MainActivity.instance != null){
                            MainActivity.instance.showTabNotify(2);//底部我的tab显示提示点
                        }
                        if (MineFragment.ivMessage != null){
                            MineFragment.ivMessage.setImageResource(R.mipmap.img_message_unread);//设置消息中心图标为未读的
                        }
                        //更新消息表的未读数
                        UnReadDao.saveOrUpdate(UnReadDao.getUnreadCount() + 1);*/

                       Log.i(TAG,"收到消息提醒，显示小红点");
                    }
                });
                break;
        }
    }


    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }
}