package fr.ni240sx.ucgt.collisionsEditor;

import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundConstraintType;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundFlags;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundJointType;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundNodeType;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundShape;
import fr.ni240sx.ucgt.collisionsEditor.CollisionBound.BoundType;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionBoxShape;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTransform;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTranslate;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionSphereShape;
import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.CollisionConvexVertice;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class BoundControls extends GridPane {
	
	private CollisionBound bound = null;
	
	private TextField name = new TextField();
	private ComboBox<BoundType> boundType = new ComboBox<>();
	private TextField flags = new TextField();
	private ComboBox<BoundShape> shape = new ComboBox<>();

	private TextField surfaceName = new TextField();
	private TextField attributeName = new TextField();
	private TextField childrenFlags = new TextField();
	
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
	private TextField XRotateW = new TextField();
	
	private TextField YRotateX = new TextField();
	private TextField YRotateY = new TextField();
	private TextField YRotateZ = new TextField();
	private TextField YRotateW = new TextField();
	
	private TextField ZRotateX = new TextField();
	private TextField ZRotateY = new TextField();
	private TextField ZRotateZ = new TextField();
	private TextField ZRotateW = new TextField();
	
	public BoundControls() {
		
		boundType.getItems().addAll(BoundType.values());
		shape.getItems().addAll(BoundShape.values());
		constraintType.getItems().addAll(BoundConstraintType.values());
		jointType.getItems().addAll(BoundJointType.values());
		nodeType.getItems().addAll(BoundNodeType.values());

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
						bound.collisionShape = new CollisionBoxShape(bound);
						break;
					case KSHAPE_MESH:
						bound.collisionShape = new CollisionConvexVertice(bound);
						break;
					case KSHAPE_SPHERE:
						bound.collisionShape = new CollisionSphereShape(bound);
						break;
					default:
						bound.collisionShape = null;
						break;
					}
					
					
				}
				
	//			bound.Shape = shape.getValue();
				bound.Flags = flagsFromText(flags.getText());
				
				bound.SurfaceName = Hash.findVLT(surfaceName.getText().strip());
				bound.AttributeName = Hash.findVLT(attributeName.getText().strip());
				bound.ChildrenFlags = flagsFromText(childrenFlags.getText());
	
				bound.ConstraintType = constraintType.getValue();
				bound.JointType = jointType.getValue();
				bound.NodeType = nodeType.getValue();
				
				bound.BoneIndex = Integer.parseInt(boneIndex.getText().strip());
				bound.RenderHierarchyIndex = (short) Integer.parseInt(renderIndex.getText().strip());
	
				try{ switch (bound.Shape) {
				case KSHAPE_BOX:
					((CollisionBoxShape)bound.collisionShape).HalfExtentsX = Float.parseFloat(boxExtX.getText().strip());
					((CollisionBoxShape)bound.collisionShape).HalfExtentsY = Float.parseFloat(boxExtY.getText().strip());
					((CollisionBoxShape)bound.collisionShape).HalfExtentsZ = Float.parseFloat(boxExtZ.getText().strip());
					((CollisionBoxShape)bound.collisionShape).HalfExtentsW = Float.parseFloat(boxExtW.getText().strip());
					break;
				case KSHAPE_SPHERE:
					((CollisionSphereShape)bound.collisionShape).radius = Float.parseFloat(sphereRadius.getText().strip());
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
				} catch (Exception shh) {} //fail to parse silently
				
				if (bound.Shape == BoundShape.KSHAPE_BOX || bound.Shape == BoundShape.KSHAPE_SPHERE) {
					
					if (!(XRotateX.getText().isBlank() || XRotateY.getText().isBlank() || XRotateZ.getText().isBlank() || 
							YRotateX.getText().isBlank() || YRotateY.getText().isBlank() || YRotateZ.getText().isBlank() || 
							ZRotateX.getText().isBlank() || ZRotateY.getText().isBlank() || ZRotateZ.getText().isBlank())) {
						// need transform
						if (bound.shapeTransform == null) bound.shapeTransform = new CollisionConvexTransform();
	
						if (!translateX.getText().isBlank()) bound.shapeTransform.TranslationX = Float.parseFloat(translateX.getText().strip());
						if (!translateY.getText().isBlank()) bound.shapeTransform.TranslationY = Float.parseFloat(translateY.getText().strip());
						if (!translateZ.getText().isBlank()) bound.shapeTransform.TranslationZ = Float.parseFloat(translateZ.getText().strip());
						if (!translateW.getText().isBlank()) bound.shapeTransform.TranslationW = Float.parseFloat(translateW.getText().strip());
	
						bound.shapeTransform.XRotationX = Float.parseFloat(XRotateX.getText().strip());
						bound.shapeTransform.XRotationY = Float.parseFloat(XRotateY.getText().strip());
						bound.shapeTransform.XRotationZ = Float.parseFloat(XRotateZ.getText().strip());
						bound.shapeTransform.XRotationW = Float.parseFloat(XRotateW.getText().strip());
						bound.shapeTransform.YRotationX = Float.parseFloat(YRotateX.getText().strip());
						bound.shapeTransform.YRotationY = Float.parseFloat(YRotateY.getText().strip());
						bound.shapeTransform.YRotationZ = Float.parseFloat(YRotateZ.getText().strip());
						bound.shapeTransform.YRotationW = Float.parseFloat(YRotateW.getText().strip());
						bound.shapeTransform.ZRotationX = Float.parseFloat(ZRotateX.getText().strip());
						bound.shapeTransform.ZRotationY = Float.parseFloat(ZRotateY.getText().strip());
						bound.shapeTransform.ZRotationZ = Float.parseFloat(ZRotateZ.getText().strip());
						bound.shapeTransform.ZRotationW = Float.parseFloat(ZRotateW.getText().strip());
						
						bound.shapeTranslate = null;
					} else if (!(translateX.getText().isBlank() || translateY.getText().isBlank() || translateZ.getText().isBlank())) {
						//need translate
						if (bound.shapeTranslate == null) bound.shapeTranslate = new CollisionConvexTranslate();
	
						bound.shapeTranslate.TranslationX = Float.parseFloat(translateX.getText().strip());
						bound.shapeTranslate.TranslationY = Float.parseFloat(translateY.getText().strip());
						bound.shapeTranslate.TranslationZ = Float.parseFloat(translateZ.getText().strip());
						bound.shapeTranslate.TranslationW = Float.parseFloat(translateW.getText().strip());
						
						bound.shapeTransform = null;
					} else {
						//no transform or translate
						bound.shapeTransform = null;
						bound.shapeTranslate = null;
						

						bound.displayPivot.setTranslateX(0);
						bound.displayPivot.setTranslateY(- CollisionsEditor.mainCollisions.Z); 
						bound.displayPivot.setTranslateZ(- CollisionsEditor.mainCollisions.X);
						bound.displayBound.setTranslateX(0);
						bound.displayBound.setTranslateY(- CollisionsEditor.mainCollisions.Z); 
						bound.displayBound.setTranslateZ(- CollisionsEditor.mainCollisions.X);
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				if (!CollisionsEditor.disableWarnings) new Alert(Alert.AlertType.ERROR, "Error refreshing bound:\n"+e.getMessage(), ButtonType.OK).show();
				System.out.println("Error refreshing bound "+Hash.getVLT(bound.NameHash));
			}
		}
		BoundCell.allCells.forEach(c -> c.update(c.getItem(), false));
		bound.updateShape();
	}
	
	public void update() {
		this.getChildren().clear();
		
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
		this.add(new Label("Children flags"), 0, 12);
		this.add(childrenFlags, 1, 12);

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
			name.setText(Hash.getVLT(bound.NameHash));
			boundType.setValue(bound.Type);
			shape.setValue(bound.Shape);
			flags.setText(flagsAsText(bound.Flags));
			
			surfaceName.setText(Hash.getVLT(bound.SurfaceName));
			attributeName.setText(Hash.getVLT(bound.AttributeName));
			childrenFlags.setText(flagsAsText(bound.ChildrenFlags));
			
			constraintType.setValue(bound.ConstraintType);
			jointType.setValue(bound.JointType);
			nodeType.setValue(bound.NodeType);

			boneIndex.setText(bound.BoneIndex+"");
			renderIndex.setText(bound.RenderHierarchyIndex+"");

			switch (bound.Shape) {
			case KSHAPE_BOX:
				
				this.add(new Label("Half extents"), 0, 40);
				var h = new HBox();
				h.getChildren().addAll(boxExtX, boxExtY, boxExtZ, boxExtW);
				this.add(h, 1, 40);
				
				boxExtX.setText(((CollisionBoxShape)bound.collisionShape).HalfExtentsX+"");
				boxExtY.setText(((CollisionBoxShape)bound.collisionShape).HalfExtentsY+"");
				boxExtZ.setText(((CollisionBoxShape)bound.collisionShape).HalfExtentsZ+"");
				boxExtW.setText(((CollisionBoxShape)bound.collisionShape).HalfExtentsW+"");

				this.add(new Separator(), 0, 49);
				this.add(new Separator(), 1, 49);

				break;
			case KSHAPE_SPHERE:

				this.add(new Label("Radius"), 0, 40);
				this.add(sphereRadius, 1, 40);

				sphereRadius.setText(((CollisionSphereShape)bound.collisionShape).radius+"");
				
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
			
			if (bound.shapeTransform != null || bound.shapeTranslate != null || bound.Shape == BoundShape.KSHAPE_BOX || bound.Shape == BoundShape.KSHAPE_SPHERE) {

				this.add(new Label("Translate"), 0, 50);
				var h = new HBox();
				h.getChildren().addAll(translateX, translateY, translateZ, translateW);
				this.add(h, 1, 50);
				
				this.add(new Label("Rotate X"), 0, 51);
				h = new HBox();
				h.getChildren().addAll(XRotateX, XRotateY, XRotateZ, XRotateW);
				this.add(h, 1, 51);
				this.add(new Label("Rotate Y"), 0, 52);
				h = new HBox();
				h.getChildren().addAll(YRotateX, YRotateY, YRotateZ, YRotateW);
				this.add(h, 1, 52);
				this.add(new Label("Rotate Z"), 0, 53);
				h = new HBox();
				h.getChildren().addAll(ZRotateX, ZRotateY, ZRotateZ, ZRotateW);
				this.add(h, 1, 53);
				
				if (bound.shapeTransform != null) {
					translateX.setText(bound.shapeTransform.TranslationX+"");
					translateY.setText(bound.shapeTransform.TranslationY+"");
					translateZ.setText(bound.shapeTransform.TranslationZ+"");
					translateW.setText(bound.shapeTransform.TranslationW+"");
					
					XRotateX.setText(bound.shapeTransform.XRotationX+"");
					XRotateY.setText(bound.shapeTransform.XRotationY+"");
					XRotateZ.setText(bound.shapeTransform.XRotationZ+"");
					XRotateW.setText(bound.shapeTransform.XRotationW+"");
					YRotateX.setText(bound.shapeTransform.YRotationX+"");
					YRotateY.setText(bound.shapeTransform.YRotationY+"");
					YRotateZ.setText(bound.shapeTransform.YRotationZ+"");
					YRotateW.setText(bound.shapeTransform.YRotationW+"");
					ZRotateX.setText(bound.shapeTransform.ZRotationX+"");
					ZRotateY.setText(bound.shapeTransform.ZRotationY+"");
					ZRotateZ.setText(bound.shapeTransform.ZRotationZ+"");
					ZRotateW.setText(bound.shapeTransform.ZRotationW+"");
				} else if (bound.shapeTranslate != null) {
					translateX.setText(bound.shapeTranslate.TranslationX+"");
					translateY.setText(bound.shapeTranslate.TranslationY+"");
					translateZ.setText(bound.shapeTranslate.TranslationZ+"");
					translateW.setText(bound.shapeTranslate.TranslationW+"");

					XRotateX.setText("");
					XRotateY.setText("");
					XRotateZ.setText("");
					XRotateW.setText("");
					YRotateX.setText("");
					YRotateY.setText("");
					YRotateZ.setText("");
					YRotateW.setText("");
					ZRotateX.setText("");
					ZRotateY.setText("");
					ZRotateZ.setText("");
					ZRotateW.setText("");				
				} else {
					translateX.setText("");
					translateY.setText("");
					translateZ.setText("");
					translateW.setText("");

					XRotateX.setText("");
					XRotateY.setText("");
					XRotateZ.setText("");
					XRotateW.setText("");
					YRotateX.setText("");
					YRotateY.setText("");
					YRotateZ.setText("");
					YRotateW.setText("");
					ZRotateX.setText("");
					ZRotateY.setText("");
					ZRotateZ.setText("");
					ZRotateW.setText("");				
				}
				
			}
			this.setDisable(false);
		} else {
			name.setText("No bound selected");
			
			this.setDisable(true);
		}
	}
	
	private static String flagsAsText(ArrayList<BoundFlags> flags) {
		if (flags.size() == 0) return "";
		String s = flags.get(0).getName();
		for (int i=1; i<flags.size(); i++) s += ", " + flags.get(i).getName();
		return s;
	}

	private static ArrayList<BoundFlags> flagsFromText(String s){
		var l = new ArrayList<BoundFlags>();
		for (var split : s.split(",")) l.add(BoundFlags.get(split.strip()));
		return l;
	}
	
	public void setBound(CollisionBound b) {
		bound = b;
		update();
	}
}
