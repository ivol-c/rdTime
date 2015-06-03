package sample;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import sample.lib.ExecuteShellComand;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

import static java.nio.charset.Charset.*;

public class Controller implements Initializable{

    private Double winWidth = 0.0;

    @FXML
    public BorderPane topPane;

    @FXML
    public TextField searchField;

    @FXML
    public Button searchBtn;
    @FXML
    public Button filterBtn;
    @FXML
    public Button settingsBtn;
    @FXML
    public Button reloadBtn;

    @FXML
    private BorderPane mainPane;

    @FXML
    private VBox items;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private BorderPane timerPane;
    @FXML
    private Text timerTitle;
    @FXML
    private Text timerInfo;

    @FXML
    private Button stopBtn;

    @FXML
    private Label digitTimer;


    @FXML
    private VBox filterPane;
    @FXML
    private ComboBox filterProject;
    @FXML
    private ComboBox filterTracker;
    @FXML
    private ComboBox filterStatus;
    @FXML
    private ComboBox filterPriority;
    @FXML
    private Button filterRunBtn;
    @FXML
    private Button filterCancelBtn;

    @FXML
    private ImageView loader;

    private Pane activePane;

    public Object activeId = null;

    public int seconds = 0;

    private Timeline timer;

    private rdParam activeRdParam;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){

        CFactory.getInstance().setMainController(this);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        timerPane.managedProperty().bind(timerPane.visibleProperty());
        timerPane.setVisible(false);

        if(renderIssues()){
            this.configureFilter();
        }else{
            reload();
        }

        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (searchField.isVisible()) {
                    searchField.setVisible(false);
                    searchField.setPrefHeight(0.0);
                    topPane.setPrefHeight(40.0);
                } else {
                    searchField.setVisible(true);
                    searchField.setPrefHeight(20.0);
                    topPane.setPrefHeight(60.0);
                }
            }
        });

        filterBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (filterPane.isVisible()) {
                    filterPane.setVisible(false);
                } else {
                    filterPane.setVisible(true);
                }
            }
        });


        settingsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Setting w = new Setting();
                try {
                    w.start(myStage);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        reloadBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                reload();
            }
        });

        stopBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openPushWindow();
            }
        });

        filterCancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                filterPane.setVisible(false);
            }
        });

        filterRunBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                runFilter();
                filterPane.setVisible(false);
            }
        });

        Timeline reloadTimer = new Timeline(new KeyFrame(Duration.seconds(60), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                reload();

                Repository repo = new Repository();
                JSONArray act = repo.getActives();
                for(int i=0; i < act.size(); i++){
                    rdParam rd = new rdParam();
                    rd.method = "POST";
                    rd.params = (String) act.get(i);
                    rd.url = "/time_entries.json";
                    Runnable ajax = new Ajax(rd, new Callback() {
                        @Override
                        public Object call(Object o){
                            if(o instanceof HashMap && (Boolean) ((HashMap) o).get("error") == false){
                                rdParam rd = (rdParam) ((HashMap) o).get("rd");
                                Repository repo = new Repository();
                                repo.removePushRequest(rd.params);
                                repo.flush();
                            }
                            return null;
                        }
                    });
                    Thread t = new Thread(ajax, "Thread");
                    t.start();
                }
            }
        }));
        reloadTimer.setCycleCount(Timeline.INDEFINITE);
        reloadTimer.play();
    }

    private Boolean renderIssues(){
        Repository repo = new Repository();
        JSONArray issues = repo.getIssues();
        refreshList(issues);
        if(issues.size() > 0){
            return true;
        }
        return false;
    }

    private void clearItems(){
        Set<Node> panes = items.lookupAll(".pane");
        for (final Node pane : panes) {
            if (pane instanceof Pane) {
                items.getChildren().remove(pane);
            }
        }
    }

    private void refreshList(JSONArray issues){
        /*clear all items*/
        clearItems();
        for(int i=0; i < issues.size(); i++){
            JSONObject item = (JSONObject) issues.get(i);
            String id = String.valueOf(item.get("id"));

            Pane pane = new Pane();
            pane.setId("item_" + id);
            pane.getStyleClass().add("pane");
            pane.setPrefHeight(Region.USE_COMPUTED_SIZE);

            BorderPane bp = new BorderPane();

            /*text*/
            Text subject = new Text((String) "#" + id + ": " + item.get("subject"));
            subject.getStyleClass().add("item-text");
            VBox tVbox = new VBox();
            tVbox.setPadding(new Insets(7, 7, 7, 7));
            tVbox.getChildren().add(subject);

            /*info*/
            JSONObject status = (JSONObject) item.get("status");
            JSONObject priority = (JSONObject) item.get("priority");
            JSONObject project = (JSONObject) item.get("project");

            Text info = new Text(status.get("name") +", "+ priority.get("name") +", "+ project.get("name"));
            info.getStyleClass().add("text-info");
            tVbox.getChildren().add(info);
            bp.setCenter(tVbox);

            /*time*/
            Object estimate = "00:00";
            if(item.get("estimated_hours") instanceof Double){
                estimate = (Double) item.get("estimated_hours");
                Double sec = (Double) estimate * 3600;
                int hours = (int) Math.floor(sec / 3600);
                int mins = (int) Math.floor((sec - (hours * 3600)) / 60);
                estimate = ((hours < 10) ? "0"+hours : hours) + ":" + ((mins < 10) ? "0"+mins : mins);

            }
            Text est_text = new Text(String.valueOf(estimate));

            est_text.getStyleClass().add("estimate-text");
            VBox esVbox = new VBox();
            esVbox.setAlignment(Pos.TOP_RIGHT);
            esVbox.getChildren().add(est_text);
            esVbox.setPadding(new Insets(10,0,0,0));
            bp.setRight(esVbox);

            /*buttons*/
            Pane buttonPane = new Pane();
            final Button play = new Button();
            play.getStyleClass().add("play");
            play.setId("playBtn_" + id);
            play.setLayoutX(62);
            play.setText("Play");

            final Hyperlink showBtn = new Hyperlink();
            showBtn.getStyleClass().add("show-btn");
            showBtn.setId("shoqBtn_" + id);
            showBtn.setLayoutX(7);
            showBtn.setText("Show");
            //showBtn.


            /*action*/
            play.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Button btn = (Button) actionEvent.getSource();
                    String[] parts = btn.getId().split("_");
                    String id = parts[1];
                    Scene scene = btn.getScene();
                    play(id);
                }
            });

            showBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Hyperlink btn = (Hyperlink) actionEvent.getSource();
                    String[] parts = btn.getId().split("_");
                    String id = parts[1];
                    String url = "http://rd.groslab.com/issues/" + id;
                    openWebpage(URI.create(url));
                }
            });

            buttonPane.getChildren().addAll(play, showBtn);
            bp.setBottom(buttonPane);


            pane.getChildren().add(bp);
            items.getChildren().add(pane);
        }

        resizable(winWidth);
    }

    private void play(String id){
        if(activeId == null){
            Pane node = (Pane) scrollPane.lookup("#item_" + id);
            //node.managedProperty().bind(node.visibleProperty());
            //node.setVisible(false);
            activePane = node;
            activeId = id;
            timerPane.setVisible(true);

            Repository repo = new Repository();
            JSONObject item = repo.getIssue(id);

            timerTitle.setText((String) item.get("subject"));

            JSONObject status = (JSONObject) item.get("status");
            JSONObject priority = (JSONObject) item.get("priority");
            JSONObject project = (JSONObject) item.get("project");
            timerInfo.setText(status.get("name") +", "+ priority.get("name") +", "+ project.get("name"));
            scrollPane.setVvalue(0);

            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    seconds++;
                    String time = secondsToTime(seconds);
                    digitTimer.setText(time);
                }
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();

            myStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {

                }
            });
        }
    }

    public void doUnActiveButton(){

    }

    private String secondsToTime(int sec){
        int hours = Math.round(sec / 3600);
        int mins = Math.round((sec - (hours * 3600)) / 60);
        int secs = Math.round(sec % 60);
        return  ((hours < 10) ? "0" + hours : "" + hours) + ":" + ((mins < 10) ? "0" + mins : "" + mins) + ":" + ((secs < 10) ? "0" + secs : "" + secs);
    }

    private void openPushWindow(){
        timer.stop();
        Push w = new Push();
        try {
            w.start(myStage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void resume(){
        timer.play();
    }

    public void push(String message, String activity_id){
        timer.stop();
        digitTimer.setText("00:00:00");
        activePane.setVisible(true);
        timerPane.setVisible(false);

        activeRdParam = RedmineApi.getInstance().pushTime(seconds, (String) activeId, message, activity_id);
        seconds = 0;
        activeId = null;

        Runnable ajax = new Ajax(activeRdParam, new Callback() {
            @Override
            public Object call(Object o) {
                if(o instanceof HashMap && (Boolean) ((HashMap) o).get("error") == true){
                    Repository repo = new Repository();
                    repo.addPushRequest(activeRdParam);
                    repo.flush();
                }
                return null;
            }
        });
        Thread t = new Thread(ajax, "Thread");
        t.start();
    }

    private Stage myStage;
    public void setStage(Stage stage) {
        myStage = stage;
    }

    public void resizable(Double width){
        winWidth = width;
        Set<Node> nodes = scrollPane.lookupAll(".scroll-bar");
        for (final Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) {
                    Double w = sb.getWidth();
                    if(sb.isVisible()){
                        width -= w;
                    }
                }
            }
        }

        items.setPrefWidth(width);

        Set<Node> panes = scrollPane.lookupAll(".pane");
        for (final Node pane : panes) {
            if (pane instanceof Pane) {
                Pane p = (Pane) pane;
                Text text = (Text) p.lookup(".item-text");

                text.setWrappingWidth(width-60);
            }
        }
    }

    private void configureFilter(){
        filterPane.managedProperty().bind(filterPane.visibleProperty());
        filterPane.setVisible(false);
        Repository repo = new Repository();

        JSONArray projects = repo.getProjects();
        String project_def_val = (filterProject.getValue() == null) ? "ALL" : (String) filterProject.getValue();
        projects.add(0, "ALL");
        filterProject.setItems(FXCollections.observableArrayList(projects));
        filterProject.setValue(project_def_val);

        JSONArray trackers = repo.getTrackers();
        String tracker_def_val = (filterTracker.getValue() == null) ? "ALL" : (String) filterTracker.getValue();
        trackers.add(0, "ALL");
        filterTracker.setItems(FXCollections.observableArrayList(trackers));
        filterTracker.setValue(tracker_def_val);

        JSONArray statuses = repo.getStatuses();
        String status_def_val = (filterStatus.getValue() == null) ? "ALL" : (String) filterStatus.getValue();
        statuses.add(0, "ALL");
        filterStatus.setItems(FXCollections.observableArrayList(statuses));
        filterStatus.setValue(status_def_val);

        JSONArray priorities = repo.getPriorities();
        String priority_def_val = (filterPriority.getValue() == null) ? "ALL" : (String) filterPriority.getValue();
        priorities.add(0, "ALL");
        filterPriority.setItems(FXCollections.observableArrayList(priorities));
        filterPriority.setValue(priority_def_val);
    }

    private void runFilter(){
        String project = (String) filterProject.getValue();
        String tracker = (String) filterTracker.getValue();
        String status = (String) filterStatus.getValue();
        String priority = (String) filterPriority.getValue();

        /*find ids*/
        Repository repo = new Repository();
        JSONArray issues = repo.getIssues();
        List<String> res = new ArrayList<String>();
        for(int i=0; i < issues.size(); i++){
            Boolean is = true;
            JSONObject item = (JSONObject) issues.get(i);
            String id = String.valueOf(item.get("id"));
            JSONObject item_project = (JSONObject) item.get("project");
            JSONObject item_tracker = (JSONObject) item.get("tracker");
            JSONObject item_priority = (JSONObject) item.get("priority");
            JSONObject item_status = (JSONObject) item.get("status");

            if(!project.equals(item_project.get("name")) && project != "ALL"){
                is = false;
            }

            if(!tracker.equals(item_tracker.get("name")) && tracker != "ALL"){
                is = false;
            }

            if(!priority.equals(item_priority.get("name")) && priority != "ALL"){
                is = false;
            }
            if(!status.equals(item_status.get("name")) && status != "ALL"){
                is = false;
            }
            if(is){
                res.add("item_" + id);
            }
        }

        Set<Node> panes = items.lookupAll(".pane");
        for (final Node pane : panes) {
            if (pane instanceof Pane) {
                pane.managedProperty().bind(pane.visibleProperty());
                if(!res.contains(pane.getId())){
                    pane.setVisible(false);
                }else{
                    pane.setVisible(true);
                }
            }
        }

    }

    public void reload(){
        loader.setVisible(true);
        Runnable worker = new Ajax(RedmineApi.getInstance().loadIssues(), new Callback() {
            @Override
            public Object call(Object o) {
                if(o instanceof HashMap && (Boolean) ((HashMap) o).get("error") == false){
                    Repository repo = new Repository();
                    repo.updateIssues((String) ((HashMap) o).get("result"));
                    repo.flush();

                    Platform.runLater(new Runnable(){
                        public void run() {
                            renderIssues();
                            configureFilter();
                            runFilter();
                            resizable(winWidth);
                            loader.setVisible(false);
                        }
                    });
                }else {
                    loader.setVisible(false);
                }

                return null;
            }
        });

        Thread t = new Thread(worker, "Thread");
        t.start();
    }

    public static void openWebpage(URI uri) {

        ExecuteShellComand obj = new ExecuteShellComand();
        String command = "python -mwebbrowser " + uri;
        String output = obj.executeCommand(command);

        /*Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }
}
