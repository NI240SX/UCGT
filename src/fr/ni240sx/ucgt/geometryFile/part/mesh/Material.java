package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.Geometry;
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
	
	public Hash ShaderHash;
	public Hash DefaultTextureHash;
	public ArrayList<Hash> TextureHashes = new ArrayList<Hash>();
	
	public String uniqueName = "";
	
	public String generateName() {
		String s = ShaderHash.label;
		for (var t : TextureHashes) s += "_"+t.label;
		return s;
	}

	public String toConfig() { //	MATERIAL	SHADER=ShaderUsage[1,2,3] 0xFLAGS000 DEFAULTTEX TEX1=TexUsage TEX2=TexUsage
//		System.out.println("TextureHash "+DefaultTextureHash.label+" : "+String.format("0x%08X", textureHash));
		String s = "";
		s += "MATERIAL	"+uniqueName+"	"+ShaderHash.label+"="+shaderUsage.getName()+"[";
		if (usageSpecific1 != -1) s+=usageSpecific1;
		if (usageSpecific2 != -1) s+=","+usageSpecific2;
		if (usageSpecific3 != -1) s+=","+usageSpecific3;
		s +="]	defTex=" + DefaultTextureHash.label+"	flags=0x"+String.format("%02X",flags[0])+String.format("%02X",flags[1])+String.format("%02X",flags[2])+String.format("%02X",flags[3]);
		for (int i=0; i<textures.size(); i++) {
			s += "	" + textureUsages.get(i).getName() + "=" + TextureHashes.get(i).label;
		}
		return s;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(flags);
		result = prime * result + Objects.hash(shaderUsage, textureHash, textureUsages, textures,
				usageSpecific1, usageSpecific2, usageSpecific3);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Material other = (Material) obj;
		return Arrays.equals(flags, other.flags) && shaderUsage == other.shaderUsage
				&& textureHash == other.textureHash && ShaderHash.binHash == other.ShaderHash.binHash
				&& Objects.equals(textureUsages, other.textureUsages) && Objects.equals(textures, other.textures)
				&& usageSpecific1 == other.usageSpecific1 && usageSpecific2 == other.usageSpecific2
				&& usageSpecific3 == other.usageSpecific3;
	}
}
