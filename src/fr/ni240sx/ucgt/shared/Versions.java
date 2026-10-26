package fr.ni240sx.ucgt.shared;

import java.io.File;

public class Versions {

	public static final String geom_version = "1.3.5";
	public static final String geom_build = "2026.10.13";

	public static final String geomGUI_Name = "UCGT Geometry Editor";
	public static final String geomGUI_GUIVersion = "1.0.5";
	
	public static final String worldEditor_Name = "UCGT World Editor";
	public static final String worldEditor_Version = "wip";
	
	public static final String dbmp_Name = "UCGT DBModelParts Editor";
	public static final String dbmp_Version = "1.2.0";
	
	public static final String collisions_Name = "UCGT Collisions Editor";
	public static final String collisions_Version = "1.1.0";
	
	public static final String settingsFolder;

	public static final String settingsGeomEdGui;
	public static final String settingsColsEd;
	public static final String settingsDbmpEd;
	public static final String settingsWrldEd;
	
	public static final String geomDefaultConfig;

	static {
		settingsFolder = "settings-%s-%s-%s".formatted(System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("user.name"));
		settingsGeomEdGui = "%s%sgeomGUI.dat".formatted(settingsFolder,File.separator);
		settingsColsEd = "%s%scolseditor.dat".formatted(settingsFolder,File.separator);
		settingsDbmpEd = "%s%sdbmpeditor.dat".formatted(settingsFolder,File.separator);
		settingsWrldEd = "%s%sworldGUI.dat".formatted(settingsFolder,File.separator);
		geomDefaultConfig = "%s%sdefaultsettings.ini".formatted(settingsFolder,File.separator);
		
		if (new File("settings").isDirectory()) new File("settings").renameTo(new File(settingsFolder)); //migrate
	}

}
