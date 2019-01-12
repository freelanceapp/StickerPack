package com.ideas.stickermaker.app;

import android.support.multidex.MultiDexApplication;

import com.orhanobut.hawk.Hawk;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this).build();
    }
}
