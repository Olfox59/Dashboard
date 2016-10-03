package com.example.dashboard;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by fle on 01/10/2016.
 */

public class Dashboard extends BaseObservable{

    private String rpm="0";

    public Dashboard(String initRpm){
        this.rpm=initRpm;
    }

    @Bindable
    public void setRpm(String rpm) {
        this.rpm = rpm;
        notifyPropertyChanged(BR.rpm);
    }

    @Bindable
    public String getRpm() {
        return rpm;
    }
}
