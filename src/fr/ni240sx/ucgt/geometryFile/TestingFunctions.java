package fr.ni240sx.ucgt.geometryFile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.stream.MemoryCacheImageInputStream;

import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.StreamChunksOffsets.ChunkInfo;
import fr.ni240sx.ucgt.geometryFile.geometry.GeomHeader;
import fr.ni240sx.ucgt.geometryFile.gui.FileOffset;
import fr.ni240sx.ucgt.geometryFile.io.WavefrontOBJ;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;
import fr.ni240sx.ucgt.geometryFile.textures.NFSTexture;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.geometryFile.textures.TPKHeader;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Hash;
import fr.ni240sx.ucgt.shared.OrbitCameraViewport;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.nikr.dds.DDSImageReader;

public class TestingFunctions extends Application{

	public static PhongMaterial vertexColorMaterial = new PhongMaterial();
	public static final PhongMaterial UNKNOWN_OBJECT = new PhongMaterial(Color.color(1, 0, 1, 0.2));
	public static WritableImage vertexColorImage;
	
	static{
		vertexColorImage = new WritableImage(256, 1);
		for (int i=0; i<vertexColorImage.getWidth(); i++) {
			vertexColorImage.getPixelWriter().setColor(i,0, Color.rgb(i, i, i, 1));
		}
		vertexColorMaterial.setDiffuseMap(vertexColorImage);
	}

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
	
	private static void unhashFile(String f) throws FileNotFoundException, IOException {
		var br = new BufferedReader(new FileReader(new File(f)));
		var bw = new BufferedWriter(new FileWriter(new File(f.contains(".") ? f.replace(".", "-unhash.") : f + "-unhash")));
		String l;
		while ((l = br.readLine()) != null) {
			if (l.startsWith("0x") || l.startsWith("0X")) {
				var hex = l.substring(2,l.length() > 10 ? 10 : l.length());
				int hash = Integer.parseUnsignedInt(hex, 16);
				bw.write("0x" + hex);
				if (Hash.getBIN(hash, null) != null) bw.write(" BIN="+Hash.getBIN(hash));
				if (Hash.getVLT(hash, null) != null) bw.write(" VLT="+Hash.getVLT(hash));
				bw.write("\n");
			} else {
				bw.write(l+"\n");
			}
		}
		br.close();
		bw.close();
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
	
//		dumpPartsAsIs("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\PS X360\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\UC PS3\\NIS_240_SX_89\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\UCGT\\proshit ps3\\240SX\\GEOMETRY.BIN");
		
		
		
//		Hash.addHashes(new File("data/parts"));
//		Hash.addHashes(new File("data/gentextures"));
//		Hash.addHashes(new File("data/shaders"));
//		Hash.addHashes(new File("data/mpoints"));
//		Hash.addHashes(new File("data/stuff"));
//		Hash.addHashes(new File("data/collisionshashes"));
//		Hash.addHashes(new File("G:\\Autres ordinateurs\\Mon ordinateur\\ressources\\vanilla hashes\\attributes.txt"));
//		Hash.addHashes(new File("G:\\Autres ordinateurs\\Mon ordinateur\\ressources\\vanilla hashes\\cars_vault.txt"));
//		Hash.addHashes(new File("G:\\Autres ordinateurs\\Mon ordinateur\\ressources\\vanilla hashes\\fe_attrib.txt"));
//		Hash.addHashes(new File("G:\\Autres ordinateurs\\Mon ordinateur\\ressources\\vanilla hashes\\gameplay (incomplete).txt"));
//		Hash.addHashes(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\mdtls\\Binercover_v2.9.0\\mainkeys\\undercover.txt"));
//		Hash.addHashes(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\mdtls\\Binercover_v2.9.0\\mainkeys\\prostreet.txt"));
//		Hash.addHashes(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\mdtls\\Binercover_v2.9.0\\mainkeys\\mostwanted.txt"));
//		Hash.addHashes(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\mdtls\\Binercover_v2.9.0\\mainkeys\\carbon.txt"));
//		
//		unhashFile("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\SurfaceNames.txt");
//		unhashFile("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\AttributeNames.txt");
//		unhashFile("C:\\Users\\gaupp\\OneDrive\\Documents\\z NFS MODDING\\z bordel\\NameHash.txt");

//		for (var vf : fr.ni240sx.ucgt.geometryFile.part.mesh.VertexFormat.values()) {
//			System.out.print(vf.getName() + " -> ");
//			for (var c : vf.components) {
////				System.out.print(c.name()+ " ");
//			}
//			System.out.println();
//		}
		
		l8rFuckeries(args, "C:\\jeux\\UCE 1.0.1.18\\TRACKS", "L8R_MW2.OLD");
//		l8rFuckeries(args, "D:\\Program Files (x86)\\Electronic Arts\\Need for Speed ProStreet\\TRACKS", "L6R_AutobahnDrift.BUN");
//		l8rFuckeries(args, "\\\\Desktop-k413n7l\\d\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\TRACKS", "L6R_AutobahnDrift.BUN");		
//		l8rFuckeries(args, "\\\\Desktop-k413n7l\\d\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\TRACKS", "L6R_FE.BUN");		
//		l8rFuckeries(args, "\\\\Desktop-k413n7l\\d\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\TRACKS", "L6R_ShutoDrift.BUN");		
//		l8rFuckeries(args, "\\\\Desktop-k413n7l\\d\\Program Files\\Electronic Arts\\Need for Speed ProStreet\\TRACKS", "L6R_ShutoExpressway.BUN");	
//		l8rFuckeries(args, "\\\\Desktop-k413n7l\\d\\Jeux\\Need for Speed Carbon Endgame V2\\TRACKS", "L5RA.BUN");	
	}
	
	public static boolean floatEquals(float a, float b) {
		return (int)(a*10) == (int)(b*10);
	}

