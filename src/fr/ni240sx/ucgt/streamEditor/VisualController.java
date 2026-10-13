package fr.ni240sx.ucgt.streamEditor;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.U;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.V;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.X;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Y;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.Z;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.imageio.stream.MemoryCacheImageInputStream;

import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.geometry.CompressedData;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.textures.NFSTexture;
import fr.ni240sx.ucgt.geometryFile.textures.TPK;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.Hash;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackStreamingSections;
import fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackStreamingSections.TrackStreamingSection;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Transform;
import net.nikr.dds.DDSImageReader;

public class VisualController {
	//controller for objects chunk
	public short ID;
	public TrackStreamingSections.TrackStreamingSection chunk;
	
	public static final float heliSheetFactorXY = 4.0f;
	public static final float heliSheetFactorZ = 16.0f;
	
	public List<Summary> summaries;
	//list content
	//	GeomController
	//		list ObjectController
	//	TPKController
	//		list TextureController
	//	Collision
	//	Objects
	//	Positions
	//	other

	public List<ModelController> models;
	public List<TextureController> textures;
	public List<ChunkScenery> sceneryGroups;
	public List<Node> visualNodes;
	
//	public short[][] heliSheetData;
//	public List<SmokeSpawner> smokeSpawners;
//	public List<VisCurtain> visCurtains;
//	public ChunkCollisions collisions;
	
	public Node heliSheetMesh;
	public List<Node> smokeSpawnersMeshes, visCurtainsMeshes, collisionsMeshes;
	
	
	public int currentLod = -1;
	public boolean isLoaded = false;
	public boolean areMaterialsDone = false;

	public static final PhongMaterial UNKNOWN_OBJECT = new PhongMaterial(Color.color(1, 0, 1, 0.2));
	public static final PhongMaterial HELIKOPTER_SHEET = new PhongMaterial(Color.color(0, 1, 1, 1));
	public static final PhongMaterial VIS_CURTAIN = new PhongMaterial(Color.color(1, 1, 1, 0.5));
	public static final PhongMaterial COLLISION = new PhongMaterial(Color.color(0, 1, 0, 1));
	public static final PhongMaterial COLLISION_ALPHA = new PhongMaterial(Color.color(0, 1, 0, 0.2));

	public VisualController(TrackStreamingSection c) {
		chunk = c;
		ID = c.SectionNumber;
	}

	public List<Summary> getContentSummary(File streamPath) throws Exception {
		if (summaries != null) return summaries;
		summaries = Collections.synchronizedList(new ArrayList<>());
		
		var chunkData = ByteBuffer.allocate(chunk.Size);
		chunkData.order(ByteOrder.LITTLE_ENDIAN);
		{
			var stream = new RandomAccessFile(streamPath, "r");
			stream.seek(chunk.FileOffset);
			stream.read(chunkData.array());
			stream.close();
		}
		Block block;
		var blocks = new ArrayList<Block>();
		if (Block.debug) System.out.println("\\/=== Blocks in chunk %s ===\\/".formatted(this.toString()));
		while (chunkData.position() < chunkData.limit()) {
//			if ((block = Block.read(chunkData)) != null) blocks.add(block);
			if ((block = Block.read(chunkData)) != null) switch (block.getBlockID()) {
			case Geometry:
				summaries.add(new Summary.Geom((Geometry) block));
				break;
			case TPK:
				summaries.add(new Summary.TexPack((TPK) block));
				break;
				
				
			case ChunkScenery:
			default:
				summaries.add(new Summary.Default(block));
			}
			blocks.add(block);
		}
		if (Block.debug) System.out.println("/\\=== Blocks in chunk %s ===/\\".formatted(this.toString()));

		return summaries;
	}

