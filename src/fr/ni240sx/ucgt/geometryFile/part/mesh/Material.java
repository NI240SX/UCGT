package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC.LegacyVertices;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.Vertices_PC;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PS3.Vertices_PS3;
import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.Vertices_X360;
import fr.ni240sx.ucgt.shared.Hash;

public class Material {
	
	public int IndexData = 0;
	public int ReducedIndexData = 0;
	public int NumIndices = 0;

	public int NumReducedIndices = 0;

	public int numVertices = -1;

//	public int usageSpecific1 = -1; //apparently unused
//	public int usageSpecific2 = -1; //apparently unused
//	public int usageSpecific3 = -1; //apparently unused
	
	public byte[] MeshFlags = {0, 0, 0, 0}; //apparently unused -128 81 0 0

	public byte LightMaterialNumber = 0;
	public ArrayList<Byte> textureIDs = new ArrayList<>();
	
	public int TextureSortKey = 0; //apparently unused
	
	public byte mMaterialIndex = 0; //controls FE rendering somehow
	public int SizeofVertexData = 0;
	
	public Shader shader = Shader.get("Diffuse");
	public ArrayList<TextureUsage> textureUsages = new ArrayList<>();
	public ArrayList<Integer> textures = new ArrayList<>(); //apparently unused
	public ArrayList<Integer> TextureParam = new ArrayList<>();
	
	public Vertices verticesBlock;
	public List<Triangle> triangles = new ArrayList<>();
	public List<Triangle> trianglesReduced = new ArrayList<>();
	
	public int materialLighting = 0;
	public int DefaultTextureHash = 0;
	public ArrayList<Integer> TextureHashes = new ArrayList<>();
	
	public byte VertexBytesAndFlag;
	
	public String uniqueName = "";
	public boolean useTangents = false;

	// used to fix frontend rendering order in some cases (eg make transparent stuff visible through windows)
	public byte renderingOrder = 0;
	
	public static final String trianglesExtraExportSuffix = "-EXTRA";
	
	public Material(Material m) {
		this.materialLighting = m.materialLighting;
		this.shader = m.shader;
		for (var h : m.TextureHashes) this.TextureHashes.add(h);
		for (var u : m.textureUsages) this.textureUsages.add(u);
		this.uniqueName = m.uniqueName;
		
		this.mMaterialIndex = m.mMaterialIndex;
		for (var d : m.TextureParam) this.TextureParam.add(d);
		this.renderingOrder = m.renderingOrder;
		this.MeshFlags = m.MeshFlags;
	}

	public Material() {
	}

	public static Material getFallbackMaterial(Geometry g) {
		if (g.isUC()) {
			if (g.SAVE_useOffsetsTable) {
				Material m = new Material();
				m.materialLighting = Hash.findBIN("DULLPLASTIC");
				m.shader = Shader.get("Diffuse");
				m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
				m.textureUsages.add(TextureUsage.DIFFUSE);
				return m;
			}
			Material m = new Material();
			m.shader = Shader.get("ar_constant");
			m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
			m.textureUsages.add(TextureUsage.DIFFUSE);
			return m;
		}
		//legacy
		if (g.SAVE_useOffsetsTable) {
			Material m = new Material();
			m.materialLighting = Hash.findBIN("DULLPLASTIC");
			m.shader = Shader.findLegacyUsage("Diffuse", g.platform);
			m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
			m.textureUsages.add(TextureUsage.DIFFUSE);
			return m;
		}
		Material m = new Material();
		m.shader = Shader.findLegacyUsage("Diffuse", g.platform);
		m.TextureHashes.add(Hash.findBIN("DEFAULTTEXTURE"));
		m.textureUsages.add(TextureUsage.DIFFUSE);
		return m;
	}

