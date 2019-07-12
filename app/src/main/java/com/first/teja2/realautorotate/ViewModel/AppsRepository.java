package com.first.teja2.realautorotate.ViewModel;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.first.teja2.realautorotate.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.first.teja2.realautorotate.Model.AppsInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Places Repository which fetches/saves data based on ViewModel's request
 * -
 * Created by
 * Bala Guna Teja Karlapudi
 */


public class AppsRepository {

    private static AppsRepository instance;
    List<AppsInfo> appsInfoList = new ArrayList<>();
    List<AppsInfo> selectedApps = new ArrayList<>();
    MutableLiveData<List<AppsInfo>> data = new MutableLiveData<>();
    MutableLiveData<List<AppsInfo>> savedData = new MutableLiveData<>();
    Context context;


    public static AppsRepository getInstance() {
        if (instance == null) {
            instance = new AppsRepository();
        }
        return instance;
    }

    public MutableLiveData<List<AppsInfo>> getApps(Context context) {

            this.context = context;
            new appListAsync().execute(PackageManager.GET_META_DATA);
            data.setValue(appsInfoList);

        //Log.d("demo","inside Repo :"+dataSet.size());

        return data;
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

    public class appListAsync extends AsyncTask<Integer, Integer, List<AppsInfo>> {
        @Override
        protected void onPostExecute(List<AppsInfo> appsInfos) {
            super.onPostExecute(appsInfos);

            data.setValue(appsInfos);

        }

        @Override
        protected List<AppsInfo> doInBackground(Integer... integers) {

            PackageManager packageManager = context.getPackageManager();
            List<ApplicationInfo> infos = packageManager.getInstalledApplications(integers[0]);

            for (ApplicationInfo info : infos) {
                if ((packageManager.getLaunchIntentForPackage(info.packageName) == null)) {
                    continue;
                } else {
                    String name = (String) info.loadLabel(packageManager);
                    if (name != null)
                        if (name.startsWith("com.")) {
                            continue;
                        }

                    AppsInfo appsInfo = new AppsInfo((String) info.loadLabel(packageManager), info.packageName);
                    appsInfoList.add(appsInfo);
                }
            }

            Collections.sort(appsInfoList, nameComparator);

            return appsInfoList;

        }
    }

    public static Comparator<AppsInfo> nameComparator = new Comparator<AppsInfo>() {

        public int compare(AppsInfo tempVar1, AppsInfo tempVar2) {
            return tempVar1.getAppName().compareTo(tempVar2.getAppName());
        }
    };

}



