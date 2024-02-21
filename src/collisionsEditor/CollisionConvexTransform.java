package collisionsEditor;

public class CollisionConvexTransform {

	// int or float ???
	int NumVertices = 0;

	float TranslationX = 0;
	float TranslationY = 0;
	float TranslationZ = 0;
	float TranslationW = 0;

	float XRotationX = 0;
	float XRotationY = 0;
	float XRotationZ = 0;
	float XRotationW = 0;

	float YRotationX = 0;
	float YRotationY = 0;
	float YRotationZ = 0;
	float YRotationW = 0;

	float ZRotationX = 0;
	float ZRotationY = 0;
	float ZRotationZ = 0;
	float ZRotationW = 0;

	float unknownFloat = (float) 0.05;
	
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
		this.unknownFloat = unknownFloat;
	}

	@Override
	public String toString() {
		return "CollisionConvexTransform [NumVertices=" + NumVertices + ", TranslationX=" + TranslationX
				+ ", TranslationY=" + TranslationY + ", TranslationZ=" + TranslationZ + ", TranslationW=" + TranslationW
				+ ", XRotationX=" + XRotationX + ", XRotationY=" + XRotationY + ", XRotationZ=" + XRotationZ
				+ ", XRotationW=" + XRotationW + ", YRotationX=" + YRotationX + ", YRotationY=" + YRotationY
				+ ", YRotationZ=" + YRotationZ + ", YRotationW=" + YRotationW + ", ZRotationX=" + ZRotationX
				+ ", ZRotationY=" + ZRotationY + ", ZRotationZ=" + ZRotationZ + ", ZRotationW=" + ZRotationW
				+ ", unknownFloat=" + unknownFloat + "]";
	}

}