	public void tryGuessHashes(Part p) {
		if (materialLighting == 0){
			if (p.shaderlist != null && LightMaterialNumber >= 0 && LightMaterialNumber < p.shaderlist.shaders.size()) {
				materialLighting = p.shaderlist.shaders.get(LightMaterialNumber);
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
			if (p.texusage.texusage.size() > t) TextureHashes.add(p.texusage.texusage.get(t).getKey());
		}
	}

	public void tryGuessFlags(Geometry geometry) {
		
		if (MeshFlags[0] == 0 && MeshFlags[1] == 0 && MeshFlags[2] == 0 && MeshFlags[3] == 0) if (geometry.SAVE_useOffsetsTable) { // for a car
			MeshFlags[0] = (byte) 0x80;
			MeshFlags[1] = (byte) 0x51;
			MeshFlags[2] = (byte) 0x00;
			MeshFlags[3] = (byte) 0x00;

			//low quality rendering or smth
			if (Hash.findBIN("PLAINNOTHING") == materialLighting || Hash.findBIN("INTERIOR") == materialLighting || Hash.findBIN("CHASSIS") == materialLighting) MeshFlags[1] -= (byte) 0x01;
			
			//reflections ? shadows ? METAL_SWATCH, %_MISC, REGPAINTBLACK, SIRENS, BLACK
			if (//ShaderHash.label.equals("CARSKIN") ||
					Hash.findBIN("CARBONFIBER") == materialLighting || Hash.findBIN("HEADLIGHTGLASS") == materialLighting ||
					Hash.findBIN("BRAKELIGHTGLASS") == materialLighting || Hash.findBIN("BRAKELIGHTGLASSRED") == materialLighting ||				
					
					Hash.findBIN("CARBONFIBRE_PLACEHOLDER") == TextureHashes.get(0) || Hash.findBIN("CARBONFIBRE") == TextureHashes.get(0) || Hash.findBIN("METAL_SWATCH") == TextureHashes.get(0) || 
					Hash.findBIN(geometry.carname+"_MISC") == TextureHashes.get(0) || Hash.findBIN("BLACK") == TextureHashes.get(0) || 
					Hash.findBIN("SIRENS") == TextureHashes.get(0)) MeshFlags[2] += (byte) 0x01;
			
			//normals
			if (textureUsages.contains(TextureUsage.NORMAL)) MeshFlags[2] += (byte) 0xA2;
			
			//idk
			if(!textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.AMBIENT)) MeshFlags[3] = (byte) 0x22;
			if(textureUsages.contains(TextureUsage.NORMAL) && textureUsages.contains(TextureUsage.AMBIENT)) MeshFlags[3] = (byte) 0x02;
			
		} else { // fallback for a generic model
			MeshFlags[0] = (byte) 0x00;
			MeshFlags[1] = (byte) 0x40;
			MeshFlags[2] = (byte) 0x2a;
			MeshFlags[3] = (byte) 0x00;
		}
		
	}
	
	public void tryGuessDefaultTex() {
		if (textures.size() == 0) for (var t : TextureHashes) {
			textures.add(t);
		}
		TextureSortKey = textures.get(0);
		DefaultTextureHash = TextureHashes.get(0);
		
	}
	
	public void tryGuessFEData(HashMap<Integer, Byte> shaderUsageIDs) {
		if (shaderUsageIDs.containsKey(this.shader.getKey())) {
			this.mMaterialIndex = shaderUsageIDs.get(shader.getKey());
		} else {
			// find the lowest usage possible
			byte lowest = 0;
			while (true) {
				if (!shaderUsageIDs.containsValue(lowest)) break;
				lowest ++;
			}
			this.mMaterialIndex = lowest;
			shaderUsageIDs.put(shader.getKey(), this.mMaterialIndex);
		}
	}

	public void removeUnneeded() {
		if (MeshFlags[0] == 80) {
			// clean up flags on car models only
			MeshFlags[0] = (byte) 0x00;
			MeshFlags[1] = (byte) 0x00;
			MeshFlags[2] = (byte) 0x00;
			MeshFlags[3] = (byte) 0x00;
		}
		TextureSortKey = 0; //we apparently can not give a shit about those ???
		for (int i=0; i<textures.size(); i++) {
			textures.set(i, 0);
		}
	}
	
	public boolean needsTangentsLow() {
		return shader == Shader.get("DiffuseNormalSwatch")  || shader == Shader.get("DiffuseNormalSwatchAlpha");
	}
	public boolean needsTangentsHigh() {
//		return shaderUsage == ShaderUsage.DiffuseNormal || shaderUsage == ShaderUsage.DiffuseNormalAlpha || shaderUsage == ShaderUsage.DiffuseNormalSwatch  || shaderUsage == ShaderUsage.DiffuseNormalSwatchAlpha
//				 || shaderUsage == ShaderUsage.car_t_nm;
		return textureUsages.contains(TextureUsage.NORMAL) || textureUsages.contains(TextureUsage.NORMAL_SAMPLER) || textureUsages.contains(TextureUsage.NORMALMAP) || textureUsages.contains(TextureUsage.BASENORMAL) || textureUsages.contains(TextureUsage.ELEMENTSNORMAL);
	}
	
	public String generateName() {
		String s;
		if (materialLighting != 0) s = Hash.getBIN(materialLighting) + "_";
		else s = shader.getName() + "_";
		for (var t : TextureHashes) s += Hash.getBIN(t) + "_";
		s = s.substring(0, s.length()-1);
		return s;
	}


