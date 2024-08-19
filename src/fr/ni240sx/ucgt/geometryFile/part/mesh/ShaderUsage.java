package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Hash;

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
	
    private final int key;
    private final String name;
    private final String[] possibleNames;
    
    public final VertexFormat vertexFormat;
    
    public static List<ShaderUsage> values = new ArrayList<ShaderUsage>();
    private static boolean isLoaded = false;

    ShaderUsage(String name, String[] possibleNames) {
    	this(-2, name, possibleNames, VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s);
    }
    
    ShaderUsage(String name, String[] possibleNames, VertexFormat vf) {
    	this(-2, name, possibleNames, vf);
    }
    
    ShaderUsage(int key, String name, String[] possibleNames) {
    	this(key, name, possibleNames, VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s);
    }
    
    ShaderUsage(int key, String name, String[] possibleNames, VertexFormat vf) {
    	if (key == -2) {
    		// compute vlt from name
    		this.key = new Hash(name).vltHash;
    	} else
        this.key = key; //Integer.reverseBytes(key);
        this.name = name;
		this.possibleNames = possibleNames;
		this.vertexFormat = vf;
//		values.add(this);
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static ShaderUsage get(int key) {
    	if (!isLoaded) {
    		updateUsages();
    		isLoaded = true;
    	}
        for (ShaderUsage c : values) {
            if (c.key == key) return c;
        }
        System.out.println("WARNING : unknown shader usage "+Integer.toHexString(Integer.reverseBytes(key)));
        ShaderUsage ret = new ShaderUsage(key, String.format("0x%08X", Integer.reverseBytes(key)), new String[] {String.format("0x%08X", Integer.reverseBytes(key))});
        values.add(ret);
        return ret; // Handle invalid value
    }

    public static ShaderUsage get(String name) {
    	if (!isLoaded) {
    		updateUsages();
    		isLoaded = true;
    	}
        for (ShaderUsage c : values) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        System.out.println("WARNING : unknown shader usage "+name);
        
        if (name.startsWith("0x") || name.startsWith("0X")) {
			//already hashed input
			ShaderUsage ret = new ShaderUsage(Integer.reverseBytes(Integer.parseUnsignedInt(name.substring(2), 16)), name, new String[] {name});
			values.add(ret);
			return ret;
		}
        
        ShaderUsage ret = new ShaderUsage(name, new String[] {name});
		values.add(ret);
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
				ArrayList<String> names = new ArrayList<>();
				VertexFormat vf = VertexFormat.Pos0s10_Tex0s32_Col0_Norm0s_Tan0s;
				if (!l.startsWith("#") && !l.startsWith("//")) {
					for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isBlank()) {
						if (s2.contains("VertexFormat=")) {
							vf = VertexFormat.get(s2.split("=")[1]);
						} else names.add(s2);
					}
				}
				if (names.size() == 1) {
					values.add(new ShaderUsage(names.get(0), names.toArray(String[]::new), vf));
				} else if (names.size() != 0) { //>1
					values.add(new ShaderUsage(new Hash(names.get(0)).vltHash, names.get(1), names.toArray(String[]::new), vf));
				}
			}
    	} catch (Exception e) {
    		System.out.println("Warning : unable to fetch shader usages from data folder !");
    		e.printStackTrace();
    		values.add(new ShaderUsage(new Hash("car").vltHash, "Diffuse", new String[]{"Diffuse","car"}));
    		values.add(new ShaderUsage(new Hash("car_a").vltHash, "DiffuseAlpha", new String[]{"DiffuseAlpha","Alpha","car_a"}));
    		values.add(new ShaderUsage("car_a_nzw", new String[]{"car_a_nzw"}));
    		values.add(new ShaderUsage(new Hash("car_nm").vltHash, "DiffuseNormal", new String[]{"DiffuseNormal","Normal","car_nm"}));
    		values.add(new ShaderUsage(new Hash("car_nm_a").vltHash, "DiffuseNormalAlpha", new String[]{"DiffuseNormalAlpha","DiffuseAlphaNormal","AlphaNormal","NormalAlpha","car_nm_a"}));
    		values.add(new ShaderUsage(new Hash("car_nm_v_s").vltHash, "DiffuseNormalSwatch", new String[]{"DiffuseNormalSwatch","DiffuseSwatchNormal","NormalSwatch","SwatchNormal","car_nm_v_s"}));
    		values.add(new ShaderUsage(new Hash("car_nm_v_s_a").vltHash, "DiffuseNormalSwatchAlpha", 
			new String[]{"DiffuseNormalSwatchAlpha","DiffuseNormalAlphaSwatch",
					"DiffuseAlphaNormalSwatch","DiffuseAlphaSwatchNormal",
					"DiffuseSwatchNormalAlpha","DiffuseSwatchAlphaNormal",
					"NormalSwatchAlpha","NormalAlphaSwatch",
					"AlphaNormalSwatch","AlphaSwatchNormal",
					"SwatchNormalAlpha","SwatchAlphaNormal", "car_nm_v_s_a"}));
    		values.add(new ShaderUsage(new Hash("car_si").vltHash, "DiffuseGlow", new String[]{"DiffuseGlow","DiffuseSelfIllumination","Glow","SelfIllumination","car_si"}));
    		values.add(new ShaderUsage(new Hash("car_si_a").vltHash, "DiffuseGlowAlpha", new String[]{"DiffuseGlowAlpha","DiffuseAlphaGlow","DiffuseSelfIlluminationAlpha","DiffuseAlphaSelfIllumination",
    				"GlowAlpha","AlphaGlow","SelfIlluminationAlpha","AlphaSelfIllumination","car_si_a"}));
    		values.add(new ShaderUsage(new Hash("car_t").vltHash, "TrafficDiffuse", new String[]{"TrafficDiffuse","Traffic","car_t"}));
    		values.add(new ShaderUsage(new Hash("car_t_a").vltHash, "TrafficDiffuseAlpha", new String[]{"TrafficDiffuseAlpha","TrafficAlpha","car_t_a"}));
    		values.add(new ShaderUsage(new Hash("car_t_nm").vltHash, "TrafficDiffuseNormal", new String[]{"TrafficDiffuseNormal","TrafficNormal","car_t_nm"}));
    		values.add(new ShaderUsage(new Hash("car_v").vltHash, "DiffuseSwatch", new String[]{"DiffuseSwatch","Swatch","car_v"}));
    	}
    }
}