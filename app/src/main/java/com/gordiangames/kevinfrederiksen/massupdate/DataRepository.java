package com.gordiangames.kevinfrederiksen.massupdate;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

/**
 * Created by Kevin Frederiksen on 2/19/2018.
 *
 * Allows for a string to be sent to the UI to update the user on the progress of background processes
 */

public class DataRepository {//start class

    private final MutableLiveData<String> data = new MutableLiveData<>();

    public LiveData<String> getMyData() {//start getMyData

        return data;

    }//end getMyData

    public void updateText(String text) {//start getMyData

        data.setValue(text);

    }//end getMyData

}//end class
