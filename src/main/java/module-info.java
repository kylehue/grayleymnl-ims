module com.imsgrayleymnl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires MaterialFX;

    opens com.ims to javafx.fxml;
    exports com.ims;
    exports com.ims.utils;
    opens com.ims.utils to javafx.fxml;
    exports com.ims.controller;
    opens com.ims.controller to javafx.fxml;
}