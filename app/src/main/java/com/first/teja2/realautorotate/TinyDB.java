package com.first.teja2.realautorotate;

import java.util.ArrayList;
import java.util.Arrays;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;


public class TinyDB {

    private SharedPreferences preferences;

    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    public void putInt(String key, int value) {
        checkForNullKey(key);
        preferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    public void checkForNullKey(String key){
        if (key == null){
            throw new NullPointerException();
        }
    }
}
