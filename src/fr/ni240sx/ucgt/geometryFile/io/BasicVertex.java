package fr.ni240sx.ucgt.geometryFile.io;

public class BasicVertex{
	float x, y, z;

	public BasicVertex(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BasicVertex(String string, String string2, String string3) {
		x = Float.parseFloat(string);
		y = Float.parseFloat(string2);
		z = Float.parseFloat(string3);
	}
}