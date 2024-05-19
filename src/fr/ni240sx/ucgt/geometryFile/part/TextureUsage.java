package fr.ni240sx.ucgt.geometryFile.part;

public enum TextureUsage {
	DIFFUSE(0xAE76334E, "DIFFUSE"),
	NORMAL(0x958A9502, "NORMAL"),
	ALPHA(0x5aff315c, "ALPHA"),
	SWATCH(0x0c8c5ed6, "SWATCH"),
	SELFILLUMINATION(0x39143000, "SELFILLUMINATION"),
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
}