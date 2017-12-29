package svmod;

import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SVModMain extends Application {

    private final int WINDOW_WIDTH = 640;
    private final int WINDOW_HEIGHT = 480;

    @Override
    public void start(Stage primaryStage) throws IOException {
        SVModView modView = SVModView.getInstance();
        modView.setWindow(primaryStage);

        Scene scene = new Scene(modView.asParent(), WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Shadowverse Mod Manager");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
