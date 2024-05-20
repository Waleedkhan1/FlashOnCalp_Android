package com.personal.flashonclap.light.status.flash.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainStorageUtils {

    public void putValue(Context context,String name,String value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name,value);
        editor.apply();
    }

    public String getValue(Context context, String name1, String value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = preferences.getString(name1, value);
        if(!name.equalsIgnoreCase(""))
        {
            name = name + "";  /* Edit the value here*/
        }
        return name;
    }
}
