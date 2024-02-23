package collisionsEditor;

import java.nio.ByteBuffer;

public class RotatedVertice {

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
