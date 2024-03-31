package fr.ni240sx.ucgt.collisionsEditor.BoundShapes;

import java.nio.ByteBuffer;

public class PlaneEquation {

	public float X = 0;
	public float Y = 0;
	public float Z = 0;
	public float W = 0;
	
	public PlaneEquation() {
		// TODO Auto-generated constructor stub
	}

	public PlaneEquation(ByteBuffer bb) {
		this.X = bb.getFloat();
		this.Y = bb.getFloat();
		this.Z = bb.getFloat();
		this.W = bb.getFloat();
	}

	@Override
	public String toString() {
		return "\n    -PlaneEquation [X=" + X + ", Y=" + Y + ", Z=" + Z + ", W=" + W + "]";
	}

}
