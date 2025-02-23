package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.collisionsEditor.CollisionBound;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;

public class CollisionBoxShape extends CollisionShape {

	public float HalfExtentsX = (float) 0.06;
	public float HalfExtentsY = (float) 0.06;
	public float HalfExtentsZ = (float) 0.06;
	public float HalfExtentsW = (float) 0.06;
	
	public float boundDistance = (float) 0.05;

	public Shape3D displayShape = new Box(1,1,1);
//	public boolean render = true;
	public CollisionBound bound = null;
	
	public CollisionBoxShape() {
		// TODO Auto-generated constructor stub
	}

	public CollisionBoxShape(float halfExtentsX, float halfExtentsY, float halfExtentsZ, float halfExtentsW,
			float unknownFloat) {
		HalfExtentsX = halfExtentsX;
		HalfExtentsY = halfExtentsY;
		HalfExtentsZ = halfExtentsZ;
		HalfExtentsW = halfExtentsW;
		this.boundDistance = unknownFloat;
	}

	public CollisionBoxShape(CollisionBound bound) {
		this.bound = bound;
	}

	@Override
	public String toString() {
		return "\n -CollisionBoxShape [HalfExtentsX=" + HalfExtentsX + ", HalfExtentsY=" + HalfExtentsY + ", HalfExtentsZ="
				+ HalfExtentsZ + ", HalfExtentsW=" + HalfExtentsW + ", unknownFloat=" + boundDistance + "]";
	}

	public static CollisionBoxShape load(ByteBuffer bb) {
		CollisionBoxShape load = new CollisionBoxShape();
		bb.position(bb.position() + 0x10);
		load.boundDistance = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		load.HalfExtentsX = bb.getFloat();
		load.HalfExtentsY = bb.getFloat();
		load.HalfExtentsZ = bb.getFloat();
		load.HalfExtentsW = bb.getFloat();
		return load;
	}

	public void updateShape(double colorR, double colorG, double colorB, double d) {
		this.displayShape.setScaleX(HalfExtentsX*2);
		this.displayShape.setScaleY(HalfExtentsY*2);
		this.displayShape.setScaleZ(HalfExtentsZ*2);
		this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, d)));
	}


	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[20+0x10+0x0C]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(new byte[0x10]);
		bb.putFloat(boundDistance);
		bb.put(new byte[0x0C]);
		bb.putFloat(HalfExtentsX);
		bb.putFloat(HalfExtentsY);
		bb.putFloat(HalfExtentsZ);
		bb.putFloat(HalfExtentsW);
		return bb.array();
	}


	@Override
	public CollisionShape deepCopy(CollisionBound bound) {
		var copy = new CollisionBoxShape();
		copy.boundDistance = this.boundDistance;
		copy.HalfExtentsX  = this.HalfExtentsX;
		copy.HalfExtentsY  = this.HalfExtentsY;
		copy.HalfExtentsZ  = this.HalfExtentsZ;
		copy.HalfExtentsW  = this.HalfExtentsW;
		copy.bound = bound;
		return copy;
	}
}
