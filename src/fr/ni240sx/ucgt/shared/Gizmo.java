package fr.ni240sx.ucgt.shared;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class Gizmo extends Group {
	public Cylinder cylX; //move/rotate
	public Cylinder cylY;
	public Cylinder cylZ;
	
	public Box boxX; //move/scale
	public Box boxY;
	public Box boxZ;
	
	public OrbitCameraViewport viewport;
	
	private double x, y;
	private Rotate rx,ry,rz;
	private Tooltip tooltip;

	public DoubleProperty ScaleX = new SimpleDoubleProperty(1);
	public DoubleProperty ScaleY = new SimpleDoubleProperty(1);
	public DoubleProperty ScaleZ = new SimpleDoubleProperty(1);
	
	public DoubleProperty RotationX = new SimpleDoubleProperty(0);
	public DoubleProperty RotationY = new SimpleDoubleProperty(0);
	public DoubleProperty RotationZ = new SimpleDoubleProperty(0);
	
//	public Affine rotations = new Affine();
	private Translate translate = new Translate();
	private Scale scale = new Scale();
	
	public DoubleProperty PositionX = new SimpleDoubleProperty(0);
	public DoubleProperty PositionY = new SimpleDoubleProperty(0);
	public DoubleProperty PositionZ = new SimpleDoubleProperty(0);
	
	public DoubleProperty offsetX = new SimpleDoubleProperty(0);
	public DoubleProperty offsetY = new SimpleDoubleProperty(0);
	public DoubleProperty offsetZ = new SimpleDoubleProperty(0);

	public double snappingFactorTranslate = 200;
	public double snappingFactorRotate = 2;
	
	private static final double movementFactor = 1800.0;
	
	public Gizmo(double size, OrbitCameraViewport viewport) {
		cylX = new Cylinder(size/20, size);
		cylY = new Cylinder(size/20, size);
		cylZ = new Cylinder(size/20, size);
		
		boxX = new Box(size, size/10, size/10);
		boxY = new Box(size/10, size, size/10);
		boxZ = new Box(size/10, size/10, size);
		
		this.viewport = viewport;
		tooltip = new Tooltip();
		tooltip.setAutoHide(true);
		
		this.getTransforms().addAll(translate,
				scale,
				rx = new Rotate(0, Rotate.X_AXIS),
				ry = new Rotate(0, Rotate.Y_AXIS),
				rz = new Rotate(0, Rotate.Z_AXIS));

		scale.setX(size*viewport.getZoom()/20);
		scale.setY(size*viewport.getZoom()/20);
		scale.setZ(size*viewport.getZoom()/20);
//
//		viewport.cameraPivotPosition.addEventHandler(TransformChangedEvent.ANY, e -> {
//			scale.setX(size*viewport.getZoom()/10);
//			scale.setY(size*viewport.getZoom()/10);
//			scale.setZ(size*viewport.getZoom()/10);
//		});
		
		
//		this.getTransforms().addAll(translate, rotations);

//		RotationX.addListener((obs, was, is) -> updateRotations());
//		RotationY.addListener((obs, was, is) -> updateRotations());
//		RotationZ.addListener((obs, was, is) -> updateRotations());
		RotationX.addListener((obs, was, is) -> rx.setAngle(is.doubleValue()));
		RotationY.addListener((obs, was, is) -> ry.setAngle(is.doubleValue()));
		RotationZ.addListener((obs, was, is) -> rz.setAngle(-is.doubleValue()));

		PositionX.addListener((obs, was, is) -> {
//			rx.setPivotX(PositionX.doubleValue() + offsetX.doubleValue());
			
			translate.setX(PositionX.doubleValue() + offsetX.doubleValue());
		});
		PositionY.addListener((obs, was, is) -> {
//			ry.setPivotY(PositionY.doubleValue() + offsetY.doubleValue());
			translate.setY(PositionY.doubleValue() + offsetY.doubleValue());
		});
		PositionZ.addListener((obs, was, is) -> {
//			rz.setPivotZ(PositionZ.doubleValue() + offsetZ.doubleValue());
			translate.setZ(PositionZ.doubleValue() + offsetZ.doubleValue());
		});
		
		offsetX.addListener((obs, was, is) -> {
			translate.setX(PositionX.doubleValue() + offsetX.doubleValue());
		});
		offsetY.addListener((obs, was, is) -> {
			translate.setY(PositionY.doubleValue() + offsetY.doubleValue());
		});
		offsetZ.addListener((obs, was, is) -> {
			translate.setZ(PositionZ.doubleValue() + offsetZ.doubleValue());
		});

		ScaleX.addListener((obs, was, is) -> {
			translate.setX(PositionX.doubleValue() + offsetX.doubleValue());
		});
		ScaleY.addListener((obs, was, is) -> {
			translate.setY(PositionY.doubleValue() + offsetY.doubleValue());
		});
		ScaleZ.addListener((obs, was, is) -> {
			translate.setZ(PositionZ.doubleValue() + offsetZ.doubleValue());
		});
		
		this.setViewOrder(-100);
		this.setDepthTest(DepthTest.DISABLE);
		
		
		
		cylX.setTranslateX(size/2);
		cylY.setTranslateY(size/2);
		cylZ.setTranslateZ(size/2);
		boxX.setTranslateX(size/2);
		boxY.setTranslateY(size/2);
		boxZ.setTranslateZ(size/2);
		
		cylX.setRotationAxis(Rotate.Z_AXIS);
		cylX.setRotate(90);
		
		cylZ.setRotationAxis(Rotate.X_AXIS);
		cylZ.setRotate(90);

		cylX.setMaterial(new PhongMaterial(Color.RED));
		cylY.setMaterial(new PhongMaterial(Color.GREEN));
		cylZ.setMaterial(new PhongMaterial(Color.BLUE));
		boxX.setMaterial(new PhongMaterial(Color.RED));
		boxY.setMaterial(new PhongMaterial(Color.GREEN));
		boxZ.setMaterial(new PhongMaterial(Color.BLUE));
		
		this.getChildren().addAll(cylX, cylY, cylZ);
		
		cylX.setOnMouseClicked(e -> {
			if (!e.isStillSincePress()) return;
			this.getChildren().removeAll(cylX, cylY, cylZ);
			this.getChildren().addAll(boxX, boxY, boxZ);
		});
		cylY.setOnMouseClicked(cylX.getOnMouseClicked());
		cylZ.setOnMouseClicked(cylX.getOnMouseClicked());
		
		boxX.setOnMouseClicked(e -> {
			if (!e.isStillSincePress()) return;
			this.getChildren().addAll(cylX, cylY, cylZ);
			this.getChildren().removeAll(boxX, boxY, boxZ);
		});
		boxY.setOnMouseClicked(boxX.getOnMouseClicked());
		boxZ.setOnMouseClicked(boxX.getOnMouseClicked());

		

		cylX.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		cylY.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		cylZ.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		
		boxX.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		boxY.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		boxZ.setOnMouseMoved(e -> {
			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});

		
		cylX.setOnMouseDragged(e -> {			
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			PositionX.set(PositionX.get() + computeDragAmount(dx, dy, new Point3D(1,0,0))*viewport.getZoom()/movementFactor);
			var px = PositionX.get();
			if (-1 < px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) && px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) < 1) PositionX.set((int)(px*snappingFactorTranslate)/snappingFactorTranslate);
			tooltip.setText("Position X %.03f".formatted(PositionX.get()));
			tooltip.show(cylX, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		cylY.setOnMouseDragged(e -> {
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			PositionY.set(PositionY.get() + computeDragAmount(dx, dy, new Point3D(0,1,0))*viewport.getZoom()/movementFactor);
			var px = PositionY.get();
			if (-1 < px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) && px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) < 1) PositionY.set((int)(px*snappingFactorTranslate)/snappingFactorTranslate);
			tooltip.setText("Position Y %.03f".formatted(PositionY.get()));
			tooltip.show(cylY, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		cylZ.setOnMouseDragged(e -> {
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			PositionZ.set(PositionZ.get() + computeDragAmount(dx, dy, new Point3D(0,0,1))*viewport.getZoom()/movementFactor);
			var px = PositionZ.get();
			if (-1 < px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) && px*snappingFactorTranslate - (int)(px*snappingFactorTranslate) < 1) PositionZ.set((int)(px*snappingFactorTranslate)/snappingFactorTranslate);
			tooltip.setText("Position Z %.03f".formatted(PositionZ.get()));
			tooltip.show(cylZ, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		
		
		
		cylX.setOnScroll(e -> {
			RotationX.set( ((RotationX.get() + e.getDeltaY())%360));
			var px = RotationX.get();
			if (-1 < px*snappingFactorRotate - (int)(px*snappingFactorRotate) && px*snappingFactorRotate - (int)(px*snappingFactorRotate) < 1) RotationX.set((int)(px*snappingFactorRotate)/snappingFactorRotate);
			tooltip.setText("Rotation X %.03f".formatted(RotationX.get()));
			tooltip.show(cylX, e.getScreenX()+10, e.getScreenY()+10);
			e.consume();
		});
		cylY.setOnScroll(e -> {
			RotationY.set( ((RotationY.get() + e.getDeltaY())%360));
			var px = RotationY.get();
			if (-1 < px*snappingFactorRotate - (int)(px*snappingFactorRotate) && px*snappingFactorRotate - (int)(px*snappingFactorRotate) < 1) RotationY.set((int)(px*snappingFactorRotate)/snappingFactorRotate);
			tooltip.setText("Rotation Y %.03f".formatted(RotationY.get()));
			tooltip.show(cylY, e.getScreenX()+10, e.getScreenY()+10);
			e.consume();
		});
		cylZ.setOnScroll(e -> {
			RotationZ.set( ((RotationZ.get() + e.getDeltaY())%360));
			var px = RotationZ.get();
			if (-1 < px*snappingFactorRotate - (int)(px*snappingFactorRotate) && px*snappingFactorRotate - (int)(px*snappingFactorRotate) < 1) RotationZ.set((int)(px*snappingFactorRotate)/snappingFactorRotate);
			tooltip.setText("Rotation Z %.03f".formatted(RotationZ.get()));
			tooltip.show(cylZ, e.getScreenX()+10, e.getScreenY()+10);
			e.consume();
		});
		

		boxX.setOnMouseDragged(e -> {
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			ScaleX.set(ScaleX.get() + computeDragAmount(dx, dy, new Point3D(1,0,0))*viewport.getZoom()/movementFactor);
			tooltip.setText("Scale X %.03f".formatted(ScaleX.get()));
			tooltip.show(boxX, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		boxY.setOnMouseDragged(e -> {			
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			ScaleY.set(ScaleY.get() + computeDragAmount(dx, dy, new Point3D(0,1,0))*viewport.getZoom()/movementFactor);
			tooltip.setText("Scale Y %.03f".formatted(ScaleY.get()));
			tooltip.show(boxY, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
		boxZ.setOnMouseDragged(e -> {			
			var dx = (e.getScreenX()-x);
			var dy = (e.getScreenY()-y);
						
			ScaleZ.set(ScaleZ.get() + computeDragAmount(dx, dy, new Point3D(0,0,1))*viewport.getZoom()/movementFactor);
			tooltip.setText("Scale Z %.03f".formatted(ScaleZ.get()));
			tooltip.show(boxZ, e.getScreenX()+10, e.getScreenY()+10);

			x = e.getScreenX();
			y = e.getScreenY();
			e.consume();
		});
	}

//	private void updateRotations() {
//		//first calculation
//		var u = Math.toRadians(RotationX.get());
//	    var v = Math.toRadians(RotationY.get());
//	    var w = Math.toRadians(-RotationZ.get());
//
//	    double cu = Math.cos(u);
//	    double su = Math.sin(u);
//	    double cv = Math.cos(v);
//	    double sv = Math.sin(v);
//	    double cw = Math.cos(w);
//	    double sw = Math.sin(w);
//
//	    var R = new double[3][3];
//	    R[0][0] = (cv * cw);
//	    R[0][1] = (cv * sw);
//	    R[0][2] = -sv;
//	    R[1][0] = (su * sv * cw - cu * sw);
//	    R[1][1] = (su * sv * sw + cu * cw);
//	    R[1][2] = (su * cv);
//	    R[2][0] = (cu * sv * cw + su * sw);
//	    R[2][1] = (cu * sv * sw - su * cw);
//	    R[2][2] = (cu * cv);
//	    
//	    //inversion
//	    double sy = -Math.asin(R[0][2]);
//	    double cy = Math.cos(sy);
//
//	    if (Math.abs(cy) > 1e-6) { // Avoid division by zero
//	        u = Math.atan2(R[1][2], R[2][2]);
//	        v = sy;
//	        w = Math.atan2(R[0][1], R[0][0]);
//	    } else { // Gimbal lock situation
//	        u = 0.0; 
////	        z = 0.0;
//	        if (R[0][2] < 0) {
////	        	x = Math.atan2(R[1][0], R[2][0] );
//	            v = Math.PI / 2;
//	            w = Math.atan2(R[1][0], R[1][1]);
//	        } else {
////	        	x = Math.atan2( -R[1][0], -R[2][0] );
//	            v = -Math.PI / 2;
//	            w = Math.atan2(-R[1][0], -R[1][1]);
//	        }
//	    }
//	    
//	    //second calculation
//	    cu = Math.cos(u);
//	    su = Math.sin(u);
//	    cv = Math.cos(v);
//	    sv = Math.sin(v);
//	    cw = Math.cos(w);
//	    sw = Math.sin(w);
//	    
//
//	    rotations.setMxx(cv * cw);
//	    rotations.setMxy(cv * sw);
//	    rotations.setMxz(-sv);
//	    rotations.setMyx(su * sv * cw - cu * sw);
//	    rotations.setMyy(su * sv * sw + cu * cw);
//	    rotations.setMyz(su * cv);
//	    rotations.setMzx(cu * sv * cw + su * sw);
//	    rotations.setMzy(cu * sv * sw - su * cw);
//	    rotations.setMzz(cu * cv);	
//	}

	public void setOffset(double x, double y, double z) {
		offsetX.set(x);
		offsetY.set(y);
		offsetZ.set(z);
	}
	
	public void setPosition(double x, double y, double z) {
		PositionX.set(x);
		PositionY.set(y);
		PositionZ.set(z);
	}

	public void setRotation(double x, double y, double z) {
		RotationX.set(x);
		RotationY.set(y);
		RotationZ.set(z);
	}

	public void setScale(double x, double y, double z) {
		ScaleX.set(x);
		ScaleY.set(y);
		ScaleZ.set(z);
	}
	
	private double computeDragAmount(
	        double dx,
	        double dy,
	        Point3D worldAxis)
	{
	    double yaw   = Math.toRadians(viewport.getAngleY());
	    double pitch = Math.toRadians(viewport.getAngleX());

	    // Camera basis vectors in world space

	    Point3D right = new Point3D(
	            Math.cos(yaw),
	            0,
	            -Math.sin(yaw));

	    Point3D up = new Point3D(
	            Math.sin(yaw) * Math.sin(pitch),
	            Math.cos(pitch),
	            Math.cos(yaw) * Math.sin(pitch));

	    // Screen-space direction of the axis
	    Point2D screenDir = new Point2D(
	            worldAxis.dotProduct(right),
	            -worldAxis.dotProduct(up));

	    double len = screenDir.magnitude();

	    if (len < 1e-6)
	        return 0;           // axis points almost directly at camera

	    screenDir = screenDir.normalize();

	    return dx * screenDir.getX()
	         + dy * screenDir.getY();
	}
}
