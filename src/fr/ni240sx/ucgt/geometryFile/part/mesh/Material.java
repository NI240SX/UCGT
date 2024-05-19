package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;

public class Material {

	public int fromVertID = 0;
	public int toVertID = 0;
	public int numVertices = 0;

	public int usageSpecific1 = -1;
	public int usageSpecific2 = -1;
	public int usageSpecific3 = -1;
	
	public byte[] flags = {-128, 81, 0, 0};

	public byte shaderID = 0;
	public ArrayList<Byte> textureIDs = new ArrayList<Byte>();
	
	public int textureHash = 0;
	
	public int materialsListOffset = 0;
	public int verticesDataLength = 0;
	
	public ShaderUsage shaderUsage = ShaderUsage.Diffuse;
	public ArrayList<TextureUsage> textureUsages = new ArrayList<TextureUsage>();
	public ArrayList<Integer> textures = new ArrayList<Integer>();
	
	public Vertices verticesBlock;
	public List<Triangle> triangles;
}
