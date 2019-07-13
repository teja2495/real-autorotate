package com.first.teja2.realautorotate.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.preference.PreferenceManager;

import com.first.teja2.realautorotate.Model.AppsInfo;
import com.first.teja2.realautorotate.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by
 * Bala Guna Teja Karlapudi
 */


public class AppsRepository {

    private static AppsRepository instance;
    List<AppsInfo> selectedApps = new ArrayList<>();
    MutableLiveData<List<AppsInfo>> savedData = new MutableLiveData<>();


    public static AppsRepository getInstance() {
        if (instance == null) {
            instance = new AppsRepository();
        }
        return instance;
    }


    public MutableLiveData<List<AppsInfo>> setSavedApps(Context context, List<AppsInfo> savedApps, AppsInfo app) {

        if(app!=null)
            savedApps.remove(app);
        else{
            Collections.sort(savedApps, nameComparator);
            selectedApps = savedApps;
        }

        Gson gson = new Gson();

        String jsonData1 = gson.toJson(savedApps);

        List<String> savedPackages = new ArrayList<>();

        for(AppsInfo appsInfo : savedApps)
            savedPackages.add(appsInfo.getAppPackageName());


        String jsonData2 = gson.toJson(savedPackages);

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.selectedApps), jsonData1)
                .putString("selectedPackages", jsonData2)
                .apply();

        savedData.setValue(selectedApps);

        return savedData;
    }

    public MutableLiveData<List<AppsInfo>> getSavedApps(Context context) {

        String jsonData = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.selectedApps), "Data Unavailable");

        if (!jsonData.equals("Data Unavailable")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AppsInfo>>() {
            }.getType();
            selectedApps = gson.fromJson(jsonData, type);
        }

        savedData.setValue(selectedApps);
        return savedData;
    }

    public static Comparator<AppsInfo> nameComparator = new Comparator<AppsInfo>() {

        public int compare(AppsInfo tempVar1, AppsInfo tempVar2) {
            return tempVar1.getAppName().compareTo(tempVar2.getAppName());
        }
    };

}



