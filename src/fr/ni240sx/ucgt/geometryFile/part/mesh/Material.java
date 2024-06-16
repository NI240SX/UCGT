package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;

public class Material {
	
	public int fromTriVertID = 0;
	public int toTriVertID = 0;
	public int numTriVertices = 0;

	public int usageSpecific1 = -1; //apparently unused
	public int usageSpecific2 = -1; //apparently unused
	public int usageSpecific3 = -1; //apparently unused
	
	public byte[] flags = {0, 0, 0, 0}; //apparently unused -128 81 0 0

	public byte shaderID = 0;
	public ArrayList<Byte> textureIDs = new ArrayList<Byte>();
	
	public int textureHash = 0; //apparently unused
	
	public int frontendRenderingData = 0; //controls FE rendering somehow
	public int verticesDataLength = 0;
	
	public ShaderUsage shaderUsage = ShaderUsage.Diffuse;
	public ArrayList<TextureUsage> textureUsages = new ArrayList<TextureUsage>();
	public ArrayList<Integer> textures = new ArrayList<Integer>(); //apparently unused
	
	public Vertices verticesBlock;
	public List<Triangle> triangles = new ArrayList<Triangle>();
	
	public Hash ShaderHash;
	public Hash DefaultTextureHash;
	public ArrayList<Hash> TextureHashes = new ArrayList<Hash>();
	
	public String uniqueName = "";
	
	public Material(Material m) {
		this.ShaderHash = m.ShaderHash;
		this.shaderUsage = m.shaderUsage;
		for (var h : m.TextureHashes) this.TextureHashes.add(h);
		for (var u : m.textureUsages) this.textureUsages.add(u);
		this.uniqueName = m.uniqueName;
	}

	public Material() {
	}

	public static Material getFallbackMaterial() {
		Material m = new Material();
		m.ShaderHash = new Hash("DULLPLASTIC");
		m.shaderUsage = ShaderUsage.Diffuse;
		m.TextureHashes.add(new Hash("DEFAULTTEXTURE"));
		m.textureUsages.add(TextureUsage.DIFFUSE);
		return m;
	}

	public void tryGuessHashes(Geometry geometry, Part p) {
		ShaderHash = Hash.guess(p.shaderlist.shaders.get(shaderID), geometry.hashlist, String.format("0x%08X", p.shaderlist.shaders.get(shaderID)), "BIN");
//		DefaultTextureHash = Hash.guess(textureHash, geometry.hashlist, String.format("0x%08X",textureHash), "BIN"); //apparently unused
//		for (var t : textures) {
//			TextureHashes.add(Hash.guess(t, geometry.hashlist, String.format("0x%08X",t), "BIN"));
//		}
		for (var t : textureIDs) { //this is actually what's being done by the game
			TextureHashes.add(Hash.guess(p.texusage.texusage.get(t).getKey(), geometry.hashlist, String.format("0x%08X",p.texusage.texusage.get(t).getKey()), "BIN"));
		}
	}
	
	public void tryGuessUsageSpecific() { // TODO find what ACTUALLY controls this value
		usageSpecific1 = -1;
		usageSpecific2 = -1;
		usageSpecific3 = -1;
		
		if (textureUsages.size() == 2) usageSpecific1 = 1; //diffuse+smth else
		else if (textureUsages.size() == 3) {
			// alpha normal 3 1
			// normal swatch 1 2
			// alpha swatch 1 2
			if (textureUsages.contains(TextureUsage.ALPHA) && textureUsages.contains(TextureUsage.NORMAL)) {
				usageSpecific1 = 3;
				usageSpecific2 = 1;
			} else {
				usageSpecific1 = 1;
				usageSpecific2 = 2;				
			}
		} else if (textureUsages.size() == 4) {
			usageSpecific1 = 1;
			usageSpecific2 = 2;
			usageSpecific3 = 3;			
		}
	}

