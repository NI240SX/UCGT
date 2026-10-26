package fr.ni240sx.ucgt.collisionsEditor;

import java.util.ArrayList;
import fr.ni240sx.ucgt.collisionsEditor.BoundEnums.*;
import fr.ni240sx.ucgt.collisionsEditor.Collisions.*;
import fr.ni240sx.ucgt.shared.Gizmo;
import fr.ni240sx.ucgt.shared.Hash;
import javafx.beans.InvalidationListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BoundControls extends GridPane {
	
	private scgBounds bound = null;
	
	private Label key = new Label();
	private CaretKeeperTextField name = new CaretKeeperTextField();
	private ComboBox<BoundType> boundType = new ComboBox<>();
	private VBox flags = new VBox();
	private ComboBox<BoundShape> shape = new ComboBox<>();

	private CaretKeeperTextField surfaceName = new CaretKeeperTextField();
	private CaretKeeperTextField attributeName = new CaretKeeperTextField();
//	private CaretKeeperTextField childrenFlags = new CaretKeeperTextField();
	
//	private ComboBox<BoundConstraintType> constraintType = new ComboBox<>();
	private ComboBox<BoundJointType> jointType = new ComboBox<>();
//	private ComboBox<BoundNodeType> nodeType = new ComboBox<>();

	private CaretKeeperTextField boneIndex = new CaretKeeperTextField();
	private CaretKeeperTextField renderIndex = new CaretKeeperTextField();
	
	private CaretKeeperTextField posX = new CaretKeeperTextField();
	private CaretKeeperTextField posY = new CaretKeeperTextField();
	private CaretKeeperTextField posZ = new CaretKeeperTextField();

	private CaretKeeperTextField orX = new CaretKeeperTextField();
	private CaretKeeperTextField orY = new CaretKeeperTextField();
	private CaretKeeperTextField orZ = new CaretKeeperTextField();
	private CaretKeeperTextField orW = new CaretKeeperTextField();

	private CaretKeeperTextField dimX = new CaretKeeperTextField();
	private CaretKeeperTextField dimY = new CaretKeeperTextField();
	private CaretKeeperTextField dimZ = new CaretKeeperTextField();

	private CaretKeeperTextField pivotX = new CaretKeeperTextField();
	private CaretKeeperTextField pivotY = new CaretKeeperTextField();
	private CaretKeeperTextField pivotZ = new CaretKeeperTextField();

	private CaretKeeperTextField boneOffX = new CaretKeeperTextField();
	private CaretKeeperTextField boneOffY = new CaretKeeperTextField();
	private CaretKeeperTextField boneOffZ = new CaretKeeperTextField();
	
	private CaretKeeperTextField boxExtX = new CaretKeeperTextField();
	private CaretKeeperTextField boxExtY = new CaretKeeperTextField();
	private CaretKeeperTextField boxExtZ = new CaretKeeperTextField();
	private CaretKeeperTextField boxExtW = new CaretKeeperTextField();

	private CaretKeeperTextField sphereRadius = new CaretKeeperTextField();
	
	private CaretKeeperTextField translateX = new CaretKeeperTextField();
	private CaretKeeperTextField translateY = new CaretKeeperTextField();
	private CaretKeeperTextField translateZ = new CaretKeeperTextField();
	private CaretKeeperTextField translateW = new CaretKeeperTextField();
	
	private CaretKeeperTextField XRotateX = new CaretKeeperTextField();
	private CaretKeeperTextField XRotateY = new CaretKeeperTextField();
	private CaretKeeperTextField XRotateZ = new CaretKeeperTextField();
//	private CaretKeeperTextField XRotateW = new CaretKeeperTextField();
	
//	private CaretKeeperTextField YRotateX = new CaretKeeperTextField();
//	private CaretKeeperTextField YRotateY = new CaretKeeperTextField();
//	private CaretKeeperTextField YRotateZ = new CaretKeeperTextField();
//	private CaretKeeperTextField YRotateW = new CaretKeeperTextField();
	
//	private CaretKeeperTextField ZRotateX = new CaretKeeperTextField();
//	private CaretKeeperTextField ZRotateY = new CaretKeeperTextField();
//	private CaretKeeperTextField ZRotateZ = new CaretKeeperTextField();
//	private CaretKeeperTextField ZRotateW = new CaretKeeperTextField();

	private CaretKeeperTextField renderNodeX = new CaretKeeperTextField();
	private CaretKeeperTextField renderNodeY = new CaretKeeperTextField();
	private CaretKeeperTextField renderNodeZ = new CaretKeeperTextField();
	private CaretKeeperTextField renderNodeW = new CaretKeeperTextField();
	
	private Gizmo boundGizmo;
//	public WeakInvalidationListener gizmoChangeListener = new WeakInvalidationListener(o -> updateBoundFromGizmo());
	public InvalidationListener gizmoChangeListener = o -> updateBoundFromGizmo();
	
	public BoundControls() {
		
		boundType.getItems().addAll(BoundType.values());
		shape.getItems().addAll(BoundShape.values());
//		constraintType.getItems().addAll(BoundConstraintType.values());
		jointType.getItems().addAll(BoundJointType.values());
//		nodeType.getItems().addAll(BoundNodeType.values());

		for (int i=1; i<BoundFlags.values().length; i++) {
			flags.getChildren().add(new CheckBox(BoundFlags.values()[i].getName()));
		}
		
		this.setOnKeyReleased(e -> {
//			if (e.getCode() == KeyCode.ENTER)
			{
				updateBound();
				update();
			}
		});
		
//		boundType.setOnAction(e -> {
//			updateBound();
//			update();
//		});
//		shape.setOnAction(boundType.getOnAction());
//		constraintType.setOnAction(boundType.getOnAction());
//		jointType.setOnAction(boundType.getOnAction());
//		nodeType.setOnAction(boundType.getOnAction());
		
		rebuild();
	}
	
	public void updateBound() {
		if (bound != null) {
			try {
				bound.NameHash = Hash.findVLT(name.getText().strip());
				bound.Type = boundType.getValue();
				
				if (bound.Shape != shape.getValue()) {
//					if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.ERROR, "Shape type change not supported.", ButtonType.OK).show();					
					bound.Shape = shape.getValue();
					switch (shape.getValue()) {
					case KSHAPE_BOX:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpBoxShape(bound); //(bound)
						break;
					case KSHAPE_MESH:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexVerticesShape(bound); //(bound)
						break;
					case KSHAPE_SPHERE:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpSphereShape(bound); //(bound)
						break;
//					default:
//						bound.collisionShape = null;
//						break;
					case KSHAPE_CYLINDER:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpCylinderShape(bound); //(bound)
						break;
					case KSHAPE_INVALID:
					case KSHAPE_TYPE_COUNT:
						break;
					}
					//FIXME trigger ui change for shape change
					
				}
				
				try{ 
	//			bound.Shape = shape.getValue();
				bound.Flags = applyFlags();
				
				bound.SurfaceName = Hash.findVLT(surfaceName.getText().strip());
				bound.AttributeName = Hash.findVLT(attributeName.getText().strip());
//				bound.ChildrenFlags = flagsFromText(childrenFlags.getText());
	
//				bound.ConstraintType = constraintType.getValue();
				bound.JointType = jointType.getValue();
//				bound.NodeType = nodeType.getValue();
				
				bound.BoneIndex = Integer.parseInt(boneIndex.getText().strip());
				bound.RenderHierarchyIndex = (short) Integer.parseInt(renderIndex.getText().strip());

				hkpShape colShape = bound.getShape();
				hkpShape colTransform = bound.getTransform();

				if (colShape != null) switch (bound.Shape) {
				case KSHAPE_BOX:
					((hkpBoxShape)colShape).HalfExtentsX = Float.parseFloat(boxExtX.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsY = Float.parseFloat(boxExtY.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsZ = Float.parseFloat(boxExtZ.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsW = Float.parseFloat(boxExtW.getText().strip());
					break;
				case KSHAPE_SPHERE:
					((hkpSphereShape)colShape).radius = Float.parseFloat(sphereRadius.getText().strip());
					break;
				case KSHAPE_CYLINDER:
					((hkpCylinderShape)colShape).radius = Float.parseFloat(sphereRadius.getText().strip());
					break;
				case KSHAPE_MESH:
					break;
				case KSHAPE_INVALID:
				case KSHAPE_TYPE_COUNT:
					bound.PositionX = Float.parseFloat(posX.getText().strip());
					bound.PositionY = Float.parseFloat(posY.getText().strip());
					bound.PositionZ = Float.parseFloat(posZ.getText().strip());
		
					bound.OrientationX = Float.parseFloat(orX.getText().strip());
					bound.OrientationY = Float.parseFloat(orY.getText().strip());
					bound.OrientationZ = Float.parseFloat(orZ.getText().strip());
					bound.OrientationW = Float.parseFloat(orW.getText().strip());
					
					bound.HalfDimensionX = Float.parseFloat(dimX.getText().strip());
					bound.HalfDimensionY = Float.parseFloat(dimY.getText().strip());
					bound.HalfDimensionZ = Float.parseFloat(dimZ.getText().strip());
					
					bound.PivotX = Float.parseFloat(pivotX.getText().strip());
					bound.PivotY = Float.parseFloat(pivotY.getText().strip());
					bound.PivotZ = Float.parseFloat(pivotZ.getText().strip());
		
					bound.BoneOffsetX = Float.parseFloat(boneOffX.getText().strip());
					bound.BoneOffsetY = Float.parseFloat(boneOffY.getText().strip());
					bound.BoneOffsetZ = Float.parseFloat(boneOffZ.getText().strip());
					
				}
				
//				if (bound.Shape == BoundShape.KSHAPE_BOX || bound.Shape == BoundShape.KSHAPE_SPHERE) {
					
					if (!(XRotateX.getText().isBlank() || XRotateY.getText().isBlank() || XRotateZ.getText().isBlank() 
//							|| YRotateX.getText().isBlank() || YRotateY.getText().isBlank() || YRotateZ.getText().isBlank() || 
//							ZRotateX.getText().isBlank() || ZRotateY.getText().isBlank() || ZRotateZ.getText().isBlank()
							)
							&&
							!(doubleEquals(Float.parseFloat(XRotateX.getText().strip()),0) || 
									doubleEquals(Float.parseFloat(XRotateY.getText().strip()),0) ||
									doubleEquals(Float.parseFloat(XRotateZ.getText().strip()),0))) {
						// need transform
						if (colTransform == null || colTransform.getClass() != hkpConvexTransformShape.class ) {
							if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
								var s = (hkpConvexTranslateShape)colTransform;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTransformShape();
								((hkpConvexTransformShape)bound.collisionShape).childShape = s.childShape;
							} else {
								var s = bound.collisionShape;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTransformShape();
								((hkpConvexTransformShape)bound.collisionShape).childShape = s;
							}
						}
						var shapeTransform = ((hkpConvexTransformShape)bound.collisionShape);
	
						if (!translateX.getText().isBlank()) shapeTransform.TranslationX = Float.parseFloat(translateX.getText().strip());
						if (!translateY.getText().isBlank()) shapeTransform.TranslationY = Float.parseFloat(translateY.getText().strip());
						if (!translateZ.getText().isBlank()) shapeTransform.TranslationZ = Float.parseFloat(translateZ.getText().strip());
						if (!translateW.getText().isBlank()) shapeTransform.TranslationW = Float.parseFloat(translateW.getText().strip());

						shapeTransform.fromEulerAngles(Float.parseFloat(XRotateX.getText().strip()), Float.parseFloat(XRotateY.getText().strip()), Float.parseFloat(XRotateZ.getText().strip()));

//						shapeTransform.XRotationX = Float.parseFloat(XRotateX.getText().strip());
//						shapeTransform.XRotationY = Float.parseFloat(XRotateY.getText().strip());
//						shapeTransform.XRotationZ = Float.parseFloat(XRotateZ.getText().strip());
//						shapeTransform.XRotationW = Float.parseFloat(XRotateW.getText().strip());
//						shapeTransform.YRotationX = Float.parseFloat(YRotateX.getText().strip());
//						shapeTransform.YRotationY = Float.parseFloat(YRotateY.getText().strip());
//						shapeTransform.YRotationZ = Float.parseFloat(YRotateZ.getText().strip());
//						shapeTransform.YRotationW = Float.parseFloat(YRotateW.getText().strip());
//						shapeTransform.ZRotationX = Float.parseFloat(ZRotateX.getText().strip());
//						shapeTransform.ZRotationY = Float.parseFloat(ZRotateY.getText().strip());
//						shapeTransform.ZRotationZ = Float.parseFloat(ZRotateZ.getText().strip());
//						shapeTransform.ZRotationW = Float.parseFloat(ZRotateW.getText().strip());
						
					} else if (!(translateX.getText().isBlank() || translateY.getText().isBlank() || translateZ.getText().isBlank())
							&&
							!(doubleEquals(Float.parseFloat(translateX.getText().strip()),0) || 
									doubleEquals(Float.parseFloat(translateY.getText().strip()),0) ||
									doubleEquals(Float.parseFloat(translateZ.getText().strip()),0))) {
						//need translate
						if (colTransform == null || colTransform.getClass() != hkpConvexTranslateShape.class ) {
							if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class ) {
								var s = (hkpConvexTransformShape)colTransform;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)bound.collisionShape).childShape = s.childShape;
							} else {
								var s = bound.collisionShape;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)bound.collisionShape).childShape = s;
							}
						}
						var shapeTranslate = ((hkpConvexTranslateShape)bound.collisionShape);
	
						shapeTranslate.TranslationX = Float.parseFloat(translateX.getText().strip());
						shapeTranslate.TranslationY = Float.parseFloat(translateY.getText().strip());
						shapeTranslate.TranslationZ = Float.parseFloat(translateZ.getText().strip());
						shapeTranslate.TranslationW = Float.parseFloat(translateW.getText().strip());
						
					} else {
						//no transform or translate
						if (bound.collisionShape != null) {
							
						}

						bound.displayPivot.setTranslateX(bound.getRenderNode().Y);
						bound.displayPivot.setTranslateY(- bound.getRenderNode().Z); 
						bound.displayPivot.setTranslateZ(- bound.getRenderNode().X);
						bound.displayBound.setTranslateX(bound.getRenderNode().Y);
						bound.displayBound.setTranslateY(- bound.getRenderNode().Z); 
						bound.displayBound.setTranslateZ(- bound.getRenderNode().X);
					}
					
					if (bound.scgRenderNode != null) {
						bound.scgRenderNode.X = Float.parseFloat(renderNodeX.getText().strip());
						bound.scgRenderNode.Y = Float.parseFloat(renderNodeY.getText().strip());
						bound.scgRenderNode.Z = Float.parseFloat(renderNodeZ.getText().strip());
						bound.scgRenderNode.W = Float.parseFloat(renderNodeW.getText().strip());
					}
//				}
				} catch (@SuppressWarnings("unused") Exception shh) {} //fail to parse silently

			} catch (Exception e) {
				e.printStackTrace();
				if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.ERROR, "Error refreshing bound:\n"+e.getMessage(), ButtonType.OK).show();
				System.out.println("Error refreshing bound "+Hash.getVLT(bound.NameHash));
			}

			bound.compute();
			bound.updateShape();
		}
		BoundCell.allCells.forEach(c -> c.update(c.getItem(), false));
	}
	
	public void rebuild() {
		try {	
			this.getChildren().clear();
			
			var l = new Label("Attrib key");
			l.setMinWidth(90);
			this.add(l, 0, 0);
			this.add(key, 1, 0);
			this.add(new Label("Name"), 0, 1);
			this.add(name, 1, 1);
			this.add(new Label("Type"), 0, 2);
			this.add(boundType, 1, 2);
			this.add(new Label("Shape"), 0, 3);
			this.add(shape, 1, 3);
			this.add(new Label("Flags"), 0, 4);
			this.add(flags, 1, 4);
	
			this.add(new Separator(), 0, 9);
			this.add(new Separator(), 1, 9);
			
			this.add(new Label("Surface name"), 0, 10);
			this.add(surfaceName, 1, 10);
			this.add(new Label("Attribute name"), 0, 11);
			this.add(attributeName, 1, 11);
	//		this.add(new Label("Children flags"), 0, 12);
	//		this.add(childrenFlags, 1, 12);
	
			this.add(new Separator(), 0, 19);
			this.add(new Separator(), 1, 19);
			
//			Label l;
//			this.add(l = new Label("Constraint type"), 0, 20);
//			l.setMinWidth(90);
//			this.add(constraintType, 1, 20);
			this.add(new Label("Joint type"), 0, 21);
			this.add(jointType, 1, 21);
//			this.add(new Label("Node type"), 0, 22);
//			this.add(nodeType, 1, 22);
	
			this.add(new Separator(), 0, 29);
			this.add(new Separator(), 1, 29);
			
			this.add(new Label("Bone index"), 0, 30);
			this.add(boneIndex, 1, 30);
			this.add(new Label("Render index"), 0, 31);
			this.add(renderIndex, 1, 31);
	
			this.add(new Separator(), 0, 39);
			this.add(new Separator(), 1, 39);
			
			if (bound != null) {
				if (boundGizmo != null && CollisionsEditor.viewportGroup.getChildren().contains(boundGizmo)) CollisionsEditor.viewportGroup.getChildren().remove(boundGizmo);
				//band aid fix because this thing does not want to cooperate
				if (boundGizmo != null) {
					boundGizmo.PositionX.removeListener(gizmoChangeListener);
					boundGizmo.PositionY.removeListener(gizmoChangeListener);
					boundGizmo.PositionZ.removeListener(gizmoChangeListener);
					boundGizmo.RotationX.removeListener(gizmoChangeListener);
					boundGizmo.RotationY.removeListener(gizmoChangeListener);
					boundGizmo.RotationZ.removeListener(gizmoChangeListener);
					boundGizmo.ScaleX.removeListener(gizmoChangeListener);
					boundGizmo.ScaleY.removeListener(gizmoChangeListener);
					boundGizmo.ScaleZ.removeListener(gizmoChangeListener);
				}
				//if (boundGizmo == null) {
					boundGizmo = new Gizmo(1, CollisionsEditor.viewport);
					boundGizmo.PositionX.addListener(gizmoChangeListener);
					boundGizmo.PositionY.addListener(gizmoChangeListener);
					boundGizmo.PositionZ.addListener(gizmoChangeListener);
					boundGizmo.RotationX.addListener(gizmoChangeListener);
					boundGizmo.RotationY.addListener(gizmoChangeListener);
					boundGizmo.RotationZ.addListener(gizmoChangeListener);
					boundGizmo.ScaleX.addListener(gizmoChangeListener);
					boundGizmo.ScaleY.addListener(gizmoChangeListener);
					boundGizmo.ScaleZ.addListener(gizmoChangeListener);
				//}
				if (!CollisionsEditor.viewportGroup.getChildren().contains(boundGizmo)) CollisionsEditor.viewportGroup.getChildren().add(boundGizmo);

				rebuildTextFields();

				updateGizmo();
				
				
			} else {
				name.setText("No bound selected");
				
				this.setDisable(true);
				
				if (boundGizmo != null && CollisionsEditor.viewportGroup.getChildren().contains(boundGizmo)) CollisionsEditor.viewportGroup.getChildren().remove(boundGizmo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rebuildTextFields() {
		key.setText("0x%08X".formatted(bound.NameHash));
		name.setTextKeepCaret(Hash.getVLT(bound.NameHash));
		boundType.setValue(bound.Type);
		shape.setValue(bound.Shape);
//				flags.setTextKeepCaret(flagsAsText(bound.Flags));
		readFlags(bound);
		
		surfaceName.setTextKeepCaret(Hash.getVLT(bound.SurfaceName));
		attributeName.setTextKeepCaret(Hash.getVLT(bound.AttributeName));
//			childrenFlags.setTextKeepCaret(flagsAsText(bound.ChildrenFlags));
		
//		constraintType.setValue(bound.ConstraintType);
		jointType.setValue(bound.JointType);
//		nodeType.setValue(bound.NodeType);

		boneIndex.setTextKeepCaret(bound.BoneIndex+"");
		renderIndex.setTextKeepCaret(bound.RenderHierarchyIndex+"");

		rebuildNumericTextFields();
		
		if (bound.scgRenderNode != null) {
			this.add(new Separator(), 0, 59);
			this.add(new Separator(), 1, 59);
			this.add(new Label("Render node"), 0, 60);
			var h = new HBox();
			h.getChildren().addAll(renderNodeX, renderNodeY, renderNodeZ, renderNodeW);
			this.add(h, 1, 60);
			renderNodeX.setTextKeepCaret(bound.scgRenderNode.X+"");
			renderNodeY.setTextKeepCaret(bound.scgRenderNode.Y+"");
			renderNodeZ.setTextKeepCaret(bound.scgRenderNode.Z+"");
			renderNodeW.setTextKeepCaret(bound.scgRenderNode.W+"");
		}
		this.setDisable(false);
	}

	public void rebuildNumericTextFields() {
		hkpShape colShape = bound.getShape();
		hkpShape colTransform = bound.getTransform();
		
		if (colShape != null) switch (bound.Shape) {
		case KSHAPE_BOX:
			
			this.add(new Label("Half extents"), 0, 40);
			var h = new HBox();
			h.getChildren().addAll(boxExtX, boxExtY, boxExtZ, boxExtW);
			this.add(h, 1, 40);
								
			boxExtX.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsX+"");
			boxExtY.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsY+"");
			boxExtZ.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsZ+"");
			boxExtW.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsW+"");

			this.add(new Separator(), 0, 49);
			this.add(new Separator(), 1, 49);

			break;
		case KSHAPE_SPHERE:

			this.add(new Label("Radius"), 0, 40);
			this.add(sphereRadius, 1, 40);
			
			sphereRadius.setTextKeepCaret(((hkpSphereShape)colShape).radius+"");
			
			this.add(new Separator(), 0, 49);
			this.add(new Separator(), 1, 49);
			
			break;
		case KSHAPE_CYLINDER:

			this.add(new Label("Radius"), 0, 40);
			this.add(sphereRadius, 1, 40);
			
			sphereRadius.setTextKeepCaret(((hkpCylinderShape)colShape).radius+"");
			
			this.add(new Separator(), 0, 49);
			this.add(new Separator(), 1, 49);
			
			break;
		case KSHAPE_MESH:
			break;
		case KSHAPE_INVALID:
		case KSHAPE_TYPE_COUNT:
			this.add(new Label("Position"), 0, 40); //better not fuck with this
			h = new HBox();
			h.getChildren().addAll(posX, posY, posZ);
			this.add(h, 1, 40);
			this.add(new Label("Orientation"), 0, 41);
			h = new HBox();
			h.getChildren().addAll(orX, orY, orZ, orW);
			this.add(h, 1, 41);
			this.add(new Label("Half dimension"), 0, 42);
			h = new HBox();
			h.getChildren().addAll(dimX, dimY, dimZ);
			this.add(h, 1, 42);
			this.add(new Label("Pivot"), 0, 43);
			h = new HBox();
			h.getChildren().addAll(pivotX, pivotY, pivotZ);
			this.add(h, 1, 43);
			this.add(new Label("Bone offsets"), 0, 44);
			h = new HBox();
			h.getChildren().addAll(boneOffX, boneOffY, boneOffZ);
			this.add(h, 1, 44);
		}

		if (bound != null) {
			posX.setTextKeepCaret(bound.PositionX+"");
			posY.setTextKeepCaret(bound.PositionY+"");
			posZ.setTextKeepCaret(bound.PositionZ+"");
			
			orX.setTextKeepCaret(bound.OrientationX+"");
			orY.setTextKeepCaret(bound.OrientationY+"");
			orZ.setTextKeepCaret(bound.OrientationZ+"");
			orW.setTextKeepCaret(bound.OrientationW+"");

			dimX.setTextKeepCaret(bound.HalfDimensionX+"");
			dimY.setTextKeepCaret(bound.HalfDimensionY+"");
			dimZ.setTextKeepCaret(bound.HalfDimensionZ+"");

			pivotX.setTextKeepCaret(bound.PivotX+"");
			pivotY.setTextKeepCaret(bound.PivotY+"");
			pivotZ.setTextKeepCaret(bound.PivotZ+"");

			boneOffX.setTextKeepCaret(bound.BoneOffsetX+"");
			boneOffY.setTextKeepCaret(bound.BoneOffsetY+"");
			boneOffZ.setTextKeepCaret(bound.BoneOffsetZ+"");
		}
		
		this.add(new Label("Translate"), 0, 50);
		var h = new HBox();
		h.getChildren().addAll(translateX, translateY, translateZ, translateW);
		this.add(h, 1, 50);

		this.add(new Label("Rotate"), 0, 51);
		h = new HBox();
		h.getChildren().addAll(XRotateX, XRotateY, XRotateZ);
		this.add(h, 1, 51);

//		this.add(new Label("Rotate X"), 0, 51);
//		h = new HBox();
//		h.getChildren().addAll(XRotateX, XRotateY, XRotateZ, XRotateW);
//		this.add(h, 1, 51);
//		this.add(new Label("Rotate Y"), 0, 52);
//		h = new HBox();
//		h.getChildren().addAll(YRotateX, YRotateY, YRotateZ, YRotateW);
//		this.add(h, 1, 52);
//		this.add(new Label("Rotate Z"), 0, 53);
//		h = new HBox();
//		h.getChildren().addAll(ZRotateX, ZRotateY, ZRotateZ, ZRotateW);
//		this.add(h, 1, 53);
		
		if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class ) {
			var shapeTransform = (hkpConvexTransformShape)colTransform;
			
			translateX.setTextKeepCaret(shapeTransform.TranslationX+"");
			translateY.setTextKeepCaret(shapeTransform.TranslationY+"");
			translateZ.setTextKeepCaret(shapeTransform.TranslationZ+"");
			translateW.setTextKeepCaret(shapeTransform.TranslationW+"");

			var angles = shapeTransform.getEulerAngles();
			XRotateX.setTextKeepCaret(angles[0]+"");
			XRotateY.setTextKeepCaret(angles[1]+"");
			XRotateZ.setTextKeepCaret(-angles[2]+"");

//			XRotateX.setTextKeepCaret(shapeTransform.XRotationX+"");
//			XRotateY.setTextKeepCaret(shapeTransform.XRotationY+"");
//			XRotateZ.setTextKeepCaret(shapeTransform.XRotationZ+"");
//			XRotateW.setTextKeepCaret(shapeTransform.XRotationW+"");
//			YRotateX.setTextKeepCaret(shapeTransform.YRotationX+"");
//			YRotateY.setTextKeepCaret(shapeTransform.YRotationY+"");
//			YRotateZ.setTextKeepCaret(shapeTransform.YRotationZ+"");
//			YRotateW.setTextKeepCaret(shapeTransform.YRotationW+"");
//			ZRotateX.setTextKeepCaret(shapeTransform.ZRotationX+"");
//			ZRotateY.setTextKeepCaret(shapeTransform.ZRotationY+"");
//			ZRotateZ.setTextKeepCaret(shapeTransform.ZRotationZ+"");
//			ZRotateW.setTextKeepCaret(shapeTransform.ZRotationW+"");
		} else if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
			var shapeTranslate = (hkpConvexTranslateShape)colTransform;
			
			translateX.setTextKeepCaret(shapeTranslate.TranslationX+"");
			translateY.setTextKeepCaret(shapeTranslate.TranslationY+"");
			translateZ.setTextKeepCaret(shapeTranslate.TranslationZ+"");
			translateW.setTextKeepCaret(shapeTranslate.TranslationW+"");

			XRotateX.setTextKeepCaret("");
			XRotateY.setTextKeepCaret("");
			XRotateZ.setTextKeepCaret("");
