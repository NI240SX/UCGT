package fr.ni240sx.ucgt.geometryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

@SuppressWarnings("unused")
public class TestingFunctions {

	public static void dumpPartsAsIs(String geomFile) {
		dumpPartsAsIs(geomFile, geomFile+"-parts\\");
	}
	public static void dumpPartsAsIs(String geomFile, String partsDirectory) {
//		Block.doNotRead.put(GeomBlock.Part_Header, true);
		
		Block.doNotRead.put(GeomBlock.Part_AutosculptLinking, true);
		Block.doNotRead.put(GeomBlock.Part_AutosculptZones, true);
		Block.doNotRead.put(GeomBlock.Part_HashAssign, true);
		Block.doNotRead.put(GeomBlock.Part_HashList, true);
		Block.doNotRead.put(GeomBlock.Part_Mesh, true);
		Block.doNotRead.put(GeomBlock.Part_MPoints, true);
		Block.doNotRead.put(GeomBlock.Part_Padding, true);
		Block.doNotRead.put(GeomBlock.Part_ShaderList, true);
		Block.doNotRead.put(GeomBlock.Part_Strings, true);
		Block.doNotRead.put(GeomBlock.Part_TexUsage, true);
		Block.doNotRead.put(GeomBlock.Part_Mesh_Materials, true);
		dumpPartsRecompiled(geomFile, partsDirectory);
		Block.doNotRead.clear();
	}
	
	public static void dumpPartsRecompiled(String geomFile) {
		dumpPartsRecompiled(geomFile, geomFile+"-parts\\");
	}
	@SuppressWarnings("resource")
	public static void dumpPartsRecompiled(String geomFile, String partsDirectory) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			Files.createDirectories(Paths.get(partsDirectory));
			
