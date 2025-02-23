package fr.ni240sx.ucgt.collisionsEditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.*;
import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.CollisionConvexVertice;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class CollisionBound {

	public int AttributeName = 0; //hashes
	public int NameHash = 0;
	public int SurfaceName;
	
	public ArrayList<BoundFlags> ChildrenFlags;
	public BoundConstraintType ConstraintType;
	public ArrayList<BoundFlags> Flags;
	public BoundJointType JointType;
	public BoundNodeType NodeType;
	public BoundShape Shape;
	public BoundType Type;
	
	public int BoneIndex = -1;
	public float BoneOffsetX = -0;
	public float BoneOffsetY = -0;
	public float BoneOffsetZ = -0;
	
	public float HalfDimensionX = 0;
	public float HalfDimensionY = 0;
	public float HalfDimensionZ = 0;
	
//	public byte NumberOfChildren = 0;
	public short RenderHierarchyIndex = 0;

	public float OrientationX = 0;
	public float OrientationY = 0;
	public float OrientationZ = 0;
	public float OrientationW = 0;

	public float PivotX = 0;
	public float PivotY = 0;
	public float PivotZ = 0;

	public float PositionX = 0;
	public float PositionY = 0;
	public float PositionZ = 0;

	CollisionShape collisionShape = null;
	CollisionConvexTransform shapeTransform = null;
	CollisionConvexTranslate shapeTranslate = null;
	
	public CollisionBound parent = null;
	public ArrayList<CollisionBound> childBounds = new ArrayList<>();
	
	public Group displayShape = new Group();
	public OpenBox displayBound = new OpenBox(1,1,1, 0.001);
	public Sphere displayPivot = new Sphere(0.02);
	public BooleanProperty render = new SimpleBooleanProperty(true);
	
	public double colorR = Math.random();
	public double colorG = Math.random();
	public double colorB = Math.random();
	
	public CollisionBound(ByteBuffer bb) {
		OrientationX = bb.getFloat();
		OrientationY = bb.getFloat();
		OrientationZ = bb.getFloat();
		OrientationW = bb.getFloat();
		PositionX = bb.getFloat();
		PositionY = bb.getFloat();
		PositionZ = bb.getFloat();
		bb.getInt(); //PositionW
		HalfDimensionX = bb.getFloat();
		HalfDimensionY = bb.getFloat();
		HalfDimensionZ = bb.getFloat();
		bb.getInt(); //HalfDimensionW
		PivotX = bb.getFloat();
		PivotY = bb.getFloat();
		PivotZ = bb.getFloat();
		bb.getInt(); //PivotW
		BoneOffsetX = bb.getFloat();
		BoneOffsetY = bb.getFloat();
		BoneOffsetZ = bb.getFloat();
		bb.getInt(); //BoneOffsetW
		Type = BoundType.get(bb.get()); //BoundType
		bb.get(); //BoundSubType
		Shape = BoundShape.get(bb.get()); //BoundShape
		Flags = BoundFlags.get(bb.get()); //BoundFlags
		AttributeName = bb.getInt(); //AttributeName VLT HASH
		SurfaceName = bb.getInt(); //SurfaceName VLT HASH
		NameHash = bb.getInt(); //NameHash VLT HASH
		//Hash.getVLT(AttributeName);
		//Hash.getVLT(SurfaceName);
		//Hash.getVLT(NameHash);
		BoneIndex = bb.getShort();
		RenderHierarchyIndex = bb.getShort();
		bb.position(bb.position()+24);
		var NumberOfChildren = bb.get(); //something 1 -> NumberOfChildren ? its a byte tho
		bb.position(bb.position()+6);
		ChildrenFlags = BoundFlags.get(bb.get());
		bb.position(bb.position()+12);
		updateShape();
		
		for (int i=0; i<NumberOfChildren; i++) {
			childBounds.add(new CollisionBound(bb));
		}
		
		render.addListener(e -> {
//			updateShape();
			if (CollisionsEditor.viewport != null) CollisionsEditor.viewport.viewportGroup.getChildren().remove(this.displayShape);
			if (render.getValue()) CollisionsEditor.viewport.viewportGroup.getChildren().add(this.displayShape);
			BoundCell.allCells.forEach(c -> {
				if (c.getItem() != null) c.checkBox.setSelected(c.getItem().render.get());
			});
		});
		
//		if (Shape == BoundShape.KSHAPE_BOX) render = false;
	}

	public CollisionBound(int attributeName, int nameHash, int surfaceName, ArrayList<BoundFlags> childrenFlags,
			BoundConstraintType constraintType, ArrayList<BoundFlags> flags, BoundJointType jointType, BoundNodeType nodeType, BoundShape shape, BoundType type,
			int boneIndex, float boneOffsetX, float boneOffsetY, float boneOffsetZ, float halfDimensionX,
			float halfDimensionY, float halfDimensionZ, int numberOfChildren, int renderHierarchyIndex,
			float orientationX, float orientationY, float orientationZ, float orientationW, float pivotX, float pivotY,
			float pivotZ, float positionX, float positionY, float positionZ) {
		AttributeName = attributeName;
		NameHash = nameHash;
		SurfaceName = surfaceName;
		ChildrenFlags = childrenFlags;
		ConstraintType = constraintType;
		Flags = flags;
		JointType = jointType;
		NodeType = nodeType;
		Shape = shape;
		Type = type;
		BoneIndex = boneIndex;
		BoneOffsetX = boneOffsetX;
		BoneOffsetY = boneOffsetY;
		BoneOffsetZ = boneOffsetZ;
		HalfDimensionX = halfDimensionX;
		HalfDimensionY = halfDimensionY;
		HalfDimensionZ = halfDimensionZ;
//		NumberOfChildren = (byte) numberOfChildren;
		RenderHierarchyIndex = (short) renderHierarchyIndex;
		OrientationX = orientationX;
		OrientationY = orientationY;
		OrientationZ = orientationZ;
		OrientationW = orientationW;
		PivotX = pivotX;
		PivotY = pivotY;
		PivotZ = pivotZ;
		PositionX = positionX;
		PositionY = positionY;
		PositionZ = positionZ;
		updateShape();

		render.addListener(e -> {
//			updateShape();
			if (CollisionsEditor.viewport != null) CollisionsEditor.viewport.viewportGroup.getChildren().remove(this.displayShape);
			if (render.getValue()) CollisionsEditor.viewport.viewportGroup.getChildren().add(this.displayShape);
			BoundCell.allCells.forEach(c -> {
				if (c.getItem() != null) c.checkBox.setSelected(c.getItem().render.get());
			});
		});
	}

	public CollisionBound() {
		render.addListener(e -> {
//			updateShape();
			if (CollisionsEditor.viewport != null) CollisionsEditor.viewport.viewportGroup.getChildren().remove(this.displayShape);
			if (render.getValue()) CollisionsEditor.viewport.viewportGroup.getChildren().add(this.displayShape);
			BoundCell.allCells.forEach(c -> {
				if (c.getItem() != null) c.checkBox.setSelected(c.getItem().render.get());
			});
		});
	}

	@Override
	public String toString() {
		return "- CollisionBound [AttributeName=" + AttributeName + ", NameHash=" + NameHash + ", SurfaceName="
				+ SurfaceName + ", ChildrenFlags=" + ChildrenFlags + ", Flags=" + Flags + ","
				+ "\n ConstraintType=" + ConstraintType + " JointType=" + JointType + ", NodeType=" + NodeType + ", Shape=" + Shape + ", Type=" + Type + ",\n"
				+ " BoneIndex=" + BoneIndex + ", BoneOffsetX=" + BoneOffsetX + ", BoneOffsetY=" + BoneOffsetY
				+ ", BoneOffsetZ=" + BoneOffsetZ + ", HalfDimensionX=" + HalfDimensionX + ", HalfDimensionY=" + HalfDimensionY + ", HalfDimensionZ=" + HalfDimensionZ + ","
//				+ "\n NumberOfChildren=" + NumberOfChildren	
				+ ", RenderHierarchyIndex=" + RenderHierarchyIndex + ", OrientationX=" + OrientationX
				+ ", OrientationY=" + OrientationY + ", OrientationZ=" + OrientationZ + ", OrientationW=" + OrientationW + ","
				+ "\n PivotX=" + PivotX + ", PivotY=" + PivotY + ", PivotZ=" + PivotZ + ", PositionX=" + PositionX
				+ ", PositionY=" + PositionY + ", PositionZ=" + PositionZ + "]";
	}

	public void updateShape() {

		displayShape.getChildren().clear();
		if (this.collisionShape != null) {
			switch(this.Shape) {
			case KSHAPE_BOX:
				((CollisionBoxShape) this.collisionShape).updateShape(colorR, colorG, colorB, 1.0);
				displayShape.getChildren().add(((CollisionBoxShape) this.collisionShape).displayShape);
				break;
			case KSHAPE_CYLINDER:
				break;
			case KSHAPE_INVALID:
				break;
			case KSHAPE_MESH:
				((CollisionConvexVertice) this.collisionShape).updateShape();
				displayShape.getChildren().add(((CollisionConvexVertice) this.collisionShape).displayShape);
				break;
			case KSHAPE_SPHERE:
				((CollisionSphereShape) this.collisionShape).updateShape(colorR, colorG, colorB, 1.0);
				displayShape.getChildren().add(((CollisionSphereShape) this.collisionShape).displayShape);
				break;
			case KSHAPE_TYPE_COUNT:
			default:
			}
			
			this.displayShape.setScaleX(-1);
			
			if (this.shapeTransform != null) {

				displayShape.setTranslateX(-shapeTransform.TranslationX);
				displayShape.setTranslateY(- CollisionsEditor.mainCollisions.Z + shapeTransform.TranslationY);
				displayShape.setTranslateZ(- CollisionsEditor.mainCollisions.X + shapeTransform.TranslationZ);
				
				double d = Math.acos((shapeTransform.XRotationX+shapeTransform.YRotationY+shapeTransform.ZRotationZ-1d)/2d);
			    if(d!=0d){
			        double den=2d*Math.sin(d);
			        Point3D p= new Point3D((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den,
			        		(shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den,
			        		(shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
			        displayShape.setRotationAxis(p);
			        displayShape.setRotate(Math.toDegrees(d));
			        displayBound.setRotationAxis(p);
			        displayBound.setRotate(Math.toDegrees(d));
			    }
			} else if (this.shapeTranslate != null) {
		        displayShape.setRotate(0);
		        displayBound.setRotate(0);
				displayShape.setTranslateX(-shapeTranslate.TranslationX);
				displayShape.setTranslateY(- CollisionsEditor.mainCollisions.Z + shapeTranslate.TranslationY);
				displayShape.setTranslateZ(- CollisionsEditor.mainCollisions.X + shapeTranslate.TranslationZ);
			} else {
		        displayShape.setRotate(0);
		        displayBound.setRotate(0);
				displayShape.setTranslateX(0);
				displayShape.setTranslateY(- CollisionsEditor.mainCollisions.Z); 
				displayShape.setTranslateZ(- CollisionsEditor.mainCollisions.X);
			}
		} //collisionShape is null, show the boundary only

		updateBoundAndPivot();

		var t = new Tooltip(Hash.getVLT(this.NameHash));
		
		this.displayShape.setOnMouseEntered(e -> {
//			System.out.println("CollisionBound | Name : "+Hash.getVLT(this.NameHash)+", SurfaceName : "+Hash.getVLT(this.SurfaceName)+", AttributeName : "+Hash.getVLT(this.AttributeName)
//					+"\nType : "+this.Type+", Shape : "+Shape+", Node : "+NodeType+", Constraint : "+ConstraintType+", Joint : "+JointType);			
			
			t.show(displayShape, e.getScreenX()+10, e.getScreenY()+10);
			
			displayShape.requestFocus();
		});
		displayShape.setOnMouseMoved(e -> {
			t.setAnchorX(e.getScreenX()+10);
			t.setAnchorY(e.getScreenY()+10);
		});
		displayShape.setOnMouseExited(e -> {
			t.hide();
		});
		this.displayShape.setOnMousePressed(e->{
//			if (e.isPrimaryButtonDown()) {
//				this.render = false;
//				CollisionsEditor.updateRender();
//			}
			CollisionsEditor.hierarchy.getRoot().getChildren().forEach(b -> {
				if (b.getValue() == this) {
					CollisionsEditor.hierarchy.getSelectionModel().select(b);
	        		CollisionsEditor.boundControls.setBound(this);
				}
			});
		});
		this.displayShape.setOnContextMenuRequested(e -> {
			if (this.Shape != BoundShape.KSHAPE_MESH) {
				var cm = new ContextMenu();
				addBoundToContextMenu(cm);
				cm.show(this.displayShape, e.getScreenX(), e.getScreenY());
			}
		});
		this.displayShape.setOnKeyPressed(e -> {
			switch (e.getCode()) {
			case H:
				render.set(false);
				break;
			default:
			}
		});
		this.displayPivot.setOnMousePressed(e -> {
			if (e.isPrimaryButtonDown()) {
				render.set(true);
			}
		});
	}

	public void addBoundToContextMenu(ContextMenu cm) {
		var info = new MenuItem("Bound "+Hash.getVLT(NameHash));
		var sep = new SeparatorMenuItem();
		var hide = new MenuItem(render.get() ? "Hide" : "Show");
		var delete = new MenuItem("Delete");
		var add = new MenuItem("Add");
		var duplicate = new MenuItem("Duplicate");
		hide.setOnAction(e2 -> {
			this.render.set(!render.get());
		});
		delete.setOnAction(e2 -> {
			if (parent != null) {
				parent.childBounds.remove(this);
				CollisionsEditor.updateAllBoundsDisplay();
				CollisionsEditor.updateRender();
				CollisionsEditor.planeControls.setPlane(null);
			} else {
				if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.WARNING, "Cannot do this on the main bound!", ButtonType.OK).show();
			}
		});
		add.setOnAction(evt -> {
			CollisionBound c;
			this.childBounds.add(c = new CollisionBound());
			c.parent = this;
			CollisionsEditor.updateAllBoundsDisplay();
			CollisionsEditor.updateRender();
		});
		duplicate.setOnAction(evt -> {
			
			CollisionBound dup = this.deepCopy();
			if (parent != null) parent.childBounds.add(dup);
			else this.childBounds.add(dup);

			CollisionsEditor.updateAllBoundsDisplay();
			CollisionsEditor.updateRender();
		});
		cm.getItems().addAll(info, sep, hide, add, delete, duplicate);
	}

	private CollisionBound deepCopy() {
		var b = new CollisionBound();
		
		b.Flags = new ArrayList<>();
		b.ChildrenFlags = new ArrayList<>();
		
		b.AttributeName = this.AttributeName;
		b.BoneIndex = this.BoneIndex;
		b.BoneOffsetX = this.BoneOffsetX;
		b.BoneOffsetY = this.BoneOffsetY;
		b.BoneOffsetZ = this.BoneOffsetZ;
		this.childBounds.forEach(b2 -> b.childBounds.add(b2.deepCopy()));
		this.ChildrenFlags.forEach(f -> b.ChildrenFlags.add(f)); // should work for an enum ?
		b.ConstraintType = this.ConstraintType;
		this.Flags.forEach(f -> b.Flags.add(f)); // should work for an enum ?
		b.HalfDimensionX = this.HalfDimensionX;
		b.HalfDimensionY = this.HalfDimensionY;
		b.HalfDimensionZ = this.HalfDimensionZ;
		b.JointType = this.JointType;
		b.NameHash = this.NameHash;
		b.NodeType = this.NodeType;
		b.OrientationX = this.OrientationX;
		b.OrientationY = this.OrientationY;
		b.OrientationZ = this.OrientationZ;
		b.OrientationW = this.OrientationW;
		b.parent = this.parent;
		b.PositionX = this.PositionX;
		b.PositionY = this.PositionY;
		b.PositionZ = this.PositionZ;
		b.PivotX = this.PivotX;
		b.PivotY = this.PivotY;
		b.PivotZ = this.PivotZ;
		b.RenderHierarchyIndex = this.RenderHierarchyIndex;
		b.Shape = this.Shape;
		b.SurfaceName = this.SurfaceName;
		b.Type = this.Type;
		
		switch (b.Shape) {
		case KSHAPE_BOX:
			b.collisionShape = collisionShape.deepCopy(b);
			break;
		case KSHAPE_MESH:
			b.collisionShape = collisionShape.deepCopy(b);
			break;
		case KSHAPE_SPHERE:
			b.collisionShape = collisionShape.deepCopy(b);
			break;
		default:
			break;
		}

		if (this.shapeTransform != null) b.shapeTransform = this.shapeTransform.deepCopy();
		if (this.shapeTranslate != null) b.shapeTranslate = this.shapeTranslate.deepCopy();
		
		b.updateShape();
		b.updateBoundAndPivot();
		return b;
	}

	public void updateBoundAndPivot() {
		
		if (collisionShape != null) switch(Shape) {
		case KSHAPE_BOX:
			HalfDimensionX = ((CollisionBoxShape)collisionShape).HalfExtentsX + ((CollisionBoxShape)collisionShape).boundDistance;
			HalfDimensionY = ((CollisionBoxShape)collisionShape).HalfExtentsY + ((CollisionBoxShape)collisionShape).boundDistance;
			HalfDimensionZ = ((CollisionBoxShape)collisionShape).HalfExtentsZ + ((CollisionBoxShape)collisionShape).boundDistance;

			// ! transforms
			
			break;
			
		case KSHAPE_MESH:
			HalfDimensionX = ((CollisionConvexVertice)collisionShape).HalfExtentsX; // + boundDistance
			HalfDimensionY = ((CollisionConvexVertice)collisionShape).HalfExtentsY;
			HalfDimensionZ = ((CollisionConvexVertice)collisionShape).HalfExtentsZ;
			PositionX = ((CollisionConvexVertice)collisionShape).CenterX;
			PositionY = ((CollisionConvexVertice)collisionShape).CenterY;
			PositionZ = ((CollisionConvexVertice)collisionShape).CenterZ;
			PivotX = ((CollisionConvexVertice)collisionShape).CenterX;
			PivotY = ((CollisionConvexVertice)collisionShape).CenterY - CollisionsEditor.mainCollisions.Z;
			PivotZ = ((CollisionConvexVertice)collisionShape).CenterZ - CollisionsEditor.mainCollisions.X;
			break;
		case KSHAPE_SPHERE:
			HalfDimensionX = ((CollisionSphereShape)collisionShape).radius;
			HalfDimensionY = ((CollisionSphereShape)collisionShape).radius;
			HalfDimensionZ = ((CollisionSphereShape)collisionShape).radius;

			// ! transforms
			
			break;
		default:
		}
		
		if ((Shape == BoundShape.KSHAPE_BOX || Shape == BoundShape.KSHAPE_SPHERE) && this.collisionShape != null) {
			if (shapeTransform != null) {
				PositionX = shapeTransform.TranslationX;
				PositionY = shapeTransform.TranslationY;
				PositionZ = shapeTransform.TranslationZ;

				PivotX = shapeTransform.TranslationX;
				PivotY = shapeTransform.TranslationY - CollisionsEditor.mainCollisions.Z;
				PivotZ = shapeTransform.TranslationZ - CollisionsEditor.mainCollisions.X;
				
				double d = Math.acos((shapeTransform.XRotationX+shapeTransform.YRotationY+shapeTransform.ZRotationZ-1d)/2d);
			    if(d!=0d){
			        double den=2d*Math.sin(d);
			        Point3D p= new Point3D((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den,
			        		(shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den,
			        		(shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
			        displayShape.setRotationAxis(p);
			        displayShape.setRotate(Math.toDegrees(d));
			        

			        // d = 2*sqrt(x²+y²+z²)
			        // d² = 4x² + 4y² + 4z²
			        // x² = d²/4 - y² - z²
			        
			        var x = ((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den);
			        var y = ((shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den);
			        var z = ((shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
			        OrientationX = (float) (x*d/2.0);
			        OrientationY = (float) (y*d/2.0);
			        OrientationZ = (float) (z*d/2.0);
			        OrientationW = 1.0f;

			        
			        Point3D p2 = new Point3D(OrientationX, OrientationY, OrientationZ);

			        displayBound.setRotationAxis(p2);
			        displayBound.setRotate(Math.toDegrees(2.0 * Math.sqrt(OrientationX*OrientationX + OrientationY*OrientationY + OrientationZ*OrientationZ)));
			    }
				
			} else if (shapeTranslate != null) {
				PositionX = shapeTranslate.TranslationX;
				PositionY = shapeTranslate.TranslationY;
				PositionZ = shapeTranslate.TranslationZ;
				
				PivotX = shapeTranslate.TranslationX;
				PivotY = shapeTranslate.TranslationY - CollisionsEditor.mainCollisions.Z;
				PivotZ = shapeTranslate.TranslationZ - CollisionsEditor.mainCollisions.X;
			} 
			else {

				PositionX = 0;
				PositionY = 0;
				PositionZ = 0;
				
				PivotX = 0;
				PivotY = - CollisionsEditor.mainCollisions.Z;
				PivotZ = - CollisionsEditor.mainCollisions.X;
			}
			
			
		}
		
		displayBound.setFocusTraversable(true);
		// THIS IS THE BOUNDARY OF THE BOUND how ironic
		this.displayBound.setScaleX(-getScaleXFromParent());
		this.displayBound.setScaleY(getScaleYFromParent());
		this.displayBound.setScaleZ(getScaleZFromParent());
		this.displayBound.setTranslateX(-getPositionXFromParent());
		this.displayBound.setTranslateY(getPositionYFromParent()); 
		this.displayBound.setTranslateZ(getPositionZFromParent());
		
			
		this.displayPivot.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, 1)));
		this.displayPivot.setTranslateX(-PivotX);
		this.displayPivot.setTranslateY(PivotY);
		this.displayPivot.setTranslateZ(PivotZ);
		this.displayPivot.setViewOrder(0);
	}
	
	public float getScaleXFromParent() {
//		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleXFromParent()*HalfDimensionX*2;
		return HalfDimensionX*2;
	}
	public float getScaleYFromParent() {
//		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleYFromParent()*HalfDimensionY*2;
		return HalfDimensionY*2;
	}
	public float getScaleZFromParent() {
//		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getScaleZFromParent()*HalfDimensionZ*2;
		return HalfDimensionZ*2;
	}

	public float getPositionXFromParent() {
		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionXFromParent()+PositionX;
		return PositionX;
	}
	public float getPositionYFromParent() {
		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionYFromParent()+PositionY;
		return PositionY - CollisionsEditor.mainCollisions.Z;
	}
	public float getPositionZFromParent() {
		if (parent != null && parent.Type != BoundType.KBOUNDS_INVALID) return parent.getPositionZFromParent()+PositionZ;
		return PositionZ - CollisionsEditor.mainCollisions.X;
	}
	
	
	public ArrayList<CollisionBound> getChildrenRecursively() {
		ArrayList<CollisionBound> list = new ArrayList<>();
		list.add(this);
		for (var c : this.childBounds) {
			c.getChildrenRecursively(list);
			c.parent = this;
		}
		return list;
	}
	
	public void getChildrenRecursively(ArrayList<CollisionBound> list) {
		list.add(this);
		for (var c : this.childBounds) {
			c.getChildrenRecursively(list);
			c.parent = this;
		}
	}
	
	public void renderSetRecursively(boolean b) {
		this.render.setValue(b);
		for (var c : this.childBounds) c.render.setValue(b);
	}

	public byte[] saveHierarchy() throws IOException {

		var bb = ByteBuffer.wrap(new byte[144]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(OrientationX);
		bb.putFloat(OrientationY);
		bb.putFloat(OrientationZ);
		bb.putFloat(OrientationW);
		bb.putFloat(PositionX);
		bb.putFloat(PositionY);
		bb.putFloat(PositionZ);
		bb.putInt(0); //PositionW
		bb.putFloat(HalfDimensionX);
		bb.putFloat(HalfDimensionY);
		bb.putFloat(HalfDimensionZ);
		bb.putInt(0); //HalfDimensionW
		bb.putFloat(PivotX);
		bb.putFloat(PivotY);
		bb.putFloat(PivotZ);
		bb.putInt(0);
		bb.putFloat(BoneOffsetX);
		bb.putFloat(BoneOffsetY);
		bb.putFloat(BoneOffsetZ);
		bb.putInt(0); //BoneOffsetW
		// byte 80
		bb.put(Type.getValue()); //BoundType
		bb.put((byte) 0); //BoundSubType
		bb.put(Shape.getValue());
		bb.put(BoundFlags.getFlags(Flags));
		bb.putInt(AttributeName);
		bb.putInt(SurfaceName);
		bb.putInt(NameHash);
		bb.putShort((short) BoneIndex);
		bb.putShort((short) RenderHierarchyIndex);
		// byte 100
		bb.put(new byte[24]);
		bb.put((byte) childBounds.size()); //NumberOfChildren
		bb.put(new byte[3]);
		bb.put((byte) childBounds.size()); 
		bb.put(new byte[2]);
		bb.put(BoundFlags.getFlags(ChildrenFlags));
		bb.put(new byte[12]);

		if (this.childBounds.size() == 0) return bb.array();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(bb.array());
		bb = null;
		for (var c : childBounds) bos.write(c.saveHierarchy());		
		return bos.toByteArray();
	}
	
	public TreeItem<CollisionBound> makeTreeViewRecursively() {
		TreeItem<CollisionBound> thisBound = new TreeItem<>(this);

		for (var c : this.childBounds) thisBound.getChildren().add(c.makeTreeViewRecursively());
		// return the item for this collision bound
		return thisBound;
	}
	
 	public enum BoundFlags {
	    KFLAG_NONE((byte) 0, "KFLAG_NONE"),
	    KFLAG_DISABLED((byte) 1, "KFLAG_DISABLED"),
	    KFLAG_INTERNAL((byte) 2, "KFLAG_INTERNAL"),
	    KFLAG_JOINT_INVERT((byte) 4, "KFLAG_JOINT_INVERT"),
	    KFLAG_NO_COLLISION_GROUND((byte) 32, "KFLAG_NO_COLLISION_GROUND"),
	    KFLAG_NO_COLLISION_BARRIER((byte) 64, "KFLAG_NO_COLLISION_BARRIER"),
	    KFLAG_NO_COLLISION_OBJECT((byte) 128, "KFLAG_NO_COLLISION_OBJECT");
//	    KFLAG_NO_COLLISION_MASK((byte) 224, "KFLAG_NO_COLLISION_MASK");

	    private final byte value;
	    private final String name;

	    private BoundFlags(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

		public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static ArrayList<BoundFlags> get(byte val) {
	    	int value = Byte.toUnsignedInt(val);
	    	var list = new ArrayList<BoundFlags>();
//	    	if (value == 224) {
//	    		list.add(KFLAG_NO_COLLISION_MASK);
//	    		return list;
//	    	}
	        for (BoundFlags s : values()) {
	            if ((s.value & value) != 0) list.add(s);
	        }
	        if (list.isEmpty()) list.add(KFLAG_NONE);
	        return list; // Handle invalid value
	    }

	    static byte getFlags(ArrayList<BoundFlags> flags) {
	    	int ret = 0;
	    	for (var f : flags) ret += Byte.toUnsignedInt(f.getValue());
			return (byte) ret;
		}

	    public static BoundFlags get(String name) {
	        for (BoundFlags s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KFLAG_NONE; // Handle invalid name
	    }
	}

	public enum BoundType {
	    KBOUNDS_INVALID((byte) 0, "KBOUNDS_INVALID"),
	    KBOUNDS_NODE((byte) 1, "KBOUNDS_NODE"),
	    KBOUNDS_GEOMETRY((byte) 2, "KBOUNDS_GEOMETRY"),
	    KBOUNDS_JOINT((byte) 3, "KBOUNDS_JOINT"),
	    KBOUNDS_CONSTRAINT((byte) 4, "KBOUNDS_CONSTRAINT"),
	    KBOUNDS_EMITTER((byte) 5, "KBOUNDS_EMITTER"),
	    KBOUNDS_TYPE_COUNT((byte) 6, "KBOUNDS_TYPE_COUNT");

	    private final byte value;
	    private final String name;

	    private BoundType(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static BoundType get(byte value) {
	        for (BoundType s : values()) {
	            if (s.value == value) return s;
	        }
	        return KBOUNDS_INVALID; // Handle invalid value
	    }

	    public static BoundType get(String name) {
	        for (BoundType s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KBOUNDS_INVALID; // Handle invalid name
	    }
	}

	public enum BoundShape {
	    KSHAPE_INVALID((byte) 0, "KSHAPE_INVALID"),
	    KSHAPE_BOX((byte) 1, "KSHAPE_BOX"),
	    KSHAPE_SPHERE((byte) 2, "KSHAPE_SPHERE"),
	    KSHAPE_CYLINDER((byte) 3, "KSHAPE_CYLINDER"),
	    KSHAPE_MESH((byte) 4, "KSHAPE_MESH"),
	    KSHAPE_TYPE_COUNT((byte) 5, "KSHAPE_TYPE_COUNT");

	    private final byte value;
	    private final String name;

	    private BoundShape(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static BoundShape get(byte value) {
	        for (BoundShape s : values()) {
	            if (s.value == value) return s;
	        }
	        return KSHAPE_INVALID; // Handle invalid value
	    }

	    public static BoundShape get(String name) {
	        for (BoundShape s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KSHAPE_INVALID; // Handle invalid name
	    }
	}

	public enum BoundJointType {
	    KBOUNDS_JOINT_INVALID((byte) 0, "KBOUNDS_JOINT_INVALID"),
	    KBOUNDS_CONSTRAINT_CONICAL((byte) 1, "KBOUNDS_CONSTRAINT_CONICAL"),
	    KBOUNDS_CONSTRAINT_PRISMATIC((byte) 2, "KBOUNDS_CONSTRAINT_PRISMATIC"),
	    KBOUNDS_JOINT_FEMALE((byte) 3, "KBOUNDS_JOINT_FEMALE"),
	    KBOUNDS_JOINT_MALE((byte) 4, "KBOUNDS_JOINT_MALE"),
	    KBOUNDS_MALE_POST((byte) 5, "KBOUNDS_MALE_POST"),
	    KBOUNDS_JOINT_SUBTYPE_COUNT((byte) 6, "KBOUNDS_JOINT_SUBTYPE_COUNT");

	    private final byte value;
	    private final String name;

	    private BoundJointType(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static BoundJointType get(byte value) {
	        for (BoundJointType s : values()) {
	            if (s.value == value) return s;
	        }
	        return KBOUNDS_JOINT_INVALID; // Handle invalid value
	    }

	    public static BoundJointType get(String name) {
	        for (BoundJointType s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KBOUNDS_JOINT_INVALID; // Handle invalid name
	    }
	}

	public enum BoundNodeType {
	    KBOUNDS_NODE_INVALID((byte) 0, "KBOUNDS_NODE_INVALID"),
	    KBOUNDS_NODE_HEIRARCHY((byte) 1, "KBOUNDS_NODE_HEIRARCHY"),
	    KBOUNDS_NODE_FRAGMENT((byte) 2, "KBOUNDS_NODE_FRAGMENT"),
	    KBOUNDS_NODE_SUBTYPE_COUNT((byte) 3, "KBOUNDS_NODE_SUBTYPE_COUNT");

	    private final byte value;
	    private final String name;

	    private BoundNodeType(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static BoundNodeType get(byte value) {
	        for (BoundNodeType s : values()) {
	            if (s.value == value) return s;
	        }
	        return KBOUNDS_NODE_INVALID; // Handle invalid value
	    }

	    public static BoundNodeType get(String name) {
	        for (BoundNodeType s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KBOUNDS_NODE_INVALID; // Handle invalid name
	    }
	}

	public enum BoundConstraintType {
	    KBOUNDS_CONSTRAINT_INVALID((byte) 0, "KBOUNDS_CONSTRAINT_INVALID"),
	    KBOUNDS_CONSTRAINT_HINGE((byte) 1, "KBOUNDS_CONSTRAINT_HINGE"),
	    KBOUNDS_CONSTRAINT_BALL_SOCKET((byte) 2, "KBOUNDS_CONSTRAINT_BALL_SOCKET"),
	    KBOUNDS_CONSTRAINT_SUBTYPE_COUNT((byte) 3, "KBOUNDS_CONSTRAINT_SUBTYPE_COUNT");

	    private final byte value;
	    private final String name;

	    private BoundConstraintType(byte value, String name) {
	        this.value = value;
	        this.name = name;
	    }

	    public byte getValue() {
	        return value;
	    }

	    public String getName() {
	        return name;
	    }

	    public static BoundConstraintType get(byte value) {
	        for (BoundConstraintType s : values()) {
	            if (s.value == value) return s;
	        }
	        return KBOUNDS_CONSTRAINT_INVALID; // Handle invalid value
	    }

	    public static BoundConstraintType get(String name) {
	        for (BoundConstraintType s : values()) {
	            if (s.name.equals(name)) return s;
	        }
	        return KBOUNDS_CONSTRAINT_INVALID; // Handle invalid name
	    }
	}
}