	static StreamChunksOffsets offsets = null;
	static ChunkBoundary currentChunk = null;
	private static void l8rFuckeries(String[] args, String folder, String file) throws FileNotFoundException, IOException {

		final var PATH = folder + File.separator + file;
		final var STREAM_PATH = folder + File.separator + "STREAM" + file;
		
		var fis = new FileInputStream(new File(PATH));
//		var stream = new FileInputStream(new File(streamPath));
		var in = ByteBuffer.wrap(fis.readAllBytes());
		fis.close();
		
		var chunksRelated = new ArrayList<ChunksRelated>();
		var boundaries = new ArrayList<ChunkBoundary>();
		var elevationRules = new ArrayList<ElevationRule>();
		
		var chunksSpheres = new HashMap<ChunkInfo,Sphere>();
		var chunksGroups = new HashMap<ChunkInfo,Group>();
		
		var meshesGroup = new Group();
		meshesGroup.setDepthTest(DepthTest.ENABLE);
		viewportGroup.getChildren().add(meshesGroup);

		in.order(ByteOrder.LITTLE_ENDIAN);
		
		Hash.addBinHashes(new File("data/worldtextures"));
		
		for (int i=0; i<10000; i++) Hash.addBinHash("BARRIER_SPLINE_"+String.format("%04d", i));
		
		try {
			while (in.hasRemaining()) {
				var blockID = in.getInt();
				var blockLength = in.getInt();
				var blockStart = in.position();
								
				if ((blockID & 0x80000000) != 0) blockLength = 0; //ignore containers
				
				switch(BlockType.get(blockID)) {
//				case ChunksZonesContainer:
//					blockLength = 0; //to read the inside block next iteration
//					break;
				case ChunksZones1:
					if (zones1_police) while (in.position() < blockStart+blockLength) {
//						System.out.println("Reading block");
						var type = in.getInt(); /*
							3 = tunnel, 
							4 = overpass, 
							6 = road names ???, 
							8 = hiding spots,
							9 = regions ? PH N/S/GCH/SH/Canyons/PC, 
							13 = forbidden locations for cops ?
							15 = highway names ???
							16 = interchanges, random parking lots and the whole gold coast highway ???
							
							50 = PC
							51 = PH
							52 = Canyons
							53 = SH
						*/
						var posX = in.getFloat();
						var posY = in.getFloat();
						var float0 = in.getFloat();
						var float1 = in.getFloat();
						var unk0 = in.getInt();
						var unk1 = in.getInt();
						var unk2 = in.getInt();
						var minX = in.getFloat();
						var minY = in.getFloat();
						var maxX = in.getFloat();
						var maxY = in.getFloat();
						var ID0 = in.getInt();
						var ID1 = in.getInt();
						var ID2 = in.getInt();
						var ID3 = in.getInt();
						var numPoints = in.getShort();
						in.getShort(); //thisDataLength = 68+numPoints*8
						var pointsX = new float[numPoints];
						var pointsY = new float[numPoints];
						for (int i=0; i<numPoints; i++) {
							pointsX[i] = in.getFloat();
							pointsY[i] = in.getFloat();
						}
						
//						if (type >= 50) continue;

						var mesh = new TriangleMesh();
						mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
						mesh.getTexCoords().addAll(0, 0);
//						for (int i=0; i<numPoints; i++) {
////							mesh.getPoints().addAll(pointsY[i], 0, pointsX[i]);
//							mesh.getPoints().addAll(pointsX[i], pointsY[i], 0); //-type*100
//						}
//						for (int i=0; i<numPoints-2; i++) {
////							mesh.getFaces().addAll((i)%numPoints, 0, (i+1)%numPoints, 0, (i+2)%numPoints, 0);
//							mesh.getFaces().addAll((numPoints-i-1)%numPoints, 0, (i)%numPoints, 0, (i+1)%numPoints, 0);
//							mesh.getFaces().addAll((numPoints-i-2)%numPoints, 0, (numPoints-i-1)%numPoints, 0, (i)%numPoints, 0);
//						}

						for (int i=0; i<numPoints; i++) {
							mesh.getPoints().addAll(pointsX[i], pointsY[i], 0);
							mesh.getPoints().addAll(pointsX[i], pointsY[i], 500);
						}
						
						for (int i=0; i<2*numPoints; i++) {
							mesh.getFaces().addAll((i)%(2*numPoints), 0, (i+1)%(2*numPoints), 0, (i+2)%(2*numPoints), 0);
							mesh.getFaces().addAll((i+1)%(2*numPoints), 0, (i+2)%(2*numPoints), 0, (i+3)%(2*numPoints), 0);
						}
						var mv = new MeshView(mesh);
						var r = Math.random();
						var g = Math.random();
						var b = Math.random();
						switch (type) {
						case 3: //tunnel
							r = 0.6;
							g = 0.4;
							b = 0.4;
							break;
						case 4: //overpass
							r = 0.7;
							g = 0.7;
							b = 0.75;
							break;
						case 8: //hiding spot
							r = 0.2;
							g = 0.4;
							b = 1;
							break;
						case 13: //locations smth
							r = 1;
							g = 0.2;
							b = 0.2;
							break;
						}
						
						mv.setMaterial(new PhongMaterial(Color.color(r, g, b, 0.2)));
						mv.setCullFace(CullFace.NONE);
//						mv.setDrawMode(DrawMode.LINE);
						mv.setBlendMode(BlendMode.LIGHTEN);
						viewportGroup.getChildren().add(mv);

//						var poly = new Polygon();
//						for (int i=0; i<numPoints; i++) {
//							poly.getPoints().addAll((double)pointsX[i], (double)pointsY[i]);
//						}
//						poly.setFill(Color.color(r, g, b, 0.2));
						
						filter.valueProperty().addListener((obs, was, is) -> {
							if (is != type) viewportGroup.getChildren().remove(mv);
							else if (!viewportGroup.getChildren().contains(mv)) viewportGroup.getChildren().add(mv);
						});
						
						var t = new Tooltip("type="+type+
								"\nfloat0="+float0+
								"\nfloat1="+float1+
								"\nunk0="+unk0+
								"\nunk1="+unk1+
								"\nunk2="+unk2+
								"\nID0="+String.format("0x%08X", ID0)+
								"\nID1="+String.format("0x%08X", ID1)+
								"\nID2="+String.format("0x%08X", ID2)+
								"\nID3="+String.format("0x%08X", ID3));
						mv.setOnMouseEntered(e -> t.show(mv, e.getScreenX()+10, e.getScreenY()+10));
						mv.setOnMouseExited(e -> t.hide());

//						viewportGroup.getChildren().add(poly);
					}
					break;
				case ChunksMarkers: //NIS markers
					while (in.getInt() == 0x11111111) {}
					in.position(in.position()-4);
					Hash.addHashes(new File("data/stuff"));
					if (data1_NISmarkers) while (in.position() < blockStart+blockLength) {
						var type1 = in.getInt(); //11
						var type2 = in.getInt(); //11
						var hash = in.getInt(); //unknown, hash?
						/*
						 * B8560C7D = BustedAlley
						 * B803A89F = BustedCity
						 * A87C527E = BustedHwy
						 * EC90A22A = pursuit breaker
						 * others...
						 */
						in.getInt(); //0
						var x = in.getFloat();
						var y = in.getFloat();
						var z = in.getFloat();
						in.getInt();
						var region = in.getInt(); //region number (8000 or 8999)
						in.getInt(); //AAAAAAAA
						in.getInt(); //AAAAAAAA
						in.get(); //AA
						in.get(); //00
						var sh = in.getShort(); //unknown
						
						var s = new Sphere(10);
						s.setTranslateX(x);
						s.setTranslateY(y);
						s.setTranslateZ(z);
						s.setOnMouseClicked(e -> viewport.moveCamera(x,  y, z));
						s.setMaterial(new PhongMaterial(Color.hsb(hash, 1, 1)));
						viewportGroup.getChildren().add(s);
						
						var t = new Tooltip("type1="+type1+
								"\ntype2="+type2+
								"\nhash="+Hash.getBIN(hash)+
								"\nregion="+region+
								"\nsh="+sh);
						s.setOnMouseEntered(e -> t.show(s, e.getScreenX()+10, e.getScreenY()+10));
						s.setOnMouseExited(e -> t.hide());
					}
					break;
					
				case ChunksBoundaries:
					while (in.position() < blockStart+blockLength) {
						var b = new ChunkBoundary(in);
						boundaries.add(b);
						
						if (zones2_chunkBoundaries) {
							var mesh = new TriangleMesh();
							mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
							mesh.getTexCoords().addAll(0, 0);
//							for (int i=0; i<numPoints; i++) {
//								mesh.getPoints().addAll(pointsY[i], 0, pointsX[i]);
//								mesh.getPoints().addAll(pointsX[i], pointsY[i], 0);
//							}
//							for (int i=0; i<numPoints-2; i++) {
//								mesh.getFaces().addAll(0, 0, i+1, 0, i+2, 0);
//								mesh.getFaces().addAll((numPoints-i-1)%numPoints, 0, (i)%numPoints, 0, (i+1)%numPoints, 0);
//								mesh.getFaces().addAll((numPoints-i-2)%numPoints, 0, (numPoints-i-1)%numPoints, 0, (i)%numPoints, 0);
//							}
							
							mesh.getPoints().addAll((b.maxX+b.minX)/2, (b.maxY+b.minY)/2, 0);
							for (int i=0; i<b.numPoints; i++) {
								mesh.getPoints().addAll(b.pointsX[i], b.pointsY[i], 0);
							}
							
							for (int i=0; i<b.numPoints; i++) {
								mesh.getFaces().addAll(0, 0, (i)%(b.numPoints)+1, 0, (i+1)%(b.numPoints)+1, 0);
							}

							//prisms
//							for (int i=0; i<b.numPoints; i++) {
//								mesh.getPoints().addAll(b.pointsX[i], b.pointsY[i], 0);
//								mesh.getPoints().addAll(b.pointsX[i], b.pointsY[i], 200);
//							}
//							
//							for (int i=0; i<2*b.numPoints; i++) {
//								mesh.getFaces().addAll((i)%(2*b.numPoints), 0, (i+1)%(2*b.numPoints), 0, (i+2)%(2*b.numPoints), 0);
//								mesh.getFaces().addAll((i+1)%(2*b.numPoints), 0, (i+2)%(2*b.numPoints), 0, (i+3)%(2*b.numPoints), 0);
//							}
							
							var mv = new MeshView(mesh);
							var r = Math.random();
							var g = Math.random();
							var blu = Math.random();
							mv.setMaterial(new PhongMaterial(Color.color(r, g, blu, 0.2)));
							mv.setCullFace(CullFace.BACK);
							mv.setDrawMode(DrawMode.FILL);

//							var poly = new Polygon();
//							
//							for (int i=0; i<b.numPoints; i++) {
//								poly.getPoints().addAll((double)b.pointsX[i], (double)b.pointsY[i]);
//							}
//							poly.setFill(Color.color(r, g, blu, 0.2));

							viewportGroup.getChildren().addAll(mv //,poly
									);
							
							var t = new Tooltip("type1="+b.type1+
									"\ntype2="+b.type2+
									"\ntype="+b.v1+
									"\nunk2="+b.unk2+
									"\nprecullerOver="+(char)('A'+(b.precullerOver/1000))+""+b.precullerOver%1000+
									"\nelevationHash="+Hash.getBIN(b.elevationHash)+
									"\nunk3="+b.unk3+
									"\nid="+(char)('A'+(b.id/1000))+""+b.id%1000);
							mv.setOnMouseEntered(e -> t.show(mv, e.getScreenX()+10, e.getScreenY()+10));
							mv.setOnMouseExited(e -> t.hide());
							
							filter.valueProperty().addListener((obs, was, is) -> {
								if (is != b.v1) viewportGroup.getChildren().removeAll(mv//, poly
										);
								else if (!viewportGroup.getChildren().contains(mv)) viewportGroup.getChildren().addAll(mv//, poly
										);
							});
							
							mv.setOnMouseClicked(e -> {
								var otc = viewport.cameraPivotPosition.getOnTransformChanged();
								viewport.cameraPivotPosition.setOnTransformChanged(null);
								viewport.moveCamera(b.posX, b.posY, 0);
								viewport.cameraPivotPosition.setOnTransformChanged(otc);
								if (!displayFromChunksBoundaries(b.id, STREAM_PATH, chunksRelated, offsets.chunkInfos, chunksGroups, new AtomicInteger(), 0)) {
									for (var chunk : offsets.chunkInfos) if (chunk.ID == b.id) {
										if (chunksGroups.get(chunk).getChildren().size() == 0) {
											var models = new ConcurrentHashMap<Integer,Part>();
											var textures = new ConcurrentHashMap<Integer, NFSTexture>();
											var obj = readChunk(STREAM_PATH, chunk, models, textures);
											var nodes = new ArrayList<Node>();
											for (var o : obj) nodes.addAll(renderObject(models, materials, o));
											javafx.application.Platform.runLater(() -> {
												chunksGroups.get(chunk).getChildren().addAll(nodes);
											});
										}	
									} else {
//										javafx.application.Platform.runLater(() -> {
//											chunksGroups.get(chunk).getChildren().clear();
//										});
									}
								}
							});

						}
					}
					break;

				case ChunksElevationRules:
					while (in.position() < blockStart+blockLength) {
						var rule = new ElevationRule(in);
						elevationRules.add(rule);
					
						if (data2_meshes) {
							var mesh = new TriangleMesh();
							mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
							mesh.getTexCoords().addAll(0, 0);
							mesh.getPoints().addAll(rule.x1, rule.y1, rule.z1, rule.x2, rule.y2, rule.z2, rule.x3, rule.y3, rule.z3);
							mesh.getFaces().addAll(0, 0, 1, 0, 2, 0);
							var mv = new MeshView(mesh);
							var r = Math.random();
							var g = Math.random();
							var b = Math.random();
							mv.setMaterial(new PhongMaterial(Color.color(r, g, b, 1)));
							mv.setCullFace(CullFace.NONE);
//							mv.setDrawMode(DrawMode.LINE);
							viewportGroup.getChildren().add(mv);

							var t = new Tooltip("hash="+String.format("0x%08X", rule.hash));
							mv.setOnMouseEntered(e -> t.show(mv, e.getScreenX()+10, e.getScreenY()+10));
							mv.setOnMouseExited(e -> t.hide());	
						}
					}
					break;
					
				case ChunksPreculler:
					while (in.position() < blockStart+blockLength) {
						chunksRelated.add(new ChunksRelated(in));
						//connect with ChunksOffsets and load from a few chunks to see
						//PRECULLER DATA ISNT CHUNKSOFFSETS BUT CHUNKZONES2
					}
					break;
					
				case BarrierSplines:
					while (in.position() < blockStart+blockLength) {
						var fromX = in.getFloat();
						var fromY = in.getFloat();
						var toX = in.getFloat();
						var toY = in.getFloat();
						in.getInt();
						var hash = in.getInt();

						if (barriersplines) {
//							var line = new Line(fromX, fromY, toX, toY);
//							var line = new Polyline(fromX, fromY, toX, toY);
							
							var mesh = new TriangleMesh();
							mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
							mesh.getTexCoords().addAll(0, 0);
							mesh.getPoints().addAll(fromX, fromY, 0, fromX, fromY, 200, toX, toY, 0, toX, toY, 200);
							mesh.getFaces().addAll(0, 0, 1, 0, 2, 0,
									1, 0, 2, 0, 3, 0);
							
							var line = new MeshView(mesh);
							line.setCullFace(CullFace.NONE);
							
							line.setMaterial(new PhongMaterial(Color.hsb(hash*20, 1, 1)));
							viewportGroup.getChildren().add(line);

							var t = new Tooltip("hash="+Hash.getBIN(hash));
							line.setOnMouseEntered(e -> t.show(line, e.getScreenX()+10, e.getScreenY()+10));
							line.setOnMouseExited(e -> t.hide());	
						}
					}
					break;
					/*
					 * 	12410300 ChunksEmpty
					 * 	47410380 ChunksZonesContainer
					 * 		4A410300 ChunksZones zones ? -> cops stuff maybe ->>> MINIMAP_ICON_HIDING_SPOT from there
					 * 	46410300 ChunksData1 positions -> NIS markers ->>> MINIMAP_ICON_PURSUIT_BREAKER from there
					 * 	50410380 container
					 * 		51410300 counts (400, 655) then list of chunk ids in int16 -> 1, 2, ..., 1001, 1002, ..., 2001, ..., 5001 (length 655*int16), then 0s (881*int16 but Z0 + 792*(Y+X+W+U))...
					 * 		52410300 ChunksZones2 MAY CONTAIN ELEVATION DATA HASH
					 * 		56410300 elevation data (hashes only 2 values BE6566DE and 927B806A (file hash))
					 * 		55410300 fixups or smth ??????? LS26/LS18/LS76/LS30 4*76-byte
					 * 				11 11 LSxx 3x0 1-int16 id-int16 7x0 0-int16 numIDs-int16 IDs-int16 list -> preculler something but doesn't match preculler data !
					 * 		53410300 ChunksRelated chunks to load ? list 11 11 0 int16 id int16 num int16 total int16 num then list of int16s (some ids are 22000+ /24000+ -> YXWU ???)
					 * 		50420300 alignment- 3 3 2 0 -> Weatherman
					 * 	01020304 no fucking clue (integrity check of some sort ?) -> seemingly completely ignored in code
					 * 	padding
					 * 	00B00380 empty
					 * 	padding
					 * 	10410300 ChunksOffsets
					 * 	13410300 empty
					 * 	11410300 0 0
					 * 	padding
					 * 		@767504 dec
					 * 	08410300 ?????? -> chunkID int16 int16 ->>> ModelSlotPool smth
					 * 		@939460 dec
					 * 	09410300 ?????? int16 -> BARRIER SPLINE INFO : 11 11 BARRIER_SPLINE_1004 int16 listlength(int16) type(int32) list of int16 -> ids still unknown
					 * 							first entry 13371 ids type 0 up to about 28600
					 * 	padding
					 * 		@1027552 dec
					 * 	0B410380 ?????? container 
					 * 	list of 0C410300 ??????
					 * 	padding
					 * 	00B80300 ?????? PRAC vertex data
					 * 	padding
					 * 	00B90380 ?????? event sequencer/smackable/something ? plain text XOs_...
					 * 	padding
					 * 	0A40A380 empty
					 * 		@2508448 dec
					 * 	4D410300 hash float2 float2 0 -> BARRIER SPLINES segments point and next point; 0 = sorting
					 * 	EOF
					 * 
					 */
					
				case ChunksOffsets:
					in.position(in.position()-4);
					offsets = new StreamChunksOffsets(in);
					for (var c : offsets.chunkInfos) {
						var g = new Group();
						g.setDepthTest(DepthTest.ENABLE);
						chunksGroups.put(c, g);
						meshesGroup.getChildren().add(g);
					}
					
					var sphereHighlight = new PhongMaterial(Color.RED);
					var sphereDefault = new PhongMaterial(Color.WHEAT);
					var sphereNoRel = new PhongMaterial(Color.WHITE);
										
					if (chunkoffsets) for (var c : offsets.chunkInfos) 
						//if (c.radius != 0 //&& c.ID % 1000 < 400
							//) 
						{
						var _hasRelations = false;
						for (var rel : chunksRelated) if (rel.chunkID == c.ID) {
							_hasRelations = true;
						}
						final var hasRelations = _hasRelations;
//						if (!hasRelations) continue;
						
						if (c.radius != 0) {
//							var s = new Sphere(c.radius);
//							s.setMaterial(new PhongMaterial(Color.color(1, 1, 1, 0.5)));
							var s = new Sphere(20);
							var newSphereX = c.centerX;
							findPlace: while (true) {
								for (var o : viewportGroup.getChildren()) if (o.getTranslateX() == newSphereX) {
									newSphereX += 10;
									continue findPlace;
								}
								break;
							}
							s.setTranslateX(newSphereX);
							s.setTranslateY(c.centerY);
							s.setOnMouseClicked(e -> viewport.moveCamera(c.centerX,  c.centerY, 0));
							s.setMaterial(hasRelations ? sphereDefault : sphereNoRel);
							viewportGroup.getChildren().add(s);
							chunksSpheres.put(c, s);
							
							var t = new Tooltip("Chunk="+c.name+
									"\nID="+c.ID+"/"+(char)('A'+(c.ID/1000))+""+c.ID%1000+
									"\nradius="+c.radius+
									"\nchecksum="+String.format("0x%08X", c.checksum)+
									"\nrender priority="+c.index);
							s.setOnMouseEntered(e -> t.show(s, e.getScreenX()+10, e.getScreenY()+10));
							s.setOnMouseExited(e -> t.hide());

							s.setOnMouseClicked(e -> {
								displayFromChunksOffsets(STREAM_PATH, chunksRelated, chunksSpheres, offsets, sphereHighlight,
										sphereDefault, sphereNoRel, meshesGroup, c, hasRelations);
								System.gc();
							});

						}
						
					}
					break;
				default:
				}
				in.position(blockStart+blockLength);
			} //file read loop
			
			
//			if (offsets != null) for (var rel : chunksRelated) {
//				ChunkInfo chunk = null;
//				for (var c : offsets.chunkInfos) {
//					if (c.ID == rel.chunkID) {
//						chunk = c;
//						break;
//					}
//				}
//				if (chunk != null) {
//					System.out.println("Preculler data available for chunk "+chunk.name);
//				} else {
//					System.out.println("Preculler data available for unknown chunk ID "+rel.chunkID);
//				}
//			}
			
			
		} catch (Exception e) {e.printStackTrace();}
				
		viewport.setAxes(Rotate.X_AXIS, Rotate.Z_AXIS);
//		viewport.setAngles(0, 0);
//		viewport.setDistance(15000);
		viewport.camera.setNearClip(1);
		
		var streamer = Executors.newSingleThreadExecutor();
		var ongoing = new AtomicInteger();
		var curID = new AtomicInteger(0);
		
		if (cameraBasedStreaming) viewport.cameraPivotPosition.setOnTransformChanged(evt -> {
			var x = viewport.cameraPivotPosition.getX();
			var y = viewport.cameraPivotPosition.getY();
			var z = viewport.cameraPivotPosition.getZ();
			
			if (currentChunk != null && !(new Point(x, y).isInside(currentChunk.pointsX, currentChunk.pointsY))) currentChunk = null;
			if (currentChunk != null && currentChunk.elevationHash != 0) {
				float f = Float.POSITIVE_INFINITY;
				for (var r : elevationRules) if (r.hash == currentChunk.elevationHash && new Point(x,y).isInside(new float[] {r.x1,r.x2,r.x3}, new float[]{r.y1, r.y2, r.y3})) {
					f = r.z1;
				}
				int id = currentChunk.id;
				if (z > f){
					id = currentChunk.precullerOver;
				}
				if (id != curID.get()) {
					curID.set(id);
					int cur = ongoing.incrementAndGet();
					streamer.execute(() -> displayFromChunksBoundaries(curID.get(), STREAM_PATH, chunksRelated, offsets.chunkInfos, chunksGroups, ongoing, cur));
				}
			}
			if (currentChunk == null) {
				for (var b : boundaries) {
					if ((b.v1 == 0 || b.precullerOver != 0) && b.minX < x && x < b.maxX && b.minY < y && y < b.maxY) {
						if (new Point((float)x,(float)y).isInside(b.pointsX, b.pointsY)) {
							curID.set(b.id);
							currentChunk = b;
							if (b.elevationHash != 0) {
								float f = Float.POSITIVE_INFINITY;
								for (var r : elevationRules) if (r.hash == b.elevationHash && new Point(x,y).isInside(new float[] {r.x1,r.x2,r.x3}, new float[]{r.y1, r.y2, r.y3})) {
									f = r.z1;
								}
								if (z > f){
									curID.set(b.precullerOver);
								}	
							}
							int cur = ongoing.incrementAndGet();
							streamer.execute(() -> displayFromChunksBoundaries(curID.get(), STREAM_PATH, chunksRelated, offsets.chunkInfos, chunksGroups, ongoing, cur));
							break;
						}						
					}
				}
				if (currentChunk == null) {
					chunksGroups.forEach((c,g) -> {
						javafx.application.Platform.runLater(() -> g.getChildren().clear());
					});
				}
			}
		});
		
		
		launch(args);
		streamer.shutdownNow();

	}
	static class Point{
		float x;
		float y;
		Point(float x, float y){
			this.x = x;
			this.y = y;
		}
		Point(double x, double y){
			this.x = (float)x;
			this.y = (float)y;
		}
		
