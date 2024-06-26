package fr.ni240sx.ucgt.geometryFile.io;

public class TexcoordData2{
	float u, v;

	public TexcoordData2(float u, float v) {
		super();
		this.u = u;
		this.v = v;
	}

	public TexcoordData2(String string, String string2) {
		u = Float.parseFloat(string);
		v = Float.parseFloat(string2);
	}
}