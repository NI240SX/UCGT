package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CollisionConvexTranslate {

	public float TranslationX = 0;
	public float TranslationY = 0;
	public float TranslationZ = 0;
	public float TranslationW = 0;
	
	public float boundDistance = (float) 0.05;
	
	public CollisionConvexTranslate() {
		// TODO Auto-generated constructor stub
	}

	public CollisionConvexTranslate(float translationX, float translationY, float translationZ, float translationW, float unknownFloat) {
		TranslationX = translationX;
		TranslationY = translationY;
		TranslationZ = translationZ;
		TranslationW = translationW;
		this.boundDistance = unknownFloat;
	}

	@Override
	public String toString() {
		return "\n -CollisionConvexTranslate [TranslationX=" + TranslationX + ", TranslationY=" + TranslationY
				+ ", TranslationZ=" + TranslationZ + ", TranslationW="+TranslationW+", unknownFloat=" + boundDistance + "]";
	}

	public static CollisionConvexTranslate load(ByteBuffer bb) {
		CollisionConvexTranslate load = new CollisionConvexTranslate();
		bb.position(bb.position() + 0x10);
		load.boundDistance = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		load.TranslationX = bb.getFloat();
		load.TranslationY = bb.getFloat();
		load.TranslationZ = bb.getFloat();
		load.TranslationW = bb.getFloat();
		return load;
	}

	
	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(new byte[0x10]);
		bb.putFloat(boundDistance);
		bb.put(new byte[0x0C]);
		bb.putFloat(TranslationX);
		bb.putFloat(TranslationY);
		bb.putFloat(TranslationZ);
		bb.putFloat(TranslationW);
		return bb.array();
	}

	
	public CollisionConvexTranslate deepCopy() {
		var copy = new CollisionConvexTranslate();
		copy.boundDistance = this.boundDistance;
		copy.TranslationX = this.TranslationX;
		copy.TranslationY = this.TranslationY;
		copy.TranslationZ = this.TranslationZ;
		copy.TranslationW = this.TranslationW;
		return copy;
	}

}
