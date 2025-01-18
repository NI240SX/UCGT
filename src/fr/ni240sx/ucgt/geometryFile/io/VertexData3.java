package fr.ni240sx.ucgt.geometryFile.io;

public class VertexData3{
	public float x;
	public float y;
	public float z;

	public VertexData3(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public VertexData3(String string, String string2, String string3) {
		x = Float.parseFloat(string);
		y = Float.parseFloat(string2);
		z = Float.parseFloat(string3);
	}
}