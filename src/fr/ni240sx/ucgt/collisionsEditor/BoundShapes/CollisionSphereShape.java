package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.collisionsEditor.CollisionBound;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;

public class CollisionSphereShape extends CollisionShape {

	public float radius = (float) 0.05;
	
	public Shape3D displayShape = new Sphere(0.5);

	public CollisionBound bound = null;
	
	public CollisionSphereShape() {
		// TODO Auto-generated constructor stub
	}

	public CollisionSphereShape(float unknownFloat) {
		this.radius = unknownFloat;
	}

	public CollisionSphereShape(CollisionBound bound2) {
		bound = bound2;
	}

	public static CollisionSphereShape load(ByteBuffer bb) {
		CollisionSphereShape load = new CollisionSphereShape();
		bb.position(bb.position() + 0x10);
		load.radius = bb.getFloat();
		bb.position(bb.position() + 0x0C);
		return load;
	}
	
	public void updateShape(double colorR, double colorG, double colorB, double d) {
		this.displayShape.setScaleX(radius*2);
		this.displayShape.setScaleY(radius*2);
		this.displayShape.setScaleZ(radius*2);
		this.displayShape.setMaterial(new PhongMaterial(Color.color(colorR, colorG, colorB, d)));
	}

	public byte[] save() {
		var bb = ByteBuffer.wrap(new byte[4+0x10+0x0C]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(new byte[0x10]);
		bb.putFloat(radius);
		bb.put(new byte[0x0C]);
		return bb.array();
	}

	@Override
	public CollisionShape deepCopy(CollisionBound bound) {
		var copy = new CollisionSphereShape();
		copy.radius = this.radius;
		copy.bound = bound;
		return copy;
	}
}
