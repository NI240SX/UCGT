package testing;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GeomDump extends Application {

	TextArea hex = new TextArea();
	TextArea dmp = new TextArea();
	ScrollBar s = new ScrollBar();
	
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setLeft(hex);
		root.setCenter(s);
		root.setRight(dmp);
		s.setOrientation(Orientation.VERTICAL);
		Scene scene = new Scene(root,1280,720);
		primaryStage.setScene(scene);
		hex.setPrefWidth(scene.getWidth()/2-10);
		dmp.setPrefWidth(scene.getWidth()/2-10);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
