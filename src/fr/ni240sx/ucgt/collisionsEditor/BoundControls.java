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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BoundControls extends GridPane {
	
	private scgBounds bound = null;
	
	private Label key = new Label();
	private TextField name = new TextField();
	private ComboBox<BoundType> boundType = new ComboBox<>();
	private VBox flags = new VBox();
	private ComboBox<BoundShape> shape = new ComboBox<>();

	private TextField surfaceName = new TextField();
	private TextField attributeName = new TextField();
//	private TextField childrenFlags = new TextField();
	
	private ComboBox<BoundConstraintType> constraintType = new ComboBox<>();
	private ComboBox<BoundJointType> jointType = new ComboBox<>();
	private ComboBox<BoundNodeType> nodeType = new ComboBox<>();

	private TextField boneIndex = new TextField();
	private TextField renderIndex = new TextField();
	
	private TextField posX = new TextField();
	private TextField posY = new TextField();
	private TextField posZ = new TextField();

	private TextField orX = new TextField();
	private TextField orY = new TextField();
	private TextField orZ = new TextField();
	private TextField orW = new TextField();

	private TextField dimX = new TextField();
	private TextField dimY = new TextField();
	private TextField dimZ = new TextField();

	private TextField pivotX = new TextField();
	private TextField pivotY = new TextField();
	private TextField pivotZ = new TextField();

	private TextField boneOffX = new TextField();
	private TextField boneOffY = new TextField();
	private TextField boneOffZ = new TextField();
	
	private TextField boxExtX = new TextField();
	private TextField boxExtY = new TextField();
	private TextField boxExtZ = new TextField();
	private TextField boxExtW = new TextField();

	private TextField sphereRadius = new TextField();
	
	private TextField translateX = new TextField();
	private TextField translateY = new TextField();
	private TextField translateZ = new TextField();
	private TextField translateW = new TextField();
	
	private TextField XRotateX = new TextField();
	private TextField XRotateY = new TextField();
	private TextField XRotateZ = new TextField();
//	private TextField XRotateW = new TextField();
	
//	private TextField YRotateX = new TextField();
//	private TextField YRotateY = new TextField();
//	private TextField YRotateZ = new TextField();
//	private TextField YRotateW = new TextField();
	
//	private TextField ZRotateX = new TextField();
//	private TextField ZRotateY = new TextField();
//	private TextField ZRotateZ = new TextField();
//	private TextField ZRotateW = new TextField();

	private TextField renderNodeX = new TextField();
	private TextField renderNodeY = new TextField();
	private TextField renderNodeZ = new TextField();
	private TextField renderNodeW = new TextField();
	
	private Gizmo boundGizmo;
//	public WeakInvalidationListener gizmoChangeListener = new WeakInvalidationListener(o -> updateBoundFromGizmo());
	public InvalidationListener gizmoChangeListener = o -> updateBoundFromGizmo();
	
	public BoundControls() {
		
		boundType.getItems().addAll(BoundType.values());
		shape.getItems().addAll(BoundShape.values());
		constraintType.getItems().addAll(BoundConstraintType.values());
		jointType.getItems().addAll(BoundJointType.values());
		nodeType.getItems().addAll(BoundNodeType.values());

		for (int i=1; i<BoundFlags.values().length; i++) {
			flags.getChildren().add(new CheckBox(BoundFlags.values()[i].getName()));
		}
		
		this.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				updateBound();
				update();
			}
		});
		
		update();
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
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpBoxShape(); //(bound)
						break;
					case KSHAPE_MESH:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpConvexVerticesShape(); //(bound)
						break;
					case KSHAPE_SPHERE:
						bound.collisionShape = CollisionsEditor.mainCollisions.new hkpSphereShape(); //(bound)
						break;
					default:
						bound.collisionShape = null;
						break;
					}
					
					
				}
				
	//			bound.Shape = shape.getValue();
				bound.Flags = applyFlags();
				
				bound.SurfaceName = Hash.findVLT(surfaceName.getText().strip());
				bound.AttributeName = Hash.findVLT(attributeName.getText().strip());
