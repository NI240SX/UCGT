package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;

public class ShaderUsage {
	
	// the following are CAR MATERIALS
	// provided : POSITION0=half4, NORMAL=dec4n, TANGENT0=dec4n, COLOR0=color, TEXCOORD0=half2
	
//	// car			uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
//	Diffuse(0xeb5337a1, "Diffuse", new String[]{"Diffuse","car"}),
//	// car_a		uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
//	DiffuseAlpha(0xe12db62e, "DiffuseAlpha", new String[]{"DiffuseAlpha","Alpha","car_a"}),
//	// car_a_nzw	uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
//	car_a_nzw(0x55A5F554, "car_a_nzw", new String[]{"car_a_nzw"}),
//	// car_nm		uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP_SAMPLER
//	DiffuseNormal(0xf5bbcc3f, "DiffuseNormal", new String[]{"DiffuseNormal","Normal","car_nm"}),
//	// car_nm_a		uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
//	DiffuseNormalAlpha(0xd70a7771, "DiffuseNormalAlpha", new String[]{"DiffuseNormalAlpha","DiffuseAlphaNormal","AlphaNormal","NormalAlpha","car_nm_a"}),
//	// car_nm_v_s	uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
//	DiffuseNormalSwatch(0xc52da6a8, "DiffuseNormalSwatch", new String[]{"DiffuseNormalSwatch","DiffuseSwatchNormal","NormalSwatch","SwatchNormal","car_nm_v_s"}),
//	// car_nm_v_s_a	uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
//	DiffuseNormalSwatchAlpha(0x6A8B36C1, "DiffuseNormalSwatchAlpha", 
//			new String[]{"DiffuseNormalSwatchAlpha","DiffuseNormalAlphaSwatch",
//					"DiffuseAlphaNormalSwatch","DiffuseAlphaSwatchNormal",
//					"DiffuseSwatchNormalAlpha","DiffuseSwatchAlphaNormal",
//					"NormalSwatchAlpha","NormalAlphaSwatch",
//					"AlphaNormalSwatch","AlphaSwatchNormal",
//					"SwatchNormalAlpha","SwatchAlphaNormal", "car_nm_v_s_a"}),
//	// car_si		uses DIFFUSE, VOLUMEMAP samplers
//	DiffuseGlow(0x88155cb2, "DiffuseGlow", new String[]{"DiffuseGlow","DiffuseSelfIllumination","Glow","SelfIllumination","car_si"}),
//	// car_si_a		uses DIFFUSE, VOLUMEMAP samplers
//	DiffuseGlowAlpha(0x5c6cdd0d, "DiffuseGlowAlpha", new String[]{"DiffuseGlowAlpha","DiffuseAlphaGlow","DiffuseSelfIlluminationAlpha","DiffuseAlphaSelfIllumination",
//			"GlowAlpha","AlphaGlow","SelfIlluminationAlpha","AlphaSelfIllumination","car_si_a"}),
//	// car_t		uses DIFFUSE, VOLUMEMAP
//	TrafficDiffuse(0x3F8A59F0, "TrafficDiffuse", new String[]{"TrafficDiffuse","Traffic","car_t"}),
//	// car_t_a		uses DIFFUSE, VOLUMEMAP
//	TrafficDiffuseAlpha(0x724870AB, "TrafficDiffuseAlpha", new String[]{"TrafficDiffuseAlpha","TrafficAlpha","car_t_a"}),
//	// car_t_nm		uses DIFFUSE, NORMALMAP, MISCMAP1
//	TrafficDiffuseNormal(0x43DDE14C, "TrafficDiffuseNormal", new String[]{"TrafficDiffuseNormal","TrafficNormal","car_t_nm"}),
//	// car_v		uses DIFFUSE, VOLUMEMAP, AMBIENT samplers
//	DiffuseSwatch(0xA2ABF064, "DiffuseSwatch", new String[]{"DiffuseSwatch","Swatch","car_v"}),
//
	
	public static final ShaderUsage INVALID = new ShaderUsage(0xFFFFFFFF, "INVALID",new String[]{""});
//	public static final ShaderUsage Diffuse = new ShaderUsage(0xa13753eb, "Diffuse", new String[]{"Diffuse","car"});
	
    public final int key;
    public final String name;
    public final String[] possibleNames;

    public final VertexFormat vertexFormat_PC;
    public final VertexFormat vertexFormat_X360;

    public static List<ShaderUsage> values = new ArrayList<>();
    public static List<ShaderUsage> legacyValues = new ArrayList<>();
//    public static boolean isLoaded = false;
    
    static {
    	updateUsages();
    }

    ShaderUsage(String name, String[] possibleNames) {
    	this(-2, name, possibleNames, VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s);
    }
    
    ShaderUsage(String name, String[] possibleNames, VertexFormat vf_PC, VertexFormat vf_X360) {
    	this(-2, name, possibleNames, vf_PC, vf_X360);
    }
    
