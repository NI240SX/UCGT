package fr.ni240sx.ucgt.geometryFile.io;

public class VertexData3{
	double x, y, z;

	public VertexData3(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public VertexData3(String string, String string2, String string3) {
		x = Double.parseDouble(string);
		y = Double.parseDouble(string2);
		z = Double.parseDouble(string3);
	}
}