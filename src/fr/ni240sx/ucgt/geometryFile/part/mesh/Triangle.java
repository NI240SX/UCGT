package fr.ni240sx.ucgt.geometryFile.part.mesh;

public class Triangle {
	public short vert0 = 0;
	public short vert1 = 0;
	public short vert2 = 0;
	
	@Override
	public String toString() {
		return "Triangle [vert1=" + vert0 + ", vert2=" + vert1 + ", vert3=" + vert2 + "]";
	}
	
	public Triangle() {
	}
	public Triangle(short v1, short v2, short v3) {
		this.vert0 = v1;
		this.vert1 = v2;
		this.vert2 = v3;
	}
}
