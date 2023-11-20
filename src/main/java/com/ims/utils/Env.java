package com.ims.utils;

import java.io.InputStream;
import java.util.Properties;

public abstract class Env {
    private static Properties properties = new Properties();
    public static void initialize() {
        try {
            InputStream stream = ClassLoader.getSystemResourceAsStream(
                "env.properties"
            );
            properties.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Properties get() {
        return properties;
    }
}
