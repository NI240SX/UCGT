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
	public ArrayList<Byte> textureIDs = new ArrayList<>();
	
	public int textureHash = 0; //apparently unused
	
	public int frontendRenderingData = 0; //controls FE rendering somehow
	public int verticesDataLength = 0;
	
	public ShaderUsage shaderUsage = ShaderUsage.get("Diffuse");
	public ArrayList<TextureUsage> textureUsages = new ArrayList<>();
	public ArrayList<Integer> textures = new ArrayList<>(); //apparently unused
	
	public Vertices verticesBlock;
	public List<Triangle> triangles = new ArrayList<>();
	
	public Hash ShaderHash;
	public Hash DefaultTextureHash;
	public ArrayList<Hash> TextureHashes = new ArrayList<>();
	
	public String uniqueName = "";
	public boolean useTangents = false;

	// used to fix frontend rendering order in some cases (eg make transparent stuff visible through windows)
	public int renderingOrder = 0;
	
	public Material(Material m) {
		this.ShaderHash = m.ShaderHash;
		this.shaderUsage = m.shaderUsage;
		for (var h : m.TextureHashes) this.TextureHashes.add(h);
		for (var u : m.textureUsages) this.textureUsages.add(u);
		this.uniqueName = m.uniqueName;
		
		this.frontendRenderingData = m.frontendRenderingData;
		this.usageSpecific1 = m.usageSpecific1;
		this.usageSpecific2 = m.usageSpecific2;
		this.usageSpecific3 = m.usageSpecific3;
		this.renderingOrder = m.renderingOrder;
	}

	public Material() {
	}

	public static Material getFallbackMaterial() {
		Material m = new Material();
		m.ShaderHash = new Hash("DULLPLASTIC");
		m.shaderUsage = ShaderUsage.get("Diffuse");
		m.TextureHashes.add(new Hash("DEFAULTTEXTURE"));
		m.textureUsages.add(TextureUsage.DIFFUSE);
		return m;
	}

	public void tryGuessHashes(Geometry geometry, Part p) {
		if (ShaderHash == null){
			if (p.shaderlist != null && shaderID >= 0 && shaderID < p.shaderlist.shaders.size()) {
				ShaderHash = Hash.guess(p.shaderlist.shaders.get(shaderID), geometry.hashlist, String.format("0x%08X", p.shaderlist.shaders.get(shaderID)), "BIN");
			} else {
//				System.out.println("Critical issue with part "+p.name+" : missing shaders ! defaulting to DULLPLASTIC");
//				ShaderHash = new Hash("DULLPLASTIC");
				//remain null, this might break some things
			}
		}
//		DefaultTextureHash = Hash.guess(textureHash, geometry.hashlist, String.format("0x%08X",textureHash), "BIN"); //apparently unused
//		for (var t : textures) {
//			TextureHashes.add(Hash.guess(t, geometry.hashlist, String.format("0x%08X",t), "BIN"));
//		}
		if (TextureHashes.size() == 0) for (var t : textureIDs) { //this is actually what's being done by the game
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
		if ("PLAINNOTHING".equals(ShaderHash.label) || "INTERIOR".equals(ShaderHash.label) || "CHASSIS".equals(ShaderHash.label)) flags[1] -= (byte) 0x01;
		
		//reflections ? shadows ? METAL_SWATCH, %_MISC, REGPAINTBLACK, SIRENS, BLACK
		if (//ShaderHash.label.equals("CARSKIN") ||
				"CARBONFIBER".equals(ShaderHash.label) || "HEADLIGHTGLASS".equals(ShaderHash.label) ||
				"BRAKELIGHTGLASS".equals(ShaderHash.label) || "BRAKELIGHTGLASSRED".equals(ShaderHash.label) ||
				"CARBONFIBRE_PLACEHOLDER".equals(TextureHashes.get(0).label) || "CARBONFIBRE".equals(TextureHashes.get(0).label) || "METAL_SWATCH".equals(TextureHashes.get(0).label) ||
				TextureHashes.get(0).label.equals(geometry.carname+"_MISC") || "BLACK".equals(TextureHashes.get(0).label) || 
				"SIRENS".equals(TextureHashes.get(0).label)) flags[2] += (byte) 0x01;
		
		//normals
		if (textureUsages.contains(TextureUsage.NORMAL)) flags[2] += (byte) 0xA2;
		
		//idk
		if(!textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x22;
		if(textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x02;
	}
	
	public void tryGuessDefaultTex() {
		for (var t : TextureHashes) {
			textures.add(t.binHash);
		}
		textureHash = textures.get(0);
		DefaultTextureHash = TextureHashes.get(0);
		
	}
	
	public void tryGuessFEData(HashMap<Integer, Integer> FERenderData) {
		if (FERenderData.containsKey(this.shaderUsage.getKey())) {
			this.frontendRenderingData = FERenderData.get(shaderUsage.getKey());
		} else {
			// find the lowest usage possible
			int lowest = 0;
			while (true) {
				if (!FERenderData.containsValue(lowest)) break;
				lowest += 256;
			}
			this.frontendRenderingData = lowest;
			FERenderData.put(shaderUsage.getKey(), this.frontendRenderingData);
		}
	}

	public void removeUnneeded() {
//		usageSpecific1 = -1;
//		usageSpecific2 = -1;
//		usageSpecific3 = -1;
		flags[0] = (byte) 0x00;
		flags[1] = (byte) 0x00;
		flags[2] = (byte) 0x00;
		flags[3] = (byte) 0x00;
		textureHash = 0; //we apparently can not give a shit about those ???
		for (int i=0; i<textures.size(); i++) {
			textures.set(i, 0);
		}
	}
	
	public boolean needsTangentsLow() {
		return shaderUsage == ShaderUsage.get("DiffuseNormalSwatch")  || shaderUsage == ShaderUsage.get("DiffuseNormalSwatchAlpha");
	}
	public boolean needsTangentsHigh() {
//		return shaderUsage == ShaderUsage.DiffuseNormal || shaderUsage == ShaderUsage.DiffuseNormalAlpha || shaderUsage == ShaderUsage.DiffuseNormalSwatch  || shaderUsage == ShaderUsage.DiffuseNormalSwatchAlpha
//				 || shaderUsage == ShaderUsage.car_t_nm;
		return textureUsages.contains(TextureUsage.NORMAL);
	}
	
	public String generateName() {
		String s;
		if (ShaderHash != null) s = ShaderHash.label + "_";
		else s = shaderUsage.getName() + "_";
		for (var t : TextureHashes) s += t.label + "_";
		s = s.substring(0, s.length()-1);
		return s;
	}


	public String toConfig(String carname) { //	MATERIAL	SHADER=ShaderUsage[1,2,3] 0xFLAGS000 DEFAULTTEX TEX1=TexUsage TEX2=TexUsage
		String s = "";
		s += "MATERIAL	"+uniqueName;
//		s += " feRenderData=" + frontendRenderingData;
		if (ShaderHash != null) s += "	"+ShaderHash.label+"="+shaderUsage.getName();
		else s += "	"+shaderUsage.getName();
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
	
	/**
	 * Create a material from a config line
	 * @param carname the carname to use, controls car-specific textures using the placeholder %
	 * @param l the config line to be interpreted
	 */
	public Material(String carname, String l) {
		int i = 0;
		for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
			if (i==0) {} //continue; //"MATERIAL"
			else if (i==1) { //material name
				uniqueName = s2;
			}
			else if (i==2) { //material + shader or shader only
				if (s2.contains("=")) {
					//shader usage
					ShaderHash = new Hash(s2.split("=")[0]);
					shaderUsage = ShaderUsage.get(s2.split("=")[1]);
				} else {
					ShaderHash = null;
					shaderUsage = ShaderUsage.get(s2);
				}
			}
			else if (i>2) { // texture and usage or setting
				if (s2.split("=")[0].equals("UseTangents")) {
					//material tangents setting
					useTangents = Boolean.getBoolean(s2.split("=")[1]);
				} else if (s2.split("=")[0].equals("FERenderingOrder")) {
					renderingOrder = Integer.parseInt(s2.split("=")[1]);
				} else if (s2.split("=")[0].equals("RenderingOrder")) { // i don't know
					usageSpecific1 = Integer.parseInt(s2.split("=")[1]);

				} else if (TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
					// texture usage
					TextureHashes.add(new Hash(s2.split("=")[0].replace("%", carname)));
					textureUsages.add(TextureUsage.get(s2.split("=")[1]));
				}
			}
			i++;
		}
//		System.out.println("Material : "+toConfig(carname));
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
		if (other.TextureHashes.size() != TextureHashes.size()) return false;
		for (int i=0; i< TextureHashes.size(); i++) if (TextureHashes.get(i).binHash != other.TextureHashes.get(i).binHash) return false;
		if (other.textureUsages.size() != textureUsages.size()) return false;
		for (int i=0; i< textureUsages.size(); i++) if (textureUsages.get(i) != other.textureUsages.get(i)) return false;
		if (ShaderHash == null) {
			if (other.ShaderHash == null) return shaderUsage == other.shaderUsage;
			return false;
		}
		if (other.ShaderHash == null) return false;		
		return ShaderHash.binHash == other.ShaderHash.binHash
				&& shaderUsage == other.shaderUsage;
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
