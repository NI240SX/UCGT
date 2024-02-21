package collisionsEditor;

public class CollisionConvexTranslate {

	float TranslationX = 0;
	float TranslationY = 0;
	float TranslationZ = 0;
	
	float unknownFloat = (float) 0.05;
	
	public CollisionConvexTranslate() {
		// TODO Auto-generated constructor stub
	}

	public CollisionConvexTranslate(float translationX, float translationY, float translationZ, float unknownFloat) {
		TranslationX = translationX;
		TranslationY = translationY;
		TranslationZ = translationZ;
		this.unknownFloat = unknownFloat;
	}

	@Override
	public String toString() {
		return "CollisionConvexTranslate [TranslationX=" + TranslationX + ", TranslationY=" + TranslationY
				+ ", TranslationZ=" + TranslationZ + ", unknownFloat=" + unknownFloat + "]";
	}

}
