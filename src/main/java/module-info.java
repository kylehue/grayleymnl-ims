module com.imsgrayleymnl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires MaterialFX;
    requires java.sql;
    requires jbcrypt;
    
    opens com.ims to javafx.fxml;
    exports com.ims;
    exports com.ims.utils;
    opens com.ims.utils to javafx.fxml;
    exports com.ims.controller;
    opens com.ims.controller to javafx.fxml;
    exports com.ims.database;
    opens com.ims.database to javafx.fxml;
}