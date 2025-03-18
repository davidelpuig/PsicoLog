package com.example.psicolog;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

public class AppViewModel extends AndroidViewModel {

    public MutableLiveData<Map<String,Object>> currentLog = new MutableLiveData<>();
    public AppViewModel(@NonNull Application application) {
        super(application);
    }
}
