module com.johnc.remotesupportssh {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.johnc.remotesupportssh to javafx.fxml;
    exports com.johnc.remotesupportssh;
}