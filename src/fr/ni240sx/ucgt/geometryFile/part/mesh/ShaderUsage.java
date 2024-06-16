package fr.ni240sx.ucgt.geometryFile.part.mesh;

public enum ShaderUsage {
	
	// the following are CAR MATERIALS
	// provided : POSITION0=half4, NORMAL=dec4n, TANGENT0=dec4n, COLOR0=color, TEXCOORD0=half2
	
	// car			uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
	Diffuse(0xeb5337a1, "Diffuse", new String[]{"Diffuse","car"}),
	// car_a		uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
	DiffuseAlpha(0xe12db62e, "DiffuseAlpha", new String[]{"DiffuseAlpha","Alpha","car_a"}),
	// car_a_nzw	uses DIFFUSE_SAMPLER and VOLUMEMAP_SAMPLER
	car_a_nzw(0x55A5F554, "car_a_nzw", new String[]{"car_a_nzw"}),
	// car_nm		uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP_SAMPLER
	DiffuseNormal(0xf5bbcc3f, "DiffuseNormal", new String[]{"DiffuseNormal","Normal","car_nm"}),
	// car_nm_a		uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
	DiffuseNormalAlpha(0xd70a7771, "DiffuseNormalAlpha", new String[]{"DiffuseNormalAlpha","DiffuseAlphaNormal","AlphaNormal","NormalAlpha","car_nm_a"}),
	// car_nm_v_s	uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
	DiffuseNormalSwatch(0xc52da6a8, "DiffuseNormalSwatch", new String[]{"DiffuseNormalSwatch","DiffuseSwatchNormal","NormalSwatch","SwatchNormal","car_nm_v_s"}),
	// car_nm_v_s_a	uses DIFFUSE, NORMALMAP, MISCMAP1, AMBIENT, VOLUMEMAP samplers
	DiffuseNormalSwatchAlpha(0x6A8B36C1, "DiffuseNormalSwatchAlpha", 
			new String[]{"DiffuseNormalSwatchAlpha","DiffuseNormalAlphaSwatch",
					"DiffuseAlphaNormalSwatch","DiffuseAlphaSwatchNormal",
					"DiffuseSwatchNormalAlpha","DiffuseSwatchAlphaNormal",
					"NormalSwatchAlpha","NormalAlphaSwatch",
					"AlphaNormalSwatch","AlphaSwatchNormal",
					"SwatchNormalAlpha","SwatchAlphaNormal", "car_nm_v_s_a"}),
	// car_si		uses DIFFUSE, VOLUMEMAP samplers
	DiffuseGlow(0x88155cb2, "DiffuseGlow", new String[]{"DiffuseGlow","DiffuseSelfIllumination","Glow","SelfIllumination","car_si"}),
	// car_si_a		uses DIFFUSE, VOLUMEMAP samplers
	DiffuseGlowAlpha(0x5c6cdd0d, "DiffuseGlowAlpha", new String[]{"DiffuseGlowAlpha","DiffuseAlphaGlow","DiffuseSelfIlluminationAlpha","DiffuseAlphaSelfIllumination",
			"GlowAlpha","AlphaGlow","SelfIlluminationAlpha","AlphaSelfIllumination","car_si_a"}),
	// car_t		uses DIFFUSE, VOLUMEMAP
	car_t(0x3F8A59F0, "car_t", new String[]{"car_t"}),
	// car_t_a		uses DIFFUSE, VOLUMEMAP
	car_t_a(0x724870AB, "car_t_a", new String[]{"car_t_a"}),
	// car_t_nm		uses DIFFUSE, NORMALMAP, MISCMAP1
	car_t_nm(0x43DDE14C, "car_t_nm", new String[]{"car_t_nm"}),
	// car_v		uses DIFFUSE, VOLUMEMAP, AMBIENT samplers
	DiffuseSwatch(0xA2ABF064, "DiffuseSwatch", new String[]{"DiffuseSwatch","Swatch","car_v"}),

	// the following are NOT car materials !
	
	// provided : POSITION0=float4, COLOR0=color, TEXCOORD0=float2, NORMAL0=dec4n
	// uses DIFFUSE, SPECULAR
	diffuse_spec_2sided(0x97CFC5D2, "diffuse_spec_2sided", new String[] {"diffuse_spec_2sided"}),
	INVALID(0xFFFFFFFF, "INVALID",new String[]{""});
	
    private final int key;
    private final String name;
    private final String[] possibleNames;

    ShaderUsage(int key, String name, String[] possibleNames) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
		this.possibleNames = possibleNames;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static ShaderUsage get(int key) {
        for (ShaderUsage c : values()) {
            if (c.key == key) return c;
        }
        System.out.println("WARNING : unknown shader usage "+Integer.toHexString(Integer.reverseBytes(key)));
        return INVALID; // Handle invalid value
    }

    public static ShaderUsage get(String name) {
        for (ShaderUsage c : values()) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        System.out.println("WARNING : unknown shader usage "+name);
        return INVALID; // Handle invalid value
    }
}