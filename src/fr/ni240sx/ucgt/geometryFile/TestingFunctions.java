package fr.ni240sx.ucgt.geometryFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.geometry.GeomHeader;
import fr.ni240sx.ucgt.geometryFile.gui.FileOffset;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.geometryFile.textures.TPKHeader;
import javafx.scene.Group;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.util.Pair;

@SuppressWarnings("unused")
public class TestingFunctions {

	public static void dumpPartsAsIs(String geomFile) {
		dumpPartsAsIs(geomFile, geomFile+"-parts\\");
	}
	public static void dumpPartsAsIs(String geomFile, String partsDirectory) {
//		Block.doNotRead.put(GeomBlock.Part_Header, true);
		
		Block.doNotRead.put(BlockType.Part_AutosculptLinking, true);
		Block.doNotRead.put(BlockType.Part_AutosculptZones, true);
		Block.doNotRead.put(BlockType.Part_HashAssign, true);
		Block.doNotRead.put(BlockType.Part_HashList, true);
		Block.doNotRead.put(BlockType.Part_Mesh, true);
		Block.doNotRead.put(BlockType.Part_MPoints, true);
		Block.doNotRead.put(BlockType.Part_Padding, true);
		Block.doNotRead.put(BlockType.Part_ShaderList, true);
		Block.doNotRead.put(BlockType.Part_Strings, true);
		Block.doNotRead.put(BlockType.Part_TexUsage, true);
		Block.doNotRead.put(BlockType.Part_Mesh_Materials_PC, true);
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
					String name;
					if (p.header != null) name = p.header.partName;
					else name = "BROKEN_"+geom.parts.indexOf(p);
					if (Block.doNotRead.get(BlockType.Part_Mesh) == null) fos = new FileOutputStream(new File(partsDirectory + name + "-recompiled"));
					else fos = new FileOutputStream(new File(partsDirectory + name));
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

	public static int getNextInt(FileInputStream fis) throws IOException {
		return fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
	}
	
	public static void findAlignment(boolean findMinFileAlignment, String s)
			throws FileNotFoundException, IOException, Exception {
		var dict = new HashMap<Integer,Integer>(); //block type, alignment
		var fis = new FileInputStream(new File(s));
		int inPos=0;
		while (fis.available()>0) {
			var bTypeRaw = getNextInt(fis);
			var bType = BlockType.get(bTypeRaw);
			var bSize = getNextInt(fis);
			
			if (!findMinFileAlignment) {
				int mod = 16384;
				while (inPos % mod != 0) mod /= 2;
				if (mod > 1024) System.out.println();
				
				if (bType.equals(BlockType.Geometry)) {
					getNextInt(fis);
					var paddingLength = getNextInt(fis);
					fis.skipNBytes(paddingLength);
					getNextInt(fis); //BlockType.GeomHeader
					var headerLength = getNextInt(fis);
					
					var headerData = ByteBuffer.allocate(headerLength+4);
					headerData.order(ByteOrder.LITTLE_ENDIAN);
					headerData.putInt(0, headerLength);
					fis.read(headerData.array(), 4, headerLength);
					
					fis.skipNBytes(bSize-headerLength-paddingLength-16);
					
					var header = new GeomHeader(headerData);
	//								System.out.println(header.geomInfo.filename);
					System.out.println(String.format("%10d", inPos) + " - " + String.format("0x%08X", bTypeRaw) + " (" + bType + ") has alignment modulo "+mod+" - \""+header.geomInfo.filename+"\" chunk "+header.geomInfo.blockname);
				} else if (bType.equals(BlockType.TPK)){
					getNextInt(fis);
					var paddingLength = getNextInt(fis);
					fis.skipNBytes(paddingLength);
					getNextInt(fis); //BlockType.GeomHeader
					var headerLength = getNextInt(fis);
					
					var headerData = ByteBuffer.allocate(headerLength+4);
					headerData.order(ByteOrder.LITTLE_ENDIAN);
					headerData.putInt(0, headerLength);
					fis.read(headerData.array(), 4, headerLength);
					
					fis.skipNBytes(bSize-headerLength-paddingLength-16);
					
					var header = new TPKHeader(headerData);
					System.out.println(String.format("%10d", inPos) + " - " + String.format("0x%08X", bTypeRaw) + " (" + bType + ") has alignment modulo "+mod+" - \""+header.info.filename+"\" chunk "+header.info.blockname);
				} else {
					if (bType == BlockType.Padding)
						System.out.println(String.format("%10d", inPos) + " - " + String.format("0x%08X", bTypeRaw) + " (" + bType + ") has alignment modulo "+mod+" - padding length "+bSize);
					else
						System.out.println(String.format("%10d", inPos) + " - " + String.format("0x%08X", bTypeRaw) + " (" + bType + ") has alignment modulo "+mod);
					fis.skipNBytes(bSize);
				}
			}else {
			
				
				int mod = 4096;
				while (inPos % mod != 0) mod /= 2;
				if (!dict.containsKey(bTypeRaw)) {
					dict.put(bTypeRaw, mod);
				} else {
					if (dict.get(bTypeRaw) > mod) dict.put(bTypeRaw, mod);
				}
				fis.skipNBytes(bSize);

			}
			
			inPos+=bSize+8;
		}
		fis.close();
	
		if (findMinFileAlignment) {
			for (var k : dict.keySet()) {
				System.out.println(String.format("0x%08X", k) + " (" + BlockType.get(k) + ") has alignment modulo "+dict.get(k));
			}
		}
	}

	public static void collisionDisplayTest(Group viewportGroup) {
		try {
			File f;
			var fis = new FileInputStream(f = new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\chunk B3 PRAC block suspected verts0"));
			byte[] verts0 = new byte[(int) f.length()];
			fis.read(verts0);
			fis.close();
			
			fis = new FileInputStream(f = new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\chunk B3 PRAC suspected tri0"));
			byte[] tri0 = new byte[(int) f.length()];
			fis.read(tri0);
			fis.close();

			var Verts0 = ByteBuffer.wrap(verts0);
			Verts0.order(ByteOrder.LITTLE_ENDIAN);
			var Tris0 = ByteBuffer.wrap(tri0);		
			Tris0.order(ByteOrder.LITTLE_ENDIAN);	
			
			TriangleMesh matMesh = new TriangleMesh();
			
			matMesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
			matMesh.getTexCoords().addAll(0, 0);
			
			while (Verts0.hasRemaining()) {
				matMesh.getPoints().addAll(Verts0.getFloat(), Verts0.getFloat(), -Verts0.getFloat());
				Verts0.getFloat();
			}
			while (Tris0.hasRemaining()) {
				var v1 = Tris0.getShort();
				var v2 = Tris0.getShort();
				var v3 = Tris0.getShort();
				matMesh.getFaces().addAll(
						// points normals   texcoords
						v1, 0, //v1
						v3, 0, //v2
						v2, 0);//v3						
				Tris0.getShort();
			}
			
			var mv = new MeshView(matMesh);
			mv.setCullFace(CullFace.NONE);
			
			viewportGroup.getChildren().add(mv);

			
			
			
			
			
			
			
			
			
			
			
			
			fis = new FileInputStream(f = new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\chunk B3 suspected verts1"));
			byte[] verts1 = new byte[(int) f.length()];
			fis.read(verts1);
			fis.close();
			
			fis = new FileInputStream(f = new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\chunk B3 suspected tri1"));
			byte[] tri1 = new byte[(int) f.length()];
			fis.read(tri1);
			fis.close();

			var Verts1 = ByteBuffer.wrap(verts1);
			Verts1.order(ByteOrder.LITTLE_ENDIAN);
			var Tris1 = ByteBuffer.wrap(tri1);		
			Tris1.order(ByteOrder.LITTLE_ENDIAN);	
			
			matMesh = new TriangleMesh();
			
			matMesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
			matMesh.getTexCoords().addAll(0, 0);
			
			while (Verts1.hasRemaining()) {
				matMesh.getPoints().addAll(Verts1.getFloat(), Verts1.getFloat(), -Verts1.getFloat());
				Verts1.getFloat();
			}
			while (Tris1.hasRemaining()) {
				var v1 = Tris1.getShort();
				var v2 = Tris1.getShort();
				var v3 = Tris1.getShort();
				matMesh.getFaces().addAll(
						// points normals   texcoords
						v1, 0, //v1
						v3, 0, //v2
						v2, 0);//v3				
				Tris1.getShort();
			}
			
			mv = new MeshView(matMesh);
			mv.setCullFace(CullFace.NONE);
			
			viewportGroup.getChildren().add(mv);


		} catch (Exception e) {
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
		
//		Geometry.SAVE_sortEverythingByName = false;
		

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
//		dumpPartsRecompiled("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\GEOMETRY BMWM3E92 PS.BIN");
		
		
		
//		File f = new File("D:\\Jeux\\UCEtesting\\NIS\\Scene_ModelTest_AnimBundle");
//		FileInputStream fis = new FileInputStream(f);
//		byte [] arr = new byte[(int)f.length()];
//		fis.read(arr);
//		fis.close();
//		var bb = ByteBuffer.wrap(arr);
//		bb.order(ByteOrder.LITTLE_ENDIAN);
//		int i=0;
//		while (bb.hasRemaining()) {
//			var blockheader = bb.position();
//			try {
//				var b = Block.read(bb);
//				System.out.println(b.getBlockID().getName());
//				if (b.getClass() == Geometry.class) {
//					var geom = (Geometry) b;
//					System.out.println("geom file name "+geom.geomHeader.geomInfo.filename+", geom block name "+geom.geomHeader.geomInfo.blockname);
//					geom.writeConfig(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\NIS\\"+geom.geomHeader.geomInfo.filename+".ini"));
//					ZModelerZ3D.save(geom, "C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\NIS\\"+geom.geomHeader.geomInfo.filename);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				bb.position(blockheader+8 + bb.getInt(blockheader+4));
//			}
//			i++;
//		}
		
		

//		dumpPartsAsIs("D:\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\CARS\\350Z\\GEOMETRY.BIN","C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\PS 350Z DUMP\\");
//		dumpPartsRecompiled("D:\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\CARS\\350Z\\GEOMETRY.BIN","C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\PS 350Z DUMP\\");
		
		
		
		
//		Geometry.dumpStream("D:\\Jeux\\UCE 1.0.1.18\\TRACKS\\STREAML8R_MW2.BUN", "C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\STREAML8R_MW2\\", "ALL", "");

			
		
//		Geometry.replaceInStream("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\STREAML8R_MW2 recompiled", "D:\\Jeux\\UCE 1.0.1.18\\TRACKS\\STREAML8R_MW2.BUN", "D:\\Jeux\\UCE 1.0.1.18\\TRACKS\\L8R_MW2.BUN");
		
//		for (var f : new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)").listFiles()) if (f.isFile()) {
//			var geom = Geometry.load(f);
//			geom.save(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)\\recomp\\"+f.getName().replace(".bin", "-resaved.bin")));
//			geom.writeConfig(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)\\recomp\\"+f.getName().replace(".bin", "-recompiled.ini")));
//			ZModelerZ3D.save(geom, "C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)\\recomp\\"+f.getName().replace(".bin", "-recompiled.z3d"));
//
//			geom = Geometry.importFromFile(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)\\recomp\\"+f.getName().replace(".bin", "-recompiled.z3d")));
//			geom.save(new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\world models (again)\\recomp\\"+f.getName().replace(".bin", "-recompiled.bin")));
//		}
		
		
//		var fis = new FileInputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\TPK X0.mpk"));
//		fis.readNBytes(4);
//		var arr = fis.readAllBytes();
//		fis.close();
//		
//		var tpk = new TPK(ByteBuffer.wrap(arr));
//		var fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\TPK X0-recompiled.mpk"));
//		fos.write(tpk.save(0));
//		fos.close();
//		tpk.exportToFolder("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08");
//		tpk.textures.forEach(t -> {
//			try {
//				var fos = new FileOutputStream(new File("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\BMW_M3_E92_08\\textures\\"+t.name));
//				fos.write(t.save(9));
//				fos.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		});

	/*
0x00000000 (Padding) has alignment modulo 4
0x0003B802 (INVALID) has alignment modulo 32
0x0003B801 (INVALID) has alignment modulo 32
0x80034100 (INVALID) has alignment modulo 128
0x00037220 (INVALID) has alignment modulo 128
0x00037260 (INVALID) has alignment modulo 4
0x0003BC00 (INVALID) has alignment modulo 16
0x80036000 (INVALID) has alignment modulo 16
0x00034027 (INVALID) has alignment modulo 128
0x00034185 (INVALID) has alignment modulo 16
0xB3300000 (TPK) has alignment modulo 128
0x80134000 (Geometry) has alignment modulo 4
0x00037250 (INVALID) has alignment modulo 4
0x00037270 (INVALID) has alignment modulo 8
0x00034159 (INVALID) has alignment modulo 4
	 */

//		findAlignment(false, "C:\\jeux\\UCE 1.0.1.18\\TRACKS\\STREAML8R_MW2.OLD");
//		findAlignment(false, "C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\stream chunk C85");
	
		dumpPartsAsIs("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\PS X360\\GEOMETRY.BIN");
		
	}
}
