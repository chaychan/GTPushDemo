package com.chaychan.pushdemo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.chaychan.pushdemo.R;

import java.util.Random;

/**
 * Created by duqian on 16/01/23.
 * Android通知栏封装
 */
public class NotificationUtils {

    private static final int SmallIcon = R.mipmap.ic_launcher;
    public static final int NotificationNumber = 1;
    private static NotificationManager mManager;
    private static NotificationCompat.Builder mBuilder;
    private static final Random RANDOM = new Random();

    /**
     * 获取Builder
     */
    public static NotificationCompat.Builder getBuilder(Context context) {
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setNumber(NotificationNumber)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)//可以让通知显示在最上面
                .setAutoCancel(true);
        //.setDefaults(Notification.DEFAULT_VIBRATE);
        return mBuilder;
    }

    /**
     * 获取NotificationManager
     */
    public static NotificationManager getManager(Context context) {

        if (mManager == null) {
            synchronized (NotificationUtils.class) {
                if (mManager == null) {
                    mManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                }
            }
        }
        return mManager;
    }


    /**
     * 显示普通的通知
     */
    public static void showOrdinaryNotification(Context context, String title, String text, String ticker,
                                                int icon, int channel) {
        mBuilder = getBuilder(context);
        mManager = getManager(context);
        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setContentIntent(getDefalutIntent(context, Notification.FLAG_AUTO_CANCEL))
                .setNumber(NotificationNumber)//显示数量
                .setTicker(ticker)//通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
                .setWhen(System.currentTimeMillis())//通知产生的时间
                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                .setAutoCancel(true)//设置让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。如一个文件下载,网络连接。
                .setDefaults(Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果。最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 //DEFAULT_VIBRATE requires VIBRATE permission
                .setSmallIcon(SmallIcon)
        ;
        Notification mNotification = mBuilder.build();
        mNotification.icon = icon;
        mManager.notify(dealWithId(channel), mNotification);
    }

    //获取默认的延期意图
    public static PendingIntent getDefalutIntent(Context context, int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(), flags);
        return pendingIntent;
    }

    //通知channel ID，唯一标示一个通知
    public static int dealWithId(int channel) {
        return channel >= 1 && channel <= 100 ? channel : RANDOM.nextInt(Integer.MAX_VALUE - 100) + 101;
    }

    //获取系统SDK版本
    public static int getSystemVersion() {
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    /**
     * 清除所有的通知
     *
     * @param context
     */
    public static void clearAllNotifification(Context context) {
        mManager = getManager(context);
        mManager.cancelAll();
    }

    /**
     * 清除通知
     */
    public static void clearNotifificationById(Context context, int channel) {
        mManager = getManager(context);
        mManager.cancel(dealWithId(channel));
    }

    private static int iconId = 0;

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    /**
     * 默认图标
     */
    private static int getPushIconId(Context context) {
        if (iconId == 0) {
            iconId = context.getApplicationInfo().icon;
        }
        if (iconId < 0) {
            iconId = android.R.drawable.sym_def_app_icon;
        }
        return iconId;
    }

    //去通知文本的前15位字符
    private static String getSubStringTitle(String desc) {
        if (desc != null && !"".equals(desc) && desc.length() > 15) {
            return desc.substring(0, 15);
        } else {
            return desc;
        }
    }

    /**
     * 显示进度的通知栏
     */
    public static void showProgressNotification(Context context, String title, String text, String ticker,
                                                Intent resultIntent, boolean indeterminate, int progress,
                                                int icon, int channel) {
        mBuilder = getBuilder(context);
        mManager = getManager(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle(ticker)//ticker,title
                .setContentText(text)
                .setTicker(ticker)
                .setSmallIcon(SmallIcon)
                .setContentIntent(pendingIntent);
        if (indeterminate) {
            //不确定进度的 设置为true就是不确定的那种进度条效果
            mBuilder.setProgress(0, 0, true).setOngoing(false).setAutoCancel(true);
        } else {
            //确定进度的
            mBuilder.setProgress(100, progress, false).setOngoing(true);
        }

        Notification progressNotification = mBuilder.build();
        progressNotification.icon = icon;
        mManager.notify(dealWithId(channel), progressNotification);
    }

    /**
     * 显示带意图的通知栏
     * @param context 上下文
     * @param title 标题
     * @param text 内容
     * @param ticker
     * @param pendingIntent
     * @param smallIcon  小图标
     * @param largeIcon 大图标
     */
    public static void showIntentNotification(Context context, String title, String text, String ticker,
                                              PendingIntent pendingIntent, int smallIcon,int largeIcon) {
        mBuilder = getBuilder(context);
        mManager = getManager(context);

        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setTicker(ticker)
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),largeIcon))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT  > Build.VERSION_CODES.JELLY_BEAN){
            //如果当前手机版本大于16 也就是Android4.1以上，可以使用大文本布局
            NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();
            inboxStyle.bigText(text);
            mBuilder.setStyle(inboxStyle);
        }

        mManager.notify(getRandowReqCode(), mBuilder.build());
    }

    public static int getRandowReqCode(){
        return RANDOM.nextInt(Integer.MAX_VALUE);
    }

}