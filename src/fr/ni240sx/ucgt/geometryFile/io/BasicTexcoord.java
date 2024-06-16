package fr.ni240sx.ucgt.geometryFile.io;

public class BasicTexcoord{
	float u, v;

	public BasicTexcoord(float u, float v) {
		super();
		this.u = u;
		this.v = v;
	}

	public BasicTexcoord(String string, String string2) {
		u = Float.parseFloat(string);
		v = Float.parseFloat(string2);
	}
}