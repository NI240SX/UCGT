package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;

public class CollisionBoxShape extends CollisionShape {

	public float HalfExtentsX = (float) 0.06;
	public float HalfExtentsY = (float) 0.06;
	public float HalfExtentsZ = (float) 0.06;
	public float HalfExtentsW = (float) 0.06;
	
	public float unknownFloat = (float) 0.05;

	public Shape3D displayShape = new Box(1,1,1);
	public boolean render = true;
	
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
