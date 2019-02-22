package com.example.teja2.realautorotate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.rvalerio.fgchecker.AppChecker;

import java.util.ArrayList;

public class realAutorotateService extends Service {

    public realAutorotateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(Settings.System.canWrite(realAutorotateService.this)){
            AppChecker appChecker = new AppChecker();
            appChecker.whenAny(new AppChecker.Listener() {
                @Override
                public void onForeground(String packageName) {

                    TinyDB tinyDB = new TinyDB(realAutorotateService.this);
                    if (tinyDB != null) {
                        ArrayList<String> selectedPackageNamesList;
                        selectedPackageNamesList = tinyDB.getListString("selectedPackageNamesList");

                        if (selectedPackageNamesList.contains(packageName)) {
                            Settings.System.putInt(realAutorotateService.this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                        }
                        else{
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
    }
}
