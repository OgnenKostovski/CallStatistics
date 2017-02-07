package com.aek.callstatistics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
    public static final String myTariff = "myTariff";
    public static final String myOperator = "myOperator";
    public static final String saveTariff = "saveTariff";
    private static final String APP_SHARED_PREFS = "preferences.xml";//AppPreferences.class.getSimpleName(); //  Name of the file -.xml
    private SharedPreferences _sharedPrefs;
    private Editor _prefsEditor;

    public AppPreferences(Context context) {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this._prefsEditor = _sharedPrefs.edit();
    }

    public String getTariff() {
        return _sharedPrefs.getString(myTariff, "");
    }

    public void saveTariff(String text) {
        _prefsEditor.putString(myTariff, text);
        _prefsEditor.commit();
    }
    
    public String getOperator() {
        return _sharedPrefs.getString(myOperator, "");
    }

    public void saveOperator(String text) {
        _prefsEditor.putString(myOperator, text);
        _prefsEditor.commit();
    }
    
    public void changeSave(boolean bool){
    	_prefsEditor.putBoolean(saveTariff, bool);
    	_prefsEditor.commit();
    }
    
    public boolean isSaved(){
    	return _sharedPrefs.getBoolean(saveTariff, false);
    }
}