	public void getContent(File streamPath, ConcurrentHashMap<Integer,ModelController> allModels, ConcurrentHashMap<Integer, TextureController> allTextures) throws Exception {
		if (isLoaded) return;
		var chunkData = ByteBuffer.allocate(chunk.Size);
		chunkData.order(ByteOrder.LITTLE_ENDIAN);
		{
			var stream = new RandomAccessFile(streamPath, "r");
			stream.seek(chunk.FileOffset);
			if (chunk.Size != chunk.CompressedSize) {
				//chunk is compressed
								
				var bb = ByteBuffer.allocate(chunk.CompressedSize);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				stream.read(bb.array());
				
				//loops on the one or multiple compressed blocks (usually RefPack, 32768 bytes each)
				while (bb.hasRemaining()) {
					CompressedData d = (CompressedData) Block.read(bb);
					var decomp = Compression.decompress(d.data);
//					if (decomp==null) continue offsets;
					chunkData.put(d.decompressionOffset, decomp);
				}
				chunkData.position(0);
			} else {
				//chunk is not compressed
				stream.read(chunkData.array());
			}
			stream.close();
		}

		if (models == null) models = new LinkedList<>();
		if (textures == null) textures = new LinkedList<>();
		if (sceneryGroups == null) sceneryGroups = new LinkedList<>();
		
		Block block;
		if (Block.debug) System.out.println("\\/=== Blocks in chunk %s ===\\/".formatted(this.toString()));
		System.out.println("chunk %s".formatted(this.toString()));
		while (chunkData.position() < chunkData.limit()) {
//			if ((block = Block.read(chunkData)) != null) blocks.add(block);
			if ((block = Block.read(chunkData)) != null) switch (block.getBlockID()) {
			case Geometry:
				((Geometry)block).parts.forEach(p -> {
					var mc = new ModelController(p);
					models.add(mc);
					allModels.put(mc.key, mc);
				});
				break;
			case TPK:
				((TPK)block).textures.forEach(tex -> {
					var tc = new TextureController(tex);
					textures.add(tc);
					allTextures.put(tc.key, tc);
				});
				break;
				
			case ChunkScenery:
				sceneryGroups.add((ChunkScenery) block);
				break;
			case ChunkHeliSheet:
				//one per chunk at most
				var heliSheetData = ((ChunkHeliSheet)block).sheet;
				if (heliSheetData != null) {
					TriangleMesh heliSheet = new TriangleMesh();
					heliSheet.setVertexFormat(VertexFormat.POINT_TEXCOORD);
					heliSheet.getTexCoords().addAll(0, 0);
					for (int i=0; i<heliSheetData.length; i++) {
						heliSheet.getPoints().addAll(
								heliSheetData[i][0]/heliSheetFactorXY, heliSheetData[i][3]/heliSheetFactorXY, heliSheetData[i][6]/heliSheetFactorZ,
								heliSheetData[i][1]/heliSheetFactorXY, heliSheetData[i][4]/heliSheetFactorXY, heliSheetData[i][7]/heliSheetFactorZ,
								heliSheetData[i][2]/heliSheetFactorXY, heliSheetData[i][5]/heliSheetFactorXY, heliSheetData[i][8]/heliSheetFactorZ						
								);
						heliSheet.getFaces().addAll(3*i, 0, 3*i+1, 0, 3*i+2, 0);
					}
					var mv = new MeshView(heliSheet);
					mv.setCullFace(CullFace.NONE);
					mv.setMaterial(HELIKOPTER_SHEET);
					mv.setDrawMode(DrawMode.LINE);
					mv.setViewOrder(-10);
					heliSheetMesh = mv;
				}
				break;
			case ChunkSmokeSpawners:
				var smokeSpawners = (((ChunkSmokeSpawners)block));
				//if (smokeSpawners != null) 
				{
					smokeSpawnersMeshes = new ArrayList<>();
					for (var spawner : smokeSpawners) {
						var s = new Sphere(1.0);
						s.setTranslateY(-spawner.pos[X]);
						s.setTranslateZ(spawner.pos[Y]);
						s.setTranslateX(spawner.pos[Z]);
						s.setMaterial(HELIKOPTER_SHEET);
						smokeSpawnersMeshes.add(s);
//						var mv = new MeshView(doLineMesh(
//								spawner.pos[Z], spawner.pos[X], spawner.pos[Y],
//								spawner.pos[Z] + spawner.vector[Z], -spawner.pos[X] - spawner.vector[X], spawner.pos[Y] + spawner.vector[Y]
//								));
//						mv.setCullFace(CullFace.NONE);
//						mv.setMaterial(HELIKOPTER_SHEET);
//						mv.setDrawMode(DrawMode.LINE);
//						mv.setViewOrder(-10);
//						nodes.add(mv);
					}
				}
				break;
			case ChunkVisCurtains:
				var visCurtains = ((ChunkVisCurtains)block);
				//if (visCurtains != null) 
				{
					visCurtainsMeshes = new ArrayList<>();
					for (var curtain : visCurtains) {
						var mv = new MeshView(doVerticalRectangle(curtain.from[X], curtain.from[Y], curtain.from[Z],
								curtain.to[X], curtain.to[Y], curtain.to[Z], curtain.vector[X], curtain.vector[Y], curtain.vector[Z]));
						mv.setCullFace(CullFace.NONE);
						mv.setMaterial(VIS_CURTAIN);
						mv.setViewOrder(-10);
//						mv.setDrawMode(DrawMode.LINE);
						visCurtainsMeshes.add(mv);
					}
				}
				break;
			case ChunkCollisions:
				var collisions = (ChunkCollisions) block;
				if (collisions.CARP.collisionInstances != null) {
					collisionsMeshes = new ArrayList<>();
					for (var inst : collisions.CARP.collisionInstances.instances) //if (inst.groupNumber == 0) //get rid of the barrier collisions
					{
						var havok = inst.collisionData;
						if (havok.collisionShape == null) continue;
						if (havok.collisionShape.shape == null || havok.collisionShape.shape.object2 == null ||
								havok.collisionShape.shape.object2.subpartListed == null) {
							System.out.println("invalid Havok data, iID %d".formatted(havok.collisionShape.iID));
							continue;
						}
						
						
						for (var mesh : havok.collisionShape.shape.object2.meshes) {
							for (int havokMatIndex=0; havokMatIndex<mesh.havokMatToSimSurface.size(); havokMatIndex++) {
								TriangleMesh collisionMesh = new TriangleMesh();
								collisionMesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
								collisionMesh.getTexCoords().addAll(0, 0);
								for (var v : mesh.vertices) {
									collisionMesh.getPoints().addAll(v.z-inst.posZ, -v.x+inst.posX, v.y-inst.posY);
								}
								boolean used = false;
								for (int j=0; j<mesh.indices.size()/4; j++) if (mesh.materialIndices.get(j) == havokMatIndex) {
									collisionMesh.getFaces().addAll(mesh.indices.get(4*j), 0, mesh.indices.get(4*j+2), 0, mesh.indices.get(4*j+1), 0);
									used = true;
								}
								if (used) {
									var mv = new MeshView(collisionMesh);
//									mv.setCullFace(CullFace.NONE);
//									mv.setMaterial(COLLISION);
//									mv.setMaterial(new PhongMaterial(Color.hsb(dat, 1, 1)));
//									mv.setMaterial(new PhongMaterial(Color.hsb(havok.data4[i].v2 *2, 1, 1)));
									mv.setMaterial(new PhongMaterial(Color.hsb(havok.collisionShape.simSurfaces.get(((mesh.havokMatToSimSurface.get(havokMatIndex).val >>> 16) & 0xff)/8), 1, 1))); //v2 & 0xff short works for pc, v2 & 0xff byte works for xbox
//										mv.setDrawMode(DrawMode.LINE);
									mv.setViewOrder(-20);
									collisionsMeshes.add(mv);
				
									var t2 = new Tooltip("""
											Collision surface: %s""".formatted(Hash.getVLT(havok.collisionShape.simSurfaces.get(((mesh.havokMatToSimSurface.get(havokMatIndex).val >>> 16) & 0xff)/8))));
									mv.setOnMouseClicked(evt -> {
										if (t2.isShowing()) t2.hide();
										else t2.show(mv, evt.getScreenX()+10, evt.getScreenY()+10);
									});
									
//									System.out.println("Surface index %d / total index %d".formatted(i, havok.data4.length));
//									System.out.println("havok data4 hashes 0x%04X%04X %d/%d   --> %d?  total surfaces %d".formatted(havok.data4[i].v2, havok.data4[i].v1,i+1,havok.data4.length, (havok.data4[i].v2 & 0xff)/8, havok.surfaces.length));
//									System.out.println("Surface %s uses color %s".formatted(Hash.getVLT(havok.collisionShape.simSurfaces.get((mesh.havokMatToSimSurface.get(havokMatIndex).v2 & 0xff)/8)), Color.hsb(havok.collisionShape.simSurfaces.get((mesh.havokMatToSimSurface.get(havokMatIndex).v2 & 0xff)/8), 1, 1).toString()));
								}
							}
						}
					}
					
					
				}
				break;
			default:
			}
		}
		if (Block.debug) System.out.println("/\\=== Blocks in chunk %s ===/\\".formatted(this.toString()));
		isLoaded = true;
	}
	
