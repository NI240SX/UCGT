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

	public int numTriVerticesExtra = 0;

//	public int usageSpecific1 = -1; //apparently unused
//	public int usageSpecific2 = -1; //apparently unused
//	public int usageSpecific3 = -1; //apparently unused
	
	public byte[] flags = {0, 0, 0, 0}; //apparently unused -128 81 0 0

	public byte shaderID = 0;
	public ArrayList<Byte> textureIDs = new ArrayList<>();
	
	public int textureHash = 0; //apparently unused
	
	public byte shaderUsageID = 0; //controls FE rendering somehow
	public int verticesDataLength = 0;
	
	public ShaderUsage shaderUsage = ShaderUsage.get("Diffuse");
	public ArrayList<TextureUsage> textureUsages = new ArrayList<>();
	public ArrayList<Integer> textures = new ArrayList<>(); //apparently unused
	public ArrayList<Integer> texturePriorities = new ArrayList<>();
	
	public Vertices verticesBlock;
	public List<Triangle> triangles = new ArrayList<>();
	public List<Triangle> trianglesExtra = new ArrayList<>();
	
	public int ShaderHash = 0;
	public int DefaultTextureHash = 0;
	public ArrayList<Integer> TextureHashes = new ArrayList<>();
	
	public String uniqueName = "";
	public boolean useTangents = false;

	// used to fix frontend rendering order in some cases (eg make transparent stuff visible through windows)
	public byte renderingOrder = 0;
	
	public static final String trianglesExtraExportSuffix = "-EXTRA";
	
	public Material(Material m) {
		this.ShaderHash = m.ShaderHash;
		this.shaderUsage = m.shaderUsage;
		for (var h : m.TextureHashes) this.TextureHashes.add(h);
		for (var u : m.textureUsages) this.textureUsages.add(u);
		this.uniqueName = m.uniqueName;
		
		this.shaderUsageID = m.shaderUsageID;
		for (var d : m.texturePriorities) this.texturePriorities.add(d);
		this.renderingOrder = m.renderingOrder;
	}

	public Material() {
	}

	public static Material getFallbackMaterial(Geometry g) {
		if (g.SAVE_useOffsetsTable) {
			Material m = new Material();
			m.ShaderHash = Hash.findBIN("DULLPLASTIC");
			m.shaderUsage = ShaderUsage.get("Diffuse");
			m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
			m.textureUsages.add(TextureUsage.DIFFUSE);
			return m;
		}
		Material m = new Material();
		m.shaderUsage = ShaderUsage.get("ar_constant");
		m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
		m.textureUsages.add(TextureUsage.DIFFUSE);
		return m;
	}

	public void tryGuessHashes(Geometry geometry, Part p) {
		if (ShaderHash == 0){
			if (p.shaderlist != null && shaderID >= 0 && shaderID < p.shaderlist.shaders.size()) {
				ShaderHash = p.shaderlist.shaders.get(shaderID);
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
			TextureHashes.add(p.texusage.texusage.get(t).getKey());
		}
	}
	
//	public void tryGuessTexturePriority() { // TODO find what ACTUALLY controls this value
//		this.texturePriorities.clear();
//		for (int i=0; i<this.textureIDs.size(); i++) {
//			texturePriorities.add(i); //not accurate but better than nothing
//		}
		
//		usageSpecific1 = -1;
//		usageSpecific2 = -1;
//		usageSpecific3 = -1;
//		
//		if (textureUsages.size() == 2) usageSpecific1 = 1; //diffuse+smth else
//		else if (textureUsages.size() == 3) {
//			// alpha normal 3 1
//			// normal swatch 1 2
//			// alpha swatch 1 2
//			if (textureUsages.contains(TextureUsage.ALPHA) && textureUsages.contains(TextureUsage.NORMAL)) {
//				usageSpecific1 = 3;
//				usageSpecific2 = 1;
//			} else {
//				usageSpecific1 = 1;
//				usageSpecific2 = 2;				
//			}
//		} else if (textureUsages.size() == 4) {
//			usageSpecific1 = 1;
//			usageSpecific2 = 2;
//			usageSpecific3 = 3;			
//		}
//	}

	public void tryGuessFlags(Geometry geometry) {
		flags[0] = (byte) 0x80;
		flags[1] = (byte) 0x51;
		flags[2] = (byte) 0x00;
		flags[3] = (byte) 0x00;

		//low quality rendering or smth
		if (Hash.findBIN("PLAINNOTHING") == ShaderHash || Hash.findBIN("INTERIOR") == ShaderHash || Hash.findBIN("CHASSIS") == ShaderHash) flags[1] -= (byte) 0x01;
		
		//reflections ? shadows ? METAL_SWATCH, %_MISC, REGPAINTBLACK, SIRENS, BLACK
		if (//ShaderHash.label.equals("CARSKIN") ||
				Hash.findBIN("CARBONFIBER") == ShaderHash || Hash.findBIN("HEADLIGHTGLASS") == ShaderHash ||
				Hash.findBIN("BRAKELIGHTGLASS") == ShaderHash || Hash.findBIN("BRAKELIGHTGLASSRED") == ShaderHash ||				
				
				Hash.findBIN("CARBONFIBRE_PLACEHOLDER") == TextureHashes.get(0) || Hash.findBIN("CARBONFIBRE") == TextureHashes.get(0) || Hash.findBIN("METAL_SWATCH") == TextureHashes.get(0) || 
				Hash.findBIN(geometry.carname+"_MISC") == TextureHashes.get(0) || Hash.findBIN("BLACK") == TextureHashes.get(0) || 
				Hash.findBIN("SIRENS") == TextureHashes.get(0)) flags[2] += (byte) 0x01;
		
		//normals
		if (textureUsages.contains(TextureUsage.NORMAL)) flags[2] += (byte) 0xA2;
		
		//idk
		if(!textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x22;
		if(textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.SWATCH)) flags[3] = (byte) 0x02;
	}
	
	public void tryGuessDefaultTex() {
		for (var t : TextureHashes) {
			textures.add(t);
		}
		textureHash = textures.get(0);
		DefaultTextureHash = TextureHashes.get(0);
		
	}
	
	public void tryGuessFEData(HashMap<Integer, Byte> shaderUsageIDs) {
		if (shaderUsageIDs.containsKey(this.shaderUsage.getKey())) {
			this.shaderUsageID = shaderUsageIDs.get(shaderUsage.getKey());
		} else {
			// find the lowest usage possible
			byte lowest = 0;
			while (true) {
				if (!shaderUsageIDs.containsValue(lowest)) break;
				lowest ++;
			}
			this.shaderUsageID = lowest;
			shaderUsageIDs.put(shaderUsage.getKey(), this.shaderUsageID);
		}
	}

	public void removeUnneeded() {
//		usageSpecific1 = -1;
//		usageSpecific2 = -1;
//		usageSpecific3 = -1;
//		texturePriorities.clear();
//		tryGuessTexturePriority(); //better like that
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
		return textureUsages.contains(TextureUsage.NORMAL) || textureUsages.contains(TextureUsage.NORMAL_SAMPLER) || textureUsages.contains(TextureUsage.NORMALMAP) || textureUsages.contains(TextureUsage.BASENORMAL) || textureUsages.contains(TextureUsage.ELEMENTSNORMAL);
	}
	
	public String generateName() {
		String s;
		if (ShaderHash != 0) s = Hash.getBIN(ShaderHash) + "_";
		else s = shaderUsage.getName() + "_";
		for (var t : TextureHashes) s += Hash.getBIN(t) + "_";
		s = s.substring(0, s.length()-1);
		return s;
	}


	public String toConfig(String carname) { //	MATERIAL	SHADER=ShaderUsage[1,2,3] 0xFLAGS000 DEFAULTTEX TEX1=TexUsage TEX2=TexUsage
		String s = "";
		s += "MATERIAL	"+uniqueName;
//		s += " feRenderData=" + frontendRenderingData;
		if (ShaderHash != 0) s += "	"+Hash.getBIN(ShaderHash)+"="+shaderUsage.getName();
		else s += "	"+shaderUsage.getName();
		//+"[";
//		if (usageSpecific1 != -1) s+=usageSpecific1;
//		if (usageSpecific2 != -1) s+=","+usageSpecific2;
//		if (usageSpecific3 != -1) s+=","+usageSpecific3;
//		s +="]	defTex=" + DefaultTextureHash.label+"	flags=0x"+String.format("%02X",flags[0])+String.format("%02X",flags[1])+String.format("%02X",flags[2])+String.format("%02X",flags[3]);
		for (int i=0; i<textureUsages.size(); i++) {
			s += "	" + Hash.getBIN(TextureHashes.get(i)).replace(carname, "%") + "=" + textureUsages.get(i).getName();
			if (texturePriorities.size() > i && texturePriorities.get(i) != i && texturePriorities.get(i) != -1) s += "," + texturePriorities.get(i);
		}
		return s;
	}
	
	/**
	 * Create a material from a config line
	 * @param carname the carname to use, controls car-specific textures using the placeholder %
	 * @param l the config line to be interpreted
	 */
	public Material(Geometry g, String l) {
		int i = 0;
		for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty()) {
			if (i==0) {} //continue; //"MATERIAL"
			else if (i==1) { //material name
				uniqueName = s2;
			}
			else if (i==2) { //material + shader or shader only
				if (s2.contains("=")) {
					//shader usage
					ShaderHash = Hash.findBIN(s2.split("=")[0]);
					shaderUsage = ShaderUsage.get(s2.split("=")[1]);
				} else {
					ShaderHash = 0;
					shaderUsage = ShaderUsage.get(s2);
				}
			}
			else if (i>2) { // texture and usage or setting
				int prio = Integer.MAX_VALUE;
				if (s2.contains(",")) prio = Integer.parseInt(s2.split(",")[1]);
				
				s2 = s2.split(",")[0];
				if (s2.split("=")[0].equals("UseTangents")) {
					//material tangents setting
					useTangents = Boolean.getBoolean(s2.split("=")[1]);
				} else if (s2.split("=")[0].equals("FERenderingOrder")) {
					renderingOrder = Byte.parseByte(s2.split("=")[1]);
				} else if (s2.split("=")[0].equals("RenderingOrder")) { // i don't know
//					usageSpecific1 = Integer.parseInt(s2.split("=")[1]);
					System.out.println("Material-specific RenderingOrder settings are now disabled because they were fundamentally wrong.");
				} else if (TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
					// texture usage
					TextureHashes.add(Hash.findBIN(s2.split("=")[0].replace("%", g.carname)));
					textureUsages.add(TextureUsage.get(s2.split("=")[1]));
					if (prio != Integer.MAX_VALUE) texturePriorities.add(prio);
					else if (g.SAVE_optimizeMaterials) texturePriorities.add(texturePriorities.size()>0 ? -1 : 0);
					else texturePriorities.add(texturePriorities.size());
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
		for (int i=0; i< TextureHashes.size(); i++) {
			int hash1 = TextureHashes.get(i);
			int hash2 = other.TextureHashes.get(i);
			if (hash1 != hash2) {
				return false;
			}
		}
		if (other.texturePriorities.size() != texturePriorities.size()) return false;
		for (int i=0; i< texturePriorities.size(); i++) {
			int prio1 = texturePriorities.get(i);
			int prio2 = other.texturePriorities.get(i);
			if (prio1 != prio2) {
				return false;
			}
		}
		if (other.textureUsages.size() != textureUsages.size()) return false;
		for (int i=0; i< textureUsages.size(); i++) if (textureUsages.get(i) != other.textureUsages.get(i)) return false;
		if (ShaderHash == 0) {
			if (other.ShaderHash == 0) return shaderUsage == other.shaderUsage;
			return false;
		}
		if (other.ShaderHash == 0) return false;		
		return ShaderHash == other.ShaderHash
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
