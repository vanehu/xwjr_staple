# xwjr_staple
### 简介
本库具有开屏、活动、升级、推送功能集成

### 导包
    
    implementation 'com.github.zhuxiaohang2016:xwjr_staple:版本号'

### 配置

gradle配置
    
    android{
        defaultConfig{
            
            ndk { abiFilters "armeabi"}
        
            manifestPlaceholders = [
                JPUSH_PKGNAME: applicationId,
                JPUSH_APPKEY : "*****************", //JPush 上注册的包名对应的 Appkey.
                JPUSH_CHANNEL: "developer-default", //暂时填写默认值即可.
             ]
          }
    }
    
 ### 初始化
    StapleUtils.init(context);//初始化工具类
    StapleConfig.INSTANCE.setAppSource(StapleConfig.APPHUB);//哪个app
    StapleConfig.INSTANCE.setDebug(true);//是否是debug模式
    JPushInterface.setDebugMode(true);//jpush是否是debug模式
    JPushInterface.init(this);//jpush初始化
    CrashHandlerManager.getInstance().init(getApplicationContext());//崩溃处理
    
    
 ### 使用
 
 1.自定义SplashActivity 继承  StapleSplashActivity，重写customDealActivityData,customDealActivityData方法会返回需要弹出的活动数据，自行处理，详见下方代码
   
       public class SplashActivity extends StapleSplashActivity {
          @Override
          public void customDealActivityData(StapleActivityBean.DataBean dataBean) {
              if (dataBean==null){
                 //无活动数据
              }else {
                 //有活动数据
              }
          }
        }
        
 2.app有activity在后台被回收后，如果需要处理，则进行如下处理
    
    在application里进行是否在后台运行的统计
    
    public final static int APP_STATUS_KILLED = 0; // 表示应用是被杀死后在启动的
    public final static int APP_STATUS_NORMAL = 1; // 表示应用时正常的启动流程
    public static int APP_STATUS = APP_STATUS_KILLED; // 记录App的启动状态
    private int mActivityCount = 0;
    
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
                AppStatusUtils.saveAppInBackgroundStatus(context,String.valueOf(mActivityCount));
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                AppStatusUtils.saveAppInBackgroundStatus(context,String.valueOf(mActivityCount));
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
        
        
     在baseActivity里的onDestory方法增加如下内容：
        if (AppStatusUtils.isAppInBackground(this)) {
            AppStatusUtils.saveHaveActivityKilledStatus(this, "true");
        }
        
     在baseActivity里的onCreate方法增加如下内容：
        if (AppStatusUtils.isNeedRestart(this)) {
                AppStatusUtils.reStartApp(this,activity);
            }
        if (MyApplication.APP_STATUS != MyApplication.APP_STATUS_NORMAL) { // 非正常启动流程，直接重新初始化应用界面
                AppStatusUtils.reStartApp(this, new WelcomeActivity());
                return;
            }
            
     在开屏页最先调用的地方增加如下内容
        MyApplication.APP_STATUS = MyApplication.APP_STATUS_NORMAL; // App正常的启动，设置App的启动状态为正常启动
            
     如果开屏页没有继承StapleSplashActivity，需要在开屏页 最先调用的地方 增加如下内容(注意：尤其开屏页也继承了baseActivity)：
         AppStatusUtils.saveHaveActivityKilledStatus(this, "");
            
            
 3.开屏图相关功能会自动处理，如需更改默认图，可以增加同名资源文件，或者修改此库，具体名称如下
    
    staple_apphub_window_bg.png   --  apphub开屏图
    staple_wwxhb_window_bg.png  --  望望先花b端开屏图
    staple_wwxhc_null_window_bg.png  --  望望先花c端未登录状态开屏图
    staple_wwxhc_wwxh_window_bg.png  --  望望先花c端借款人开屏图
    staple_wwxhc_xssq_window_bg.png  --  望望先花c端业务员角色开屏图
    staple_wwxjk_window_bg.png   --  望望小金卡开屏图
    staple_xwb_window_bg.png  --  希望宝开屏图
    staple_xwjr_window_bg.png  --  希望金融开屏图
 
 4.推送功能目前只支持简单的打开app操作，最好给用户配置alias 和 tag， 详见下方代码
 
    alias以用户手机号为标准，
 
    tags根据不同app区分。 
 
    希望金融：以用户等级
    
    望望先花b：以用户权限
    
    望望先花c：以用户角色
    
    望望小金卡：暂无
    
    希望宝：暂无
    
 
     JPushInterface.setAlias(this, 5233, "a0000000");

     Set<String> tags = new HashSet<>();
     tags.add("A");
     tags.add("B");
     JPushInterface.setTags(this, 5233, tags);
     
     JpushManager.defaultJpushView(this);//设置默认推送样式，不设置则使用JPush默认样式

 5.身份证识别/活体检测相关功能
 
        //尽量提前调用
        AuthManager.getIDCardLicense(this)
        AuthManager.getLivingLicense(this)
        
        //初始化authManagerHelper 
         val authManagerHelper = AuthManagerHelper(activity)
         authManagerHelper.setRiskShieldDataListener(object : AuthManagerHelper.RiskShieldData {
                                override fun liveData(isApproved: Boolean) {
                                    //处理活体数据
                                }

                                override fun idCardData(authIDCardBean: StapleAuthIDCardBean.ResultBean) {
                                    //处理身份证数据
                                }
                                
                                override fun stepData(riskShieldStepBean: StapleRiskShieldStepBean.ResultBean) {
                                    //处理风控中心数据
                                }
                            })
        //获取风控中心相关数据
        authManagerHelper.queryRiskShieldStep()
        
        //开始扫描身份证 activity:当前activity  fragment:当前fragment（可为null）  side：0(正面)1(反面)
        AuthManager.openScanIdActivity(activity,fragment,side)
        
        //开始活体检测 activity:当前activity  fragment:当前fragment（可为null）
        AuthManager.startLivingDetect(activity,fragment)
        
        //onActivityResult固定处理
         try {
            if (resultCode == Activity.RESULT_OK)
                when (requestCode) {
                    AuthManager.PAGE_INTO_IDCARDSCAN -> {
                        AuthManager.dealIDCardScan(data!!) { filePath ->
                            authManagerHelper.upLoadIDCardInfo(filePath)
                        }
                    }

                    AuthManager.PAGE_INTO_LIVENESS -> {
                        AuthManager.dealLivingData(this, data!!) { imagesMap, bestImg, delta ->
                            authManagerHelper.upLoadLiveData(idName, idNo, delta, imagesMap)
                        }
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
     
  6.webView相关
        
        StapleWebView
        
        //xml
        <com.xwjr.staple.activity.StapleWebView
            android:id="@+id/webView"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
       
        //url访问地址
        webView.loadUrl(url)
        
        //监听标题变化
         webView.addTitleChangeListener(object : StapleWebView.TitleChangeListener {
            override fun changedTitle(title: String) {
                tv_title.text = title
            }
        })
        
        //监听js调用原生的接口
         webView.registerHandler("BidListPage") { data, _ ->
            logI("webViewHandler $data ")
        }
        
        //设置进度条颜色
         webView.setProgressBarColors(0xffff0000.toInt(),0xff00ff00.toInt())
         
         //监听返回键处理
         
         webView.onBackListener()
         //example
         override fun onBackPressed() {
             if (webView.onBackListener()) {
                return
                }
               super.onBackPressed()
         }   
         
   7.其他网络请求
   
       //初始化stapleHttpHelper
       var stapleHttpHelper = StapleHttpHelper(this)
       
       //获取图形验证码 以及 监听事件
       stapleHelper?.getCaptchaData()
       stapleHelper?.addCaptchaListener(object : StapleHttpHelper.CaptchaListener {
            override fun backData(captchaToken: String, captchaBitmap: Bitmap) {
            }
        })
        
       //发送短信验证码 以及 监听事件
       stapleHelper?.sendSMSCaptcha("18810409404", captchaToken, et_captcha.text.toString())
       stapleHelper?.addSMSCaptchaListener(object : StapleHttpHelper.SMSCaptchaListener {
            override fun backData(smsCaptchaToken: String) {
                showToast("token:$smsCaptchaToken")
            }
        })
        
   8.JWT 鉴权
    
        //获取JWT值 type: JWTUtils.CONTRACT(合同中心)  JWTUtils.SMS（短信中心）  JWTUtils.LOCATION（定位相关）
        //JWTUtils.XIAODAI(小贷系统)  JWTUtils.FKZX(风控中心)
        JWTUtils.getJWT(type)  
        JWTUtils.getJWT(type，playholder) //playholder example : {"name":"zxh"} 
        
   9.progressDialog 使用
    
        //初始化  resId:资源id  hint:提示内容（默认"加载中..."）
        val progress = ProgressDialogFragment.newInstance(resId,hint)
        //显示
        progress.show(supportFragmentManager)
        //消失
        progress.dismiss()


        
### 注意事项
    
    1.用户登录之后需要调用
    StapleUserTokenManager.saveUserToken(token) //access_token
    2.用户退出登录需调用
    StapleUserTokenManager.clearUserToken()
    3.所有涉及接口调用的方法，都有 showProgress 参数 代表是否使用 "加载中..." 显示框