		public static boolean onSegment(Point p, Point q, Point r)
	    {
	        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
	                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
	            return true;
	        return false;
	    }
	 
	    public static int orientation(Point p, Point q, Point r)
	    {
	        float val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
	 
	        if (val == 0)
	            return 0;
	        return (val > 0) ? 1 : 2;
	    }
	 
	    public static boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
	    {
	 
	        var o1 = orientation(p1, q1, p2);
	        var o2 = orientation(p1, q1, q2);
	        var o3 = orientation(p2, q2, p1);
	        var o4 = orientation(p2, q2, q1);
	 
	        if (o1 != o2 && o3 != o4)
	            return true;
	 
	        if (o1 == 0 && onSegment(p1, p2, q1))
	            return true;
	 
	        if (o2 == 0 && onSegment(p1, q2, q1))
	            return true;
	 
	        if (o3 == 0 && onSegment(p2, p1, q2))
	            return true;
	 
	        if (o4 == 0 && onSegment(p2, q1, q2))
	            return true;
	 
	        return false;
	    }
	 
	    public static boolean isInside(Point polygon[], int n, Point p)
	    {
	        int INF = 10000;
	        if (n < 3)
	            return false;
	 
	        Point extreme = new Point(INF, p.y);
	 
	        int count = 0, i = 0;
	        do
	        {
	            int next = (i + 1) % n;
	            if (doIntersect(polygon[i], polygon[next], p, extreme))
	            {
	                if (orientation(polygon[i], p, polygon[next]) == 0)
	                    return onSegment(polygon[i], p, polygon[next]);
	 
	                count++;
	            }
	            i = next;
	        } while (i != 0);
	 
	        return (count & 1) == 1 ? true : false;
	    }
	    public boolean isInside(Point[] polygon) {
	    	return isInside(polygon, polygon.length, this); 
	    }
	    public boolean isInside(float[] ptX, float[] ptY) {
			Point[] poly = new Point[ptX.length];
			for (int i=0;i<ptX.length;i++) poly[i] = new Point(ptX[i], ptY[i]);
	    	var b = isInside(poly, ptX.length, this);
	    	poly = null;
	    	return b;
	    }
	}
	
