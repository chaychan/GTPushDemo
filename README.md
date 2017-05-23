# GTPushDemo
###推送通知的跳转处理和消息提醒
&emsp;&emsp;消息推送功能在App开发中经常用到，用于及时通知用户，推送用户订阅的相关的信息。本篇文章并非详细介绍如何集成和使用推送，关于推送相关知识的介绍，郭神已经在慕课网中有相关的教程，大家如果想要深入研究，可以去观看郭神的相关视频。

###效果演示
一、App处于运行状态下:

1.接收到通知，点击通知打开对应activity的演示:

![](./img/1.gif)

2.接收到通知，点击通知传值并打开对应的activity的演示:

![](./img/2.gif)


二、App进程处于销毁状态，但是后台依旧运行着推送常驻的service：

1.接收到通知，点击通知打开对应的activity, activity启动流程,SplashActivity -> MainActivity - > 对应的activity

![](./img/3.gif)

2.接收到通知，点击通知传值并打开对应的activity, activity启动流程,SplashActivity -> MainActivity - > 对应的activity

![](./img/4.gif)


###推送的相关流程

我所使用的是个推的推送功能，个推在推送这方面，做得还是相对不错的。

1. 绑定推送别名，一般在登录完成以后进行别名的绑定。（可以绑定用户名为推送的别名，后台可以通过用户名进行推送）。

这是demo的登录操作：

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


2.解绑推送别名，一般在退出登录以后进行别名的解绑。（解绑后则不会再接收到使用别名类型的推送）。

这是demo的退出登录操作:

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

3.接收到后台推送的消息：    
&emsp;&emsp;个推支持自定义消息的处理，具体请查看个推官方文档，很容易上手的。 自己定义一个类继承 GTIntentService，并在清单文件中注册，当收到透传消息时，onReceiveMessageData（）方法会进行回调。虽然个推可以在后台向Android端设备直接推送通知，但由于IOS端仅可接收透传消息，所以这里通过在透传消息中设置一个标识，如果是通知类消息，获取到消息后，自己创建对应的通知进行显示。

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

showNotification（）方法是对通知类的透传消息的处理：

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

dealNotifyMessage()方法是对提醒类的透传消息的处理：
   
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


###通知的点击处理
&emsp;&emsp;关于通知的点击处理，showNotification()在创建通知的时候，需要设置PendingIntent：

      //设置点击通知后是发送广播，传递对应的数据
        Intent broadcastIntent = new Intent(context, NotificationReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt(PushConstants.PAGE_NUMBER, pageNum);//所要打开的页面的编号
        bundle.putString(PushConstants.CONTENT_ID, contentId);//id
        broadcastIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(context, NotificationUtils.getRandowReqCode(), broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

&emsp;&emsp;我们通过PendingIntent.getBroadcast（）方法创建PendingIntent，需要传递的数据：pageNum属于页面的编号，定义在PushConstants中，用于判断跳转哪个页面,如果是订单的通知，则contentId则是对应订单的id,用于订单详情页获取订单数据。

&emsp;&emsp;当点击通知的时候，将会发送广播并传值给NotificationReceiver，具体的跳转操作交由它进行处理，接下来介绍最重要的内容。

&emsp;&emsp;NotificationReceiver接收到点击通知后发出的广播，在onReceive()方法中回调，获取传过来的数据并进行相应的处理。

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        int pageNum = bundle.getInt(PushConstants.PAGE_NUMBER, 0);
        String contentId = bundle.getString(PushConstants.CONTENT_ID, "");

        Intent destinationIntent = null;//目标intent
        switch (pageNum) {
            case PushConstants.PAGE_ORDER_DETAIL:
                //订单详情页
                destinationIntent = new Intent(context, OrderDetailActivity.class);
                destinationIntent.putExtra(OrderDetailActivity.ORDER_ID, contentId);//传订单的id
                break;
            case PushConstants.PAGE_MESSAGE_CENTER:
                //消息中心
                destinationIntent = new Intent(context, MessageCenterActivity.class);
                break;
        }

        if (SystemUtils.isAppAlive(context, context.getPackageName())) {
            //如果存活的话，就直接启动目标Activity，但要考虑一种情况，就是app的进程虽然仍然在
            //但Task栈已经空了，比如用户点击Back键退出应用，但进程还没有被系统回收，如果直接启动
            //目标Activity,再按Back键就不会返回MainActivity了。所以在启动 目标Activity前，要先启动MainActivity。
            //将MainAtivity的launchMode设置成SingleTask, 或者在下面flag中加上Intent.FLAG_CLEAR_TOP,
            //如果Task栈中有MainActivity的实例，就会把它移到栈顶，把在它之上的Activity都清理出栈，
            //如果Task栈不存在MainActivity实例，则在栈顶创建

 			Log.i(TAG, "the app process is alive");
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intentList == null){
                intentList = new ArrayList<Intent>();
            }
            intentList.add(mainIntent);
            //如果目标intent不为空，一起打开
            if (destinationIntent != null){
                intentList.add(destinationIntent);
            }
            context.startActivities(intentList.toArray(new Intent[intentList.size()]));
        } else {
            //如果app进程已经被杀死，先重新启动app，将目标Activity的启动参数传入Intent中，参数经过
            //SplashActivity传入MainActivity，此时app的初始化已经完成，在MainActivity中就可以根据传入
            // 参数跳转到目标Activity中去了
            Log.i(TAG, "the app process is dead");
            Intent launchIntent = context.getPackageManager().
                    getLaunchIntentForPackage(context.getPackageName());
            launchIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launchIntent.putExtras(bundle);
            context.startActivity(launchIntent);
        }
    }

