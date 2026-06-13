package fr.ni240sx.ucgt.shared;

import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class OrbitCameraViewport extends SubScene {

	public Group viewportGroup;
    

    private double anchorX, anchorY;// posX, posY;
    private double angleX = -15, angleY = 30;//, x=0, y=0;
    private double radius = 10; // Distance of the camera from the object
    private final double minRadius = 0.1, maxRadius = 100000;
    private final double sensitivity = 0.5; // Mouse sensitivity
    private final double zoomFactor = 1.1;

    public PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group cameraPivot = new Group();
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate cameraPivotOffset = new Translate(0, 0, 0);
    public final Translate cameraPivotPosition = new Translate(0, 0, 0);
    private final Translate cameraDistance = new Translate(0, 0, -radius);
    
    public BooleanProperty trackpadMode = new SimpleBooleanProperty();

    private final Set<KeyCode> keys = new HashSet<>();

    public boolean mouseLookEnabled = false;

    private double mouseSensitivity = 0.15;
    private double movementSpeed = 1;

    private AnimationTimer movementTimer;

    private double lastMouseX;
    private double lastMouseY;
    
    private long lastUpdate = 0;
    
    /**
	 * @param viewportGroup
	 * @param width
	 * @param height
	 */
	public OrbitCameraViewport(Group viewportGroup, double width, double height) {
		super(viewportGroup, width, height, true, SceneAntialiasing.BALANCED);

		this.viewportGroup = viewportGroup;
		this.setFocusTraversable(true);
//        this.buildAxes();
//        if (CollisionsEditor.debug) this.viewportGroup.getChildren().add(new Box(0.1,0.1,0.1));
        
        
     // Set up a camera
        camera.setNearClip(0.1);
        camera.setFarClip(50000);
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
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();

            requestFocus();

//            if (e.getButton() == MouseButton.SECONDARY) {
//                mouseLookEnabled = true;
//            }
            if (e.getClickCount() == 2) mouseLookEnabled = !mouseLookEnabled;
        });
        setOnMouseMoved(event -> {
        	if (mouseLookEnabled) {
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
        setOnMouseDragged(event -> {
        	if (event.isSecondaryButtonDown()) {
        		handleMouseLook(event);
//	            double deltaX = (event.getSceneX() - anchorX) * sensitivity/100*radius/10;
//	            double deltaY = (event.getSceneY() - anchorY) * sensitivity/100*radius/10;
//
//	            cameraPivotOffset.setX(-deltaX);
//	            cameraPivotOffset.setY(-deltaY);
        		
        	} else {
	            double deltaX = (event.getSceneX() - anchorX) * sensitivity;
	            double deltaY = (event.getSceneY() - anchorY) * sensitivity;
	            anchorX = event.getSceneX();
	            anchorY = event.getSceneY();
	
	            angleY -= deltaX;
	            angleX -= deltaY;
	
//	    	    angleX = Math.max(0, Math.min(180, angleX));
	    	    
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
        setOnMouseReleased(e -> {
//            if (e.getButton() == MouseButton.SECONDARY) {
//                mouseLookEnabled = false;
//            }
        });
        this.focusedProperty().addListener((obs, was, is) -> {
        	if (!is) keys.clear();
        });
        
        
        setOnKeyPressed(e -> {
        	if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) return;
        	keys.add(e.getCode());
        });
        setOnKeyReleased(e -> keys.remove(e.getCode()));
        
        movementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                updateMovement(deltaSeconds);
            }
        };

        movementTimer.start();
	}
	
	private void handleMouseLook(MouseEvent event) {

	    if (!mouseLookEnabled)
	        return;

	    double dx = event.getSceneX() - lastMouseX;
	    double dy = event.getSceneY() - lastMouseY;

	    lastMouseX = event.getSceneX();
	    lastMouseY = event.getSceneY();

	    angleY -= dx * mouseSensitivity;
	    angleX -= dy * mouseSensitivity;

	    angleX = Math.max(-89, Math.min(89, angleX));

	    rotateY.setAngle(angleY);
	    rotateX.setAngle(180 + angleX);
	}
	
	private void updateMovement(double deltaSeconds) {

	    double speed = movementSpeed * deltaSeconds * 60.0;

	    if (keys.contains(KeyCode.SHIFT)) {
	        speed *= 3.0;
	    }

	    /*
	     * Get camera local axes in world space
	     */

	    Transform transform = cameraPivot.getLocalToSceneTransform();

	    Point3D forward = transform.deltaTransform(0, 0, 1).normalize();
	    Point3D right   = transform.deltaTransform(1, 0, 0).normalize();
	    Point3D up      = transform.deltaTransform(0, -1, 0).normalize();

	    Point3D movement = Point3D.ZERO;

	    // Forward
	    if (keys.contains(KeyCode.W)
	            || keys.contains(KeyCode.Z)) {

	        movement = movement.add(forward);
	    }

	    // Backward
	    if (keys.contains(KeyCode.S)) {

	        movement = movement.subtract(forward);
	    }

	    // Left
	    if (keys.contains(KeyCode.A)
	            || keys.contains(KeyCode.Q)) {

	        movement = movement.subtract(right);
	    }

	    // Right
	    if (keys.contains(KeyCode.D)) {

	        movement = movement.add(right);
	    }

	    // Up
	    if (keys.contains(KeyCode.SPACE)) {
	        movement = movement.add(up);
	    }

	    // Down
	    if (keys.contains(KeyCode.C)) {
	        movement = movement.subtract(up);
	    }

	    if (movement.magnitude() > 0) {

	        movement = movement.normalize().multiply(speed);

	        cameraPivotPosition.setX(
	                cameraPivotPosition.getX() + movement.getX());

	        cameraPivotPosition.setY(
	                cameraPivotPosition.getY() + movement.getY());

	        cameraPivotPosition.setZ(
	                cameraPivotPosition.getZ() + movement.getZ());
	    }
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

	public void setAxes(Point3D x, Point3D y) {
		rotateX.setAxis(x);
		rotateY.setAxis(y);
	}
	public void setAngles(double x, double y) {
		angleX = x;
		angleY = y;
        rotateY.setAngle(angleY);
        rotateX.setAngle(180 + angleX);
	}
	public void setDistance(double d) {
		radius = d;
        cameraDistance.setZ(-radius);
	}
}