//			XRotateW.setTextKeepCaret("");
//			YRotateX.setTextKeepCaret("");
//			YRotateY.setTextKeepCaret("");
//			YRotateZ.setTextKeepCaret("");
//			YRotateW.setTextKeepCaret("");
//			ZRotateX.setTextKeepCaret("");
//			ZRotateY.setTextKeepCaret("");
//			ZRotateZ.setTextKeepCaret("");
//			ZRotateW.setTextKeepCaret("");				
		} else {
			translateX.setTextKeepCaret("");
			translateY.setTextKeepCaret("");
			translateZ.setTextKeepCaret("");
			translateW.setTextKeepCaret("");

			XRotateX.setTextKeepCaret("");
			XRotateY.setTextKeepCaret("");
			XRotateZ.setTextKeepCaret("");
//			XRotateW.setTextKeepCaret("");
//			YRotateX.setTextKeepCaret("");
//			YRotateY.setTextKeepCaret("");
//			YRotateZ.setTextKeepCaret("");
//			YRotateW.setTextKeepCaret("");
//			ZRotateX.setTextKeepCaret("");
//			ZRotateY.setTextKeepCaret("");
//			ZRotateZ.setTextKeepCaret("");
//			ZRotateW.setTextKeepCaret("");				
		}
	}

	public void update() {
		try {
			if (bound != null) {
				updateTextFields();
				updateGizmo();
			} else {
				name.setText("No bound selected");
				if (boundGizmo != null && CollisionsEditor.viewportGroup.getChildren().contains(boundGizmo)) CollisionsEditor.viewportGroup.getChildren().remove(boundGizmo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTextFields() {
		key.setText("0x%08X".formatted(bound.NameHash));
		name.setTextKeepCaret(Hash.getVLT(bound.NameHash));
		boundType.setValue(bound.Type);
		shape.setValue(bound.Shape);
		readFlags(bound);
		
		surfaceName.setTextKeepCaret(Hash.getVLT(bound.SurfaceName));
		attributeName.setTextKeepCaret(Hash.getVLT(bound.AttributeName));		
		jointType.setValue(bound.JointType);
		boneIndex.setTextKeepCaret(bound.BoneIndex+"");
		renderIndex.setTextKeepCaret(bound.RenderHierarchyIndex+"");

		updateNumericTextFields();
		
		if (bound.scgRenderNode != null) {
			renderNodeX.setTextKeepCaret(bound.scgRenderNode.X+"");
			renderNodeY.setTextKeepCaret(bound.scgRenderNode.Y+"");
			renderNodeZ.setTextKeepCaret(bound.scgRenderNode.Z+"");
			renderNodeW.setTextKeepCaret(bound.scgRenderNode.W+"");
		}
	}

	public void updateNumericTextFields() {
		hkpShape colShape = bound.getShape();
		hkpShape colTransform = bound.getTransform();
		
		if (colShape != null) switch (bound.Shape) {
		case KSHAPE_BOX:
			boxExtX.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsX+"");
			boxExtY.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsY+"");
			boxExtZ.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsZ+"");
			boxExtW.setTextKeepCaret(((hkpBoxShape)colShape).HalfExtentsW+"");
			break;
		case KSHAPE_SPHERE:
			sphereRadius.setTextKeepCaret(((hkpSphereShape)colShape).radius+"");
			break;
		case KSHAPE_CYLINDER:
			sphereRadius.setTextKeepCaret(((hkpCylinderShape)colShape).radius+"");
			break;
		case KSHAPE_MESH:
			break;
		case KSHAPE_INVALID:
		case KSHAPE_TYPE_COUNT:
		}

		if (bound != null) {
			posX.setTextKeepCaret(bound.PositionX+"");
			posY.setTextKeepCaret(bound.PositionY+"");
			posZ.setTextKeepCaret(bound.PositionZ+"");
			
			orX.setTextKeepCaret(bound.OrientationX+"");
			orY.setTextKeepCaret(bound.OrientationY+"");
			orZ.setTextKeepCaret(bound.OrientationZ+"");
			orW.setTextKeepCaret(bound.OrientationW+"");

			dimX.setTextKeepCaret(bound.HalfDimensionX+"");
			dimY.setTextKeepCaret(bound.HalfDimensionY+"");
			dimZ.setTextKeepCaret(bound.HalfDimensionZ+"");

			pivotX.setTextKeepCaret(bound.PivotX+"");
			pivotY.setTextKeepCaret(bound.PivotY+"");
			pivotZ.setTextKeepCaret(bound.PivotZ+"");

			boneOffX.setTextKeepCaret(bound.BoneOffsetX+"");
			boneOffY.setTextKeepCaret(bound.BoneOffsetY+"");
			boneOffZ.setTextKeepCaret(bound.BoneOffsetZ+"");
		}
		
		if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class ) {
			var shapeTransform = (hkpConvexTransformShape)colTransform;
			translateX.setTextKeepCaret(shapeTransform.TranslationX+"");
			translateY.setTextKeepCaret(shapeTransform.TranslationY+"");
			translateZ.setTextKeepCaret(shapeTransform.TranslationZ+"");
			translateW.setTextKeepCaret(shapeTransform.TranslationW+"");

			var angles = shapeTransform.getEulerAngles();
			XRotateX.setTextKeepCaret(angles[0]+"");
			XRotateY.setTextKeepCaret(angles[1]+"");
			XRotateZ.setTextKeepCaret(-angles[2]+"");
		
		} else if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
			var shapeTranslate = (hkpConvexTranslateShape)colTransform;
			translateX.setTextKeepCaret(shapeTranslate.TranslationX+"");
			translateY.setTextKeepCaret(shapeTranslate.TranslationY+"");
			translateZ.setTextKeepCaret(shapeTranslate.TranslationZ+"");
			translateW.setTextKeepCaret(shapeTranslate.TranslationW+"");

			XRotateX.setTextKeepCaret("");
			XRotateY.setTextKeepCaret("");
			XRotateZ.setTextKeepCaret("");
		} else {
			translateX.setTextKeepCaret("");
			translateY.setTextKeepCaret("");
			translateZ.setTextKeepCaret("");
			translateW.setTextKeepCaret("");

			XRotateX.setTextKeepCaret("");
			XRotateY.setTextKeepCaret("");
			XRotateZ.setTextKeepCaret("");
		}
	}

	public void updateGizmo() {
		hkpShape colShape = bound.getShape();
		hkpShape colTransform = bound.getTransform();
		
		if (colShape != null) switch (bound.Shape) {
		case KSHAPE_BOX:
			boundGizmo.setScale(((hkpBoxShape)colShape).HalfExtentsX, ((hkpBoxShape)colShape).HalfExtentsY, ((hkpBoxShape)colShape).HalfExtentsZ);
			break;
		case KSHAPE_SPHERE:
			boundGizmo.setScale(((hkpSphereShape)colShape).radius, ((hkpSphereShape)colShape).radius, ((hkpSphereShape)colShape).radius);
			break;
		case KSHAPE_CYLINDER:
			boundGizmo.setScale(((hkpCylinderShape)colShape).radius, ((hkpCylinderShape)colShape).radius, ((hkpCylinderShape)colShape).radius);
			break;
		case KSHAPE_INVALID:
		case KSHAPE_TYPE_COUNT:
		case KSHAPE_MESH:
			break;
		}
		
		if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class ) {
			var shapeTransform = (hkpConvexTransformShape)colTransform;
			boundGizmo.setPosition(-shapeTransform.TranslationX, shapeTransform.TranslationY, shapeTransform.TranslationZ);
			
			var angles = shapeTransform.getEulerAngles();
			boundGizmo.setRotation(angles[0], angles[1], angles[2]);
			
		} else if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
			var shapeTranslate = (hkpConvexTranslateShape)colTransform;
			boundGizmo.setPosition(-shapeTranslate.TranslationX, shapeTranslate.TranslationY, shapeTranslate.TranslationZ);
			boundGizmo.setRotation(0, 0, 0);
		} else {
			boundGizmo.setPosition(0, 0, 0);			
			boundGizmo.setRotation(0, 0, 0);
		}
		
		boundGizmo.setOffset(-bound.getRenderNode().Y, -bound.getRenderNode().Z, -bound.getRenderNode().X);
	}

	private void updateBoundFromGizmo() {
		if (bound != null) {
			try {
				hkpShape colShape = bound.getShape();
				hkpShape colTransform = bound.getTransform();
	
				try{ if (colShape != null) switch (bound.Shape) {
				case KSHAPE_BOX:
					((hkpBoxShape)colShape).HalfExtentsX = boundGizmo.ScaleX.floatValue();
					((hkpBoxShape)colShape).HalfExtentsY = boundGizmo.ScaleY.floatValue();
					((hkpBoxShape)colShape).HalfExtentsZ = boundGizmo.ScaleZ.floatValue();
					break;
				case KSHAPE_SPHERE:
					((hkpSphereShape)colShape).radius = boundGizmo.ScaleX.floatValue();
					break;
				case KSHAPE_CYLINDER:
					((hkpSphereShape)colShape).radius = boundGizmo.ScaleX.floatValue();
					break;
				case KSHAPE_MESH:
					break;
				case KSHAPE_INVALID:
				case KSHAPE_TYPE_COUNT:
					bound.PositionX = Float.parseFloat(posX.getText().strip());
					bound.PositionY = Float.parseFloat(posY.getText().strip());
					bound.PositionZ = Float.parseFloat(posZ.getText().strip());
		
					bound.OrientationX = Float.parseFloat(orX.getText().strip());
					bound.OrientationY = Float.parseFloat(orY.getText().strip());
					bound.OrientationZ = Float.parseFloat(orZ.getText().strip());
					bound.OrientationW = Float.parseFloat(orW.getText().strip());
					
					bound.HalfDimensionX = Float.parseFloat(dimX.getText().strip());
					bound.HalfDimensionY = Float.parseFloat(dimY.getText().strip());
					bound.HalfDimensionZ = Float.parseFloat(dimZ.getText().strip());
					
					bound.PivotX = Float.parseFloat(pivotX.getText().strip());
					bound.PivotY = Float.parseFloat(pivotY.getText().strip());
					bound.PivotZ = Float.parseFloat(pivotZ.getText().strip());
		
					bound.BoneOffsetX = Float.parseFloat(boneOffX.getText().strip());
					bound.BoneOffsetY = Float.parseFloat(boneOffY.getText().strip());
					bound.BoneOffsetZ = Float.parseFloat(boneOffZ.getText().strip());
					
				}
				} catch (@SuppressWarnings("unused") Exception shh) {} //fail to parse silently
				
	//			if (bound.Shape == BoundShape.KSHAPE_BOX || bound.Shape == BoundShape.KSHAPE_SPHERE) {
					
					if (!doubleEquals(boundGizmo.RotationX.get(),0) || 
							!doubleEquals(boundGizmo.RotationY.get(),0) || 
							!doubleEquals(boundGizmo.RotationZ.get(),0)) {
						// need transform
						if (colTransform == null || colTransform.getClass() != hkpConvexTransformShape.class ) {
							if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
								var s = (hkpConvexTranslateShape)colTransform;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTransformShape();
								((hkpConvexTransformShape)bound.collisionShape).childShape = s.childShape;
							} else {
								var s = bound.collisionShape;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTransformShape();
								((hkpConvexTransformShape)bound.collisionShape).childShape = s;
							}
						}
						var shapeTransform = ((hkpConvexTransformShape)bound.collisionShape);

						shapeTransform.TranslationX = -boundGizmo.PositionX.floatValue();
						shapeTransform.TranslationY = boundGizmo.PositionY.floatValue();
						shapeTransform.TranslationZ = boundGizmo.PositionZ.floatValue();
	
//						shapeTransform.fromTransform(boundGizmo.rotations);
						shapeTransform.fromEulerAngles(boundGizmo.RotationX.get(), boundGizmo.RotationY.get(), boundGizmo.RotationZ.get());
						
					} else if (!doubleEquals(boundGizmo.PositionX.get(),0) || 
							!doubleEquals(boundGizmo.PositionY.get(),0) || 
							!doubleEquals(boundGizmo.PositionZ.get(),0)) {
						//need translate
						if (colTransform == null || colTransform.getClass() != hkpConvexTranslateShape.class ) {
							if (colTransform != null && colTransform.getClass() == hkpConvexTransformShape.class ) {
								var s = (hkpConvexTransformShape)colTransform;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)bound.collisionShape).childShape = s.childShape;
							} else {
								var s = bound.collisionShape;
								bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexTranslateShape();
								((hkpConvexTranslateShape)bound.collisionShape).childShape = s;
							}
						}
						var shapeTransform = ((hkpConvexTranslateShape)bound.collisionShape);	
						shapeTransform.TranslationX = -boundGizmo.PositionX.floatValue();
						shapeTransform.TranslationY = boundGizmo.PositionY.floatValue();
						shapeTransform.TranslationZ = boundGizmo.PositionZ.floatValue();
						
					} else {
						//no transform or translate
						if (bound.collisionShape != null) {
							
						}
	
						bound.displayPivot.setTranslateX(bound.getRenderNode().Y);
						bound.displayPivot.setTranslateY(- bound.getRenderNode().Z); 
						bound.displayPivot.setTranslateZ(- bound.getRenderNode().X);
						bound.displayBound.setTranslateX(bound.getRenderNode().Y);
						bound.displayBound.setTranslateY(- bound.getRenderNode().Z); 
						bound.displayBound.setTranslateZ(- bound.getRenderNode().X);
					}
					
	//			}
				
			} catch (Exception e) {
				e.printStackTrace();
				if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.ERROR, "Error refreshing bound:\n"+e.getMessage(), ButtonType.OK).show();
				System.out.println("Error refreshing bound "+Hash.getVLT(bound.NameHash));
			}
	
			updateNumericTextFields();
			
			bound.compute();
			bound.updateShape();
		}
		BoundCell.allCells.forEach(c -> c.update(c.getItem(), false));

	}

	private static boolean doubleEquals(double a, double b) {
		return (int)(a*1000) == (int)(b*1000);
	}

