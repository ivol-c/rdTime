package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by olegivancev on 11.01.15.
 */
public class SettingController implements Initializable {

    @FXML
    private TextField serverUrl;

    @FXML
    private TextField apiKey;

    @FXML
    private TextField licenseKey;

    @FXML
    private Button saveBtn;

    private String settingFilePath = new File("settings.txt").getAbsolutePath();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        Map settings = getSettings();
        serverUrl.setText((String) settings.get("serverUrl"));
        apiKey.setText((String) settings.get("apiKey"));
        licenseKey.setText((String) settings.get("licenseKey"));

        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    Map< String, String > map = new HashMap<String, String>();
                    map.put("serverUrl", serverUrl.getText());
                    map.put("apiKey", apiKey.getText());
                    map.put("licenseKey", licenseKey.getText());
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(settingFilePath));
                    out.writeObject(map);
                    out.flush();
                    out.close();
                    RedmineApi.getInstance().init();
                } catch (IOException iox) {
                    iox.printStackTrace();
                }

                Stage stage = (Stage) saveBtn.getScene().getWindow();
                stage.close();
                Stage rootStage = CFactory.getInstance().primaryStage;

                if(!rootStage.isShowing()){
                    rootStage.show();
                }

                Controller controller = CFactory.getInstance().mainController;
                controller.reload();
            }
        });
    }

    public Map getSettings(){

        try{
            FileInputStream fis = new FileInputStream(settingFilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map<String, String> a = (Map) ois.readObject();
            ois.close();
            fis.close();
            return a;
        }catch(IOException ioe){
            //ioe.printStackTrace();

        }catch(ClassNotFoundException c){
            System.out.println("Class not found");
            c.printStackTrace();
        }

        Map< String, String > map = new HashMap<String, String>();

        if(serverUrl instanceof TextField){
            map.put("serverUrl", serverUrl.getText());
            map.put("apiKey", apiKey.getText());
            map.put("licenseKey", licenseKey.getText());
        }

        return map;

    }
}
