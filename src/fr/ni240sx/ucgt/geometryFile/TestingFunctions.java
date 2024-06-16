package fr.ni240sx.ucgt.geometryFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptLink;
import fr.ni240sx.ucgt.geometryFile.part.AutosculptLinking;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;

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
		Block.doNotRead.put(GeomBlock.Part_Mesh_Materials, true);
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
	
	public static void exportToFile2(String geomFile, String exportFile) { //ready ?
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
			var br = new BufferedReader(new FileReader(new File(configFile)));
			String l;
			while ((l=br.readLine())!=null) {
				switch (l.split("	")[0].split(" ")[0]) { // support for both space and tab separators
				case "SETTING":
					for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("SETTING")) {
						switch (s2.split("=")[0]) {
						case "CompressionLevel":
							Part.defaultCompressionLevel = CompressionLevel.fromName(s2.split("=")[1]);
							System.out.println("Compression level set : "+Part.defaultCompressionLevel.getName());
							break;
						case "CarName":
							geom.carname = s2.split("=")[1];
							System.out.println("Car name set : "+geom.carname);
							break;
						default:
							System.out.println("Setting not supported : "+s2);
						}
					}
					break;
					
				case "MATERIAL": // IF THE CAR NAME ISN'T SET BEFORE THIS, CAR-SPECIFIC TEXTURES WILL BREAK
					var m = new fr.ni240sx.ucgt.geometryFile.part.mesh.Material();
					geom.materials.add(m);
					for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MATERIAL")) {
						if (!s2.contains("=")) { //material name
							m.uniqueName = s2;
						} else { //shader or texture usage
							if (TextureUsage.get(s2.split("=")[1]) != TextureUsage.INVALID) {
								// texture usage
								m.TextureHashes.add(new Hash(s2.split("=")[0].replace("%", geom.carname)));
								m.textureUsages.add(TextureUsage.get(s2.split("=")[1]));
							} else {
								//shader usage
								m.ShaderHash = new Hash(s2.split("=")[0]);
								m.shaderUsage = ShaderUsage.get(s2.split("=")[1]);
							}
						}
					}
//					System.out.println("Material : "+m.toConfig(geom.carname));
					break;
					
				case "MARKER":
					var mp = new MPoint();
					geom.mpoints.add(mp);
					int iterator = 0;
					float u=0, v=0, w=0; //euler angles
					for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("MARKER")) {
						switch (iterator) {
						case 0:
							mp.uniqueName = s2;
							break;
						case 1:
							mp.nameHash = new Hash(s2);
							break;
						case 2:
							mp.tempPartName = s2;
							break;
						case 3:
							u = Float.parseFloat(s2);
							break;
						case 4:
							v = Float.parseFloat(s2);
							break;
						case 5:
							w = Float.parseFloat(s2);
							break;
						}
						iterator++;
					}
					mp.matrix = MPoint.eulerAnglesToMatrix(u, v, w);
//					System.out.println("Marker : "+l);
					break;
				case "ASLINK":
					var asl = new AutosculptLinking();
					geom.asLinking.add(asl);
					for (var s1 : l.split("	")) for (var s2 : s1.split(" ")) if (!s2.isEmpty() && !s2.equals("ASLINK")) {
						if (!s2.contains(",")) { //part name
							asl.tempPartName = s2;
						} else {
							asl.links.add(new AutosculptLink(new Hash(s2.split(",")[0]).binHash, 
									Short.parseShort(s2.split(",")[1]), 
									Short.parseShort(s2.split(",")[2]), 
									Short.parseShort(s2.split(",")[3]), 
									Short.parseShort(s2.split(",")[4])  ));
						}
					}
//					System.out.println("Autosculpt link : "+l);
					break;
				}
			}//loop on config file lines
			br.close();
			

			System.out.println("Config read in "+(System.currentTimeMillis()-time)+" ms.");
			time = System.currentTimeMillis();
			
			//load 3D model file and create parts and materials			
			WavefrontOBJ.load(geom, new File(modelFile));
			
			

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
	
	public static void main(String[] args) throws Exception {

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak");
//		dumpPartsRecompiled("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak");		
		
//		dumpPartsRecompiled("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN");

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A");
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");

//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();

//		try {
//			var g = Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak"));
//			for (var p : g.parts) for (var m : p.mesh.materials.materials) {
//				System.out.println(p.header.partName+" - "+m.ShaderHash.label+" - triVertices="+m.numTriVertices+"/vertices="+m.verticesBlock.vertices.size()+"/triangles x3="+(m.triangles.size()*3));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		writeConfig("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak");
//		writeConfig("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
		
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_GTR_R35_08\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NSX\\GEOMETRY.BIN");


//		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak", "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY", FileFormat.WAVEFRONTOBJ);
//		exportToFile2("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak", "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.obj");
		
		
		
		
		// RECOMPILING MY SHIT ???
		var geom = importFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.obj");		//geom.geomHeader.geomInfo.setHeaderVanilla();
//		var vanilla = Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak"));

		Part.defaultCompressionLevel = CompressionLevel.Maximum;
//		swapMeshes(geom, vanilla);
//		geom.materials = vanilla.materials;
//		geom.geomHeader = vanilla.geomHeader;
//		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");
		save(geom, "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");

//		swapMeshes(vanilla, geom);
//		save(vanilla, "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");
//		save(vanilla, "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.BIN");
		
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");

//		exportToFile2("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.BIN", "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade_redecomp.obj");
	
//		PartVisualizer.setPartsFromFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY_homemade.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();
		
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\ctk\\GEOMETRY.BIN");
	}
}
