package fr.ni240sx.ucgt.geometryFile.settings;

public enum SettingsImport_Tangents {
	OFF("Off",new String[]{"Off", "No", "None"}, "very light file but weird shading on normalmapped materials"),
	LOW("Optimized",new String[]{"Low", "ByShaderUsage", "Optimized"}, "light file, only calculated for carskin, some other materials might miss depth"),
	HIGH("High",new String[]{"High", "ByTextureUsage"}, "recommended, calculated for all materials with a normalmap assigned"),
	MANUAL("Manual",new String[]{"Manual", "PerMaterial"}, "set for each material in the config, for troubleshooting"),
	ON("All",new String[]{"On", "ForceCalculate", "All"}, "outputs a heavy geometry, useless but can help troubleshooting");
	
    private final String name;
    private final String[] possibleNames;
    private final String desc;

    SettingsImport_Tangents(String name, String[] possibleNames, String desc) {
        this.name = name;
		this.possibleNames = possibleNames;
		this.desc = desc;
    }
    
    public String getName() {
        return name;
    }

    public static SettingsImport_Tangents get(String name) {
        for (SettingsImport_Tangents c : values()) for (String n : c.possibleNames) {
            if (n.equals(name)) return c;
        }
        return OFF; // Handle invalid value
    }

	public String getDesc() {
		return desc;
	}
}
