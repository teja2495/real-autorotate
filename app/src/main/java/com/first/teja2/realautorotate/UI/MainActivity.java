package com.first.teja2.realautorotate.UI;

/**
 * Created by
 * Bala Guna Teja Karlapudi
 */

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.first.teja2.realautorotate.Model.AppsInfo;
import com.first.teja2.realautorotate.R;
import com.first.teja2.realautorotate.Service.realAutorotateService;
import com.first.teja2.realautorotate.ViewModel.MainViewModel;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.first.teja2.realautorotate.ViewModel.AppsRepository.nameComparator;
import static com.rvalerio.fgchecker.Utils.hasUsageStatsPermission;


public class MainActivity extends AppCompatActivity {

    TextView title;
    List<AppsInfo> appsInfoList = new ArrayList<>();
    List<AppsInfo> selectedAppsList = new ArrayList<>();

    private MainViewModel mMainViewModel;
    ImageView imageView;
    TextView tv, tv2;
    ProgressBar pb;
    AlertDialog.Builder dialogBuilder;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    LabeledSwitch labeledSwitch;

    @Override
    protected void onResume() {
        super.onResume();

        checkUsagePermission();

        Boolean flag = false;

        for (int i = 0; i < selectedAppsList.size(); i++) {

            if (!isPackageInstalled(selectedAppsList.get(i).getAppPackageName(), getPackageManager())) {
                mMainViewModel.setSelectedApps(this, selectedAppsList, selectedAppsList.get(i));
                flag = true;
            }
        }

        if (flag) {

            if (getStatus() == 1) {
                stopService(new Intent(this, realAutorotateService.class));
                startService(new Intent(this, realAutorotateService.class));
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.toolbar_title);

        tv = findViewById(R.id.textView);
        tv.setVisibility(View.INVISIBLE);

        tv2 = findViewById(R.id.textView2);
        tv2.setVisibility(View.INVISIBLE);

        pb = findViewById(R.id.loadingBar);
        pb.setVisibility(View.INVISIBLE);

        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);

        labeledSwitch = findViewById(R.id.switch1);
        labeledSwitch.setColorOn(Color.parseColor("#283c97"));

        mRecyclerView = findViewById(R.id.recyclerView);

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.initSavedApps(getApplicationContext());

        selectedAppsList = mMainViewModel.getSavedApps().getValue();

        mMainViewModel.getSavedApps().observe(this, new Observer<List<AppsInfo>>() {
            @Override
            public void onChanged(@Nullable List<AppsInfo> appsList) {

                selectedAppsList = appsList;

                if (selectedAppsList.size() > 0) {

                    labeledSwitch.setEnabled(true);

                    if (getStatus() == 1)
                        labeledSwitch.setOn(true);
                    else
                        labeledSwitch.setOn(false);


                    initRecyclerView();

                    appsSelectedVisibilitySettings();

                    stopService(new Intent(MainActivity.this, realAutorotateService.class));
                    startService(new Intent(MainActivity.this, realAutorotateService.class));

                } else {

                    noAppsVisibilitySettings();

                    labeledSwitch.setOn(false);
                    labeledSwitch.setEnabled(false);

                }
            }
        });


        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {

                    setStatus(1);

                    startService(new Intent(MainActivity.this, realAutorotateService.class));
                    labeledSwitch.setOn(true);

                } else {

                    setStatus(0);

                    stopService(new Intent(MainActivity.this, realAutorotateService.class));
                }
            }
        });

        int status = getStatus();

        if (selectedAppsList.size() > 0) {

            initRecyclerView();

            if (status == 1)
                labeledSwitch.setOn(true);

        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.addapp);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!Settings.System.canWrite(MainActivity.this)) {

                    setWritePermissions();

                } else {

                    imageView.setVisibility(View.INVISIBLE);
                    tv2.setVisibility(View.INVISIBLE);
                    tv.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);

                    pb.setVisibility(View.VISIBLE);

                    new AppListAsync().execute(PackageManager.GET_META_DATA);

                }

            }
        });

    }

    void checkUsagePermission() {
        if (!hasUsageStatsPermission(this)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setTitle("Permission Request");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setMessage("This app requires USAGE ACCESS permission to work. Would you like to grant the permission?");

            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user clicked OK
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user clicked Cancel
                    finishAndRemoveTask();
                }
            });
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    }

    void setWritePermissions() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle("Permission Request");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setMessage("This app requires WRITE SETTINGS permission to toggle AutoRotation. Would you like to grant the permission?");

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked Cancel
                finishAndRemoveTask();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    void initDialogBox() {

        String[] appNames = new String[appsInfoList.size()];
        final boolean[] checkedItems = new boolean[appsInfoList.size()];

        for (int i = 0; i < appsInfoList.size(); i++)
            appNames[i] = appsInfoList.get(i).getAppName();

        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Select the apps");

        HashSet<String> packageHashSet = new HashSet<>();

        for (AppsInfo app : selectedAppsList)
            packageHashSet.add(app.getAppPackageName());


        for (int i = 0; i < appsInfoList.size(); i++) {
            if (packageHashSet.contains(appsInfoList.get(i).getAppPackageName()))
                checkedItems[i] = true;
        }

        dialogBuilder.setMultiChoiceItems(appNames, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                selectedAppsList.clear();

                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedAppsList.add(appsInfoList.get(i));
                    }
                }
                if (selectedAppsList.isEmpty()) {

                    noAppsVisibilitySettings();
                    Snackbar.make(findViewById(R.id.cLayout), "No Apps Selected", Snackbar.LENGTH_LONG).show();
                    labeledSwitch.setOn(false);

                } else {

                    appsSelectedVisibilitySettings();

                    mMainViewModel.setSelectedApps(getApplicationContext(), selectedAppsList, null);

                    labeledSwitch.setOn(true);

                    setStatus(1);

                    initRecyclerView();

                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (selectedAppsList.isEmpty()) {

                    noAppsVisibilitySettings();

                    Snackbar.make(findViewById(R.id.cLayout), "No Apps Selected", Snackbar.LENGTH_LONG).show();
                    labeledSwitch.setOn(false);

                } else {

                    appsSelectedVisibilitySettings();

                }
                dialog.dismiss();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        pb.setVisibility(View.INVISIBLE);
        dialog.show();
    }

    void initRecyclerView() {

        //Log.d("demo", "Inside Recycler Init:"+selectedAppsList.size());
        mAdapter = new ItemAdapter(selectedAppsList, getApplicationContext(), mMainViewModel);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    public class AppListAsync extends AsyncTask<Integer, Integer, List<AppsInfo>> {

        @Override
        protected List<AppsInfo> doInBackground(Integer... integers) {

            PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> infos = packageManager.getInstalledApplications(integers[0]);

            appsInfoList.clear();

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

        @Override
        protected void onPostExecute(List<AppsInfo> appsInfos) {
            super.onPostExecute(appsInfos);

            pb.setVisibility(View.INVISIBLE);
            initDialogBox();

        }
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageGids(packagename);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    int getStatus() {

        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getInt("status", -1);

    }

    void setStatus(int num) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putInt("status", num)
                .apply();
    }

    void noAppsVisibilitySettings(){

        tv.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

    }

    void appsSelectedVisibilitySettings(){

        tv.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        tv2.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

    }

}