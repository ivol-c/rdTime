package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by olegivancev on 11.01.15.
 */
public class Push extends Application {

    @Override public void start(Stage primaryStage) throws Exception{
        final Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage.getScene().getWindow());
        stage.setResizable(false);

        stage.initStyle(StageStyle.DECORATED);

        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("push_form.fxml"));
        final Parent root = (Parent) fxmlLoader.load();
        final Scene scene = new Scene(root, 500, 320);

        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                BorderPane bp = (BorderPane) scene.lookup(".root");
                //Double h = bp.getHeight() + 50;
                //System.out.println(h);
                //stage.setHeight(500);
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