	public void unload(ConcurrentHashMap<Integer, ModelController> allModels, ConcurrentHashMap<Integer, TextureController> allTextures) {
		if (!isLoaded) return;
//		System.out.println("UNLOADING %s".formatted(toString()));
		if (models != null) {
			for (var m : models) {
//				allModels.remove(m.key, m);
				allModels.values().remove(m);
			}
			models.clear();
			models = null;
		}
		if (textures != null) {
			for (var t : textures) {
//				allTextures.remove(t.key, t);
				allTextures.values().remove(t);
			}
			textures.clear();
			textures = null;
		}
		sceneryGroups = null;
		visualNodes = null;
//		heliSheetData = null;
//		smokeSpawners = null;
//		visCurtains = null;
//		collisions = null;
		
		heliSheetMesh = null;
		smokeSpawnersMeshes = null;
		visCurtainsMeshes = null;
		collisionsMeshes = null;
		
		isLoaded = false;
		areMaterialsDone = false;
		currentLod = -1;
	}
	
	public boolean isReady() {
		return visualNodes != null && currentLod == WorldEditorGUI.displayLod.getSelectionModel().getSelectedIndex();
	}
	
	public List<Node> renderContent(ConcurrentHashMap<Integer, ModelController> allModels){
		var nodes = new ArrayList<Node>();
		currentLod = WorldEditorGUI.displayLod.getSelectionModel().getSelectedIndex();
		
		if (WorldEditorGUI.visibleHeliSheet.isSelected() && heliSheetMesh != null) nodes.add(heliSheetMesh);
		if (WorldEditorGUI.visibleSmokeSpawners.isSelected() && smokeSpawnersMeshes != null) nodes.addAll(smokeSpawnersMeshes);
		if (WorldEditorGUI.visibleVisCurtains.isSelected() && visCurtainsMeshes != null) nodes.addAll(visCurtainsMeshes);
		if (WorldEditorGUI.visibleCollisions.isSelected() && collisionsMeshes != null) nodes.addAll(collisionsMeshes);
		if (!WorldEditorGUI.visibleScenery.isSelected()) return nodes;
		
		if (isReady()) {
			nodes.addAll(visualNodes);
			return nodes;
		}

		visualNodes = new ArrayList<>();
		
		for (var scenery : sceneryGroups) {
			int index = 0;
			for (var obj : scenery.objects) {
				for (var pos : scenery.instances)
				if (pos.SceneryInfoNumber == index){
					ModelController model = null;
					int lod = 0;
					for (lod = WorldEditorGUI.displayLod.getSelectionModel().getSelectedIndex(); lod < 4; lod++) {
						model = allModels.get(obj.lod[lod]);
						if (model != null) break;
					}					
					
					if (model == null) {
						System.out.println("%s: Object %s not found in loaded chunks!".formatted(toString(),obj.name));
							var b = new Box(pos.boundsMax[X] - pos.boundsMin[X],
									pos.boundsMax[Y] - pos.boundsMin[Y],
									pos.boundsMax[Z] - pos.boundsMin[Z]);
							
							b.setTranslateX((pos.boundsMax[X] + pos.boundsMin[X])/2);
							b.setTranslateY((pos.boundsMax[Y] + pos.boundsMin[Y])/2);
							b.setTranslateZ((pos.boundsMax[Z] + pos.boundsMin[Z])/2);
						    
							b.setMaterial(UNKNOWN_OBJECT);
							b.setDepthTest(DepthTest.ENABLE);
							b.setViewOrder(-10);
							
							visualNodes.add(b);
							
//							var t2 = new Tooltip(
//									"Model="+Hash.getBIN(obj.lod[lod])+" ("+obj.name+"/"+String.format("0x%08X", obj.lod[lod])+")"+
//						    		"\nChunk="+toString()+
//						    		"\nGroupID="+pos.groupID+
//						    		"\nInstanceID="+pos.instanceID+
////						    		"\nPositionHash="+Hash.getBIN(pos.hash)+
//						    		"\nObjectID="+pos.objectID+
//						    		"\nv1="+pos.v1+
//						    		"\nv2="+pos.v2+
//						    		"\nv3="+pos.v3+
//						    		"\nv4="+pos.v4+
//						    		"\nv5="+pos.v5+
//						    		"\nv6="+pos.v6+
//						    		"\nv7="+pos.v7+
//						    		"\nv8="+pos.v8
//						    		);
//							b.setOnMouseEntered(evt -> t2.show(b, evt.getScreenX()+10, evt.getScreenY()+10));
//							b.setOnMouseExited(evt -> t2.hide());
//							b.setOnMouseClicked(evt -> {
//								if (WorldEditorGUI.centerOnPart.isSelected()) WorldEditorGUI.viewport.moveCameraNoListener(
//										(pos.boundsMax[X] + pos.boundsMin[X])/2,
//										(pos.boundsMax[Y] + pos.boundsMin[Y])/2,
//										(pos.boundsMax[Z] + pos.boundsMin[Z])/2);
//							});
							
					} else for (var mat : model.materials) {
						var mv = new MeshView(mat.mesh);
						mat.meshViews.add(mv);
						
						mv.setDepthTest(DepthTest.ENABLE);
						if (mat.textureUsages.contains(TextureUsage.OPACITY) || mat.textureUsages.contains(TextureUsage.OPACITY_SPEC_REFL)) {
							mv.setViewOrder(-1);
							mv.setCullFace(CullFace.NONE);
						}

						var m = pos.matrix;													
						var transform = Transform.affine(m[0][0], m[0][1], m[0][2], pos.pos[X],
								m[1][0], m[1][1], m[1][2], pos.pos[Y],
								m[2][0], m[2][1], m[2][2], pos.pos[Z]);
						mv.getTransforms().add(transform);
					    
						visualNodes.add(mv);
						
						
//						mv.setOnMouseEntered(evt -> t2.show(mv, evt.getScreenX()+10, evt.getScreenY()+10));
//						mv.setOnMouseExited(evt -> t2.hide());
//						mv.setOnMouseClicked(evt -> {
//							if (evt.isStillSincePress() && WorldEditorGUI.centerOnPart.isSelected()) WorldEditorGUI.viewport.moveCameraNoListener(
//									(pos.boundsMax[X] + pos.boundsMin[X])/2, 
//									(pos.boundsMax[Y] + pos.boundsMin[Y])/2, 
//									(pos.boundsMax[Z] + pos.boundsMin[Z])/2);
//						});
					}
					
				}
				index++;
			}	
		}

		nodes.addAll(visualNodes);
		return nodes;
	}

