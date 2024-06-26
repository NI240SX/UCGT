package fr.ni240sx.ucgt.compression;

public enum CompressionType {
	RefPack("RefPack",new String[]{"RefPack", "RFPK"}),
	JDLZ("JDLZ",new String[]{"JDLZ", "EA_JDLZ"}),
	RawDecompressed("RawDecompressed",new String[]{"RawDecompressed", "RAWW"}),
	Unknown("Unknown",new String[]{""});
	
    private final String name;
    private final String[] possibleNames;

    CompressionType(String name, String[] possibleNames) {
        this.name = name;
		this.possibleNames = possibleNames;
    }
    
    public String getName() {
        return name;
    }

    public static CompressionType get(String name) {
        for (CompressionType c : values()) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        System.out.println("Unknown compression type : "+name);
        return Unknown; // Handle invalid value
    }
}
