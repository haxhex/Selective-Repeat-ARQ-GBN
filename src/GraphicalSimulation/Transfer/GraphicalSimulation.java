package GraphicalSimulation.Transfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

// Run the Application JavaFX
public class GraphicalSimulation extends Application {
    public static Stage MAINSTAGE;
    public static String [] ar;
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("DataEntry.fxml"));
        Scene scene = new Scene(root);
        MAINSTAGE=stage;
        stage.getIcons().add(new Image("file:icon.png"));
        stage.setTitle("Graphical Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws Exception  {
        launch(args);
    }
    
}
