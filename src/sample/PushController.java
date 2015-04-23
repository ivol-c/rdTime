package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by olegivancev on 11.01.15.
 */

public class PushController implements Initializable {

    @FXML
    private Text itemText;

    @FXML
    private Text infoText;

    @FXML
    private BorderPane container;

    @FXML
    private Button saveBtn;

    @FXML
    private TextArea message;

    @FXML
    private Button cancelBtn;

    private Controller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        itemText.setWrappingWidth(480);
        mainController = CFactory.getInstance().mainController;
        Repository repo = new Repository();
        JSONObject item = repo.getIssue((String) mainController.activeId);

        itemText.setText((String) item.get("subject"));

        JSONObject status = (JSONObject) item.get("status");
        JSONObject priority = (JSONObject) item.get("priority");
        JSONObject project = (JSONObject) item.get("project");
        infoText.setText(status.get("name") +", "+ priority.get("name") +", "+ project.get("name"));


        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) container.getScene().getWindow();
                stage.close();
                mainController.push(message.getText());
            }
        });

        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) container.getScene().getWindow();
                stage.close();
                mainController.resume();
            }
        });

    }
}