	static final int STREAMER_LAZINESS = 0;
	//                              hash          image         use count
	public static ConcurrentHashMap<Integer, ImageTextureHolder> imageTextures = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Material, PhongMaterial> materials = new ConcurrentHashMap<>();

	private static boolean displayFromChunksBoundaries(
			int precullerID, String streamPath, ArrayList<ChunksRelated> chunksRelated, ArrayList<ChunkInfo> chunkInfos, HashMap<ChunkInfo,Group> chunksGroups
			//HashMap<ChunkInfo, Sphere> chunksSpheres, 
//			PhongMaterial sphereHighlight, PhongMaterial sphereDefault, PhongMaterial sphereNoRel, 
			//ChunkInfo c, final boolean hasRelations,
			,AtomicInteger phaser, int phase
			) {


		imageTextures.forEach((hash, imgtex) -> {
			if (imgtex.useCount < 10) imageTextures.remove(hash);
		});
		System.gc();
		
//		System.out.println("Phase "+phaser.get());
		if (phaser.get() != phase) {
//			System.out.println("SKIPPING EARLY");
			return false;
		}		
				
		for (var rel : chunksRelated) {
//									System.out.println(rel.chunkID);
			if ((rel.chunkID == precullerID)) {
				
				var models = new ConcurrentHashMap<Integer,Part>();
				var textures = new ConcurrentHashMap<Integer, NFSTexture>();
//				var materials = new ConcurrentHashMap<Material, PhongMaterial>();
//				var phaser = new Phaser(1);

				var pool = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()/2));
				var objects //= Collections.synchronizedList(new ArrayList<ChunkObject>());
							= new ConcurrentHashMap<ChunkInfo,ArrayList<ChunkObject>>();
//				var unload = new ArrayList<ChunkInfo>();
				
				var clearChunks = new ArrayList<Group>();
				
				var readLater = new ArrayList<Runnable>();

				
				
				System.out.println("Now in section "+(char)('A'+(precullerID/1000))+""+precullerID%1000);
//				System.out.println("Relations for chunk "+precullerID);
				for (var chunk2 : chunkInfos) {
					var found = false;
					for (int i=0; i< rel.relatedChunks.length; i++) {
						if (chunk2.ID == rel.relatedChunks[i]) {
							found = true;
							break;
						}
					}
//											if (chunk2.ID%1000 > 400) found = false;
//					if (chunk2.ID%1000 == 0) found = true; //force load Z0 25000 and Y0 24000
					if (found || chunk2.ID%1000 == 0) {
						if (phaser.get() != phase) break;
//						System.out.println("- "+chunk2.name);
//						if (chunksSpheres.get(chunk2) != null) chunksSpheres.get(chunk2).setMaterial(sphereHighlight);
						if (chunksGroups.get(chunk2) != null) {//&& chunksGroups.get(chunk2).getChildren().size() == 0
//							clearChunks.add(chunksGroups.get(chunk2));
							Runnable r = () -> {
								if (phaser.get() != phase) return;
//								objects.put(chunk2, readChunk(streamPath, chunk2, models, textures));
								var nodes = new ArrayList<Node>();
								for (var o : readChunk(streamPath, chunk2, models, textures)) nodes.addAll(renderObject(models, materials, o));
								javafx.application.Platform.runLater(() -> {
									chunksGroups.get(chunk2).getChildren().clear();
									chunksGroups.get(chunk2).getChildren().addAll(nodes);
								});
								updateTextures(textures, materials, phaser, phase);
								try {
									Thread.sleep(STREAMER_LAZINESS);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							};
							if (chunksGroups.get(chunk2).getChildren().size() == 0 || chunk2.ID%1000 == 0) { //TODO build permanent objects hash map for chunks that arent reloaded
								//priority
								pool.execute(r);
							} else {
								readLater.add(r);
							}
						}
					} else {
						if (chunksGroups.get(chunk2) != null) {
							clearChunks.add(chunksGroups.get(chunk2));
//							if (chunksGroups.get(chunk2).getChildren().size() != 0) unload.add(chunk2);
						}
					}
				}

				javafx.application.Platform.runLater(() -> {
					for (var g : clearChunks) {
						g.getChildren().clear();
					}
				});
				
				
				pool.shutdown();
				try {
					pool.awaitTermination(5, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pool = null;
				
//				var pool2 = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()/2));
//				objects.forEach((chunk, obj) -> {
//					pool2.execute(() -> {
//						var nodes = new ArrayList<Node>();
//						for (var o : obj) nodes.addAll(renderObject(models, materials, o));
//						javafx.application.Platform.runLater(() -> {
//							chunksGroups.get(chunk).getChildren().clear();
//							chunksGroups.get(chunk).getChildren().addAll(nodes);
//						});
//					});
//				});
//				for (var u : unload) {
//					pool.execute(() -> {
//						unloadChunkTextures(streamPath, u);
//						try {
//							Thread.sleep(STREAMER_LAZINESS);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					});
//				}
//				pool2.shutdown();
//				try {
//					pool2.awaitTermination(60, TimeUnit.SECONDS);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				phaser.arriveAndAwaitAdvance();
				
//				updateTextures(textures, materials, phaser, phase);

				if (phaser.get() != phase) {
//					System.out.println("SKIPPING");
					return false;
				}
				
				objects.clear();
				var pool3 = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()/2));
				for (var r : readLater) pool3.execute(r);
				objects.forEach((chunk, obj) -> {
					pool3.execute(() -> {
						var nodes = new ArrayList<Node>();
						for (var o : obj) nodes.addAll(renderObject(models, materials, o));
						javafx.application.Platform.runLater(() -> {
							chunksGroups.get(chunk).getChildren().clear();
							chunksGroups.get(chunk).getChildren().addAll(nodes);
						});
						updateTextures(textures, materials, phaser, phase);
					});
				});
				pool3.shutdown();
				try {
					pool3.awaitTermination(5, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

//				updateTextures(textures, materials, phaser, phase);
				
				models.clear();
				textures.clear();
				materials.clear();
				imageTextures.clear();
				System.gc();
				
				return true;
			}
		}

		return false;
	}
	
		
	//probably causes a memory leak
	private static void updateTextures(ConcurrentHashMap<Integer, NFSTexture> textures,
			ConcurrentHashMap<Material, PhongMaterial> materials, AtomicInteger phaser, int phase) {
		try {
			materials.forEach((mat, matVis) -> {
				if (matVis.getDiffuseMap() == null)
			try {
//				if (phaser.get() != phase) {
//					System.out.println("SKIPPING TEXTURE");
//					return;
//				}
				
				//diffuse map
				var has_d = mat.textureUsages.contains(TextureUsage.DIFFUSE);
				var has_o = mat.textureUsages.contains(TextureUsage.OPACITY);
				var has_re = mat.textureUsages.contains(TextureUsage.ROADELEMENTS);

				//selfillumination map
				var has_i = mat.textureUsages.contains(TextureUsage.ILLUMINATE);

				//specular map
				var has_s = mat.textureUsages.contains(TextureUsage.SPECULAR);
				
				//normal map
				var has_n = mat.textureUsages.contains(TextureUsage.NORMAL);
				var has_nm = mat.textureUsages.contains(TextureUsage.NORMALMAP);
				var has_en = mat.textureUsages.contains(TextureUsage.ELEMENTSNORMAL);
				
				//mixed
				var has_osr = mat.textureUsages.contains(TextureUsage.OPACITY_SPEC_REFL);
				var has_bsr = mat.textureUsages.contains(TextureUsage.BLEND_SPEC_REFL);

				//blend stuff
				var has_b = mat.textureUsages.contains(TextureUsage.BLEND);
				var has_d2 = mat.textureUsages.contains(TextureUsage.DIFFUSE2);
				var has_s2 = mat.textureUsages.contains(TextureUsage.SPECULAR2);
								
				var diffuseMap = (has_d ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
						+ (has_o ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)) : 0)
						+ (has_osr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //opacity component
						+ (has_re ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)) : 0) //never with diffuse
						+ (has_b && has_d2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
						+ (has_b && has_d2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)) : 0);
				
				var selfIlluminationMap = (has_d && (has_i || mat.shaderUsage.key == Hash.findVLT("ar_constant")) ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
						+ (has_i ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)) : 0);
				
				var specularMap = (has_s ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)) : 0)
						+ (has_osr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //specular component
						+ (has_bsr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)) : 0) //specular component
						+ (has_b && has_s2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
						+ (has_b && has_s2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)) : 0);
				
				var normalMap = (has_n ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)) : 0)
						+ (has_nm ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)) : 0)
						+ (has_en ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)) : 0);
				
				if (diffuseMap != 0) {
					if (imageTextures.get(diffuseMap) == null) {
						var imgtex = new ImageTextureHolder();
						imageTextures.put(diffuseMap, imgtex);
						//make diffuse map

						NFSTexture blend = null;
						if (has_b) {
							blend = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)));
							if (blend == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)))+" not found in loaded chunks!");
						}

						if (has_d) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)))+" not found in loaded chunks!");
							NFSTexture opacity = null;
							NFSTexture illum = null;
							NFSTexture opacity_spec_refl = null;
							NFSTexture diffuse2 = null;
							if (has_o) {
								opacity = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)));
								if (opacity == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)))+" not found in loaded chunks!");
							}
							if (has_i) {
								illum = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)));
								if (illum == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)))+" not found in loaded chunks!");
							}
							if (has_osr) {
								opacity_spec_refl = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)));
								if (opacity_spec_refl == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)))+" not found in loaded chunks!");
							}

							if (has_d2) {
								diffuse2 = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)));
								if (diffuse2 == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)))+" not found in loaded chunks!");
							}
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								if (mat.isOpaque()) {
									r.ignoreAlpha = true;										
								}
								WritableImage img = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();

								if (blend != null && diffuse2 != null) {
									//TODO this actually uses the second uv map, LIGHTMAP third uv map
									var r_b = new DDSImageReader(null);
									var iis_b= new MemoryCacheImageInputStream(new ByteArrayInputStream(blend.DDSImage));
									r_b.setInput(iis_b);

									var r_d2 = new DDSImageReader(null);
									var iis_d2= new MemoryCacheImageInputStream(new ByteArrayInputStream(diffuse2.DDSImage));
									r_d2.setInput(iis_d2);
									
									WritableImage img_b = SwingFXUtils.toFXImage(r_b.read(0), null);
									WritableImage img_d2 = SwingFXUtils.toFXImage(r_d2.read(0), null);
									
									applyBlend(img, img_b, img_d2);

									iis_b.close();
									iis_d2.close();
									img_b = null;
									img_d2 = null;
								} 
								
								if (opacity != null) {
									var r_o = new DDSImageReader(null);
									var iis_o= new MemoryCacheImageInputStream(new ByteArrayInputStream(opacity.DDSImage));
									r_o.setInput(iis_o);

									WritableImage img_o = SwingFXUtils.toFXImage(r_o.read(0), null);
									
									applyOpacity(img, img_o);
									img_o = null;

									iis_o.close();
								} else if (opacity_spec_refl != null){
									//red = opacity
									//green = specular
									//blue = reflection (unsupported by the viewer)
									var r_osr = new DDSImageReader(null);
									var iis_osr = new MemoryCacheImageInputStream(new ByteArrayInputStream(opacity_spec_refl.DDSImage));
									r_osr.setInput(iis_osr);
									WritableImage img_osr = SwingFXUtils.toFXImage(r_osr.read(0), null);

									applyOpacity(img, img_osr);
									
									for (var x=0; x<img_osr.getWidth(); x++) {
										for (var y=0; y<img_osr.getHeight(); y++) {
											img_osr.getPixelWriter().setArgb(x, y, 
													0xff000000 | //ignore alpha
													(img_osr.getPixelReader().getArgb(x, y)) & 0x0000ff00 | 
													(img_osr.getPixelReader().getArgb(x, y) << 8) & 0x00ff0000 |
													(img_osr.getPixelReader().getArgb(x, y) >>> 8) & 0x000000ff); 
										}
									}

									var imgtex_s = new ImageTextureHolder();
									imgtex_s.image = img_osr;
									imageTextures.put(specularMap, imgtex_s);
									//matVis.setSpecularMap(img_osr);
									
									iis_osr.close();
								} 
								
								imgtex.image = img;
								//matVis.setDiffuseMap(img);
							}
						}
						if (mat.textureUsages.contains(TextureUsage.ROADELEMENTS)) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								if (mat.isOpaque()) {
									r.ignoreAlpha = true;										
								}
								imgtex.image = SwingFXUtils.toFXImage(r.read(0), null);
								
								//matVis.setDiffuseMap(SwingFXUtils.toFXImage(r.read(0), null));
								iis.close();
							}
						}
					}
					
					//apply diffuse map
					matVis.setDiffuseMap(imageTextures.get(diffuseMap).image);
					if (matVis.getDiffuseMap() == null) matVis.setDiffuseColor(Color.rgb(255, 0, 255));
					else matVis.setDiffuseColor(Color.WHITE);
					imageTextures.get(diffuseMap).useCount++;
				}
				
				if (selfIlluminationMap != 0) {
					if (imageTextures.get(selfIlluminationMap) == null) {
						var imgtex = new ImageTextureHolder();
						imageTextures.put(selfIlluminationMap, imgtex);
						//make si map
						if (has_d) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)))+" not found in loaded chunks!");
							NFSTexture illum = null;

							if (has_i) {
								illum = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)));
								if (illum == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)))+" not found in loaded chunks!");
							}
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								if (mat.isOpaque()) {
									r.ignoreAlpha = true;										
								}
								WritableImage img = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();

								if (illum != null) {
									r = new DDSImageReader(null);
									iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
									r.setInput(iis);
									var r_i = new DDSImageReader(null);
									var iis_i = new MemoryCacheImageInputStream(new ByteArrayInputStream(illum.DDSImage));
									r_i.setInput(iis_i);
		
									WritableImage img_i = SwingFXUtils.toFXImage(r_i.read(0), null);
									
									applyOpacity(img, img_i);
		
									//matVis.setSelfIlluminationMap(img);
									
									iis_i.close();
									iis.close();
								}

								imgtex.image = img;
							}
						}
					}
					
					//apply si map
					if (imageTextures.get(selfIlluminationMap) != null) matVis.setSelfIlluminationMap(imageTextures.get(selfIlluminationMap).image);
					imageTextures.get(selfIlluminationMap).useCount++;
				}
				
				if (specularMap != 0) {
					if (imageTextures.get(specularMap) == null) {
						var imgtex = new ImageTextureHolder();
						imageTextures.put(specularMap, imgtex);
						//make specular map
						NFSTexture blend = null;
						if (has_b) {
							blend = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)));
							if (blend == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)))+" not found in loaded chunks!");
						}

						if (mat.textureUsages.contains(TextureUsage.SPECULAR)) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								WritableImage img = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();
								
								if (blend != null && mat.textureUsages.contains(TextureUsage.SPECULAR2)) {
									var specular2 = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)));
									if (specular2 == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)))+" not found in loaded chunks!");
									else {
										var r_b = new DDSImageReader(null);
										var iis_b= new MemoryCacheImageInputStream(new ByteArrayInputStream(blend.DDSImage));
										r_b.setInput(iis_b);
										
										var r_d2 = new DDSImageReader(null);
										var iis_d2= new MemoryCacheImageInputStream(new ByteArrayInputStream(specular2.DDSImage));
										r_d2.setInput(iis_d2);
										
										WritableImage img_b = SwingFXUtils.toFXImage(r_b.read(0), null);
										WritableImage img_d2 = SwingFXUtils.toFXImage(r_d2.read(0), null);
										
										applyBlend(img, img_b, img_d2);

										iis_b.close();
										iis_d2.close();
										img_b = null;
										img_d2 = null;
									}
								} 
								//matVis.setSpecularMap(img);
								imgtex.image = img;

							}
						}
						if (mat.textureUsages.contains(TextureUsage.BLEND_SPEC_REFL)) {
							//red = blend (unsupported)
							//green = specular
							//blue = reflection (unsupported by the viewer)
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								
								WritableImage img_bsr = SwingFXUtils.toFXImage(r.read(0), null);
																				
								for (var x=0; x<img_bsr.getWidth(); x++) {
									for (var y=0; y<img_bsr.getHeight(); y++) {
										img_bsr.getPixelWriter().setArgb(x, y, 
												0xff000000 | //ignore alpha
												(img_bsr.getPixelReader().getArgb(x, y)) & 0x0000ff00 | 
												(img_bsr.getPixelReader().getArgb(x, y) << 8) & 0x00ff0000 |
												(img_bsr.getPixelReader().getArgb(x, y) >>> 8) & 0x000000ff); 
									}
								}

								//matVis.setSpecularMap(img_osr);
								imgtex.image = img_bsr;
								iis.close();
							}
						}

					}
					//apply specular map
					if (imageTextures.get(specularMap) != null) matVis.setSpecularMap(imageTextures.get(specularMap).image);
					imageTextures.get(specularMap).useCount++;
				}
				
				if (normalMap != 0) {
					if (imageTextures.get(normalMap) == null) {
						var imgtex = new ImageTextureHolder();
						imageTextures.put(normalMap, imgtex);
						//make normal map
						if (mat.textureUsages.contains(TextureUsage.NORMAL)) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								//matVis.setBumpMap(SwingFXUtils.toFXImage(r.read(0), null));
								imgtex.image = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();
							}
						}
						if (mat.textureUsages.contains(TextureUsage.NORMALMAP)) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								//matVis.setBumpMap(SwingFXUtils.toFXImage(r.read(0), null));
								imgtex.image = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();
							}
						}
						if (mat.textureUsages.contains(TextureUsage.ELEMENTSNORMAL)) {
							var nfstex = textures.get(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)));
							if (nfstex == null) System.out.println("Texture "+Hash.getBIN(mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)))+" not found in loaded chunks!");
							if (nfstex != null) {
								var r = new DDSImageReader(null);
								var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(nfstex.DDSImage));
								r.setInput(iis);
								//matVis.setBumpMap(SwingFXUtils.toFXImage(r.read(0), null));
								imgtex.image = SwingFXUtils.toFXImage(r.read(0), null);
								iis.close();
							}
						}
					}
					//apply normal map
					if (imageTextures.get(normalMap) != null) matVis.setBumpMap(imageTextures.get(normalMap).image);
					imageTextures.get(normalMap).useCount++;
				}
				
				
				
				
				
				}catch(Exception ex2) {}
			});
