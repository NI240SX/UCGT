package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CollisionConvexTransform {

	// int or float ???
	public int NumVertices = 0;

	public float TranslationX = 0;
	public float TranslationY = 0;
	public float TranslationZ = 0;
	public float TranslationW = 0;

	public float XRotationX = 0;
	public float XRotationY = 0;
	public float XRotationZ = 0;
	public float XRotationW = 0;

	public float YRotationX = 0;
	public float YRotationY = 0;
	public float YRotationZ = 0;
	public float YRotationW = 0;

	public float ZRotationX = 0;
	public float ZRotationY = 0;
	public float ZRotationZ = 0;
	public float ZRotationW = 0;

	public float boundDistance = (float) 0.05;
	
	public CollisionConvexTransform() {
		// TODO Auto-generated constructor stub
	}

	public CollisionConvexTransform(int numVertices, float translationX, float translationY, float translationZ,
			float translationW, float xRotationX, float xRotationY, float xRotationZ, float xRotationW,
			float yRotationX, float yRotationY, float yRotationZ, float yRotationW, float zRotationX, float zRotationY,
			float zRotationZ, float zRotationW, float unknownFloat) {
		NumVertices = numVertices;
		TranslationX = translationX;
		TranslationY = translationY;
		TranslationZ = translationZ;
		TranslationW = translationW;
		XRotationX = xRotationX;
		XRotationY = xRotationY;
		XRotationZ = xRotationZ;
		XRotationW = xRotationW;
		YRotationX = yRotationX;
		YRotationY = yRotationY;
		YRotationZ = yRotationZ;
		YRotationW = yRotationW;
		ZRotationX = zRotationX;
		ZRotationY = zRotationY;
		ZRotationZ = zRotationZ;
		ZRotationW = zRotationW;
		this.boundDistance = unknownFloat;
	}

	@Override
	public String toString() {
		return "\n -CollisionConvexTransform [NumVertices=" + NumVertices + ", TranslationX=" + TranslationX
				+ ", TranslationY=" + TranslationY + ", TranslationZ=" + TranslationZ + ", TranslationW=" + TranslationW
				+ ", XRotationX=" + XRotationX + ", XRotationY=" + XRotationY + ", XRotationZ=" + XRotationZ
				+ ", XRotationW=" + XRotationW + ", YRotationX=" + YRotationX + ", YRotationY=" + YRotationY
				+ ", YRotationZ=" + YRotationZ + ", YRotationW=" + YRotationW + ", ZRotationX=" + ZRotationX
				+ ", ZRotationY=" + ZRotationY + ", ZRotationZ=" + ZRotationZ + ", ZRotationW=" + ZRotationW
				+ ", unknownFloat=" + boundDistance + "]";
	}

	public static CollisionConvexTransform load(ByteBuffer bb) {
		CollisionConvexTransform load = new CollisionConvexTransform();
		bb.position(bb.position() + 0x10);
		load.boundDistance = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		load.XRotationX = bb.getFloat();
		load.XRotationY = bb.getFloat();
		load.XRotationZ = bb.getFloat();
		load.XRotationW = bb.getFloat();
		load.YRotationX = bb.getFloat();
		load.YRotationY = bb.getFloat();
		load.YRotationZ = bb.getFloat();
		load.YRotationW = bb.getFloat();
		load.ZRotationX = bb.getFloat();
		load.ZRotationY = bb.getFloat();
		load.ZRotationZ = bb.getFloat();
		load.ZRotationW = bb.getFloat();
		load.TranslationX = bb.getFloat();
		load.TranslationY = bb.getFloat();
		load.TranslationZ = bb.getFloat();
		load.TranslationW = bb.getFloat();
		return load;
	}

	
	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[68+0x10+0x0C]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(new byte[0x10]);
		bb.putFloat(boundDistance);
		bb.put(new byte[0x0C]);
		bb.putFloat(XRotationX);
		bb.putFloat(XRotationY);
		bb.putFloat(XRotationZ);
		bb.putFloat(XRotationW);
		bb.putFloat(YRotationX);
		bb.putFloat(YRotationY);
		bb.putFloat(YRotationZ);
		bb.putFloat(YRotationW);
		bb.putFloat(ZRotationX);
		bb.putFloat(ZRotationY);
		bb.putFloat(ZRotationZ);
		bb.putFloat(ZRotationW);
		bb.putFloat(TranslationX);
		bb.putFloat(TranslationY);
		bb.putFloat(TranslationZ);
		bb.putFloat(TranslationW);
		return bb.array();
	}

	
	public CollisionConvexTransform deepCopy() {
		CollisionConvexTransform copy = new CollisionConvexTransform();
		copy.boundDistance = this.boundDistance;
		copy.NumVertices = this.NumVertices;
		copy.TranslationX = this.TranslationX;
		copy.TranslationY = this.TranslationY;
		copy.TranslationZ = this.TranslationZ;
		copy.TranslationW = this.TranslationW;
		copy.XRotationX = this.XRotationX;
		copy.XRotationY = this.XRotationY;
		copy.XRotationZ = this.XRotationZ;
		copy.XRotationW = this.XRotationW;
		copy.YRotationX = this.YRotationX;
		copy.YRotationY = this.YRotationY;
		copy.YRotationZ = this.YRotationZ;
		copy.YRotationW = this.YRotationW;
		copy.ZRotationX = this.ZRotationX;
		copy.ZRotationY = this.ZRotationY;
		copy.ZRotationZ = this.ZRotationZ;
		copy.ZRotationW = this.ZRotationW;
		
		return copy;
	}

}
