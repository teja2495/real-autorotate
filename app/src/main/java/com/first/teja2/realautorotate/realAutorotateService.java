package com.first.teja2.realautorotate;

/*
Created By
Bala Guna Teja Karlapudi
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.rvalerio.fgchecker.AppChecker;

import java.util.ArrayList;

public class realAutorotateService extends Service {

    TinyDB tinyDB;
    AppChecker appChecker;

    public realAutorotateService() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
         tinyDB = new TinyDB(this);
          appChecker = new AppChecker();
        ArrayList<String> selectedAppsList = tinyDB.getListString("selectedAppsList");
        Log.d("demo", String.valueOf(tinyDB.getInt("status")));
        if(Settings.System.canWrite(realAutorotateService.this) && !selectedAppsList.isEmpty() && tinyDB.getInt("status")==1 ){
            appChecker.whenAny(new AppChecker.Listener() {
                @Override
                public void onForeground(String packageName) {
                    if(tinyDB!=null) {
                        ArrayList<String> selectedPackageNamesList;
                        selectedPackageNamesList = tinyDB.getListString("selectedPackageNamesList");
                        if (selectedPackageNamesList.contains(packageName)) {
                            Settings.System.putInt(realAutorotateService.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                        } else {
                            Settings.System.putInt(realAutorotateService.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        }
                    }
                }
            })
                    .timeout(1000)
                    .start(this);
        }
        else
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

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        Log.d("demo", "Task Removed");
        super.onTaskRemoved(rootIntent);
    }
}