	public static TriangleMesh doLineMesh(float x1, float y1, float z1, float x2, float y2, float z2) {
		TriangleMesh mesh = new TriangleMesh();
		mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
		mesh.getTexCoords().addAll(0, 0);
		mesh.getPoints().addAll(x1,y1,z1,x2,y2,z2);
		mesh.getFaces().addAll(0, 0, 1, 0, 0, 0);
		return mesh;
	}

	public static TriangleMesh doVerticalRectangle(float x1, float y1, float z1, float x2, float y2, float z2) {
		var mesh = new TriangleMesh();
		mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
		mesh.getTexCoords().addAll(0, 0);
		mesh.getPoints().addAll(x1, y1, z1,
				x1, y1, z2,
				x2, y2, z1, 
				x2, y2, z2);
		mesh.getFaces().addAll(	0, 0, 	1, 0, 	2, 0,
								1, 0, 	3, 0, 	2, 0);
		return mesh;
	}
	public static TriangleMesh doVerticalRectangle(float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz) {
		var mesh = new TriangleMesh();
		mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
		mesh.getTexCoords().addAll(0, 0);
		mesh.getNormals().addAll(nx, ny, nz);
		mesh.getPoints().addAll(x1, y1, z1,
				x1, y1, z2,
				x2, y2, z1, 
				x2, y2, z2);
		mesh.getFaces().addAll(	0, 0, 0, 	1, 0, 0, 	2, 0, 0,
								1, 0, 0, 	3, 0, 0, 	2, 0, 0);
		return mesh;
	}
	
