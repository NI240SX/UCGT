package fr.ni240sx.ucgt.geometryFile;

public enum Platform {
	PC("PC", "Undercover_PC"), //UC
	X360("X360", "Undercover_X360", "Xenon", "Undercover_Xenon"),
	Prostreet_PC("Prostreet_PC"),
	Prostreet_X360("Prostreet_X360", "Prostreet_Xenon"),
	Carbon_PC("Carbon_PC");
	
    private final String[] names;

    Platform(String... names){
    	this.names = names;
    }
    
    public String getName() {
    	return names[0];
    }
    
    public static Platform get(String s) {
    	for (var p : values()) for (var n : p.names){
    		if (n.equals(s)) return p;
    	}
    	return PC;
    }

	public static int indexOf(Platform p) {
		for (int i=0; i<values().length; i++) {
			if (values()[i] == p) return i;
		}
		return -1;
	}
}
