package fr.ni240sx.ucgt.geometryFile.io;

public class VertexData6 extends VertexData3{
	double x, y, z, a, b, c;

	public VertexData6(double x, double y, double z, double a, double b, double c) {
		super(x, y, z);
		this.a = a;
		this.b = b;
		this.c = c;

	}

	public VertexData6(String x, String y, String z, String a, String b, String c) {
		super(x, y, z);
		this.a = Double.parseDouble(a);
		this.b = Double.parseDouble(b);
		this.c = Double.parseDouble(c);
	}
}