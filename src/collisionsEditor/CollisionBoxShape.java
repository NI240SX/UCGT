package collisionsEditor;

import java.nio.ByteBuffer;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

public class CollisionBoxShape extends CollisionShape {

	float HalfExtentsX = (float) 0.06;
	float HalfExtentsY = (float) 0.06;
	float HalfExtentsZ = (float) 0.06;
	float HalfExtentsW = (float) 0.06;
	
	float unknownFloat = (float) 0.05;

	Shape3D displayShape = new Box(1,1,1);
	boolean render = true;
	
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
		updateShape();
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
		load.updateShape();
		return load;
	}

	
	public void updateShape() {
		this.displayShape.setScaleX(HalfExtentsX*2);
		this.displayShape.setScaleY(HalfExtentsY*2);
		this.displayShape.setScaleZ(HalfExtentsZ*2);
		this.displayShape.setMaterial(new PhongMaterial(Color.color(Math.random(), Math.random(), Math.random(), 0.4)));
	}
}
