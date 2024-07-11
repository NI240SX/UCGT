package fr.ni240sx.ucgt.geometryFile.part.mesh;

public class Triangle {
	public int vert0 = 0; //using ints to get around signed/unsigned issues
	public int vert1 = 0;
	public int vert2 = 0;
	
	@Override
	public String toString() {
		return "Triangle [vert1=" + vert0 + ", vert2=" + vert1 + ", vert3=" + vert2 + "]";
	}
	
	public Triangle() {
	}
	public Triangle(int v1, int v2, int v3) {
		this.vert0 = v1;
		this.vert1 = v2;
		this.vert2 = v3;
	}
}
