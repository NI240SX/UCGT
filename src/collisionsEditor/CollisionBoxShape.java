package collisionsEditor;

public class CollisionBoxShape {

	float HalfExtentsX = (float) 0.06;
	float HalfExtentsY = (float) 0.06;
	float HalfExtentsZ = (float) 0.06;
	float HalfExtentsW = (float) 0.06;
	
	float unknownFloat = (float) 0.05;
	
	public CollisionBoxShape() {
		// TODO Auto-generated constructor stub
	}

	public CollisionBoxShape(float halfExtentsX, float halfExtentsY, float halfExtentsZ, float halfExtentsW,
			float unknownFloat) {
		HalfExtentsX = halfExtentsX;
		HalfExtentsY = halfExtentsY;
		HalfExtentsZ = halfExtentsZ;
		HalfExtentsW = halfExtentsW;
		this.unknownFloat = unknownFloat;
	}

	@Override
	public String toString() {
		return "CollisionBoxShape [HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY + ", HalfExtentsZ="
				+ HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", unknownFloat=" + unknownFloat + "]";
	}

}
