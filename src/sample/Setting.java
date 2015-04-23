package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by olegivancev on 11.01.15.
 */
public class Setting extends Application {

    @Override public void start(Stage primaryStage) throws Exception{
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage.getScene().getWindow());
        stage.setResizable(false);

        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("setting.fxml"));
        final Parent root = (Parent) fxmlLoader.load();
        final Scene scene = new Scene(root, 420, 180);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
