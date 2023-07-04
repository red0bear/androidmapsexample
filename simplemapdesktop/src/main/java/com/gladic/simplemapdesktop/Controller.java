package com.gladic.simplemapdesktop;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.swing.SwingUtilities;
import mapclassworker.mapclass;

public class Controller implements Initializable{

    @FXML
    private Pane mapsforgecontainermap;
    
    @FXML
    private ComboBox comboboxtilesize;

    protected javax.swing.JPanel jPanelMap ;
        
    private File selectedFile;
        
    private mapclass mapssample;
    private SwingNode swingNode;
    private StackPane pane;

    @FXML
    private void OnActionSetDebugEnable(ActionEvent event)
    {
        mapssample.set_debug();
    }
    
    @FXML
    private void OnActionStartNewMap(ActionEvent event)
    {
        if(selectedFile != null)
        {    
            swingNode = new SwingNode();

            jPanelMap = new javax.swing.JPanel(new java.awt.BorderLayout());
            jPanelMap.setPreferredSize(new java.awt.Dimension(2000   ,1000));

            createAndSetSwingContent(swingNode);

            mapsforgecontainermap.getChildren().addAll(swingNode);

            mapssample.set_tile_size(comboboxtilesize.getSelectionModel().getSelectedItem().toString());
            
            mapssample.build(jPanelMap);
        }        
    }
    
    @FXML
    private void OnActionSetNewMapPath(ActionEvent event)
    {
    
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open Resource File");
        
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Map Files", "*.map"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
       
        Window mainStage = null;

       selectedFile = fileChooser.showOpenDialog(mainStage);  
       
       mapssample.set_path(selectedFile.getAbsolutePath());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      
        comboboxtilesize.setItems(observableArrayList(new String[]{"128","256","512","1024","2048","4096","NONE"}));
        comboboxtilesize.getSelectionModel().selectFirst();
        mapssample = new mapclass();
    }
    
    protected void createAndSetSwingContent(final SwingNode swingNode) {
     
       SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     swingNode.setContent(jPanelMap);
                     
                 
        }});
    }      
}
