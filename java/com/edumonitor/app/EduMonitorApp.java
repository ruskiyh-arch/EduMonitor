package com.edumonitor.app;

import android.app.Application;
import com.edumonitor.app.utils.DataSeeder;

public class EduMonitorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DataSeeder.seedIfNeeded();
    }
}
