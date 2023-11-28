package com.ims;

public abstract class Config {
    public final static boolean isProductionMode = true;
    public final static int maxThreads = Math.max(
        Runtime.getRuntime().availableProcessors(),
        4
    );
    public final static int maxCategoryNameLength = 30;
    public final static int maxRoleNameLength = 50;
    public final static int maxEmailLength = 320;
    public final static int maxProductNameLength = 50;
    public final static int maxImageURLLength = 2048;
    /**
     * The threshold rate indicating when a product's stock is considered low.
     */
    public final static float lowStockRate = 0.4f; // 40%
    public final static int productLoadLimit = 9;
    public final static int categoryLoadLimit = 12;
    public final static int roleLoadLimit = 9;
    public final static int userLoadLimit = 12;
    public final static int historyLoadLimit = 12;
}