	public void updateMaterials(ConcurrentHashMap<Integer, TextureController> allTextures) {
//		if (areMaterialsDone) return;
		if (this.models == null) return;
		for (var m : models) for (var mat : m.materials) if (mat.needsTexturing) {
			//USE NEW TEXTURE CONTROLLERS IF NEEDED
			
			
			
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
							
			var diffuseMap = (has_d ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
					+ (has_o ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)) : 0)
					+ (has_osr ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //opacity component
					+ (has_re ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)) : 0) //never with diffuse
					+ (has_b && has_d2 ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
					+ (has_b && has_d2 ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)) : 0);
			
			var selfIlluminationMap = (has_d && (has_i || mat.isConstant) ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)) : 0)
					+ (has_i ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)) : 0);
			
			var specularMap = (has_s ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)) : 0)
					+ (has_osr ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)) : 0) //specular component
					+ (has_bsr ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)) : 0) //specular component
					+ (has_b && has_s2 ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)) : 0)
					+ (has_b && has_s2 ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)) : 0);
			
			var normalMap = (has_n ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)) : 0)
					+ (has_nm ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)) : 0)
					+ (has_en ? mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)) : 0);

			TextureController finalDiffuse = null;
			TextureController finalSelfIllumination = null;
			TextureController finalSpecular = null;
			TextureController finalNormal = null;
			
			if (diffuseMap != 0) {
//				if (imageTextures.get(diffuseMap) == null) {
//					var imgtex = new ImageTextureHolder();
//					imageTextures.put(diffuseMap, imgtex);
					//make diffuse map

					TextureController blend = null;
					if (has_b) {
						blend = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)));
						if (blend == null) System.out.println("Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)))+" not found in loaded chunks!");
					}

					if (has_d) {
						var diffuse = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)));
						if (diffuse == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)))+" not found in loaded chunks!");
						TextureController opacity = null;
						TextureController illum = null;
						TextureController opacity_spec_refl = null;
						TextureController diffuse2 = null;
						if (has_o) {
							opacity = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)));
							if (opacity == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY)))+" not found in loaded chunks!");
						}
						if (has_i) {
							illum = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)));
							if (illum == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)))+" not found in loaded chunks!");
						}
						if (has_osr) {
							opacity_spec_refl = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)));
							if (opacity_spec_refl == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.OPACITY_SPEC_REFL)))+" not found in loaded chunks!");
						}

						if (has_d2) {
							diffuse2 = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)));
							if (diffuse2 == null) System.out.println("Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE2)))+" not found in loaded chunks!");
						}
						if (diffuse != null) {							
							if (opacity != null) {
								if (!diffuse.isProcessed) applyOpacity(diffuse.image, opacity.image);
								diffuse.isProcessed = true;
							} else if (opacity_spec_refl != null){
								//red = opacity
								//green = specular
								//blue = reflection (unsupported by the viewer)

								if (!diffuse.isProcessed && !opacity_spec_refl.isProcessed) {
									applyOpacity(diffuse.image, opacity_spec_refl.image);
									diffuse.isProcessed = true;
								}
								
								if (!opacity_spec_refl.isProcessed) for (var x=0; x<opacity_spec_refl.image.getWidth(); x++) {
									for (var y=0; y<opacity_spec_refl.image.getHeight(); y++) {
										opacity_spec_refl.image.getPixelWriter().setArgb(x, y, 
												0xff000000 | //ignore alpha
												(opacity_spec_refl.image.getPixelReader().getArgb(x, y)) & 0x0000ff00 | 
												(opacity_spec_refl.image.getPixelReader().getArgb(x, y) << 8) & 0x00ff0000 |
												(opacity_spec_refl.image.getPixelReader().getArgb(x, y) >>> 8) & 0x000000ff); 
									}
								}
								opacity_spec_refl.isProcessed = true;
								
//								matVis.setSpecularMap(opacity_spec_refl.image);
								finalSpecular = opacity_spec_refl;
							} 
							
							if (blend != null && diffuse2 != null) {
								//TODO this actually uses the second uv map, LIGHTMAP third uv map
								if (!blend.isProcessed) applyBlend(diffuse.image, blend.image, diffuse2.image);
								blend.isProcessed = true;
								//put everything in blend
//								matVis.setDiffuseMap(blend.image);
								finalDiffuse = blend;
							} else
							
//							matVis.setDiffuseMap(diffuse.image);
								finalDiffuse = diffuse;
						}
					}
					if (mat.textureUsages.contains(TextureUsage.ROADELEMENTS)) {
						var roadelements = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)));
						if (roadelements == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ROADELEMENTS)))+" not found in loaded chunks!");
						if (roadelements != null) {
//							matVis.setDiffuseMap(roadelements.image);
							finalDiffuse = roadelements;
						}
					}
