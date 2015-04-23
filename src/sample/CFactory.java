package sample;

import javafx.stage.Stage;

/**
 * Created by olegivancev on 21.01.15.
 */
public class CFactory {
    private static CFactory instance;

    public Controller mainController;
    public Stage primaryStage;

    public static CFactory getInstance(){
        if (instance == null){
            instance = new CFactory();
        }
        return instance;
    }

    public void setMainController(Controller controller){
        mainController = controller;
    }

    public void setPrimaryStage(Stage stage) { primaryStage = stage; }
}
