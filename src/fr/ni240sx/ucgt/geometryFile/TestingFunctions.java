package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;

public class TestingFunctions {

	public static void dumpPartsAsIs(String geomFile) {
		dumpPartsAsIs(geomFile, geomFile+"-parts\\");
	}
	public static void dumpPartsAsIs(String geomFile, String partsDirectory) {

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
		dumpPartsRecompiled(geomFile, partsDirectory);
		Block.doNotRead.clear();
	}
	
	public static void dumpPartsRecompiled(String geomFile) {
		dumpPartsRecompiled(geomFile, geomFile+"-parts\\");
	}
	public static void dumpPartsRecompiled(String geomFile, String partsDirectory) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			Files.createDirectories(Paths.get(partsDirectory));
			
			for (Part p : geom.parts) {
				FileOutputStream fos;
				try {
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
	
	
	public static void dumpMaterials(String geomFile) {
		dumpMaterials(geomFile, geomFile+"-materials.txt");
	}
	public static void dumpMaterials(String geomFile, String materialsDump) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			try {
				var bw = new BufferedWriter(new FileWriter(new File(materialsDump)));
				
				bw.write("=== MATERIALS DUMP FOR CAR "+geom.carname+" ===\n"
						+ "UCGT by NI240SX\n");
				for (var m : geom.materials) {
					bw.write(m.toConfig()+"\n");
				}
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			System.out.println("Materials dump saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) throws InterruptedException {

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN");
//		dumpPartsRecompiled("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN");

//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A");
//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.run();

//		try {
//			Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak");
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\DOD_VIP_SRT_08\\GEOMETRY.BIN");
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\KOE_CCX_STK_06\\GEOMETRY.BIN");
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\PON_SOL_GXP_06\\GEOMETRY.BIN");
		dumpMaterials("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_GTR_R35_08\\GEOMETRY.BIN");

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_GTR_R35_08\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
	}
}