//				}
//				if (matVis.getDiffuseMap() == null) matVis.setDiffuseColor(Color.rgb(255, 0, 255));
//				else matVis.setDiffuseColor(Color.WHITE);
			}
			
			if (selfIlluminationMap != 0 && WorldEditorGUI.doSelfIllum.isSelected()) {
				//if (imageTextures.get(selfIlluminationMap) == null) {
					//make si map
					if (has_d) {
						var diffuse = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)));
						if (diffuse == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.DIFFUSE)))+" not found in loaded chunks!");
						TextureController illum = null;

						if (has_i) {
							illum = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)));
							if (illum == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ILLUMINATE)))+" not found in loaded chunks!");
						}
						if (diffuse != null) {
							if (illum != null) {
								//DONT OVERRIDE DIFFUSE
								if (!illum.isProcessed) applyOpacityToImage2(diffuse.image, illum.image);
								illum.isProcessed = true;
//								matVis.setSelfIlluminationMap(illum.image);
								finalSelfIllumination = illum;
							}
						}
					}
				//}
			}
			
			if (specularMap != 0 && WorldEditorGUI.doSpecular.isSelected()) {
				//if (imageTextures.get(specularMap) == null) {

					//make specular map
					TextureController blend = null;
					if (has_b) {
						blend = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)));
						if (blend == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND)))+" not found in loaded chunks!");
					}

					if (mat.textureUsages.contains(TextureUsage.SPECULAR)) {
						var specular = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)));
						if (specular == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR)))+" not found in loaded chunks!");
						if (specular != null) {
							
							if (blend != null && mat.textureUsages.contains(TextureUsage.SPECULAR2)) {
								var specular2 = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)));
								if (specular2 == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.SPECULAR2)))+" not found in loaded chunks!");
								else {
//									applyBlend(specular.image, blend.image, specular2.image);
								}
							} 
