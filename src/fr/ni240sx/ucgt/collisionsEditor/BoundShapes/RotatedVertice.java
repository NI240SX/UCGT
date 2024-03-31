package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;

public class RotatedVertice {

	public float XRotationX = 0;
	public float XRotationY = 0;
	public float XRotationZ = 0;
	public float XRotationW = 0;

	public float YRotationX = 0;
	public float YRotationY = 0;
	public float YRotationZ = 0;
	public float YRotationW = 0;

	public float ZRotationX = 0;
	public float ZRotationY = 0;
	public float ZRotationZ = 0;
	public float ZRotationW = 0;
	
	public RotatedVertice() {
		// TODO Auto-generated constructor stub
	}

	public RotatedVertice(float xRotationX, float xRotationY, float xRotationZ, float xRotationW, float yRotationX,
			float yRotationY, float yRotationZ, float yRotationW, float zRotationX, float zRotationY, float zRotationZ,
			float zRotationW) {
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
	}

	public RotatedVertice(ByteBuffer bb) {
        this.XRotationX = bb.getFloat();
        this.XRotationY = bb.getFloat();
        this.XRotationZ = bb.getFloat();
        this.XRotationW = bb.getFloat();
        this.YRotationX = bb.getFloat();
        this.YRotationY = bb.getFloat();
        this.YRotationZ = bb.getFloat();
        this.YRotationW = bb.getFloat();
        this.ZRotationX = bb.getFloat();
        this.ZRotationY = bb.getFloat();
        this.ZRotationZ = bb.getFloat();
        this.ZRotationW = bb.getFloat();
	}

	@Override
	public String toString() {
		return "\n    -RotatedVertice [XRotationX=" + XRotationX + ", XRotationY=" + XRotationY + ", XRotationZ=" + XRotationZ
				+ ", XRotationW=" + XRotationW + ", YRotationX=" + YRotationX + ", YRotationY=" + YRotationY
				+ ", YRotationZ=" + YRotationZ + ", YRotationW=" + YRotationW + ", ZRotationX=" + ZRotationX
				+ ", ZRotationY=" + ZRotationY + ", ZRotationZ=" + ZRotationZ + ", ZRotationW=" + ZRotationW + "]";
	}

}