			for (Part p : geom.parts) {
				try {
					FileOutputStream fos;
					if (Block.doNotRead.get(GeomBlock.Part_Mesh) == null) fos = new FileOutputStream(new File(partsDirectory + p.header.partName + "-recompiled"));
					else fos = new FileOutputStream(new File(partsDirectory + p.header.partName));
					fos.write(p.save(0));
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
						
			System.out.println("Parts saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void writeConfig(String geomFile) {
		writeConfig(geomFile, geomFile.replace(".BIN", ".ini"));
	}
	public static void writeConfig(String geomFile, String materialsDump) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			geom.writeConfig(new File(materialsDump));
			
			System.out.println("Config saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void exportToFile(String geomFile, String exportFile) { //ready ?
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			WavefrontOBJ.save(geom, exportFile);
//			geom.writeConfig(new File(exportFile.replace(ff.getExtension(), "")+".ini"));
			
			System.out.println("Saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Geometry importFromFile(String modelFile) {
		return importFromFile(modelFile, modelFile.replace(modelFile.split("\\.")[modelFile.split("\\.").length-1], "ini"));
	}
	public static Geometry importFromFile(String modelFile, String configFile) {
		try {

			long time = System.currentTimeMillis();
			
			Geometry geom = new Geometry();
//			ArrayList<AutosculptLinking> asLinking = new ArrayList<AutosculptLinking>();
			
			//load config
			geom.readConfig(new File(configFile));
			

			System.out.println("Config read in "+(System.currentTimeMillis()-time)+" ms.");
			time = System.currentTimeMillis();
			
			//load 3D model file and create parts and materials			
			if (modelFile.endsWith(".obj")) WavefrontOBJ.load(geom, new File(modelFile));
			if (modelFile.endsWith(".z3d")) ZModelerZ3D.load(geom, new File(modelFile));
			
			

			System.out.println("3D model converted in "+(System.currentTimeMillis()-time)+" ms.");
			time = System.currentTimeMillis();
			
			return geom;
			
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void save(Geometry geom, String path) {
		try {
			long t = System.currentTimeMillis();
			
			var save = geom.save(0);
			var fos = new FileOutputStream(new File(path));
			fos.write(save);
			fos.close();
			
			System.out.println("Geom saved in "+(System.currentTimeMillis()-t)+" ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void recompileCar(String car) {
		if (!new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY-VANILLA.BIN").isFile()) {
			//create vanilla backup if not existing
			new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY.BIN").renameTo(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY-VANILLA.BIN"));
		}
		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY-VANILLA.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY_exported.obj");
		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY_exported.obj");
		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY.BIN");
	}
	
	public static void generateConfigFromCtkConfig(String ctkConfig, String ucgtConfig) {
		try {
			Geometry.ctkConfigToUCGTConfig(new File(ctkConfig), new File(ucgtConfig));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY-VANILLA.BIN");


//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A");
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_GTR_R35_08\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NSX\\GEOMETRY.BIN");

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\COP_CAR_MID_05\\GEOMETRY.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A KIT00_LIGHTBAR_A"
//		);
//		PartVisualizer.run();
		
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\E36\\GEOMETRY.BIN", 
//				"KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A KIT00_LIGHTBAR_A"
//		"KIT00_BODY_D KIT00_BUMPER_FRONT_D KIT00_BUMPER_REAR_D KIT00_DOOR_LEFT_D KIT00_DOOR_RIGHT_D KIT00_HOOD_D KIT00_TRUNK_D KIT00_CHASSIS_D"
//		+ "KIT00_HEADLIGHT_LEFT_D KIT00_HEADLIGHT_RIGHT_D KIT00_BRAKELIGHT_LEFT_D KIT00_BRAKELIGHT_RIGHT_D KIT00_BASE_D"
//		);
//		PartVisualizer.run();
		
		
//		Geometry.defaultCompressionLevel = CompressionLevel.Maximum;
//		Geometry.defaultCompressionLevel = CompressionLevel.High;
//		Geometry.defaultCompressionType = CompressionType.RawDecompressed;
//		Geometry.IMPORT_importVertexColors = false;
//		Geometry.IMPORT_calculateVertexColors = true;
//		Geometry.IMPORT_Tangents = SettingsImport_Tangents.HIGH;
//		Geometry.EXPORT_vanillaPlusMaterials = true;

//		long t = System.currentTimeMillis();
		
//		recompileCar("AUD_RS4_STK_08");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\COP_CAR_MID_05\\GEOMETRY-VANILLA.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\COP_CAR_MID_05\\GEOMETRY_exported.obj");
//		recompileCar("COP_CAR_MID_05");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\COP_CAR_MID_05\\GEOMETRY.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\COP_CAR_MID_05\\GEOMETRY_redecomp.obj");
//		recompileCar("BMW_M3_E46_03");
//		recompileCar("CHE_VEL_SS_70");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY_redecomp.obj");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY-VANILLA.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY_exported.obj");
//		PartVisualizer.run();

//		recompileCar("LAM_GAL_560_09");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\LAM_GAL_560_09\\GEOMETRY.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\LAM_GAL_560_09\\GEOMETRY_redecomp.obj");
//		recompileCar("FOR_GT_STK_06");

//		recompileCar("BMW_M6_STK_08");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\BMW_M6_STK_08\\GEOMETRY.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\BMW_M6_STK_08\\GEOMETRY_redecomp.obj");
//		recompileCar("DOD_CHA_CON_06");
//		var car = "DOD_CHA_CON_06";
//////		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY-VANILLA.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY_exported.obj");
//		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY_exported.obj");
//		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\"+car+"\\GEOMETRY.BIN");
//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\DOD_CHA_CON_06\\GEOMETRY.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\DOD_CHA_CON_06\\GEOMETRY_redecomp.obj");

//		recompileCar("LEX_IS_F_08");
//		recompileCar("MIT_EVO_IX_06");
//		recompileCar("POR_911_GT2_08");
		
//		Geometry.defaultCompressionType = CompressionType.RawDecompressed;
		
//		recompileCar("TRF_CAR_SED_81");
//		recompileCar("TRF_CAR_SML_03");
//		recompileCar("TRF_CAR_STW_83");
//		recompileCar("TRF_CAR_SUV_01");
//		recompileCar("TRF_CAR_TAX_96");
//		recompileCar("TRF_CAR_TRK_97");
//		recompileCar("TRF_CAR_VAN_93");
//		recompileCar("TRF_PRO_SUV_01");
//		recompileCar("TRF_TRA_BOX_83");
//		recompileCar("TRF_TRA_FLT_84");
//		recompileCar("TRF_TRA_LOG_84");
//		recompileCar("TRF_TRK_BUS_87");
//		recompileCar("TRF_TRK_DMP_91");
//		recompileCar("TRF_TRK_MOV_94");
//		recompileCar("TRF_TRK_SEM_95");
//		recompileCar("TRF_VAN_STD_84");
		
//		System.out.println("Fried the laptop for a good "+(int)((System.currentTimeMillis()-t)/1000)+" seconds");

//		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRy blender.obj");
//		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRy blender ctk.obj");
//		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\CHE_VEL_SS_70\\GEOMETRY.BIN");

//
//		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.z3d");
//		
//		//blockinterval, searchlength, samevaltotrack, bruteforcelength
//		Geometry.defaultCompressionLevel = new CompressionLevel(1, 1, 10, 8192, "High"); 	//	27976 ms, 7511 kB
//		Geometry.defaultCompressionLevel = new CompressionLevel(1, 256, 10, 8192, "High"); 	//	 ms,  kB
//		Geometry.defaultCompressionLevel = new CompressionLevel(4, 16, 10, 8192, "High"); 	//	26033 ms, 7511 kB and lower ram usage
//		Geometry.defaultCompressionLevel = new CompressionLevel(64, 256, 32, 8192, "High"); //	27003 ms, 7511 kB
//		Geometry.defaultCompressionLevel = new CompressionLevel(8, 64, 10, 8192, "High"); 	//	25384 ms, 7511 kB
//		Geometry.defaultCompressionLevel = new CompressionLevel(16, 16, 50, 8192, "High"); 	//	27341 ms,  kB much lower ram usage
//		Geometry.defaultCompressionLevel = new CompressionLevel(8, 64, 10, 8192, "High"); 	//	 ms,  kB
////		Geometry.defaultCompressionLevel = new CompressionLevel(1, 1, 10, 8192, "High"); 	//	 ms,  kB
//
//		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\compression tests\\GEOMETRY 8 64 10 8192.BIN");
		
		Geometry.SAVE_sortEverythingByName = false;
		

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\POR_911_GT2_08\\GEOMETRY.BIN");
		
		
//		var geom1 = Geometry.load(new File("G:\\Autres ordinateurs\\Mon ordinateur\\a UCE BETA 1 PREVIEW PACK\\DATA\\CARS\\LAM_DIA_SV_95\\GEOMETRY.BIN"));
//		for (var p : geom1.parts) {
//			if (p.name.contains("BRAKELIGHT") && p.lod.equals("A")) {
//				System.out.println(p.name);
//				for (var mat : p.mesh.materials.materials) {
//					System.out.println(mat.uniqueName+", frontend rendering data : "+mat.frontendRenderingData + " usage specific : "+ mat.usageSpecific1 +", "
//				+ mat.usageSpecific2+", "+ mat.usageSpecific3+" flags : "+mat.flags[0]+","+mat.flags[1]+","+mat.flags[2]+","+mat.flags[3]);
//				}
//				System.out.println();
//			}
//		}
//		System.out.println("ctk ^");
//		
		
//		var geom = Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\LAM_DIA_SV_95\\GEOMETRY.BIN"));
//		for (var p : geom.parts) {
//
//			if (p.name.equals("KIT00_BODY_A")) {
//				System.out.println(p.name);
//				for (var mat : p.mesh.materials.materials) {
//					System.out.println(mat.uniqueName+", frontend rendering data : "+mat.frontendRenderingData + " usage specific : "+ mat.usageSpecific1 +", "
//				+ mat.usageSpecific2+", "+ mat.usageSpecific3+" flags : "+mat.flags[0]+","+mat.flags[1]+","+mat.flags[2]+","+mat.flags[3]);
//				}
//				System.out.println();
//				//GRILL, DECAL, CARSKIN, DULLPLASTIC, ENGINE
//				p.mesh.materials.materials.get(0).usageSpecific1 = 3; //3, GRILL
//				p.mesh.materials.materials.get(1).usageSpecific1 = 2; //2, DECAL
//			}
//
//			if (p.name.contains("BRAKELIGHT") && p.lod.equals("A")) {
//				System.out.println(p.name);
//				for (var mat : p.mesh.materials.materials) {
//					System.out.println(mat.uniqueName+", frontend rendering data : "+mat.frontendRenderingData + " usage specific : "+ mat.usageSpecific1 +", "
//				+ mat.usageSpecific2+", "+ mat.usageSpecific3+" flags : "+mat.flags[0]+","+mat.flags[1]+","+mat.flags[2]+","+mat.flags[3]);
//				}
//				System.out.println();
//				if (p.name.equals("KIT00_BRAKELIGHT_LEFT_A")) {//DECAL, DULLPLASTIC, BRAKELIGHT
//					p.mesh.materials.materials.get(0).usageSpecific1 = 0; //2, DECAL
//					p.mesh.materials.materials.get(0).usageSpecific2 = -1; //1, DECAL
//					p.mesh.materials.materials.get(2).usageSpecific1 = -1; //3, BRAKELIGHT
//				}
//				if (p.name.equals("KIT00_BRAKELIGHT_GLASS_LEFT_A")) {//BRAKELIGHTGLASSRED
//					p.mesh.materials.materials.get(0).usageSpecific1 = -1; //1
//				}
//			}
//			
//			
//			if (p.name.contains("BUMPER") && p.lod.equals("A")) {
//				System.out.println(p.name);
//				for (var mat : p.mesh.materials.materials) {
//					System.out.println(mat.uniqueName+", frontend rendering data : "+mat.frontendRenderingData + " usage specific : "+ mat.usageSpecific1 +", "
//				+ mat.usageSpecific2+", "+ mat.usageSpecific3+" flags : "+mat.flags[0]+","+mat.flags[1]+","+mat.flags[2]+","+mat.flags[3]);
//				}
//				System.out.println();
//			}
//		}
//		geom.save(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\LAM_DIA_SV_95\\GEOMETRY.BIN"));
		
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\LAM_DIA_SV_95\\GEOMETRY.BIN", "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\a UCE\\CARS- DIABLO SV\\PARTS-UCGT\\");
//		dumpPartsAsIs("G:\\Autres ordinateurs\\Mon ordinateur\\a UCE BETA 1 PREVIEW PACK\\DATA\\CARS\\LAM_DIA_SV_95\\GEOMETRY.BIN", "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\a UCE\\CARS- DIABLO SV\\PARTS-CTK\\");
		
		
//		geom = Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\POR_911_GT2_08\\GEOMETRY.BIN"));
//		for (var p : geom.parts) {
//			if ((p.name.contains("LIGHT")) && p.lod.equals("A")) {
//				System.out.println(p.name);
//				for (var mat : p.mesh.materials.materials) {
//					System.out.println(mat.uniqueName+", frontend rendering data : "+mat.frontendRenderingData + " usage specific : "+ mat.usageSpecific1 +", "
//				+ mat.usageSpecific2+", "+ mat.usageSpecific3+" flags : "+mat.flags[0]+","+mat.flags[1]+","+mat.flags[2]+","+mat.flags[3]);
//				}
//				System.out.println();
//			}
//		}
		dumpPartsRecompiled("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\GEOMETRY BMWM3E92 PS.BIN");
	}
}
