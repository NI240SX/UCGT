package fr.ni240sx.ucgt.collisionsEditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    

    private double anchorX, anchorY;// posX, posY;
    private double angleX = -15, angleY = 30;//, x=0, y=0;
    private double radius = 10; // Distance of the camera from the object
    private final double minRadius = 0.1, maxRadius = 1000;
    private final double sensitivity = 0.5; // Mouse sensitivity
    private final double zoomFactor = 1.1;

    public PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group cameraPivot = new Group();
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate cameraPivotOffset = new Translate(0, 0, 0);
    private final Translate cameraPivotPosition = new Translate(0, 0, 0);
    private final Translate cameraDistance = new Translate(0, 0, -radius);
    
    public BooleanProperty trackpadMode = new SimpleBooleanProperty();
	/**
	 * @param viewportGroup
	 * @param width
	 * @param height
	 */
	public OrbitCameraViewport(Group viewportGroup, double width, double height) {
		super(viewportGroup, width, height, true, SceneAntialiasing.BALANCED);

		this.viewportGroup = viewportGroup;
//        this.buildAxes();
//        if (CollisionsEditor.debug) this.viewportGroup.getChildren().add(new Box(0.1,0.1,0.1));
        
        
     // Set up a camera
        camera.setNearClip(0.01);
        camera.setFarClip(100000);
//        updateCameraPosition();

        camera.getTransforms().add(cameraDistance);

        // Create pivot for the camera
        cameraPivot.getChildren().add(camera);
        cameraPivot.getTransforms().addAll(rotateY, rotateX, cameraPivotOffset);
        var pivot2 = new Group();
        pivot2.getChildren().add(cameraPivot);
		pivot2.getTransforms().add(cameraPivotPosition);
        rotateY.setAngle(angleY);
        rotateX.setAngle(180 + angleX);
        
        this.setFill(Color.rgb(0, 0, 0, 0));
        this.setCamera(camera);
        
     // Handle mouse control
        setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
//            posX = e.getSceneX()+cameraPivotPosition.getX()/sensitivity*100;
//            posY = e.getSceneY()+cameraPivotPosition.getY()/sensitivity*100;
        });
        setOnMouseDragged(event -> {
        	if (event.isSecondaryButtonDown()) {
	            double deltaX = (event.getSceneX() - anchorX) * sensitivity/100*radius/10;
	            double deltaY = (event.getSceneY() - anchorY) * sensitivity/100*radius/10;

	            cameraPivotOffset.setX(-deltaX);
	            cameraPivotOffset.setY(-deltaY);
        		
        	} else {
	            double deltaX = (event.getSceneX() - anchorX) * sensitivity;
	            double deltaY = (event.getSceneY() - anchorY) * sensitivity;
	            anchorX = event.getSceneX();
	            anchorY = event.getSceneY();
	
	            angleY -= deltaX;
	            angleX -= deltaY;
	
	            rotateY.setAngle(angleY);
	            rotateX.setAngle(180 + angleX);
        	}
        });
        setOnZoom(e -> {
        	radius *= e.getTotalZoomFactor();
            radius = Math.max(minRadius, Math.min(maxRadius, radius));
//            updateCameraPosition();
        });
        setOnScroll(e ->{
        	if (e.isControlDown() || !trackpadMode.get()) {
        		//zoom
	            if (e.getDeltaY() > 0) {
	                radius /= zoomFactor;
	            } else {
	                radius *= zoomFactor;
	            }
	            radius = Math.max(minRadius, Math.min(maxRadius, radius));
	            cameraDistance.setZ(-radius);
	            
        	} else {
        		angleY -= e.getDeltaX() * sensitivity;
        		rotateY.setAngle(angleY);

        		angleX -= e.getDeltaY() * sensitivity;
        		rotateX.setAngle(180 + angleX);
        	}
        });
        
	}
	
	public void resetCamera() {
		rotateX.setAngle(180);
		rotateX.setAxis(Rotate.X_AXIS);
		rotateY.setAngle(0);
		rotateY.setAxis(Rotate.Y_AXIS);
	    cameraDistance.setX(0);
	    cameraDistance.setY(0);
	    cameraDistance.setZ(-10);

	    cameraPivotOffset.setX(0);
	    cameraPivotOffset.setY(0);
	    cameraPivotOffset.setZ(0);

	    cameraPivotPosition.setX(0);
	    cameraPivotPosition.setY(0);
	    cameraPivotPosition.setZ(0);
	    
        rotateY.setAngle(30);
        rotateX.setAngle(180 + -15);

        angleX = -15;
        angleY = 30;
        radius = 10;
	}
	
	public void moveCamera(double x, double y, double z) {
		cameraPivotPosition.setX(x);
		cameraPivotPosition.setY(y);
		cameraPivotPosition.setZ(z);
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
