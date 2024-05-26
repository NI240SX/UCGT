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

import com.aspose.threed.*;

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
	
	public static void exportToFile(String geomFile, String exportFile, FileFormat ff) {
		try {
			long t = System.currentTimeMillis();
			
			var geom = Geometry.load(new File(geomFile));
			
			System.out.println("Geom read in "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();
			
			
			Scene scene = new Scene();
//			scene.getRootNode().createChildNode("aaa", null)
			for (var p : geom.parts) {
				Node partNode = new Node(p.kit+"_"+p.part+"_"+p.lod);
				scene.getRootNode().addChildNode(partNode);
				Mesh partMesh = new Mesh();
				VertexElementNormal elementNormal = (VertexElementNormal)partMesh.createElement(VertexElementType.NORMAL, MappingMode.CONTROL_POINT, ReferenceMode.DIRECT);
				VertexElementUV elementUV = partMesh.createElementUV(TextureMapping.DIFFUSE, MappingMode.CONTROL_POINT, ReferenceMode.DIRECT);
				VertexElementMaterial elementMaterial = (VertexElementMaterial) partMesh.createElement(VertexElementType.MATERIAL, MappingMode.POLYGON, ReferenceMode.INDEX);
				
				for (var m : p.mesh.materials.materials) {
					var meshLength = partMesh.getControlPoints().size();

					PhongMaterial mat = new PhongMaterial();
					Texture diffuse = new Texture();
					diffuse.setFileName(m.TextureHashes.get(0).label + ".dds");
					mat.setName(m.generateName());
					mat.setTexture(Material.MAP_DIFFUSE, diffuse);
					mat.setSpecularColor(new Vector3(1, 0, 0));
					mat.setShininess(30);
					partNode.getMaterials().add(mat);
					

					for (var v : m.verticesBlock.vertices) {
						partMesh.getControlPoints().add(new Vector4(v.posX, v.posY, v.posZ, 1.0));
						elementNormal.getData().add(new Vector4(v.normX, v.normY, v.normZ, v.normW));
						elementUV.getData().add(new Vector4(v.texU, v.texV, 0, 1.0));
					}
					
					for (var tr : m.triangles) {
						//meshLength should be equal to m.fromVertID but somehow isn't
						partMesh.createPolygon(meshLength+ tr.vert1,meshLength+ tr.vert2,meshLength+ tr.vert3);
						elementMaterial.getIndices().add(partNode.getMaterials().indexOf(mat));
					}					
				}
				partNode.addEntity(partMesh);
			}
			
			if (!exportFile.contains(ff.getExtension())) exportFile += ff.getExtension();
			
			scene.save(exportFile, ff);
			geom.writeConfig(new File(exportFile.replace(ff.getExtension(), "")+".ini"));
			
			System.out.println(ff.getExtension()+" saving time "+(System.currentTimeMillis()-t)+" ms.");
			t = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException {

//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN");
//		dumpPartsRecompiled("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN");

//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\LAM_GAL_SUP_08\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A");
//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\CARS\\MER_G63AMG\\GEOMETRY.BIN", 
//				"KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A KIT00_BASE_A");
//		PartVisualizer.setParts("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN", "KIT00_BASE_A"
//		+ "KIT00_BODY_A KIT00_BUMPER_FRONT_A KIT00_BUMPER_REAR_A KIT00_CHASSIS_A KIT00_FENDER_FRONT_LEFT_A KIT00_FENDER_FRONT_RIGHT_A "
//		+ " KIT00_DOOR_LEFT_A KIT00_DOOR_RIGHT_A KIT00_DOOR_REAR_LEFT_A KIT00_DOOR_REAR_RIGHT_A KIT00_HOOD_A KIT00_TRUNK_A"
//		+ " KIT00_SKIRT_LEFT_A KIT00_SKIRT_RIGHT_A KIT00_HEADLIGHT_LEFT_A KIT00_HEADLIGHT_RIGHT_A KIT00_BRAKELIGHT_LEFT_A KIT00_BRAKELIGHT_RIGHT_A"
//		+ " KIT00_MUFFLER_00_A KIT00_EXHAUST_TIPS_LEFT_00_A KIT00_EXHAUST_TIPS_RIGHT_00_A KIT00_EXHAUST_TIPS_CENTER_A"
//		+ " KIT00_SIDEMIRROR_LEFT_A KIT00_SIDEMIRROR_RIGHT_A KIT00_SPOILER_A"
//		);
//		PartVisualizer.run();

//		try {
//			Geometry.load(new File("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
//		writeConfig("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak");
//		writeConfig("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");
		
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\0 VANILLA 1.0.1.18 FILES BACKUP\\CARS\\NIS_GTR_R35_08\\GEOMETRY.BIN");
//		dumpPartsAsIs("C:\\jeux\\UCE 1.0.1.18\\CARS\\NIS_240_SX_89\\GEOMETRY.BIN");

		exportToFile("C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY.BIN.bak", "C:\\jeux\\UCE 1.0.1.18\\CARS\\AUD_RS4_STK_08\\GEOMETRY", FileFormat.FBX7700_BINARY);
	}
}
