package com.first.teja2.realautorotate;

/*
Created By
Bala Guna Teja Karlapudi
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.rvalerio.fgchecker.Utils.hasUsageStatsPermission;


public class MainActivity extends AppCompatActivity {

    TextView title;
    ArrayList<appsInfo> appsInfosList = new ArrayList<>();
    ArrayList<String> appNamesList = new ArrayList<>();
    ArrayList<String> packageNamesList = new ArrayList<>();
    ArrayList<String> selectedPackageNamesList = new ArrayList<>();
    ArrayList<String> selectedAppsList = new ArrayList<>();
    ImageView imageView;
    TextView tv;
    Switch aSwitch;
    customAdapter customAdapter;
    ListView lv;
    ProgressBar pb;
    Typeface roboto;
    TinyDB tinyDB;

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
        tv=findViewById(R.id.textView);
        tv.setVisibility(View.INVISIBLE);
        pb = findViewById(R.id.loadingBar);
        pb.setVisibility(View.INVISIBLE);
        lv = findViewById(R.id.listView);
        imageView=findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        aSwitch=findViewById(R.id.switch1);
        aSwitch.setText("Disabled");

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!Settings.System.canWrite(MainActivity.this)) {
                        setWritePermissions();
                        aSwitch.setChecked(false);
                    }
                    else if(selectedAppsList.isEmpty()){
                        Snackbar.make(findViewById(R.id.cLayout), "Select the Apps before Enabling the Service", Snackbar.LENGTH_LONG).show();
                        aSwitch.setChecked(false);
                    }
                    else
                    {
                        tinyDB.putInt("status",1);
                        startService(new Intent(MainActivity.this, realAutorotateService.class));
                        Log.d("demo", "Service Started");
                        aSwitch.setText("Enabled");
                    }
                }
                else{
                    tinyDB.putInt("status",0);
                    stopService(new Intent(MainActivity.this, realAutorotateService.class));
                    Log.d("demo", "Service Stopped");
                    aSwitch.setText("Disabled");
                }
            }
        });



        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#01579B"));
        tinyDB = new TinyDB(MainActivity.this);

        if(tinyDB.getListString("selectedAppsList") != null)
            Log.d("demo", tinyDB.toString());

        if (tinyDB!=null){
            selectedAppsList=tinyDB.getListString("selectedAppsList");
            selectedPackageNamesList=tinyDB.getListString("selectedPackageNamesList");
            if(!selectedAppsList.isEmpty())
                tv.setVisibility(View.VISIBLE);
            else
                imageView.setVisibility(View.VISIBLE);
            customAdapter = new customAdapter(selectedAppsList);
            lv.setAdapter(customAdapter);
            if(tinyDB.getInt("status") == 1)
                aSwitch.setChecked(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.addapp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
                appNamesList.clear();
                packageNamesList.clear();
                appsInfosList.clear();
                new appListAsync().execute(PackageManager.GET_META_DATA);
            }
        });

    }

    void checkUsagePermission(){
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

    void setWritePermissions(){
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

/*    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

    class appListAsync extends AsyncTask<Integer, Integer, ArrayList<appsInfo>> {
        @Override
        protected ArrayList<appsInfo> doInBackground(Integer... integers) {

            PackageManager packageManager = getPackageManager();
            List<ApplicationInfo> infos = packageManager.getInstalledApplications(integers[0]);

            for (ApplicationInfo info : infos) {
                if((packageManager.getLaunchIntentForPackage(info.packageName) == null)){
                    continue;
                }
                else{
                    String name = (String) info.loadLabel(packageManager);
                    if(name!=null)
                        if(name.startsWith("com.")){
                            continue;
                        }
                    appsInfo appsInfo=new appsInfo((String) info.loadLabel(packageManager), info.packageName );
                    appsInfosList.add(appsInfo);
                }
            }
            return appsInfosList;
        }

        @Override
        protected void onPostExecute(ArrayList<appsInfo> appsInfos) {
            super.onPostExecute(appsInfos);

            Collections.sort(appsInfosList, nameComparator);
            for (appsInfo obj : appsInfosList) {
                appNamesList.add(obj.getAppName());
                packageNamesList.add(obj.getAppPackageName());
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setTitle("Select the apps");
            final boolean[] checkedItems = new boolean[appsInfosList.size()];

            String[] appNames = new String[appNamesList.size()];
            appNames = appNamesList.toArray(appNames);

            for (int i = 0; i < packageNamesList.size(); i++) {
                if (selectedPackageNamesList.contains(packageNamesList.get(i)))
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
                    // user clicked OK
                    selectedPackageNamesList.clear();
                    selectedAppsList.clear();
                    for (int i = 0; i < checkedItems.length; i++) {
                        boolean checked = checkedItems[i];
                        if (checked) {
                            selectedPackageNamesList.add(packageNamesList.get(i));
                            selectedAppsList.add(appNamesList.get(i));
                        }
                    }
                        if(!selectedAppsList.isEmpty()){
                            tv.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.INVISIBLE);
                        }
                        else{
                            imageView.setVisibility(View.VISIBLE);
                            Snackbar.make(findViewById(R.id.cLayout), "No Apps Selected", Snackbar.LENGTH_LONG).show();
                            aSwitch.setChecked(false);

                        }
                    if (selectedAppsList != null) {
                        customAdapter = new customAdapter(selectedAppsList);
                        lv.setAdapter(customAdapter);
                    }

                    if(selectedPackageNamesList != null && selectedAppsList !=null){
                        TinyDB tinyDB = new TinyDB(MainActivity.this);
                        tinyDB.remove("selectedAppsList");
                        tinyDB.remove("selectedPackageNamesList");
                        tinyDB.putListString("selectedAppsList", selectedAppsList);
                        tinyDB.putListString("selectedPackageNamesList", selectedPackageNamesList);
                    }
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(selectedAppsList.isEmpty()){
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
    }

    class customAdapter extends BaseAdapter {
        List<String> displayList;

        public customAdapter(List<String> displayList) {
            this.displayList = displayList;
        }

        @Override
        public int getCount() {
            return displayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            //position = (displayList.size() - 1) - position;
            view = getLayoutInflater().inflate(R.layout.custom_layout, null);
            TextView appName = view.findViewById(R.id.appName);
            CheckBox checkBox = view.findViewById(R.id.checkBox);
            appName.setTypeface(roboto);

            appName.setText(displayList.get(position));
            checkBox.setChecked(true);

            checkBox.setTag(position);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = (Integer) buttonView.getTag();
                    if (!isChecked) {
                        selectedAppsList.remove(position);
                        selectedPackageNamesList.remove(position);
                        customAdapter = new customAdapter(selectedAppsList);
                        lv.setAdapter(customAdapter);
                        TinyDB tinyDB = new TinyDB(MainActivity.this);
                        tinyDB.remove("selectedAppsList");
                        tinyDB.remove("selectedPackageNamesList");
                        tinyDB.putListString("selectedAppsList", selectedAppsList);
                        tinyDB.putListString("selectedPackageNamesList", selectedPackageNamesList);
                        if(selectedAppsList.isEmpty()){
                            tv.setVisibility(View.INVISIBLE);
                            imageView.setVisibility(View.VISIBLE);
                            aSwitch.setChecked(false);
                        }
                    }
                }
            });
            return view;
        }
    }
    public static Comparator<appsInfo> nameComparator = new Comparator<appsInfo>() {

        public int compare(appsInfo tempVar1, appsInfo tempVar2) {
            return tempVar1.getAppName().compareTo(tempVar2.getAppName());
        }
    };
}


