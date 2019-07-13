package com.first.teja2.realautorotate.ViewModel;

/**
 * View Model, which accesses data from the repository
 * -
 * Created by
 * Bala Guna Teja Karlapudi
 */


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.first.teja2.realautorotate.Model.AppsInfo;

import java.util.List;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<AppsInfo>> mSavedApps;
    private AppsRepository mRepo;

    public void initSavedApps(Context context) {

        if (mSavedApps != null && mSavedApps.getValue().size()>0) {
            return;
        }

        mRepo = AppsRepository.getInstance();
        mSavedApps = mRepo.getSavedApps(context);

    }

    public void setSelectedApps(Context context, List<AppsInfo> apps, AppsInfo appsInfo) {

        mSavedApps = mRepo.setSavedApps(context, apps, appsInfo);

    }

    public LiveData<List<AppsInfo>> getSavedApps() {
        return mSavedApps;
    }

}
