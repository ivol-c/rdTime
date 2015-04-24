package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        CFactory.getInstance().setPrimaryStage(primaryStage);
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        final Parent root = (Parent) fxmlLoader.load();
        final Controller controller = (Controller)fxmlLoader.getController();
        controller.setStage(primaryStage);

        final Scene scene = new Scene(root, 420, 800);

        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                controller.resizable((Double) t1 - 30);
            }
        });

        primaryStage.setTitle("RDTimer v1.0");
        primaryStage.setScene(scene);

        /* if no settings - open window */
        if(RedmineApi.getInstance().serverUrl == null){
            Setting w = new Setting();
            try {
                w.start(primaryStage);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            primaryStage.show();
            CFactory.getInstance().mainController.reload();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
