package fr.ni240sx.ucgt.compression;

public enum CompressionType {
	RefPack(0x4B504652, "RefPack", "RFPK"),
	JDLZ(0x5A4C444A, "JDLZ", "EA_JDLZ"),
	COMP(0x504D4F43, "COMP", "EA_COMP"),
	HUFF(0x46465548, "HUFF", "EA_HUFF", "Huffman"),
	RawDecompressed(0x57574152, "RawDecompressed", "RAWW"),
	Unknown(-1, "Unknown");
	
	final int magic;
    private final String name;
    private final String[] possibleNames;

    CompressionType(int magic, String... names) {
    	this.magic = magic;
        this.name = names[0];
		this.possibleNames = names;
    }
    
    public String getName() {
        return name;
    }
    @Override
	public String toString() {
    	return name;
    }

    public static CompressionType get(String name) {
        for (CompressionType c : values()) for (String n : c.possibleNames) {
            if (n.toUpperCase().equals(name.toUpperCase())) return c;
        }
        System.out.println("Unknown compression type : "+name);
        return Unknown; // Handle invalid value
    }
    
    public static CompressionType get(int magic) {
    	for (var c: values()) if (c.magic == magic) return c;
    	return Unknown;
    }
}