	public void tryGuessFlags(Geometry geometry) {
		flags[0] = (byte) 0x80;
		flags[1] = (byte) 0x51;
		flags[2] = (byte) 0x00;
		flags[3] = (byte) 0x00;

		//low quality rendering or smth
		if (ShaderHash.label.equals("PLAINNOTHING") || ShaderHash.label.equals("INTERIOR") || ShaderHash.label.equals("CHASSIS")) flags[1] -= (byte) 0x01;
		
		//reflections ? shadows ? METAL_SWATCH, %_MISC, REGPAINTBLACK, SIRENS, BLACK
		if (//ShaderHash.label.equals("CARSKIN") ||
				ShaderHash.label.equals("CARBONFIBER") || ShaderHash.label.equals("HEADLIGHTGLASS") ||
				ShaderHash.label.equals("BRAKELIGHTGLASS") || ShaderHash.label.equals("BRAKELIGHTGLASSRED") ||
				TextureHashes.get(0).label.equals("CARBONFIBRE_PLACEHOLDER") || TextureHashes.get(0).label.equals("CARBONFIBRE") || TextureHashes.get(0).label.equals("METAL_SWATCH") ||
				TextureHashes.get(0).label.equals(geometry.carname+"_MISC") || TextureHashes.get(0).label.equals("BLACK") || 
				TextureHashes.get(0).label.equals("SIRENS")) flags[2] += (byte) 0x01;
		
		//normals
		if (textureUsages.contains(TextureUsage.NORMAL)) flags[2] += (byte) 0xA2;
		
		//idk
		if(!textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x22;
		if(textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x02;
	}
//	
//	public void tryGuessDefaultTex() {
//		textureHash = textures.get(0);
//		DefaultTextureHash = TextureHashes.get(0);
//		
//	}
	
	public void tryGuessFEData(HashMap<ShaderUsage, Integer> FERenderData) {
		if (FERenderData.containsKey(this.shaderUsage)) {
			this.frontendRenderingData = FERenderData.get(shaderUsage);
		} else {
			this.frontendRenderingData = FERenderData.size()*256;
			FERenderData.put(shaderUsage, this.frontendRenderingData);
		}
	}

	public void removeUnneeded() {
		usageSpecific1 = -1;
		usageSpecific2 = -1;
		usageSpecific3 = -1;
		flags[0] = (byte) 0x00;
		flags[1] = (byte) 0x00;
		flags[2] = (byte) 0x00;
		flags[3] = (byte) 0x00;
		textureHash = 0; //we apparently can not give a shit about those ???
		for (int i=0; i<textures.size(); i++) {
			textures.set(i, 0);
		}
	}
	
	public String generateName() {
		String s = ShaderHash.label;
		for (var t : TextureHashes) s += "_"+t.label;
		return s;
	}

	public String toConfig(String carname) { //	MATERIAL	SHADER=ShaderUsage[1,2,3] 0xFLAGS000 DEFAULTTEX TEX1=TexUsage TEX2=TexUsage
		String s = "";
		s += "MATERIAL	"+uniqueName;
//		s += " feRenderData=" + frontendRenderingData;
		s += "	"+ShaderHash.label+"="+shaderUsage.getName();
		//+"[";
//		if (usageSpecific1 != -1) s+=usageSpecific1;
//		if (usageSpecific2 != -1) s+=","+usageSpecific2;
//		if (usageSpecific3 != -1) s+=","+usageSpecific3;
//		s +="]	defTex=" + DefaultTextureHash.label+"	flags=0x"+String.format("%02X",flags[0])+String.format("%02X",flags[1])+String.format("%02X",flags[2])+String.format("%02X",flags[3]);
		for (int i=0; i<textureUsages.size(); i++) {
			s += "	" + TextureHashes.get(i).label.replace(carname, "%") + "=" + textureUsages.get(i).getName();
		}
		return s;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(ShaderHash, TextureHashes, shaderUsage, textureUsages);
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
		return Objects.equals(ShaderHash, other.ShaderHash) && Objects.equals(TextureHashes, other.TextureHashes)
				&& shaderUsage == other.shaderUsage && Objects.equals(textureUsages, other.textureUsages);
	}

	
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Material other = (Material) obj;
//		return Arrays.equals(flags, other.flags) && shaderUsage == other.shaderUsage
//				&& textureHash == other.textureHash && ShaderHash.binHash == other.ShaderHash.binHash
//				&& Objects.equals(textureUsages, other.textureUsages) && Objects.equals(textures, other.textures)
//				&& usageSpecific1 == other.usageSpecific1 && usageSpecific2 == other.usageSpecific2
//				&& usageSpecific3 == other.usageSpecific3;
//	}

}
