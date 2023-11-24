package com.ims.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer extends SimpleIntegerProperty {
    private int seconds;
    private Timer timer = new Timer();
    private final int originalSeconds;
    private int speedMultiplier = 1;
    private final BooleanProperty ended = new SimpleBooleanProperty(false);
    
    public CountdownTimer(int seconds) {
        this.seconds = seconds;
        this.originalSeconds = seconds;
    }
    
    public CountdownTimer(int seconds, int speedMultiplier) {
        this.seconds = seconds;
        this.originalSeconds = seconds;
        this.speedMultiplier = speedMultiplier;
    }
    
    public void start() {
        this.reset();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (seconds > 0) {
                    seconds--;
                    set(seconds);
                } else {
                    reset();
                    endedProperty().set(true);
                }
            }
        }, 0, 1000 / Math.max(1, speedMultiplier));
    }
    
    public BooleanProperty endedProperty() {
        return ended;
    }
    
    public boolean hasEnded() {
        return this.endedProperty().get();
    }
    
    public void reset() {
        timer.cancel();
        timer = new Timer();
        this.endedProperty().set(false);
        this.seconds = originalSeconds;
    }
}
