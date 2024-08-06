module com.disk.diskanalyzer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.disk.diskanalyzer to javafx.fxml;
    exports com.disk.diskanalyzer;
}