    ShaderUsage(int key, String name, String[] possibleNames) {
    	this(key, name, possibleNames, VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s);
    }
    

    ShaderUsage(int key, String name, String[] possibleNames, VertexFormat vf_PC) {
    	this(key, name, possibleNames, vf_PC, null);
    }
    
    ShaderUsage(int key, String name, String[] possibleNames, VertexFormat vf_PC, VertexFormat vf_X360) {
    	if (key == -2) {
    		// compute vlt from name
    		this.key = Hash.findVLT(name);
    	} else
        this.key = key; //Integer.reverseBytes(key);
        this.name = name;
		this.possibleNames = possibleNames;
		this.vertexFormat_PC = vf_PC;
		this.vertexFormat_X360 = vf_X360;
//		values.add(this);
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static ShaderUsage get(int key) {
        for (ShaderUsage c : values) {
            if (c.key == key) return c;
        }
        for (ShaderUsage c : legacyValues) {
            if (c.key == key) return c;
        }
        System.out.println("WARNING : unknown shader usage "+Integer.toHexString(Integer.reverseBytes(key)));
        ShaderUsage ret = new ShaderUsage(key, String.format("0x%08X", Integer.reverseBytes(key)), new String[] {String.format("0x%08X", Integer.reverseBytes(key))});
//        values.add(ret);
        return ret; // Handle invalid value
    }

    public static ShaderUsage getNGOrDefault(String name, ShaderUsage def) {
        for (ShaderUsage c : values) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        return def;
    }
    
    public static ShaderUsage get(String name) {
        for (ShaderUsage c : values) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        System.out.println("WARNING : unknown shader usage "+name);
        
        if (name.startsWith("0x") || name.startsWith("0X")) {
			//already hashed input
			ShaderUsage ret = new ShaderUsage(Integer.reverseBytes(Integer.parseUnsignedInt(name.substring(2,10), 16)), name, new String[] {name});
//			values.add(ret);
			return ret;
		}
        
        ShaderUsage ret = new ShaderUsage(name, new String[] {name});
//		values.add(ret);
		return ret;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
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
		ShaderUsage other = (ShaderUsage) obj;
		if (key != other.key)
			return false;
		return true;
	}

	public static void updateUsages() {
    	values.clear();
    	values.add(INVALID);
    	try {
	    	var br = new BufferedReader(new FileReader(new File("data/shaderusages")));
	    	String l;
			while ((l = br.readLine())!=null){
				parseUsage(l);
			}

	    	br = new BufferedReader(new FileReader(new File("data/shaderusages_legacy")));
	    	while ((l = br.readLine())!=null){
				parseUsageLegacy(l);
			}
    	} catch (Exception e) {
    		System.out.println("Warning : unable to fetch shader usages from data folder !");
    		e.printStackTrace();
    		values.add(new ShaderUsage(Hash.findVLT("car"), "Diffuse", new String[]{"Diffuse","car"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_a"), "DiffuseAlpha", new String[]{"DiffuseAlpha","Alpha","car_a"}));
    		values.add(new ShaderUsage("car_a_nzw", new String[]{"car_a_nzw"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_nm"), "DiffuseNormal", new String[]{"DiffuseNormal","Normal","car_nm"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_nm_a"), "DiffuseNormalAlpha", new String[]{"DiffuseNormalAlpha","DiffuseAlphaNormal","AlphaNormal","NormalAlpha","car_nm_a"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_nm_v_s"), "DiffuseNormalSwatch", new String[]{"DiffuseNormalSwatch","DiffuseSwatchNormal","NormalSwatch","SwatchNormal","car_nm_v_s"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_nm_v_s_a"), "DiffuseNormalSwatchAlpha", 
			new String[]{"DiffuseNormalSwatchAlpha","DiffuseNormalAlphaSwatch",
					"DiffuseAlphaNormalSwatch","DiffuseAlphaSwatchNormal",
					"DiffuseSwatchNormalAlpha","DiffuseSwatchAlphaNormal",
					"NormalSwatchAlpha","NormalAlphaSwatch",
					"AlphaNormalSwatch","AlphaSwatchNormal",
					"SwatchNormalAlpha","SwatchAlphaNormal", "car_nm_v_s_a"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_si"), "DiffuseGlow", new String[]{"DiffuseGlow","DiffuseSelfIllumination","Glow","SelfIllumination","car_si"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_si_a"), "DiffuseGlowAlpha", new String[]{"DiffuseGlowAlpha","DiffuseAlphaGlow","DiffuseSelfIlluminationAlpha","DiffuseAlphaSelfIllumination",
    				"GlowAlpha","AlphaGlow","SelfIlluminationAlpha","AlphaSelfIllumination","car_si_a"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_t"), "TrafficDiffuse", new String[]{"TrafficDiffuse","Traffic","car_t"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_t_a"), "TrafficDiffuseAlpha", new String[]{"TrafficDiffuseAlpha","TrafficAlpha","car_t_a"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_t_nm"), "TrafficDiffuseNormal", new String[]{"TrafficDiffuseNormal","TrafficNormal","car_t_nm"}));
    		values.add(new ShaderUsage(Hash.findVLT("car_v"), "DiffuseSwatch", new String[]{"DiffuseSwatch","Swatch","car_v"}));
    	}
//    	isLoaded = true;
    }

	public static void parseUsage(String l) {
		ArrayList<String> names = new ArrayList<>();
		VertexFormat vf_PC = VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s;
		VertexFormat vf_X360 = null;
		if (!l.startsWith("#") && !l.startsWith("//")) {
			for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isBlank()) {
				if (s2.contains("VertexFormatPC=")) {
					vf_PC = VertexFormat.get(s2.split("=")[1]);
				} else if (s2.contains("VertexFormatX360=")) {
					vf_X360 = VertexFormat.get(s2.split("=")[1]);
				} else if (s2.startsWith("#") || s2.startsWith("//")) {
					break;
				} else names.add(s2);
			}
		}
		if (names.size() == 1) {
			values.add(new ShaderUsage(names.get(0), names.toArray(String[]::new), vf_PC, vf_X360));
		} else if (names.size() != 0) { //>1
			values.add(new ShaderUsage(Hash.findVLT(names.get(0)), names.get(1), names.toArray(String[]::new), vf_PC, vf_X360));
		}
	}

	public static void parseUsageLegacy(String l) {
		ArrayList<String> names = new ArrayList<>();
		ArrayList<TextureUsage> texusages = new ArrayList<>(); //map legacy BS to uc's texusages
		Platform platform = null;
		int shaderIndex = -1;
		VertexFormat vf = null;
		boolean def = false;
		if (!l.startsWith("#") && !l.startsWith("//") && !l.isBlank()) {
			int i = 0;
			for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isBlank()) {
				if (i == 0) platform = Platform.get(s2);
				else if (i == 1) shaderIndex = Integer.valueOf(s2);
				else if (s2.contains("VertexFormat=")) {
					vf = VertexFormat.get(s2.split("=")[1]);
				} else if (s2.contains("TextureUsages=")) {
					for (var split : s2.split("=")[1].split(",")) texusages.add(TextureUsage.get(split));
				} else if (s2.equals("default")) {
					def = true;
				} else if (s2.startsWith("#") || s2.startsWith("//")) {
					break;
				} else names.add(s2);
				i++;
			}
			
			if (i>2 && names.size() == 0 && platform != null) names.add(platform.getName() + "_" +shaderIndex);
			if (texusages.size() == 0) texusages.add(TextureUsage.DIFFUSE);
			if (i>2 && vf != null && platform != null) legacyValues.add(new Legacy(Platform.indexOf(platform) << 24 | shaderIndex, names.get(0), names.toArray(String[]::new), vf, vf, texusages, def));
			
//			System.out.println(String.format("0x%08X",Platform.indexOf(platform) << 24 | shaderIndex));
		}
	}

	public static int findLegacyIndex(String name, Platform platform) {
		for (var u : legacyValues) {
			if (u.key >>> 24 == Platform.indexOf(platform)) {
				if (u.name.equals(name)) return u.key & 0xFFFFFF;
				for (var s : u.possibleNames) if (s.equals(name)) return u.key & 0xFFFFFF;
			}
		}
		System.out.println("Cannot find legacy shader index: "+name);
		for (var u : legacyValues) {
			if (u.key >>> 24 == Platform.indexOf(platform) && ((Legacy)u).isDefault) {
				return u.key & 0xFFFFFF;
			}
		}
		return 0;
	}
	
	public static ShaderUsage findLegacyUsage(String name, Platform platform) {
		for (var u : legacyValues) {
			if (u.key >>> 24 == Platform.indexOf(platform)) {
				for (var s : u.possibleNames) if (s.equals(name)) return u;
			}
		}
		for (var u : legacyValues) {
			if (u.key >>> 24 == Platform.indexOf(platform)) {
				for (var s : u.possibleNames) if (s.contains(name) || name.contains(s)) {
					System.out.println("Cannot find legacy shader usage with name: "+name+"; picked "+s+" instead.");
					return u;
				}
			}
		}
		for (var u : legacyValues) {
			if (u.key >>> 24 == Platform.indexOf(platform) && ((Legacy)u).isDefault) {
				System.out.println("Cannot find legacy shader usage with name: "+name+"; defaulted to "+u.name+" instead.");
				return u;
			}
		}
		return null;
	}

	public static class Legacy extends ShaderUsage{

		public List<TextureUsage> texusages;
		public boolean isDefault = false;
		
		public Legacy(int i, String string, String[] array, VertexFormat vf, VertexFormat vf2, List<TextureUsage> texusages, boolean def) {
			super(i, string, array, vf, vf2);
			this.texusages = texusages;
			isDefault = def;
		}
		
	}
}