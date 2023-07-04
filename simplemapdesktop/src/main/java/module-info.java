module com.gladic.simplemapdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    
        //requires mapsforge.themes;
    requires mapsforge.core;
    requires mapsforge.map;
    requires mapsforge.map.awt;
    requires mapsforge.map.reader;
    //requires mapsforge.map.writer;  

    requires java.desktop;

    opens com.gladic.simplemapdesktop to javafx.fxml;
    exports com.gladic.simplemapdesktop;
}
