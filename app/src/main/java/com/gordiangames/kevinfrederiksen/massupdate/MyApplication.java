package com.gordiangames.kevinfrederiksen.massupdate;

import android.support.multidex.MultiDexApplication;

/**
 * Created by Kevin Frederiksen on 2/19/2018.
 *
 * Background application necessary for the creation of a DataRepository, which provides functionality necessary for background processes to
 * communicate with the UI
 */
public class MyApplication extends MultiDexApplication {
    private static MyApplication INSTANCE;

    DataRepository dataRepository; // this is YOUR class

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        dataRepository = new DataRepository();
    }

    public static MyApplication get() {
        return INSTANCE;
    }
    public DataRepository getDataRepository(){return dataRepository;}
}
