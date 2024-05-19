package fr.ni240sx.ucgt.collisionsEditor;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTransform;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionConvexTranslate;
import fr.ni240sx.ucgt.collisionsEditor.BoundShapes.CollisionShape;
import fr.ni240sx.ucgt.collisionsEditor.CollisionMesh.CollisionConvexVertice;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

public class CollisionBound {

	Hash AttributeName = new Hash("");
	Hash NameHash = new Hash("");
	Hash SurfaceName;
	
	BoundFlags ChildrenFlags;
	BoundConstraintType ConstraintType;
	BoundFlags Flags;
	BoundJointType JointType;
	BoundNodeType NodeType;
	BoundShape Shape;
	BoundType Type;
	
	float BoneIndex = -1;
	float BoneOffsetX = -0;
	float BoneOffsetY = -0;
	float BoneOffsetZ = -0;
	
	float HalfDimensionX = 0;
	float HalfDimensionY = 0;
	float HalfDimensionZ = 0;
	
	byte NumberOfChildren = 0;
	short RenderHierarchyIndex = 0;

	float OrientationX = 0;
	float OrientationY = 0;
	float OrientationZ = 0;
	float OrientationW = 0;

	float PivotX = 0;
	float PivotY = 0;
	float PivotZ = 0;

	float PositionX = 0;
	float PositionY = 0;
	float PositionZ = 0;

	CollisionShape collisionShape = null;
	CollisionConvexTransform shapeTransform = null;
	CollisionConvexTranslate shapeTranslate = null;
	
	public Shape3D displayShape = new Box(1,1,1);
	public Sphere displayPivot = new Sphere(0.05);
	public boolean render = true;
	
	double colorR = Math.random();
	double colorG = Math.random();
	double colorB = Math.random();
	
	public CollisionBound() {
		// TODO Auto-generated constructor stub
	}

	public CollisionBound(Hash attributeName, Hash nameHash, Hash surfaceName, BoundFlags childrenFlags,
			BoundConstraintType constraintType, BoundFlags flags, BoundJointType jointType, BoundNodeType nodeType, BoundShape shape, BoundType type,
			float boneIndex, float boneOffsetX, float boneOffsetY, float boneOffsetZ, float halfDimensionX,
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
		NumberOfChildren = (byte) numberOfChildren;
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
	}

	@Override
	public String toString() {
		return "\n- CollisionBound [AttributeName=" + AttributeName + ", NameHash=" + NameHash + ", SurfaceName="
				+ SurfaceName + ", ChildrenFlags=" + ChildrenFlags + ", Flags=" + Flags + ","
				+ "\n ConstraintType=" + ConstraintType + " JointType=" + JointType + ", NodeType=" + NodeType + ", Shape=" + Shape + ", Type=" + Type + ",\n"
				+ " BoneIndex=" + BoneIndex + ", BoneOffsetX=" + BoneOffsetX + ", BoneOffsetY=" + BoneOffsetY
				+ ", BoneOffsetZ=" + BoneOffsetZ + ", HalfDimensionX=" + HalfDimensionX + ", HalfDimensionY=" + HalfDimensionY + ", HalfDimensionZ=" + HalfDimensionZ + ","
				+ "\n NumberOfChildren=" + NumberOfChildren	+ ", RenderHierarchyIndex=" + RenderHierarchyIndex + ", OrientationX=" + OrientationX
				+ ", OrientationY=" + OrientationY + ", OrientationZ=" + OrientationZ + ", OrientationW=" + OrientationW + ","
				+ "\n PivotX=" + PivotX + ", PivotY=" + PivotY + ", PivotZ=" + PivotZ + ", PositionX=" + PositionX
				+ ", PositionY=" + PositionY + ", PositionZ=" + PositionZ + "]";
	}

