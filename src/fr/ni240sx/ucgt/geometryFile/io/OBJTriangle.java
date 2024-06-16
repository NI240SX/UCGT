package fr.ni240sx.ucgt.geometryFile.io;

import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;

public class OBJTriangle {
	
	public BasicVertex[] verts = new BasicVertex[3];
	public BasicTexcoord[] texcoords = new BasicTexcoord[3];
	public BasicVertex[] normals = new BasicVertex[3];
	public Material mat;
}
