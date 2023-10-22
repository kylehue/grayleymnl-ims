package com.ims.canvas.utils;

public class Vector {
    private double x;
    private double y;
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public Vector setX(double x) {
        this.x = x;
        return this;
    }
    
    public Vector setY(double y) {
        this.y = y;
        return this;
    }
    
    public Vector randomizeX(double min, double max) {
        this.setX(Utils.generateRandomNumber(min, max));
        return this;
    }
    
    public Vector randomizeY(double min, double max) {
        this.setY(Utils.generateRandomNumber(min, max));
        return this;
    }
    
    public Vector add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    public Vector mult(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
}
