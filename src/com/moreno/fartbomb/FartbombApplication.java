package com.moreno.fartbomb;

import android.app.Application;
import android.content.Context;

public class FartbombApplication extends Application {

    private static Context ctxApplication;

    public static Context getAppContext() {
        return ctxApplication;
    }

    @Override
    public void onCreate() {
        ctxApplication = this;
        super.onCreate();
    }
}
