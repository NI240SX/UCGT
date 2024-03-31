package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;

public class CollisionConvexTranslate {

	public float TranslationX = 0;
	public float TranslationY = 0;
	public float TranslationZ = 0;
	public float TranslationW = 0;
	
	public float unknownFloat = (float) 0.05;
	
	public CollisionConvexTranslate() {
		// TODO Auto-generated constructor stub
	}

	public CollisionConvexTranslate(float translationX, float translationY, float translationZ, float translationW, float unknownFloat) {
		TranslationX = translationX;
		TranslationY = translationY;
		TranslationZ = translationZ;
		TranslationW = translationW;
		this.unknownFloat = unknownFloat;
	}

	@Override
	public String toString() {
		return "\n -CollisionConvexTranslate [TranslationX=" + TranslationX + ", TranslationY=" + TranslationY
				+ ", TranslationZ=" + TranslationZ + ", TranslationW="+TranslationW+", unknownFloat=" + unknownFloat + "]";
	}

	public static CollisionConvexTranslate load(ByteBuffer bb) {
		CollisionConvexTranslate load = new CollisionConvexTranslate();
		bb.position(bb.position() + 0x10);
		load.unknownFloat = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		load.TranslationX = bb.getFloat();
		load.TranslationY = bb.getFloat();
		load.TranslationZ = bb.getFloat();
		load.TranslationW = bb.getFloat();
		return load;
	}

}
