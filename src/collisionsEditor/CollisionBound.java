package collisionsEditor;

import binstuff.Hash;

public class CollisionBound {

	Hash AttributeName = new Hash("");
	Hash NameHash = new Hash(""); //TODO VLT HASH !!
	Hash SurfaceName;
	
	Object ChildrenFlags;
	Object ConstraintType;
	Object Flags;
	Object JointType;
	Object NodeType;
	Object Shape;
	Object Type;
	
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
	
	public CollisionBound() {
		// TODO Auto-generated constructor stub
	}

	public CollisionBound(Hash attributeName, Hash nameHash, Hash surfaceName, Object childrenFlags,
			Object constraintType, Object flags, Object jointType, Object nodeType, Object shape, Object type,
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

}
