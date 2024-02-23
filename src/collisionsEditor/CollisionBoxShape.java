package collisionsEditor;

import java.nio.ByteBuffer;

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
		return "\n -CollisionBoxShape [HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY + ", HalfExtentsZ="
				+ HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", unknownFloat=" + unknownFloat + "]";
	}

	public static CollisionBoxShape load(ByteBuffer bb) {
		CollisionBoxShape load = new CollisionBoxShape();
		bb.position(bb.position() + 0x10);
		load.unknownFloat = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		load.HalfExtentsX = bb.getFloat();
		load.HalfExtentsY = bb.getFloat();
		load.HalfExtentsZ = bb.getFloat();
		load.HalfExtentsW = bb.getFloat();
		return load;
	}

}
