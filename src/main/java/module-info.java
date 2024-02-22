module com.example.cs4076_clientfx {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.example.cs4076_clientfx to javafx.fxml;
    exports com.example.cs4076_clientfx;
}