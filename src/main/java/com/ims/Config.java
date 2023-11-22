package com.ims;

public abstract class Config {
    public static int maxCategoryNameLength = 30;
    public static int maxRoleNameLength = 50;
    public static int maxEmailLength = 320;
    public static int maxProductNameLength = 50;
    public static int maxImageURLLength = 2048;
    /**
     * The threshold rate indicating when a product's stock is considered low.
     */
    public static float lowStockRate = 0.33f; // 33%
    public static int productLoadLimit = 9;
}
