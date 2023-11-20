module com.imsgrayleymnl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires MaterialFX;
    requires java.sql;
    requires jbcrypt;
    requires java.mail;
    
    opens com.ims to javafx.fxml;
    exports com.ims;
    exports com.ims.utils;
    opens com.ims.utils to javafx.fxml;
    exports com.ims.controller;
    opens com.ims.controller to javafx.fxml;
    exports com.ims.database;
    opens com.ims.database to javafx.fxml;
    exports com.ims.model;
    opens com.ims.model to javafx.fxml;
    exports com.ims.model.objects;
    opens com.ims.model.objects to javafx.fxml;
}