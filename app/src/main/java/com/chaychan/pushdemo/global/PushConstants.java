package com.chaychan.pushdemo.global;

/**
 * @author chaychan
 * @description: 推送的常量配置
 * @date 2017/4/26  11:31
 */
public class PushConstants {
    /**
     * 属于通知
     */
    public static final String IS_NOTIFICATION = "isNotification";
    /**
     * 标题
     */
    public static final String TITLE = "title";
    /**
     * 内容
     */
    public static final String CONTENT = "content";
    /**
     * 内容的id
     */
    public static final String CONTENT_ID = "contentId";

    /**
     * 对应打开页面的编号
     */
    public static final String PAGE_NUMBER = "pageNumber";

    /** 主页面 */
    public static final int PAGE_MAIN = 0;

    /**
     * 订单详情页
     */
    public static final int PAGE_ORDER_DETAIL = 1000;

    /**
     * 消息中心
     */
    public static final int PAGE_MESSAGE_CENTER = 1001;

    //******************************推送提醒***********************************************

    public static final String NOTIFY_TYPE = "notifyType";

    public static final String UNREAD_COUNT = "unReadCount";

    /**
     * 消息中心的提醒
     */
    public static final int MESSAGE_CENTER_NOTIFY = 2000;

}
