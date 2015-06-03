package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;

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
    private ComboBox tracker;

    @FXML
    private Button cancelBtn;

    private Controller mainController;

    public JSONObject current;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        itemText.setWrappingWidth(480);
        mainController = CFactory.getInstance().mainController;
        Repository repo = new Repository();
        JSONObject item = repo.getIssue((String) mainController.activeId);
        tracker.setDisable(true);
        saveBtn.setDisable(true);

        String subject = (String) item.get("subject");
        if(subject.length() <= 90){
            itemText.setText(subject);
        }else {
            itemText.setText(subject.substring(0, 90) + "..");
        }

        JSONObject status = (JSONObject) item.get("status");
        JSONObject priority = (JSONObject) item.get("priority");
        JSONObject project = (JSONObject) item.get("project");
        current = item;
        infoText.setText(status.get("name") +", "+ priority.get("name") +", "+ project.get("name"));

        /*load project data*/
        Runnable worker = new Ajax(RedmineApi.getInstance().timeEntryActivities(), new Callback() {
            @Override
            public Object call(Object o) {
                if(o instanceof HashMap && (Boolean) ((HashMap) o).get("error") == false){
                    String result = (String) ((HashMap) o).get("result");
                    try {
                        JSONObject jsonObj = (JSONObject)new JSONParser().parse(result);
                        JSONArray trackerData = (JSONArray) jsonObj.get("time_entry_activities");
                        List<Object> trackerOptions = new ArrayList<Object>();
                        for(int i=0; i < trackerData.size(); i++){
                            JSONObject item = (JSONObject) trackerData.get(i);
                            trackerOptions.add(new RdTracker(item.get("id")+"", item.get("name")+""));

                        }
                        tracker.setItems(FXCollections.observableArrayList(trackerOptions));
                        tracker.setDisable(false);
                        saveBtn.setDisable(false);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
               }
                return null;
            }
        });
        Thread t = new Thread(worker, "Thread");
        t.start();

        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) container.getScene().getWindow();
                stage.close();
                RdTracker tr = (RdTracker) tracker.getValue();
                String id = (tr == null) ? "0" : tr.id;
                mainController.push(message.getText(), id);
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

    class RdTracker{
        String id;
        String name;

        public RdTracker(Object n, Object a){
            id = (String) n;
            name = (String) a;
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
