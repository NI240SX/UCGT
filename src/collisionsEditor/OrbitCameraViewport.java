package collisionsEditor;

import javafx.beans.NamedArg;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

public class OrbitCameraViewport extends SubScene {

	Group viewportGroup;
    public PerspectiveCamera camera = new PerspectiveCamera(true);
    Rotate rotationX = new Rotate(0, Rotate.X_AXIS);
    Rotate rotationY = new Rotate(0, Rotate.Y_AXIS);
    Rotate rotationZ = new Rotate(0, Rotate.Z_AXIS);
	
	public OrbitCameraViewport(Group viewportGroup, @NamedArg(value="width") double width, @NamedArg(value="height") double height) {
		super(viewportGroup, width, height, true, SceneAntialiasing.BALANCED);

		this.viewportGroup = viewportGroup;
        
		final Box xAxis = new Box(1200, 1, 1);
        final Box yAxis = new Box(1, 1200, 1);
        final Box zAxis = new Box(1, 1, 1200);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        Group axisGroup = new Group();
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);

        this.viewportGroup.getChildren().add(axisGroup);
        
        if (CollisionsEditor.debug) this.viewportGroup.getChildren().add(new Box(100,100,100));
        
        camera.setTranslateZ(-1000);
        camera.setNearClip(0.01);
        camera.setFarClip(10000);
        camera.getTransforms().addAll(rotationX, rotationY, rotationZ);

        this.setCamera(camera);
        
        this.setOnScroll((final ScrollEvent e) -> {
            rotationX.setAngle(rotationX.getAngle() + e.getDeltaY() / 10);
            rotationY.setAngle(rotationY.getAngle() - e.getDeltaX() / 10);
            setTranslateX(getTranslateX() + e.getDeltaX());
            setTranslateY(getTranslateY() + e.getDeltaY());
        });
	}
}
