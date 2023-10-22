package com.ims.canvas.utils;

import javafx.animation.AnimationTimer;

public abstract class FixedTimestepAnimationTimer extends AnimationTimer {
    
    private long lastUpdateTime = 0;
    private final double fixedTimestep;
    private double accumulator = 0;
    
    public FixedTimestepAnimationTimer(double framesPerSecond) {
        this.fixedTimestep = 1.0 / framesPerSecond;
    }
    
    @Override
    public void handle(long now) {
        if (lastUpdateTime == 0) {
            lastUpdateTime = now;
            return;
        }
        
        // Convert from nanoseconds to seconds
        double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
        accumulator += deltaTime;
        
        // Rendering logic can directly use deltaTime
        render(deltaTime);
        
        // Update logic uses the fixed timestep
        while (accumulator >= fixedTimestep) {
            update(fixedTimestep);
            accumulator -= fixedTimestep;
        }
        
        lastUpdateTime = now;
    }
    
    public abstract void update(double fixedTimestep);
    
    public abstract void render(double deltaTime);
}