&emsp;&emsp;获取到传过来的数据，通过pageNum（页面编号）判断要打开哪个activity，如果是订单详情页的activity则需要传递一个contentId（此时则为订单的id），作为详情页获取数据的一个参数。

主要分为两种情况考虑：

App进程存在和App进程不存在两种情况。判断App进程是否存在，可以通过遍历当前手机系统中的所有运行的进程，通过判断运行的进程的包名是否与当前App的包名一致从而知道App进程是否存在。

    `/**
     * 判断应用是否已经启动
     * @param context 一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){
                Log.i("NotificationLaunch",
                        String.format("the %s is running, isAppAlive return true", packageName));
                return true;
            }
        }
        Log.i("NotificationLaunch",
                String.format("the %s is not running, isAppAlive return false", packageName));
        return false;
    }`


&emsp;&emsp;一、当APP进程存在的时候，在这种情况下，进程虽然存在，但是任务栈可能已经为空，所在无论MainActivity是否已经在栈内，都要打开MainActivity和目标activity,必须注意的是，要将MainActivity的启动模式设置为SingleTask，即栈内复用并且会清除位于其顶部的所有已经开启的activity，只留下MainActivity（一般App中MainActivity位于栈低）和目标activity。

&emsp;&emsp;二、当App进程已经被杀死，点击通知后启动App，将要传递的数据通过SplashActivity传入MainActivity，在MainActivity根据数据判断要打开的目标activity，从而进行跳转,SplashActivity中，通过判断getIntent().getExtras()是否为空，如果不为空，则传递给MainActivity。

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

MainActivity中，做了以下处理：

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ...

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

&emsp;&emsp;到此为止，关于App推送通知的处理介绍就结束了，至于数据的定义和相关页面跳转的判断，每个人有各自的规则，我这里是通过在PushConstant中定义json对应的key的常量以及页面编号的常量，后台和App端都定义了相同的常量，同时封装好了对应的推送工具类，适用于安卓和IOS端，需要提醒的是，IOS需要在个推管理后台中配置推送证书。关于演示的App的demo和后台的推送demo，大家可以通过下面的github地址进行clone，希望可以帮助到大家。

[https://github.com/chaychan/GTPushDemo.git](https://github.com/chaychan/GTPushDemo.git)(android端)

[https://github.com/chaychan/GTPushDemoJava.git](https://github.com/chaychan/GTPushDemoJava.git)（后台java代码）
