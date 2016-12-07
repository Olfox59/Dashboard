package com.example.dashboard;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fle on 07/12/2016.
 */

public class Sensors extends BaseObservable {
    private String text="0";
    private int rpmprogress=0;

    public Sensors(){
        text = "coucou flo new view sensors";
    }

    @Bindable
    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.rpm);
    }

    @Bindable
    public String getText() {

        return  text;
    }

}
