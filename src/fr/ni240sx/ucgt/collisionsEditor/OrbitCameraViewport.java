package fr.ni240sx.ucgt.collisionsEditor;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class OrbitCameraViewport extends SubScene {

	public Group viewportGroup;
    

    private double anchorX, anchorY;
    private double angleX = -15, angleY = 30;
    private double radius = 10; // Distance of the camera from the object
    private final double minRadius = 0.1, maxRadius = 100;
    private final double sensitivity = 0.5; // Mouse sensitivity
    private final double zoomFactor = 1.1;

    public PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group cameraPivot = new Group();
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate cameraDistance = new Translate(0, 0, -radius);
    
	/**
	 * @param viewportGroup
	 * @param width
	 * @param height
	 */
	public OrbitCameraViewport(Group viewportGroup, double width, double height) {
		super(viewportGroup, width, height, true, SceneAntialiasing.BALANCED);

		this.viewportGroup = viewportGroup;
        this.buildAxes();
//        if (CollisionsEditor.debug) this.viewportGroup.getChildren().add(new Box(0.1,0.1,0.1));
        
        
     // Set up a camera
        camera.setNearClip(0.01);
        camera.setFarClip(10000);
//        updateCameraPosition();

        camera.getTransforms().add(cameraDistance);

        // Create pivot for the camera
        cameraPivot.getChildren().add(camera);
        cameraPivot.getTransforms().addAll(rotateY, rotateX);
        rotateY.setAngle(angleY);
        rotateX.setAngle(180 + angleX);
        
        this.setFill(Color.rgb(0, 0, 0, 0));
        this.setCamera(camera);
        
     // Handle mouse control
        setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
        });
        setOnMouseDragged(event -> {
            double deltaX = (event.getSceneX() - anchorX) * sensitivity;
            double deltaY = (event.getSceneY() - anchorY) * sensitivity;
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();

            angleY -= deltaX;
            angleX -= deltaY;

            rotateY.setAngle(angleY);
            rotateX.setAngle(180 + angleX);
        });
        setOnZoom(e -> {
        	radius *= e.getTotalZoomFactor();
            radius = Math.max(minRadius, Math.min(maxRadius, radius));
//            updateCameraPosition();
        });
        setOnScroll(e ->{
//        	if (e.isDirect() || e.isControlDown()) {
	            if (e.getDeltaY() > 0) {
	                radius /= zoomFactor;
	            } else {
	                radius *= zoomFactor;
	            }
	            radius = Math.max(minRadius, Math.min(maxRadius, radius));
	            cameraDistance.setZ(-radius);
//        	} else {
//
//                double deltaX = e.getDeltaX() * sensitivity;
//                double deltaY = e.getDeltaY() * sensitivity;
//
//                angleY -= deltaX;
//                angleX -= deltaY;
//
//                rotateY.setAngle(angleY);
//                rotateX.setAngle(180 + angleX);
//        	}
        });
        
	}
	
	public void buildAxes() {

		final Box xAxis = new Box(4, 0.01, 0.01);
        final Box yAxis = new Box(0.01, 4, 0.01);
        final Box zAxis = new Box(0.01, 0.01, 4);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        xAxis.setTranslateX(2);
        yAxis.setTranslateY(2);
        zAxis.setTranslateZ(2);
        Group axisGroup = new Group();
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);

        this.viewportGroup.getChildren().add(axisGroup);
	}

}
