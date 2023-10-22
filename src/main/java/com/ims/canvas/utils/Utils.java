package com.ims.canvas.utils;

public class Utils {
    private static int idCounter = 0;
    
    public static double generateRandomNumber(double min, double max) {
        return min + Math.random() * (max - min);
    }
    
    public static double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    public static String generateId() {
        return "$" + idCounter++;
    }
}
