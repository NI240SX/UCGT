package fr.ni240sx.ucgt.damageFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.collisionsEditor.OrbitCameraViewport;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

public 	class DamageDebug3DView extends Application{
	
	Group viewportGroup = new Group();
	
	static String name;
	static Damage damage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
		for (int i=0; i<damage.floats1_0.length/3; i++) {
			Sphere s = new Sphere(0.05);
			viewportGroup.getChildren().add(s);
			s.setTranslateX(damage.floats1_0[i*3]);
			s.setTranslateY(damage.floats1_0[i*3+1]);
			s.setTranslateZ(damage.floats1_0[i*3+2]);
		}

        var viewport = new OrbitCameraViewport(viewportGroup, 1024, 600);
        BorderPane root = new BorderPane();
        root.setCenter(viewport);
        Scene scene = new Scene(root, 1024, 600);
        primaryStage.setTitle(name);
        primaryStage.setScene(scene);
        primaryStage.show();
	}

	public static void main(String[] args) throws IOException {
		var f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\DAMAGE\\MIT_EVO_X_08.damage");
//		var f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\DAMAGE\\BMW_M3_E92_08.damage");
//		var f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\DAMAGE\\TRF_CAR_SML_03.damage");
//		var f = new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\DAMAGE\\TRF_TRK_DMP_91.damage");
		FileInputStream fis = new FileInputStream(f);
		byte [] arr = new byte[(int)f.length()];
		fis.read(arr);
		fis.close();

		
		var in = ByteBuffer.wrap(arr);
		in.getInt();
		damage = new Damage(in);
		name = f.getName();
		launch(args);
	}
}