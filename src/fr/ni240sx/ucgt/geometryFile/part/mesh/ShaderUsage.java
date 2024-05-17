package fr.ni240sx.ucgt.geometryFile.part.mesh;

public enum ShaderUsage {
	Diffuse(0xeb5337a1, "Diffuse"),
	DiffuseNormal(0xf5bbcc3f, "DiffuseNormal"),
	DiffuseAlpha(0xe12db62e, "DiffuseAlpha"),
	DiffuseAlphaNormal(0xd70a7771, "DiffuseAlphaNormal"),
	DiffuseGlow(0x88155cb2, "DiffuseGlow"),
	DiffuseGlowAlpha(0x5c6cdd0d, "DiffuseGlowAlpha"),
	DiffuseNormalSwatch(0xc52da6a8, "DiffuseNormalSwatch"),
	INVALID(0xFFFFFFFF, "INVALID");
	
    private final int key;
    private final String name;

    ShaderUsage(int key, String name) {
        this.key = Integer.reverseBytes(key);
        this.name = name;
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
        return INVALID; // Handle invalid value
    }
}