//			imageTextures.forEach((hash, imgtex) -> {
//				if (imgtex.useCount == 0) imageTextures.remove(hash);
//			});
		} catch (Exception|Error ex) {
			ex.printStackTrace();
		}
	}
	
	private static void displayFromChunksOffsets(String streamPath, ArrayList<ChunksRelated> chunksRelated,
			HashMap<ChunkInfo, Sphere> chunksSpheres, StreamChunksOffsets offsets, PhongMaterial sphereHighlight,
			PhongMaterial sphereDefault, PhongMaterial sphereNoRel, Group meshesGroup, ChunkInfo c,
			final boolean hasRelations) {
		
		viewport.moveCamera(c.centerX, c.centerY, 0);
		meshesGroup.getChildren().clear();
		
		new Thread(() -> {
			System.gc();

			var models = new ConcurrentHashMap<Integer,Part>();
			var textures = new ConcurrentHashMap<Integer, NFSTexture>();
			var materials = new ConcurrentHashMap<Material, PhongMaterial>();
			var objects //= Collections.synchronizedList(new ArrayList<ChunkObject>());
						= new ConcurrentHashMap<ChunkInfo,ArrayList<ChunkObject>>();
			imageTextures.clear();
			ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()/2));

//			var phaser = new Phaser(1);
			if (!hasRelations) {
				objects.put(c, readChunk(streamPath, c, models, textures));
				objects.forEach((chunk, obj) -> {
					var nodes = new ArrayList<Node>();
					for (var o : obj) nodes.addAll(renderObject(models, materials, o));
					javafx.application.Platform.runLater(() -> {
						meshesGroup.getChildren().addAll(nodes);
					});
				});
				
			} else {
				for (var chunk2 : offsets.chunkInfos) {
					var _hasRel = false;
					for (var rel : chunksRelated) if (rel.chunkID == chunk2.ID) {
						_hasRel = true;
					}
					if (chunksSpheres.get(chunk2) != null) chunksSpheres.get(chunk2).setMaterial(_hasRel ? sphereDefault : sphereNoRel);
				}
				for (var rel : chunksRelated) {
//											System.out.println(rel.chunkID);
					if ((rel.chunkID == c.ID)// || (c.ID % 1000 > 400 && rel.chunkID == c.ID-400)
							) {
						System.out.println("Relations for chunk "+c.name);
						for (var chunk2 : offsets.chunkInfos) {
							var found = false;
							for (int i=0; i< rel.relatedChunks.length; i++) {
								if (chunk2.ID == rel.relatedChunks[i]) {
									found = true;
									break;
								}
							}
//													if (chunk2.ID%1000 > 400) found = false;
							if (chunk2.ID%1000 == 0) found = true; //force load Z0 25000 and Y0 24000
							if (found) {
								System.out.println("- "+chunk2.name);
								if (chunksSpheres.get(chunk2) != null) chunksSpheres.get(chunk2).setMaterial(sphereHighlight);
								pool.execute(() -> {
									objects.put(c, readChunk(streamPath, chunk2, models, textures));
								});
							}
						}
					}
				}
				pool.shutdown();
				try {
					pool.awaitTermination(10, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pool = null;
				var pool2 = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()/2));

				objects.forEach((chunk, obj) -> {
					pool2.execute(() -> {
						var nodes = new ArrayList<Node>();
						for (var o : obj) nodes.addAll(renderObject(models, materials, o));
						javafx.application.Platform.runLater(() -> {
							meshesGroup.getChildren().addAll(nodes);
						});
					});
				});
				
				pool2.shutdown();
				try {
					pool2.awaitTermination(10, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			phaser.arriveAndAwaitAdvance();
			updateTextures(textures, materials, new AtomicInteger(), 0);
			System.gc();

		}).start();
	}
	private static void applyOpacity(WritableImage diffuse, WritableImage opacity) {
		double widthRatio = opacity.getWidth()/diffuse.getWidth();
		double heightRatio = opacity.getHeight()/diffuse.getHeight();
		
		
		for (var x=0; x<diffuse.getWidth(); x++) {
			for (var y=0; y<diffuse.getHeight(); y++) {
				diffuse.getPixelWriter().setArgb(x, y, 
						diffuse.getPixelReader().getArgb(x, y) & 0x00ffffff | 
						(opacity.getPixelReader().getArgb((int)(x*widthRatio), (int)(y*heightRatio)) << 8) & 0xff000000);
			}
		}
	}
	private static void applyBlend(WritableImage diffuse, WritableImage blend, WritableImage diffuse2) {
		double widthRatioBlend = blend.getWidth()/diffuse.getWidth();
		double heightRatioBlend = blend.getHeight()/diffuse.getHeight();
		double widthRatioDiffuse2 = diffuse2.getWidth()/diffuse.getWidth();
		double heightRatioDiffuse2 = diffuse2.getHeight()/diffuse.getHeight();
		
		
		for (var x=0; x<diffuse.getWidth(); x++) {
			for (var y=0; y<diffuse.getHeight(); y++) {
//				diffuse.getPixelWriter().setArgb(x, y, 
//						(~blend.getPixelReader().getArgb((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)) &
//						diffuse.getPixelReader().getArgb(x, y)) |
//						(blend.getPixelReader().getArgb((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)) &
//						diffuse2.getPixelReader().getArgb((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)))
//						);
				diffuse.getPixelWriter().setColor(x, y, Color.color(
						(1-blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getRed()) * diffuse.getPixelReader().getColor(x, y).getRed()
						+ blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getRed() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getRed(), 
						(1-blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getGreen()) * diffuse.getPixelReader().getColor(x, y).getGreen()
						+ blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getGreen() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getGreen(), 
						(1-blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getBlue()) * diffuse.getPixelReader().getColor(x, y).getBlue()
						+ blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getBlue() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getBlue(), 
						(1-blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getOpacity()) * diffuse.getPixelReader().getColor(x, y).getOpacity()
						+  blend.getPixelReader().getColor((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)).getOpacity() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getOpacity()
						));
			}
		}
	}
	private static ArrayList<ChunkObject> readChunk(String streamPath, ChunkInfo chunk,
			ConcurrentHashMap<Integer, Part> models, ConcurrentHashMap<Integer, NFSTexture> textures//, Phaser phaser
			) {
		var objects = new ArrayList<ChunkObject>();
		try {
//			phaser.register();
			var stream = new RandomAccessFile(new File(streamPath), "r");
			stream.seek(chunk.offset);
			
			var chunkData = ByteBuffer.allocate(chunk.size);
			chunkData.order(ByteOrder.LITTLE_ENDIAN);
			stream.read(chunkData.array());
			stream.close();

			var positions = new ArrayList<ObjectPosition>();
																
			int length;
			int start;
			while (chunkData.position() < chunkData.limit()) {
				BlockType block = BlockType.get(chunkData.getInt());
				length = chunkData.getInt();
				start = chunkData.position();

				switch(block) {
				case Geometry:
					chunkData.position(chunkData.position()-4);
					new Geometry(chunkData).parts.forEach(p -> models.put(p.header.binKey, p));
					break;
					
				case TPK:
					chunkData.position(chunkData.position()-4);
					new TPK(chunkData).textures.forEach(tex -> textures.put(tex.binKey, tex));
					break;
					
				case ChunkObjects:
					length = 0; //skip block size to enter the container
					break;
					
				case ChunkObjects_Positions:
					while (chunkData.getInt() == 0x11111111) {}
					chunkData.position(chunkData.position()-4);
					positions.clear();
					while (chunkData.position() < start + length) {
						positions.add(new ObjectPosition(chunkData));
					}
					break;
					
				case ChunkObjects_Objects:
					while (chunkData.getInt() == 0x11111111) {}
					chunkData.position(chunkData.position()-4);
					int index=0;
					while (chunkData.position() < start + length) {
						var o = new ChunkObject(chunkData);
						for (var p : positions) if (p.objectIndex == index) o.positions.add(p);
						o.chunk = chunk;
						objects.add(o);
						//rendering used to be here
						index++;
					}
					break;
					
				default:
				}
				chunkData.position(start+length);
			}
			chunkData = null;
			positions = null;
//			phaser.arriveAndDeregister();
		} catch (Exception e1) {
			e1.printStackTrace();
//			phaser.arriveAndDeregister();
		}
		return objects;
	}
	private static ArrayList<Node> renderObject(ConcurrentHashMap<Integer, Part> models,
			ConcurrentHashMap<Material, PhongMaterial> materials, ChunkObject o) {
		var nodes = new ArrayList<Node>();
		try {
			if (o.lodA == 0xEC6185C8 || o.lodA == 0xED85BA82) return nodes; //skip skybox
			var model = models.get(o.lodA);
//			var model = models.get(o.lodZ);
			var chunk = o.chunk;
			
			
			if (model == null) {
				model = models.get(o.lodB);
				if (model == null) {
					model = models.get(o.lodC);
					if (model == null) {
						model = models.get(o.lodZ);
						if (model == null) {
							System.out.println(chunk.name +": Object "+o.name+" not found in loaded chunks!");
							for (var p : o.positions) {
								var b = new Box(p.maxX-p.minX,p.maxY-p.minY,p.maxZ-p.minZ);
								
//								var m = p.matrix;
//								var transform = Transform.affine(m[0][0], m[0][1], m[0][2], p.x,
//										m[1][0], m[1][1], m[1][2], p.y,
//										m[2][0], m[2][1], m[2][2], p.z);
//								b.getTransforms().add(transform);
								b.setTranslateX((p.minX + p.maxX)/2);
								b.setTranslateY((p.minY + p.maxY)/2);
								b.setTranslateZ((p.minZ + p.maxZ)/2);
							    
								b.setMaterial(UNKNOWN_OBJECT);
								b.setDepthTest(DepthTest.ENABLE);
								b.setViewOrder(-10);
								
							    var t2 = new Tooltip("Model="+Hash.getBIN(o.lodA)+" ("+o.name+"/"+String.format("0x%08X", o.lodA)+")"+
							    		"\nChunk="+o.chunk.name+
							    		"\nGroupID="+p.groupID+
							    		"\nInstanceID="+p.instanceID+
//							    		"\nPositionHash="+Hash.getBIN(p.hash)+
							    		"\nObjectID="+p.objectID+
							    		"\nv1="+p.v1+
							    		"\nv2="+p.v2+
							    		"\nv3="+p.v3+
							    		"\nv4="+p.v4+
							    		"\nv5="+p.v5+
							    		"\nv6="+p.v6+
							    		"\nv7="+p.v7+
							    		"\nv8="+p.v8+
										"\nTx="+p.x+", Ty="+p.y+", Tz="+p.z);
								b.setOnMouseEntered(evt -> t2.show(b, evt.getScreenX()+10, evt.getScreenY()+10));
								b.setOnMouseExited(evt -> t2.hide());

								nodes.add(b);
							}
						}
					}
				}
			}
			
			if (model != null && model.mesh != null && model.mesh.materials != null) for (var mat : model.mesh.materials.materials){
				TriangleMesh matMesh = new TriangleMesh();
				
				if (mat.verticesBlock.vertexFormat.hasNormals()) matMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
				else matMesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
				
				if (mat.verticesBlock != null) {
					for (var v : mat.verticesBlock.vertices) {
						matMesh.getPoints().addAll(v.pos[X], v.pos[Y], v.pos[Z]);
					}
					if (mat.verticesBlock.vertexFormat.hasNormals()) for (var v : mat.verticesBlock.vertices) {
						matMesh.getNormals().addAll(v.norm[X], v.norm[Y], v.norm[Z]);
					}
					if (mat.verticesBlock.vertexFormat.getNumTexChannels() > 0) for (var v : mat.verticesBlock.vertices) {
						matMesh.getTexCoords().addAll(v.tex[0][U], 1-v.tex[0][V]);
					} else for (@SuppressWarnings("unused") var v : mat.verticesBlock.vertices) {
						matMesh.getTexCoords().addAll(0, 0);
					}
//						if (mat.verticesBlock.vertexFormat.hasColor()) for (var v : mat.verticesBlock.vertices) {
//							matMesh.getTexCoords().addAll(v.color[R], 0); //vertex colors
//						}
				}
				
				if (mat.verticesBlock.vertexFormat.hasNormals()) for (var tr : mat.triangles) {
					matMesh.getFaces().addAll(
							// points normals   texcoords
							tr.vert0, tr.vert0, tr.vert0, //v1
							tr.vert1, tr.vert1, tr.vert1, //v2
							tr.vert2, tr.vert2, tr.vert2);//v3
				} else for (var tr : mat.triangles) {
					matMesh.getFaces().addAll(
							// points texcoords
							tr.vert0, tr.vert0, //v1
							tr.vert1, tr.vert1, //v2
							tr.vert2, tr.vert2);//v3
				}
				
				
				for (var p : o.positions) {
					var mv = new MeshView(matMesh);
					
					mv.setMaterial(materials.get(mat));
//						mv.setMaterial(vertexColorMaterial);
					
					if (mv.getMaterial() == null) {
						var matVis = new PhongMaterial();
						mv.setMaterial(matVis);
						materials.put(mat, matVis);
					}
					mv.setDepthTest(DepthTest.ENABLE);
					if (mat.textureUsages.contains(TextureUsage.OPACITY) || mat.textureUsages.contains(TextureUsage.OPACITY_SPEC_REFL)) {
						mv.setViewOrder(-1);
						mv.setCullFace(CullFace.NONE);
					}

					var m = p.matrix;													
					var transform = Transform.affine(m[0][0], m[0][1], m[0][2], p.x,
							m[1][0], m[1][1], m[1][2], p.y,
							m[2][0], m[2][1], m[2][2], p.z);
					mv.getTransforms().add(transform);
				    
				    var t2 = new Tooltip("Model="+model.name+" ("+String.format("0x%08X", model.header.binKey)+")"+
				    		"\nChunk="+o.chunk.name+
				    		"\nMaterial="+mat.generateName()+
				    		"\nGroupID="+p.groupID+
				    		"\nInstanceID="+p.instanceID+
//				    		"\nPositionHash="+Hash.getBIN(p.hash)+
				    		"\nObjectID="+p.objectID+
				    		"\nv1="+p.v1+
				    		"\nv2="+p.v2+
				    		"\nv3="+p.v3+
				    		"\nv4="+p.v4+
				    		"\nv5="+p.v5+
				    		"\nv6="+p.v6+
				    		"\nv7="+p.v7+
				    		"\nv8="+p.v8+
							"\nTx="+p.x+", Ty="+p.y+", Tz="+p.z);
					mv.setOnMouseEntered(evt -> t2.show(mv, evt.getScreenX()+10, evt.getScreenY()+10));
					mv.setOnMouseExited(evt -> t2.hide());
//						mv.setOnMouseClicked(evt -> {
//							if (p.z != 0) {
//								viewport.moveCamera(p.x, p.y, p.z);
//							} else {
//								viewport.moveCamera((model.header.boundsXmin+model.header.boundsXmax)/2, 
//										(model.header.boundsYmin+model.header.boundsYmax)/2, 
//										(model.header.boundsZmin+model.header.boundsZmax)/2);																			
//							}
//						});

					nodes.add(mv);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
//			phaser.arriveAndDeregister();
		}
		return nodes;
	}
	private static void unloadChunkTextures(String streamPath, ChunkInfo chunk//, Phaser phaser
			) {
		try {
//			phaser.register();
			var stream = new RandomAccessFile(new File(streamPath), "r");
			stream.seek(chunk.offset);
			
			var chunkData = ByteBuffer.allocate(chunk.size);
			chunkData.order(ByteOrder.LITTLE_ENDIAN);
			stream.read(chunkData.array());
			stream.close();
			
			var positions = new ArrayList<ObjectPosition>();
			var materials = new ArrayList<Material>();
			var models = new HashMap<Integer, Part>();
			
			int length;
			int start;
			while (chunkData.position() < chunkData.limit()) {
				BlockType block = BlockType.get(chunkData.getInt());
				length = chunkData.getInt();
				start = chunkData.position();

				switch(block) {
				case Geometry:
					chunkData.position(chunkData.position()-4);
					new Geometry(chunkData).parts.forEach(p -> models.put(p.header.binKey, p));
					break;
					
				case ChunkObjects:
					length = 0; //skip block size to enter the container
					break;
					
				case ChunkObjects_Positions:
					while (chunkData.getInt() == 0x11111111) {}
					chunkData.position(chunkData.position()-4);
					positions.clear();
					while (chunkData.position() < start + length) {
						positions.add(new ObjectPosition(chunkData));
					}
					break;
					
				case ChunkObjects_Objects:
					while (chunkData.getInt() == 0x11111111) {}
					chunkData.position(chunkData.position()-4);
					int index=0;
					while (chunkData.position() < start + length) {
						var o = new ChunkObject(chunkData);
						
						for (var p : positions) if (p.objectIndex == index) {
							var model = models.get(o.lodA);
							
							if (model == null) {
								model = models.get(o.lodB);
								if (model == null) {
									model = models.get(o.lodC);
									if (model == null) {
										model = models.get(o.lodZ);
									}
								}
							}
							
							if (model != null && model.mesh != null && model.mesh.materials != null) for (var mat : model.mesh.materials.materials){
								if (!materials.contains(mat)) {
									materials.add(mat);
									
									//diffuse map
									var has_d = mat.textureUsages.contains(TextureUsage.DIFFUSE);
									var has_o = mat.textureUsages.contains(TextureUsage.OPACITY);
									var has_re = mat.textureUsages.contains(TextureUsage.ROADELEMENTS);

									//selfillumination map
									var has_i = mat.textureUsages.contains(TextureUsage.ILLUMINATE);

									//specular map
									var has_s = mat.textureUsages.contains(TextureUsage.SPECULAR);
									
									//normal map
									var has_n = mat.textureUsages.contains(TextureUsage.NORMAL);
									var has_nm = mat.textureUsages.contains(TextureUsage.NORMALMAP);
									var has_en = mat.textureUsages.contains(TextureUsage.ELEMENTSNORMAL);
									
									//mixed
									var has_osr = mat.textureUsages.contains(TextureUsage.OPACITY_SPEC_REFL);
									var has_bsr = mat.textureUsages.contains(TextureUsage.BLEND_SPEC_REFL);

									//blend stuff
									var has_b = mat.textureUsages.contains(TextureUsage.BLEND);
									var has_d2 = mat.textureUsages.contains(TextureUsage.DIFFUSE2);
									var has_s2 = mat.textureUsages.contains(TextureUsage.SPECULAR2);
													
									var diffuseMap = (has_d ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
											+ (has_o ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)) : 0)
											+ (has_osr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //opacity component
											+ (has_re ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)) : 0) //never with diffuse
											+ (has_b && has_d2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
											+ (has_b && has_d2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)) : 0);
									
									var selfIlluminationMap = (has_d && (has_i || mat.shaderUsage.key == Hash.findVLT("ar_constant")) ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
											+ (has_i ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)) : 0);
									
									var specularMap = (has_s ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)) : 0)
											+ (has_osr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //specular component
											+ (has_bsr ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)) : 0) //specular component
											+ (has_b && has_s2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
											+ (has_b && has_s2 ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)) : 0);
									
									var normalMap = (has_n ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)) : 0)
											+ (has_nm ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)) : 0)
											+ (has_en ? mat.TextureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)) : 0);

									if (diffuseMap != 0 && imageTextures.get(diffuseMap) != null) imageTextures.get(diffuseMap).useCount--;
									if (selfIlluminationMap != 0 && imageTextures.get(selfIlluminationMap) != null) imageTextures.get(selfIlluminationMap).useCount--;
									if (specularMap != 0 && imageTextures.get(specularMap) != null) imageTextures.get(specularMap).useCount--;
									if (normalMap != 0 && imageTextures.get(normalMap) != null) imageTextures.get(normalMap).useCount--;
								}
							}
						}
						index++;
					}
					break;
					
				default:
				}
				chunkData.position(start+length);
			}
			chunkData = null;
			positions = null;