	public void updateShape() {
		switch(this.Shape) {
		case KSHAPE_BOX:
			this.displayShape = new Box(1,1,1);
			this.displayShape.setDrawMode(DrawMode.FILL);
			break;
		case KSHAPE_CYLINDER:
			this.displayShape = new Cylinder(1,1);
			this.displayShape.setDrawMode(DrawMode.FILL);
			break;
		case KSHAPE_INVALID:
			this.displayShape = new Box(1,1,1);
			this.displayShape.setDrawMode(DrawMode.LINE);
			break;
		case KSHAPE_MESH:
			if (this.collisionShape != null) {
				((CollisionConvexVertice) this.collisionShape).updateShape();
		        this.displayShape = ((CollisionConvexVertice) this.collisionShape).shape;
		        this.displayShape.setDrawMode(DrawMode.FILL);
		        this.displayShape.setCullFace(CullFace.NONE); // Ensure both sides of the plane are visible
			} else {
				this.displayShape = new Box(1,1,1);
				this.displayShape.setDrawMode(DrawMode.LINE);
			}			
			break;
		case KSHAPE_SPHERE:
			this.displayShape = new Sphere(0.5);
			this.displayShape.setDrawMode(DrawMode.FILL);
			break;
		case KSHAPE_TYPE_COUNT:
			this.displayShape = new Box(1,1,1);
			this.displayShape.setDrawMode(DrawMode.LINE);
			break;
		default:
			this.displayShape = new Box(1,1,1);
			this.displayShape.setDrawMode(DrawMode.LINE);
			break;
		}
		if (this.Shape != BoundShape.KSHAPE_MESH || this.collisionShape == null){
			this.displayShape.setScaleX(HalfDimensionX*2);
			this.displayShape.setScaleY(HalfDimensionY*2);
			this.displayShape.setScaleZ(HalfDimensionZ*2);
			this.displayShape.setTranslateX(PositionX);
			this.displayShape.setTranslateY(PositionY - CollisionsEditor.mainCollisions.Z); 
			this.displayShape.setTranslateZ(PositionZ - CollisionsEditor.mainCollisions.X);
		} else {
			this.displayShape.setTranslateY(- CollisionsEditor.mainCollisions.Z); 
			this.displayShape.setTranslateZ(- CollisionsEditor.mainCollisions.X);
		}
		this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, 0.4)));

			
		if (this.shapeTransform != null) {
			double d = Math.acos((shapeTransform.XRotationX+shapeTransform.YRotationY+shapeTransform.ZRotationZ-1d)/2d);
		    if(d!=0d){
		        double den=2d*Math.sin(d);
		        Point3D p= new Point3D((shapeTransform.YRotationZ-shapeTransform.ZRotationY)/den,
		        		(shapeTransform.ZRotationX-shapeTransform.XRotationZ)/den,
		        		(shapeTransform.XRotationY-shapeTransform.YRotationX)/den);
		        displayShape.setRotationAxis(p);
		        displayShape.setRotate(Math.toDegrees(d));                    
		    }
		}
		
		this.displayPivot.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, 1)));
		this.displayPivot.setTranslateX(PivotX);
		this.displayPivot.setTranslateY(PivotY);
		this.displayPivot.setTranslateZ(PivotZ);
		this.displayPivot.setViewOrder(0);
		
		this.displayShape.setOnMouseEntered(e -> {
			System.out.println("CollisionBound | Index : "+ (CollisionsEditor.mainCollisions.childBounds.indexOf(this)+1) +",Name : "+this.NameHash.label+", SurfaceName : "+this.SurfaceName.label+", AttributeName : "+this.AttributeName.label
					+"\nType : "+this.Type+", Shape : "+Shape+", Node : "+NodeType+", Constraint : "+ConstraintType+", Joint : "+JointType);			
		});
		this.displayShape.setOnMouseClicked(e->{
			this.render = false;
			CollisionsEditor.updateRender();
		});
		this.displayPivot.setOnMouseClicked(e -> {
			this.render = true;
			CollisionsEditor.updateRender();
		});
	}
	
	public static CollisionBound load(ByteBuffer bb) {
		CollisionBound ret = new CollisionBound();
		ret.OrientationX = bb.getFloat();
		ret.OrientationY = bb.getFloat();
		ret.OrientationZ = bb.getFloat();
		ret.OrientationW = bb.getFloat()*180;
		ret.PositionX = bb.getFloat();
		ret.PositionY = bb.getFloat();
		ret.PositionZ = bb.getFloat();
		bb.getInt(); //PositionW
		ret.HalfDimensionX = bb.getFloat();
		ret.HalfDimensionY = bb.getFloat();
		ret.HalfDimensionZ = bb.getFloat();
		bb.getInt(); //HalfDimensionW
		ret.PivotX = bb.getFloat();
		ret.PivotY = bb.getFloat();
		ret.PivotZ = bb.getFloat();
		bb.getInt(); //PivotW
		ret.BoneOffsetX = bb.getFloat();
		ret.BoneOffsetY = bb.getFloat();
		ret.BoneOffsetZ = bb.getFloat();
		bb.getInt(); //BoneOffsetW
		ret.Type = BoundType.get(bb.get()); //BoundType
		bb.get(); //BoundSubType
		ret.Shape = BoundShape.get(bb.get()); //BoundShape
		ret.Flags = BoundFlags.get(bb.get()); //BoundFlags
		ret.AttributeName = Hash.guess(bb.getInt(), Collisions.commonCollisionHashes, "[unknown AttributeName]", "VLT"); //AttributeName VLT HASH
		ret.SurfaceName = Hash.guess(bb.getInt(), Collisions.commonCollisionHashes, "[unknown SurfaceName]", "VLT"); //SurfaceName VLT HASH
		ret.NameHash = Hash.guess(bb.getInt(), Collisions.commonCollisionHashes, "[unknown NameHash]", "VLT"); //NameHash VLT HASH
		ret.BoneIndex = bb.getShort();
		ret.RenderHierarchyIndex = bb.getShort();
		bb.position(bb.position()+24);
		ret.NumberOfChildren = bb.get(); //something 1 -> NumberOfChildren ? its a byte tho
		bb.position(bb.position()+6);
		ret.ChildrenFlags = BoundFlags.get(bb.get());
		bb.position(bb.position()+12);
		ret.updateShape();
		return ret;
	}
	
 	public enum BoundFlags {
	    KFLAG_NONE((byte) 0, "KFLAG_NONE"),
	    KFLAG_DISABLED((byte) 1, "KFLAG_DISABLED"),
	    KFLAG_INTERNAL((byte) 2, "KFLAG_INTERNAL"),
	    KFLAG_JOINT_INVERT((byte) 4, "KFLAG_JOINT_INVERT"),
	    KFLAG_NO_COLLISION_GROUND((byte) 32, "KFLAG_NO_COLLISION_GROUND"),
	    KFLAG_NO_COLLISION_BARRIER((byte) 64, "KFLAG_NO_COLLISION_BARRIER"),
	    KFLAG_NO_COLLISION_OBJECT((byte) 128, "KFLAG_NO_COLLISION_OBJECT"),
	    KFLAG_NO_COLLISION_MASK((byte) 224, "KFLAG_NO_COLLISION_MASK");

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

	    public static BoundFlags get(byte value) {
	        for (BoundFlags s : values()) {
	            if (s.value == value) return s;
	        }
	        return KFLAG_NONE; // Handle invalid value
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