//	private static String flagsAsText(ArrayList<BoundFlags> flags) {
//		if (flags == null || flags.size() == 0) return "";
//		String s = flags.get(0).getName();
//		for (int i=1; i<flags.size(); i++) s += ", " + flags.get(i).getName();
//		return s;
//	}

	private ArrayList<BoundFlags> applyFlags(){
		var l = new ArrayList<BoundFlags>();
//		if (s == null) return l;
//		for (var split : s.split(",")) l.add(BoundFlags.get(split.strip()));
		for (int i=1; i<BoundFlags.values().length; i++) if (((CheckBox)flags.getChildren().get(i-1)).isSelected()) l.add(BoundFlags.values()[i]);
		return l;
	}
	
	public void readFlags(scgBounds b) {
		for (int i=1; i<BoundFlags.values().length; i++) {
			((CheckBox)flags.getChildren().get(i-1)).setSelected(false);
		}
		for (var f : b.Flags) {
			for (int i=1; i<BoundFlags.values().length; i++) {
				if (f == BoundFlags.values()[i]) ((CheckBox)flags.getChildren().get(i-1)).setSelected(true);
			}
		}
	}
	
	public void setBound(scgBounds b) {
		updateBound();
		bound = b;
		rebuild();
	}
}

class CaretKeeperTextField extends TextField{
	public void setTextKeepCaret(String s) {
		if (s.equals("-") || s.endsWith(".")) return; //fix negative numbers and decimals mid typing
		var p = this.getCaretPosition();
		var sel = this.getSelection();
		setText(s);
		if (p == sel.getStart()) this.selectRange(Math.min(sel.getEnd(), s.length()), Math.min(sel.getStart(), s.length()));
		else this.selectRange(Math.min(sel.getStart(), s.length()), Math.min(sel.getEnd(), s.length()));
	}
}