//			phaser.arriveAndDeregister();
		} catch (Exception e1) {
			e1.printStackTrace();
//			phaser.arriveAndDeregister();
		}
	}
	
	static Group viewportGroup = new Group();
	static OrbitCameraViewport viewport = new OrbitCameraViewport(viewportGroup, 0, 0);
	static Spinner<Integer> filter = new Spinner<>(0, 1000, 0);
	
	static final boolean zones1_police = false;
	static final boolean data1_NISmarkers = false;
	static final boolean zones2_chunkBoundaries = true;
	static final boolean data2_meshes = false;
	static final boolean chunkoffsets = false;
	static final boolean barriersplines = false;
	static final boolean cameraBasedStreaming = true;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		var bp = new BorderPane();
		bp.setCenter(viewport);
		var hb = new HBox();
		var coordX = new Label();
		coordX.textProperty().bind(viewport.cameraPivotPosition.xProperty().asString("%.2f"));
		var coordY = new Label();
		coordY.textProperty().bind(viewport.cameraPivotPosition.yProperty().asString("%.2f"));
		var coordZ = new Label();
		coordZ.textProperty().bind(viewport.cameraPivotPosition.zProperty().asString("%.2f"));
		hb.getChildren().addAll(filter, new Separator(), coordX, new Separator(), coordY, new Separator(), coordZ);
		bp.setTop(hb);
		viewport.widthProperty().bind(bp.widthProperty());
		viewport.heightProperty().bind(bp.heightProperty());
		primaryStage.setScene(new Scene(bp, 1024, 600));
		primaryStage.show();
        primaryStage.getScene().getRoot().mouseTransparentProperty().bind(primaryStage.focusedProperty().not()); //fix tooltips
	}
}

