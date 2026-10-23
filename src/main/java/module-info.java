module com.example.dearfreshman {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.dearfreshman to javafx.fxml;
    exports com.example.dearfreshman;
}