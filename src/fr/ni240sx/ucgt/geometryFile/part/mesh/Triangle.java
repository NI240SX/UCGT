package fr.ni240sx.ucgt.geometryFile.part.mesh;

public class Triangle {
	public short vert1 = 0;
	public short vert2 = 0;
	public short vert3 = 0;
	
	@Override
	public String toString() {
		return "Triangle [vert1=" + vert1 + ", vert2=" + vert2 + ", vert3=" + vert3 + "]";
	}
	
	public Triangle() {
	}
	public Triangle(short v1, short v2, short v3) {
		this.vert1 = v1;
		this.vert2 = v2;
		this.vert3 = v3;
	}
}
