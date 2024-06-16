package fr.ni240sx.ucgt.geometryFile.part;

public enum TextureUsage {
	DIFFUSE(0xAE76334E, "DIFFUSE"),		//DIFFUSE_SAMPLER		diffuse
	VOLUMEMAP(0x4595E87E, "VOLUMEMAP"),	//VOLUMEMAP_SAMPLER		volume
	NORMAL(0x958A9502, "NORMAL"),		//NORMALMAP_SAMPLER		normal
	MISCMAP1(0x8E1E2578, "MISCMAP1"),	//MISCMAP1_SAMPLER		diffuse2
	SWATCH(0x0C8C5ED6, "SWATCH"),		//AMBIENT_SAMPLER		vinyl
	OPACITY(0x5aff315c, "OPACITY"),			//OPACITY_SAMPLER		opacity
	ALPHA(0x5aff315c, "ALPHA"),			//OPACITY_SAMPLER		opacity	TODO remove this line soon, for compatibility with old exports
	SELFILLUMINATION(0x39143000, "SELFILLUMINATION"),	//SELFILLUMINATION_SAMPLER	selfillumination
	// not in cars !
	SPECULAR(0xEE420FBD, "SPECULAR"),	//SPECULAR_SAMPLER		specular
	ILLUMINATE(0x533C7F18, "ILLUMINATE"),	//ILLUMINATE_SAMPLER	selfillumination
	BLEND(0x047662FA, "BLEND"),			//BLEND_SAMPLER			blend
	DIFFUSE2(0x653FFF1F, "DIFFUSE2"),	//DIFFUSE2_SAMPLER		diffuse2
	SPECULAR2(0xB5078143, "SPECULAR2"),	//SPECULAR2_SAMPLER		specular2
	REFLECTION(0x987D5A4A,"REFLECTION"),//REFLECTION_SAMPLER	reflection
	// more samplers exist, cba to add them as they're pointless here anyways
	INVALID(0xFFFFFFFF, "INVALID");
	
    private final int key;
    private final String name;

    TextureUsage(int key, String name) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
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
            if (c.name.equals(value)) return c;
        }
//        System.out.println("WARNING : unknown texture usage "+value);
        return INVALID; // Handle invalid value
    }
}