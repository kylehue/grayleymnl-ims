package com.ims.utils;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer extends SimpleIntegerProperty {
    private int seconds;
    
    public CountdownTimer(int seconds) {
        this.seconds = seconds;
    }
    
    public void start() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (seconds > 0) {
                    seconds--;
                    set(seconds);
                } else {
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }
}