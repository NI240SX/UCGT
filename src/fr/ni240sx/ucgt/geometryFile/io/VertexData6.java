package fr.ni240sx.ucgt.geometryFile.io;

public class VertexData6 extends VertexData3{
	double a, b, c;

	public VertexData6(float x, float y, float z, float a, float b, float c) {
		super(x, y, z);
		this.a = a;
		this.b = b;
		this.c = c;

	}

	public VertexData6(String x, String y, String z, String a, String b, String c) {
		super(x, y, z);
		this.a = Float.parseFloat(a);
		this.b = Float.parseFloat(b);
		this.c = Float.parseFloat(c);
	}
}