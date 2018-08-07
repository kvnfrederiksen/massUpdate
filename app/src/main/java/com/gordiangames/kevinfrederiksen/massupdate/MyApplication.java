package com.gordiangames.kevinfrederiksen.massupdate;

import android.support.multidex.MultiDexApplication;

/**
 * Created by Kevin Frederiksen on 2/19/2018.
 *
 * Background application necessary for the creation of a DataRepository, which provides functionality necessary for background processes to
 * communicate with the UI
 */
public class MyApplication extends MultiDexApplication {//start class

    private static MyApplication INSTANCE;

    DataRepository dataRepository;

    @Override
    public void onCreate() {//start onCreate

        super.onCreate();
        INSTANCE = this;
        dataRepository = new DataRepository();

    }//end onCreate

    public static MyApplication get() {//start get

        return INSTANCE;

    }//end get

    public DataRepository getDataRepository() {//start getDataRepository

        return dataRepository;

    }//end getDataRepository

}//end class
