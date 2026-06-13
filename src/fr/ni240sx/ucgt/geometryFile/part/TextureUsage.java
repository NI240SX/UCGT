package fr.ni240sx.ucgt.geometryFile.part;

public enum TextureUsage {
	DIFFUSE(0xAE76334E),		//DIFFUSE_SAMPLER		diffuse
	VOLUMEMAP(0x4595E87E),		//VOLUMEMAP_SAMPLER		volume
	NORMALMAP(0x958A9502),
//	NORMAL(0x958A9502, "NORMAL"),		//NORMALMAP_SAMPLER		normal	TODO remove this line soon, for compatibility with old exports (wrong as this is the normalmap sampler not the normal one !)
	MISCMAP1(0x8E1E2578),	//MISCMAP1_SAMPLER		diffuse2
	AMBIENT(0x0C8C5ED6),		//AMBIENT_SAMPLER		vinyl
//	SWATCH(0x0C8C5ED6, "SWATCH"),		//AMBIENT_SAMPLER		vinyl	TODO remove this line soon, for compatibility with old exports
	OPACITY(0x5aff315c),			//OPACITY_SAMPLER		opacity
//	ALPHA(0x5aff315c, "ALPHA"),			//OPACITY_SAMPLER		opacity	TODO remove this line soon, for compatibility with old exports
	SELFILLUMINATION(0x39143000),	//SELFILLUMINATION_SAMPLER	selfillumination
	// not in cars !
	SPECULAR(0xEE420FBD),	//SPECULAR_SAMPLER		specular
	ILLUMINATE(0x533C7F18),	//ILLUMINATE_SAMPLER	selfillumination
	BLEND(0x047662FA),			//BLEND_SAMPLER			blend
	DIFFUSE2(0x653FFF1F),	//DIFFUSE2_SAMPLER		diffuse2
	SPECULAR2(0xB5078143),	//SPECULAR2_SAMPLER		specular2
	REFLECTION(0x987D5A4A),//REFLECTION_SAMPLER	reflection
	SHADOWMAP(0x86C0D820),
	CAR_DIFFUSE(0x7501B46A),
	ENVMAP(0x92C541A0),
	MISCMAP1D(0x7C5EA688),
	MASK(0xE2869C70),
	DISPLACEMENT(0x0B46857E),
	SPECULARBONE(0xAAF03291),
	NORMAL(0x90E0C892),
	NORMAL_SAMPLER(0x90E0C892),	//TODO remove soon, for compatibility
	ROADELEMENTS(0xB7BC3023),
	BLEND_SPEC_REFL(0x649E9B97),
	ELEMENTSMASK(0x3DC91356),
	ROADBASE(0xECE777E1),
	BASESPEC(0xE37F2EE9),
	ELEMENTSNORMAL(0x64966205),
	BASENORMAL(0xFF546B1F),
	LIGHTMAP(0x1FA9A254),
	LIGHTMAP_SAMPLER(0x1FA9A254), //TODO remove, for compatibility
	OPACITY_SPEC_REFL(0xB36383C6),
	// more samplers exist, cba to add them as they're pointless here anyways
	INVALID(0xFFFFFFFF);
	
    private final int key;

    TextureUsage(int key) {
        this.key = Integer.reverseBytes(key);
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name();
    }

    public static TextureUsage get(int key) {
        for (TextureUsage c : values()) {
            if (c.key == key) return c;
        }
        System.out.println("WARNING : unknown texture usage "+Integer.toHexString(Integer.reverseBytes(key)));
        return INVALID; // Handle invalid value
    }
    
    public static TextureUsage get(String value) {
        for (TextureUsage c : values()) {
            if (c.name().equals(value)) return c;
        }
//        System.out.println("WARNING : unknown texture usage "+value);
        return INVALID; // Handle invalid value
    }
}