class ChunksRelated{
	short chunkID;
	short[] relatedChunks;
	public ChunksRelated(ByteBuffer in) {
		in.getInt(); //11
		in.getInt(); //11
		in.getInt(); //0
		chunkID = in.getShort();
		in.getShort(); //dataCount
		var dataCapacity = in.getShort();
		var dataCount = in.getShort();
		relatedChunks = new short[dataCount];
		for (int i=0; i<dataCount; i++) {
			relatedChunks[i] = in.getShort(); //related chunk id
		}
		for (int i=dataCount; i<dataCapacity; i++) in.getShort(); //filler
		in.getInt();
//		System.out.println("ChunksRelated: "+chunkID+", num relations "+dataCount);
	}
}

class ChunkBoundary{
	int type1, type2, elevationHash, unk3;
	short id, precullerOver, unk2;
	byte v1, numPoints;
	float minX, maxX, minY, maxY, posX, posY;
	float[] pointsX, pointsY;
	public ChunkBoundary(ByteBuffer in) {
		type1 = in.getInt(); //11
		type2 = in.getInt(); //11
		id = in.getShort(); //id ?
		numPoints = in.get();
		v1 = in.get(); // 0=chunks, 1=panoramas
		precullerOver = in.getShort();
		if (precullerOver != 0) System.out.println("Chunk "+id+": id1="+precullerOver);
		unk2 = in.getShort();
		if (unk2 != 0) System.out.println("Chunk "+id+": unk2="+unk2);
		elevationHash = in.getInt();
		if (elevationHash != 0) System.out.println("Chunk "+id+": elevationHash="+String.format("0x%08X", elevationHash));
		unk3 = in.getInt();
		if (unk3 != 0) System.out.println("Chunk "+id+": unk3="+String.format("0x%08X", unk3));
		minX = in.getFloat();
		minY = in.getFloat();
		maxX = in.getFloat();
		maxY = in.getFloat();
		posX = in.getFloat();
		posY = in.getFloat();
		pointsX = new float[numPoints];
		pointsY = new float[numPoints];
		for (int i=0; i<numPoints; i++) {
			pointsX[i] = in.getFloat();
			pointsY[i] = in.getFloat();
		}
	}
}
class ChunkObject{
	ArrayList<ObjectPosition> positions = new ArrayList<>();
	String name;
	int lodA, lodB, lodC, lodZ;
	ChunkInfo chunk = null;
	ChunkObject(ByteBuffer in){
		var nameEnd = in.position()+24;
		name = Block.readString(in);
		in.position(nameEnd); //ignore name
		lodA = in.getInt();
		lodB = in.getInt();
		lodC = in.getInt();
		lodZ = in.getInt();
		//0
		//0
		//0
		//0
		//unk float
		//unk int, 0 or 8
		//0
		//0
		in.position(in.position()+32); //ignore rest
	}
}
class ObjectPosition{
	short objectIndex, objectID, v1, v2, v3, v4, v5, v6, v7, v8; 
	short groupID, instanceID;
	//int hash;
	float x, y, z;
	float minX, minY, minZ, maxX, maxY, maxZ;
	float[][] matrix;
	ObjectPosition(ByteBuffer in){
		minX = in.getFloat(); //bounds min
		minY = in.getFloat();
		minZ = in.getFloat();
//												in.getInt(); 
		v1 = in.getShort();
		objectID = in.getShort();

		maxX = in.getFloat(); //bounds max
		maxY = in.getFloat();
		maxZ = in.getFloat();
		v2 = in.getShort();
		objectIndex = in.getShort();
		
		x = in.getFloat();
		y = in.getFloat();
		z = in.getFloat();
//												System.out.println(x+", "+y+", "+z);
		instanceID = in.getShort();
		groupID = in.getShort();
//		hash = in.getInt();
		
		matrix = new float[3][3];
		matrix[0][0] = in.getFloat();
		matrix[1][0] = in.getFloat();
		matrix[2][0] = in.getFloat();
		v3 = in.getShort();
		v4 = in.getShort();
		
		matrix[0][1] = in.getFloat();
		matrix[1][1] = in.getFloat();
		matrix[2][1] = in.getFloat();
		v5 = in.getShort();
		v6 = in.getShort();
		
		matrix[0][2] = in.getFloat();
		matrix[1][2] = in.getFloat();
		matrix[2][2] = in.getFloat();
		v7 = in.getShort();
		v8 = in.getShort();
	}
}
class ImageTextureHolder{
	public WritableImage image = null;
	public int useCount = 0;
}
class ElevationRule{
	int hash;
	float x1, y1, z1, x2, y2, z2, x3, y3, z3;
	ElevationRule(ByteBuffer in){
		hash = in.getInt(); //hash
		in.getInt();
		in.getInt();
		in.getInt();
		x1 = in.getFloat();
		y1 = in.getFloat();
		z1 = in.getFloat();
		in.getInt();
		x2 = in.getFloat();
		y2 = in.getFloat();
		z2 = in.getFloat();
		in.getInt();
		x3 = in.getFloat();
		y3 = in.getFloat();
		z3 = in.getFloat();
		in.getInt();
	}
}