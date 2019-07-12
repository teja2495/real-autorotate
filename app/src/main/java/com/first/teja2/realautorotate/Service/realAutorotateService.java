package com.first.teja2.realautorotate.Service;

/*
Created By
Bala Guna Teja Karlapudi
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rvalerio.fgchecker.AppChecker;

import java.lang.reflect.Type;
import java.util.HashSet;

public class realAutorotateService extends Service {

    AppChecker appChecker;
    HashSet<String> selectedApps;

    public realAutorotateService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        String jsonData = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("selectedPackages", "Data Unavailable");

        Gson gson = new Gson();
        Type type = new TypeToken<HashSet<String>>() {
        }.getType();
        selectedApps = gson.fromJson(jsonData, type);

        int status = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt("status", -1);

        appChecker = new AppChecker();


        if (Settings.System.canWrite(realAutorotateService.this) && !selectedApps.isEmpty() && status == 1) {
            appChecker.whenAny(new AppChecker.Listener() {
                @Override
                public void onForeground(String packageName) {
                    if (selectedApps.contains(packageName)) {
                        Settings.System.putInt(realAutorotateService.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    } else {
                        Settings.System.putInt(realAutorotateService.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    }
                }
            })
                    .timeout(1000)
                    .start(this);
        } else
            this.stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appChecker.stop();
    }
}
