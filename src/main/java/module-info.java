module com.imsgrayleymnl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires MaterialFX;

    opens com.ims to javafx.fxml;
    exports com.ims;
}