//							matVis.setSpecularMap(specular.image);
							finalSpecular = specular;
						}
					}
					if (mat.textureUsages.contains(TextureUsage.BLEND_SPEC_REFL)) {
						//red = blend (unsupported)
						//green = specular
						//blue = reflection (unsupported by the viewer)
						var blend_spec_refl = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)));
						if (blend_spec_refl == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.BLEND_SPEC_REFL)))+" not found in loaded chunks!");
						if (blend_spec_refl != null) {
																			
							if (!blend_spec_refl.isProcessed) for (var x=0; x<blend_spec_refl.image.getWidth(); x++) {
								for (var y=0; y<blend_spec_refl.image.getHeight(); y++) {
									blend_spec_refl.image.getPixelWriter().setArgb(x, y, 
											0xff000000 | //ignore alpha
											(blend_spec_refl.image.getPixelReader().getArgb(x, y)) & 0x0000ff00 | 
											(blend_spec_refl.image.getPixelReader().getArgb(x, y) << 8) & 0x00ff0000 |
											(blend_spec_refl.image.getPixelReader().getArgb(x, y) >>> 8) & 0x000000ff); 
								}
							}
							blend_spec_refl.isProcessed = true;

//							matVis.setSpecularMap(blend_spec_refl.image);
							finalSpecular = blend_spec_refl;
						}
					}

				//}
			}
			
			if (normalMap != 0 && WorldEditorGUI.doNormal.isSelected()) {
				//if (imageTextures.get(normalMap) == null) {
					//make normal map
					if (mat.textureUsages.contains(TextureUsage.NORMAL)) {
						var nfstex = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)));
						if (nfstex == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMAL)))+" not found in loaded chunks!");
						if (nfstex != null) {
//							matVis.setBumpMap(nfstex.image);
							finalNormal = nfstex;
						}
					}
					if (mat.textureUsages.contains(TextureUsage.NORMALMAP)) {
						var nfstex = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)));
						if (nfstex == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.NORMALMAP)))+" not found in loaded chunks!");
						if (nfstex != null) {
//							matVis.setBumpMap(nfstex.image);
							finalNormal = nfstex;
						}
					}
					if (mat.textureUsages.contains(TextureUsage.ELEMENTSNORMAL)) {
						var nfstex = allTextures.get(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)));
						if (nfstex == null) System.out.println(toString()+": Texture "+Hash.getBIN(mat.textureHashes.get(mat.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)))+" not found in loaded chunks!");
						if (nfstex != null) {
//							matVis.setBumpMap(nfstex.image);
							finalNormal = nfstex;
						}
					}
				//}
			}

			mat.diffuse = finalDiffuse != null ? finalDiffuse.image : null;
			mat.normal = finalNormal != null ? finalNormal.image : null;
			mat.specular = finalSpecular != null ? finalSpecular.image : null;
			mat.selfillum = finalSelfIllumination != null ? finalSelfIllumination.image : null;
			
			var latch = new CountDownLatch(1);
			javafx.application.Platform.runLater(() -> {
				try {
					mat.matVis = new PhongMaterial();
					
					mat.matVis.setDiffuseMap(WorldEditorGUI.doDiffuse.isSelected() ? mat.diffuse : null);
					mat.matVis.setBumpMap(WorldEditorGUI.doNormal.isSelected() ? mat.normal : null);
					mat.matVis.setSpecularMap(WorldEditorGUI.doSpecular.isSelected() ? mat.specular : null);
					mat.matVis.setSelfIlluminationMap(WorldEditorGUI.doSelfIllum.isSelected() ? mat.selfillum : null);
					
					if (diffuseMap != 0 && WorldEditorGUI.doDiffuse.isSelected()) {
						if (mat.matVis.getDiffuseMap() == null) mat.matVis.setDiffuseColor(Color.rgb(255, 0, 255));
						else {
							mat.matVis.setDiffuseColor(Color.WHITE);
						}
					}	

					if((diffuseMap == 0 || (WorldEditorGUI.doDiffuse.isSelected() && mat.matVis.getDiffuseMap() != null))
							&& (normalMap == 0 || (WorldEditorGUI.doNormal.isSelected() && mat.matVis.getBumpMap() != null))
							&& (specularMap == 0 || (WorldEditorGUI.doSpecular.isSelected() && mat.matVis.getSpecularMap() != null))
							&& (selfIlluminationMap == 0 || (WorldEditorGUI.doSelfIllum.isSelected() && mat.matVis.getSelfIlluminationMap() != null)))
						mat.needsTexturing = false;
						
					for (var mv : mat.meshViews) mv.setMaterial(mat.matVis);
					latch.countDown();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mat.meshViews.clear();

			if (this.models == null) return;
		} else {
			var latch = new CountDownLatch(1);
			javafx.application.Platform.runLater(() -> {
				try {
					mat.matVis.setDiffuseMap(WorldEditorGUI.doDiffuse.isSelected() ? mat.diffuse : null);
					mat.matVis.setBumpMap(WorldEditorGUI.doNormal.isSelected() ? mat.normal : null);
					mat.matVis.setSpecularMap(WorldEditorGUI.doSpecular.isSelected() ? mat.specular : null);
					mat.matVis.setSelfIlluminationMap(WorldEditorGUI.doSelfIllum.isSelected() ? mat.selfillum : null);
					for (var mv : mat.meshViews) mv.setMaterial(mat.matVis);
					latch.countDown();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mat.meshViews.clear();
			if (this.models == null) return;
		}
//		areMaterialsDone = true;
	}
	
	private static void applyOpacity(WritableImage diffuse, WritableImage opacity) {
		if (diffuse == null || opacity == null) return;
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
	private static void applyOpacityToImage2(WritableImage diffuse, WritableImage opacity) {
		if (diffuse == null || opacity == null) return;
		double widthRatio = opacity.getWidth()/diffuse.getWidth();
		double heightRatio = opacity.getHeight()/diffuse.getHeight();
		
		for (var x=0; x<opacity.getWidth(); x++) {
			for (var y=0; y<opacity.getHeight(); y++) {
				opacity.getPixelWriter().setArgb(x, y, 
						diffuse.getPixelReader().getArgb((int)(x/widthRatio), (int)(y/heightRatio)) & 0x00ffffff | 
						(opacity.getPixelReader().getArgb(x,y) << 8) & 0xff000000);
			}
		}
	}
	private static void applyBlend(WritableImage diffuse, WritableImage blend, WritableImage diffuse2) {
		double widthRatioDiffuse = diffuse.getWidth()/blend.getWidth();
		double heightRatioDiffuse = diffuse.getHeight()/blend.getHeight();
		double widthRatioDiffuse2 = diffuse2.getWidth()/blend.getWidth();
		double heightRatioDiffuse2 = diffuse2.getHeight()/blend.getHeight();
		
		
		for (var x=0; x<blend.getWidth(); x++) {
			for (var y=0; y<blend.getHeight(); y++) {
//				diffuse.getPixelWriter().setArgb(x, y, 
//						(~blend.getPixelReader().getArgb((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)) &
//						diffuse.getPixelReader().getArgb(x, y)) |
//						(blend.getPixelReader().getArgb((int)(x*widthRatioBlend), (int)(y*heightRatioBlend)) &
//						diffuse2.getPixelReader().getArgb((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)))
//						);
				blend.getPixelWriter().setColor(x, y, Color.color(
						(1-blend.getPixelReader().getColor(x,y).getRed()) * diffuse.getPixelReader().getColor((int)(x*widthRatioDiffuse), (int)(y*heightRatioDiffuse)).getRed()
						+ blend.getPixelReader().getColor(x,y).getRed() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getRed(), 
						(1-blend.getPixelReader().getColor(x,y).getGreen()) * diffuse.getPixelReader().getColor((int)(x*widthRatioDiffuse), (int)(y*heightRatioDiffuse)).getGreen()
						+ blend.getPixelReader().getColor(x,y).getGreen() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getGreen(), 
						(1-blend.getPixelReader().getColor(x,y).getBlue()) * diffuse.getPixelReader().getColor((int)(x*widthRatioDiffuse), (int)(y*heightRatioDiffuse)).getBlue()
						+ blend.getPixelReader().getColor(x,y).getBlue() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getBlue(), 
						(1-blend.getPixelReader().getColor(x,y).getOpacity()) * diffuse.getPixelReader().getColor((int)(x*widthRatioDiffuse), (int)(y*heightRatioDiffuse)).getOpacity()
						+  blend.getPixelReader().getColor(x,y).getOpacity() * diffuse2.getPixelReader().getColor((int)(x*widthRatioDiffuse2), (int)(y*heightRatioDiffuse2)).getOpacity()
						));
			}
		}
	}	
	public static class ModelController{
		public int key;
//		Part model;
		public List<MaterialController> materials = new ArrayList<>();
		
		public ModelController(Part model) {
			if (model != null && model.header != null)
				key = model.header.binKey;
			
			if (model != null && model.mesh != null && model.mesh.materials != null) for (var mat : model.mesh.materials.materials) if (mat.verticesBlock != null){
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
				
				materials.add(new MaterialController(mat, matMesh));
			}
		}
	}
	
	public static class TextureController{
		public int key;
//		NFSTexture texture;
		public WritableImage image;
		boolean isProcessed = false;
		
		public TextureController(NFSTexture t) {
			key = t.binKey;
			
			try {
			var r = new DDSImageReader(null);
			var iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(t.DDSImage));
			r.setInput(iis);
//			if (mat.isOpaque()) {
//				r.ignoreAlpha = true;										
//			}
			image = SwingFXUtils.toFXImage(r.read(0), null);
			iis.close();
			}catch(Exception e) {
				System.err.println("Cannot load texture: "+e.getMessage());
//				e.printStackTrace();
			}
		}
	}
	
	public static class MaterialController{
		TriangleMesh mesh;
		ArrayList<MeshView> meshViews = new ArrayList<>();
		PhongMaterial matVis;
		ArrayList<Integer> textureHashes;
		ArrayList<TextureUsage> textureUsages;
		boolean isConstant;
		public boolean needsTexturing = true;
		
		WritableImage diffuse;
		WritableImage specular;
		WritableImage normal;
		WritableImage selfillum;
		
		public MaterialController(Material mat, TriangleMesh matMesh) {
			textureHashes = mat.TextureHashes;
			textureUsages = mat.textureUsages;
			mesh = matMesh;
			isConstant = mat.shaderUsage.key == Hash.findVLT("ar_constant");
		}
	}
	
	@Override
	public String toString() {
		return ((char)('A'+(ID/1000))+""+ID%1000);
	}
}