	public String toConfig(String carname) { //	MATERIAL	SHADER=ShaderUsage[1,2,3] 0xFLAGS000 DEFAULTTEX TEX1=TexUsage TEX2=TexUsage
		String s = "";
		s += "MATERIAL	"+uniqueName;
//		s += " feRenderData=" + frontendRenderingData;
		if (materialLighting != 0) s += "	"+Hash.getBIN(materialLighting)+"="+shader.getName();
		else s += "	"+shader.getName();
		//+"[";
//		if (usageSpecific1 != -1) s+=usageSpecific1;
//		if (usageSpecific2 != -1) s+=","+usageSpecific2;
//		if (usageSpecific3 != -1) s+=","+usageSpecific3;
//		s +="]	defTex=" + DefaultTextureHash.label+"	flags=0x"+String.format("%02X",flags[0])+String.format("%02X",flags[1])+String.format("%02X",flags[2])+String.format("%02X",flags[3]);
		for (int i=0; i<TextureHashes.size(); i++) {
			s += "	" + Hash.getBIN(TextureHashes.get(i)).replace(carname, "%");
			if (shader.getClass() != Shader.Legacy.class && textureUsages.size() > i) s +=  "=" + textureUsages.get(i).getName();
//			if (texturePriorities.size() > i && texturePriorities.get(i) != i && texturePriorities.get(i) != -1) s += "," + texturePriorities.get(i);
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
				switch(g.platform) {
				case PC:
				case X360:
				case PS3:
					if (s2.contains("=")) {
						//shader usage
						materialLighting = Hash.findBIN(s2.split("=")[0]);
						shader = Shader.get(s2.split("=")[1]);
					} else {
						materialLighting = 0;
						shader = Shader.get(s2);
					}
					break;
				case Prostreet_PC:
				case Prostreet_X360:
				case Carbon_PC:
					if (s2.contains("=")) {
						//shader usage
						materialLighting = Hash.findBIN(s2.split("=")[0]);
						shader = Shader.findLegacyUsage(s2.split("=")[1], g.platform);
					} else {
						materialLighting = 0;
						shader = Shader.findLegacyUsage(s2, g.platform);
					}
					break;
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
				} else if (s2.split("=").length == 1 || TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
					// texture usage
					if (s2.split("=").length < 2) {
						if (shader.getClass() == Shader.Legacy.class && ((Shader.Legacy)shader).texusages.size() > TextureHashes.size()) 
							textureUsages.add(((Shader.Legacy)shader).texusages.get(TextureHashes.size()));
					} else {
						textureUsages.add(TextureUsage.get(s2.split("=")[1]));
					}
					TextureHashes.add(Hash.findBIN(s2.split("=")[0].replace("%", g.carname)));
					if (prio != Integer.MAX_VALUE) TextureParam.add(prio);
					else if (g.SAVE_optimizeMaterials && g.SAVE_useOffsetsTable && (g.platform == Platform.PC)) TextureParam.add(TextureParam.size()>0 ? -1 : 0);
					else TextureParam.add(TextureParam.size());
				}
			}
			i++;
		}
		if (!g.SAVE_useOffsetsTable) {
			MeshFlags[0] = (byte) 0x00;
			MeshFlags[1] = (byte) 0x40;
			MeshFlags[2] = (byte) 0x2a;
			MeshFlags[3] = (byte) 0x00;
		}
//		System.out.println("Material : "+toConfig(carname));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(materialLighting, TextureHashes, shader, textureUsages);
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
//		if (other.texturePriorities.size() != texturePriorities.size()) return false;
//		for (int i=0; i< texturePriorities.size(); i++) {
//			int prio1 = texturePriorities.get(i);
//			int prio2 = other.texturePriorities.get(i);
//			if (prio1 != prio2) {
//				return false;
//			}
//		}
		if (other.textureUsages.size() != textureUsages.size()) return false;
		for (int i=0; i< textureUsages.size(); i++) if (textureUsages.get(i) != other.textureUsages.get(i)) return false;
		if (materialLighting == 0) {
			if (other.materialLighting == 0) return shader == other.shader;
			return false;
		}
		if (other.materialLighting == 0) return false;		
		return materialLighting == other.materialLighting
				&& shader == other.shader;
	}

	public void createVerticesBlock(Platform p) {
		switch(p) {
		case PC:
			verticesBlock = new Vertices_PC();
			break;
		case X360:
			verticesBlock = new Vertices_X360();
			break;
		case PS3:
			verticesBlock = new Vertices_PS3();
			break;
		case Prostreet_PC:
		case Prostreet_X360:
		case Carbon_PC:
			verticesBlock = new LegacyVertices();
			break;
		
		}
	}

	public boolean isOpaque() {
		return !hasTransparency();
	}
	public boolean hasTransparency() {
		return 
				//shaderUsage == ShaderUsage.get("car_a") ||
				//shaderUsage == ShaderUsage.get("car_a_nzw") ||
				//shaderUsage == ShaderUsage.get("car_nm_a") ||
				//shaderUsage == ShaderUsage.get("car_nm_v_s_a") ||
				//shaderUsage == ShaderUsage.get("car_si_a") ||
				//shaderUsage == ShaderUsage.get("car_t_a") ||
				//shaderUsage.getClass() == ShaderUsage.Legacy.class ||
				shader.name.contains("Alpha") ||
				shader.name.contains("opacity") ||
				shader.name.contains("org_") ||
				shader.name.contains("overlay");
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
