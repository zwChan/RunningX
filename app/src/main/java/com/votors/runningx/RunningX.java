package com.votors.runningx;

import android.app.Application;

//import cn.sharesdk.framework.ShareSDK;


public class RunningX extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Conf.init(getApplicationContext());
//		ShareSDK.initSDK(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
//		ShareSDK.stopSDK(this);
	}
}