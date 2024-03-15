package collisionsEditor;

import java.nio.ByteBuffer;

public class CollisionSphereShape extends CollisionShape {

	float unknownFloat = (float) 0.05;
		
	public CollisionSphereShape() {
		// TODO Auto-generated constructor stub
	}

	public CollisionSphereShape(float unknownFloat) {
		this.unknownFloat = unknownFloat;
	}

	public static CollisionSphereShape load(ByteBuffer bb) {
		CollisionSphereShape load = new CollisionSphereShape();
		bb.position(bb.position() + 0x10);
		load.unknownFloat = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		return load;
	}
}