//				bound.ChildrenFlags = flagsFromText(childrenFlags.getText());
	
				bound.ConstraintType = constraintType.getValue();
				bound.JointType = jointType.getValue();
				bound.NodeType = nodeType.getValue();
				
				bound.BoneIndex = Integer.parseInt(boneIndex.getText().strip());
				bound.RenderHierarchyIndex = (short) Integer.parseInt(renderIndex.getText().strip());

				hkpShape colShape = bound.getShape();
				hkpShape colTransform = bound.getTransform();

				try{ if (colShape != null) switch (bound.Shape) {
				case KSHAPE_BOX:
					((hkpBoxShape)colShape).HalfExtentsX = Float.parseFloat(boxExtX.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsY = Float.parseFloat(boxExtY.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsZ = Float.parseFloat(boxExtZ.getText().strip());
					((hkpBoxShape)colShape).HalfExtentsW = Float.parseFloat(boxExtW.getText().strip());
					break;
				case KSHAPE_SPHERE:
					((hkpSphereShape)colShape).radius = Float.parseFloat(sphereRadius.getText().strip());
					break;
				case KSHAPE_MESH:
					break;
				default:
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
				
//				if (bound.Shape == BoundShape.KSHAPE_BOX || bound.Shape == BoundShape.KSHAPE_SPHERE) {
					
					if (!(XRotateX.getText().isBlank() || XRotateY.getText().isBlank() || XRotateZ.getText().isBlank() 
//							|| YRotateX.getText().isBlank() || YRotateY.getText().isBlank() || YRotateZ.getText().isBlank() || 
//							ZRotateX.getText().isBlank() || ZRotateY.getText().isBlank() || ZRotateZ.getText().isBlank()
							)) {
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
						
					} else if (!(translateX.getText().isBlank() || translateY.getText().isBlank() || translateZ.getText().isBlank())) {
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
	
	public void update() {
		try {	
			this.getChildren().clear();

			this.add(new Label("Vault key"), 0, 0);
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
			
			Label l;
			this.add(l = new Label("Constraint type"), 0, 20);
			l.setMinWidth(90);
			this.add(constraintType, 1, 20);
			this.add(new Label("Joint type"), 0, 21);
			this.add(jointType, 1, 21);
			this.add(new Label("Node type"), 0, 22);
			this.add(nodeType, 1, 22);
	
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

				updateTextFields();

				hkpShape colShape = bound.getShape();
				hkpShape colTransform = bound.getTransform();
				
				if (colShape != null) switch (bound.Shape) {
				case KSHAPE_BOX:
					boundGizmo.setScale(((hkpBoxShape)colShape).HalfExtentsX, ((hkpBoxShape)colShape).HalfExtentsY, ((hkpBoxShape)colShape).HalfExtentsZ);
					break;
				case KSHAPE_SPHERE:
					boundGizmo.setScale(((hkpSphereShape)colShape).radius, ((hkpSphereShape)colShape).radius, ((hkpSphereShape)colShape).radius);
					break;
				default:
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
				
				
			} else {
				name.setText("No bound selected");
				
				this.setDisable(true);
				
				if (boundGizmo != null && CollisionsEditor.viewportGroup.getChildren().contains(boundGizmo)) CollisionsEditor.viewportGroup.getChildren().remove(boundGizmo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTextFields() {
		key.setText("0x%08X".formatted(bound.NameHash));
		name.setText(Hash.getVLT(bound.NameHash));
		boundType.setValue(bound.Type);
		shape.setValue(bound.Shape);
//				flags.setText(flagsAsText(bound.Flags));
		readFlags(bound);
		
		surfaceName.setText(Hash.getVLT(bound.SurfaceName));
		attributeName.setText(Hash.getVLT(bound.AttributeName));
//			childrenFlags.setText(flagsAsText(bound.ChildrenFlags));
		
		constraintType.setValue(bound.ConstraintType);
		jointType.setValue(bound.JointType);
		nodeType.setValue(bound.NodeType);

		boneIndex.setText(bound.BoneIndex+"");
		renderIndex.setText(bound.RenderHierarchyIndex+"");

		updateNumericTextFields();
		
		if (bound.scgRenderNode != null) {
			this.add(new Separator(), 0, 59);
			this.add(new Separator(), 1, 59);
			this.add(new Label("Render node"), 0, 60);
			var h = new HBox();
			h.getChildren().addAll(renderNodeX, renderNodeY, renderNodeZ, renderNodeW);
			this.add(h, 1, 60);
			renderNodeX.setText(bound.scgRenderNode.X+"");
			renderNodeY.setText(bound.scgRenderNode.Y+"");
			renderNodeZ.setText(bound.scgRenderNode.Z+"");
			renderNodeW.setText(bound.scgRenderNode.W+"");
		}
		this.setDisable(false);
	}

	public void updateNumericTextFields() {
		hkpShape colShape = bound.getShape();
		hkpShape colTransform = bound.getTransform();
		
		if (colShape != null) switch (bound.Shape) {
		case KSHAPE_BOX:
			
			this.add(new Label("Half extents"), 0, 40);
			var h = new HBox();
			h.getChildren().addAll(boxExtX, boxExtY, boxExtZ, boxExtW);
			this.add(h, 1, 40);
								
			boxExtX.setText(((hkpBoxShape)colShape).HalfExtentsX+"");
			boxExtY.setText(((hkpBoxShape)colShape).HalfExtentsY+"");
			boxExtZ.setText(((hkpBoxShape)colShape).HalfExtentsZ+"");
			boxExtW.setText(((hkpBoxShape)colShape).HalfExtentsW+"");

			this.add(new Separator(), 0, 49);
			this.add(new Separator(), 1, 49);

			break;
		case KSHAPE_SPHERE:

			this.add(new Label("Radius"), 0, 40);
			this.add(sphereRadius, 1, 40);
			
			sphereRadius.setText(((hkpSphereShape)colShape).radius+"");
			
			this.add(new Separator(), 0, 49);
			this.add(new Separator(), 1, 49);
			
			break;
		case KSHAPE_MESH:
			break;
		default:
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
			posX.setText(bound.PositionX+"");
			posY.setText(bound.PositionY+"");
			posZ.setText(bound.PositionZ+"");
			
			orX.setText(bound.OrientationX+"");
			orY.setText(bound.OrientationY+"");
			orZ.setText(bound.OrientationZ+"");
			orW.setText(bound.OrientationW+"");

			dimX.setText(bound.HalfDimensionX+"");
			dimY.setText(bound.HalfDimensionY+"");
			dimZ.setText(bound.HalfDimensionZ+"");

			pivotX.setText(bound.PivotX+"");
			pivotY.setText(bound.PivotY+"");
			pivotZ.setText(bound.PivotZ+"");

			boneOffX.setText(bound.BoneOffsetX+"");
			boneOffY.setText(bound.BoneOffsetY+"");
			boneOffZ.setText(bound.BoneOffsetZ+"");
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
			
			translateX.setText(shapeTransform.TranslationX+"");
			translateY.setText(shapeTransform.TranslationY+"");
			translateZ.setText(shapeTransform.TranslationZ+"");
			translateW.setText(shapeTransform.TranslationW+"");

			var angles = shapeTransform.getEulerAngles();
			XRotateX.setText(angles[0]+"");
			XRotateY.setText(angles[1]+"");
			XRotateZ.setText(-angles[2]+"");

//			XRotateX.setText(shapeTransform.XRotationX+"");
//			XRotateY.setText(shapeTransform.XRotationY+"");
//			XRotateZ.setText(shapeTransform.XRotationZ+"");
//			XRotateW.setText(shapeTransform.XRotationW+"");
//			YRotateX.setText(shapeTransform.YRotationX+"");
//			YRotateY.setText(shapeTransform.YRotationY+"");
//			YRotateZ.setText(shapeTransform.YRotationZ+"");
//			YRotateW.setText(shapeTransform.YRotationW+"");
//			ZRotateX.setText(shapeTransform.ZRotationX+"");
//			ZRotateY.setText(shapeTransform.ZRotationY+"");
//			ZRotateZ.setText(shapeTransform.ZRotationZ+"");
//			ZRotateW.setText(shapeTransform.ZRotationW+"");
		} else if (colTransform != null && colTransform.getClass() == hkpConvexTranslateShape.class ) {
			var shapeTranslate = (hkpConvexTranslateShape)colTransform;
			
			translateX.setText(shapeTranslate.TranslationX+"");
			translateY.setText(shapeTranslate.TranslationY+"");
			translateZ.setText(shapeTranslate.TranslationZ+"");
			translateW.setText(shapeTranslate.TranslationW+"");

			XRotateX.setText("");
			XRotateY.setText("");
			XRotateZ.setText("");
//			XRotateW.setText("");
//			YRotateX.setText("");
//			YRotateY.setText("");
//			YRotateZ.setText("");
//			YRotateW.setText("");
//			ZRotateX.setText("");
//			ZRotateY.setText("");
//			ZRotateZ.setText("");
//			ZRotateW.setText("");				
		} else {
			translateX.setText("");
			translateY.setText("");
			translateZ.setText("");
			translateW.setText("");

			XRotateX.setText("");
			XRotateY.setText("");
			XRotateZ.setText("");
//			XRotateW.setText("");
//			YRotateX.setText("");
//			YRotateY.setText("");
//			YRotateZ.setText("");
//			YRotateW.setText("");
//			ZRotateX.setText("");
//			ZRotateY.setText("");
//			ZRotateZ.setText("");
//			ZRotateW.setText("");				
		}
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
				case KSHAPE_MESH:
					break;
				default:
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
					
					if (boundGizmo.RotationX.get() != 0 || boundGizmo.RotationY.get() != 0 || boundGizmo.RotationZ.get() != 0) {
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
						
					} else if (boundGizmo.PositionX.get() != 0 || boundGizmo.PositionY.get() != 0 || boundGizmo.PositionZ.get() != 0) {
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
		bound = b;
		update();
	}
}
