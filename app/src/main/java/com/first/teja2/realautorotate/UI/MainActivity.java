package com.first.teja2.realautorotate.UI;

/*
Created By
Bala Guna Teja Karlapudi
 */

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.first.teja2.realautorotate.Model.AppsInfo;
import com.first.teja2.realautorotate.R;
import com.first.teja2.realautorotate.Service.realAutorotateService;
import com.first.teja2.realautorotate.ViewModel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.rvalerio.fgchecker.Utils.hasUsageStatsPermission;


public class MainActivity extends AppCompatActivity {

    TextView title;
    List<AppsInfo> appsInfoList = new ArrayList<>();
    List<AppsInfo> selectedAppsList = new ArrayList<>();

    private MainViewModel mMainViewModel;
    ImageView imageView;
    TextView tv;
    Switch aSwitch;
    ProgressBar pb;
    Typeface roboto;
    AlertDialog.Builder dialogBuilder;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        checkUsagePermission();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roboto = Typeface.createFromAsset(MainActivity.this.getAssets(), "font/Roboto-Regular.ttf"); //use this.getAssets if you are calling from an Activity
        title = findViewById(R.id.toolbar_title);
        title.setTypeface(roboto);
        tv = findViewById(R.id.textView);
        tv.setVisibility(View.INVISIBLE);
        pb = findViewById(R.id.loadingBar);
        pb.setVisibility(View.INVISIBLE);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        aSwitch = findViewById(R.id.switch1);
        aSwitch.setText("Disabled");
        mRecyclerView = findViewById(R.id.recyclerView);

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.initSavedApps(getApplicationContext());

        selectedAppsList = mMainViewModel.getSavedApps().getValue();

        mMainViewModel.getSavedApps().observe(this, new Observer<List<AppsInfo>>() {
            @Override
            public void onChanged(@Nullable List<AppsInfo> appsList) {
                selectedAppsList = appsList;

                //Log.d("demo", "Inside Get Saved Apps: "+selectedAppsList.size()+"");

                if (selectedAppsList.size() > 0) {

                    aSwitch.setChecked(true);
                    initRecyclerView();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    tv.setVisibility(View.VISIBLE);

                    stopService(new Intent(MainActivity.this, realAutorotateService.class));
                    startService(new Intent(MainActivity.this, realAutorotateService.class));

                } else {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.INVISIBLE);
                    aSwitch.setChecked(false);
                }
            }
        });

        mMainViewModel.initAllApps(getApplicationContext());

        mMainViewModel.getApps().observe(this, new Observer<List<AppsInfo>>() {
            @Override
            public void onChanged(@Nullable List<AppsInfo> appsList) {

                appsInfoList = appsList;

                //Log.d("demo", "Inside Get Apps: "+appsInfoList.size()+"");
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!Settings.System.canWrite(MainActivity.this)) {
                        setWritePermissions();
                        aSwitch.setChecked(false);
                    } else if (selectedAppsList.isEmpty()) {
                        Snackbar.make(findViewById(R.id.cLayout), "Select the Apps before Enabling the Service", Snackbar.LENGTH_LONG).show();
                        aSwitch.setChecked(false);
                    } else {

                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                .edit()
                                .putInt("status", 1)
                                .apply();

                        startService(new Intent(MainActivity.this, realAutorotateService.class));
                        //Log.d("demo", "Service Started");
                        aSwitch.setText("Enabled");
                    }
                } else {

                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putInt("status", 0)
                            .apply();

                    stopService(new Intent(MainActivity.this, realAutorotateService.class));
                    //Log.d("demo", "Service Stopped");
                    aSwitch.setText("Disabled");
                }
            }
        });


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#01579B"));

        int status = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getInt("status", -1);

        if (selectedAppsList.size() > 0) {

            initRecyclerView();

            if (status == 1)
                aSwitch.setChecked(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.addapp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.INVISIBLE);
                //pb.setVisibility(View.VISIBLE);
                mMainViewModel.initAllApps(getApplicationContext());
                initDialogBox();
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

        for (int i = 0; i < appsInfoList.size(); i++) {
            if (selectedAppsList.contains(appsInfoList.get(i)))
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
                if (!selectedAppsList.isEmpty()) {
                    tv.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    mMainViewModel.setSelectedApps(getApplicationContext(), selectedAppsList, null);
                    initRecyclerView();
                } else {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.cLayout), "No Apps Selected", Snackbar.LENGTH_LONG).show();
                    aSwitch.setChecked(false);
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (selectedAppsList.isEmpty()) {
                    tv.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(R.id.cLayout), "No Apps Selected", Snackbar.LENGTH_LONG).show();
                    aSwitch.setChecked(false);
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
}




