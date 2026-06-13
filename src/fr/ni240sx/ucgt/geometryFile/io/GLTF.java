package fr.ni240sx.ucgt.geometryFile.io;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.GeometryEditorCLI;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.Hash;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;


public class GLTF {

	public final static int glTF_blender = 571017851;
	public final static int glTF_BINARY = 1179937895; //glb, actually
	public final static int JSON = 1313821514;
	public final static int BIN = 5130562;
	
	public static final String LOD_TRIANGLES_SUFFIX = "-ROADLOD";
	
	public static void load(Geometry geom, File f) throws IOException, JSONException {
		/*
		 * TODO
		 * this is still quite barebones
		 * no format verifications
		 * 
		 * IMPORT
		 * X -> -Y
		 * Y -> Z
		 * Z -> -X
		 * 
		 * EXPORT
		 * X -> -Z
		 * Y -> -X
		 * Z -> Y
		 */
		
		System.out.println("Reading glTF header...");
		long time = System.currentTimeMillis();		
		geom.isImported = true;

		FileInputStream fis = new FileInputStream(f);
		int signature = getNextInt(fis);

		String json = "";
		if (signature == glTF_BINARY) {
			getNextInt(fis); //2, probably number of parts in file
			getNextInt(fis); //total file length
			var jsonLength = getNextInt(fis);
			getNextInt(fis); //JSON magic
			
			var jsonBytes = new byte[jsonLength];
			fis.read(jsonBytes);
			
			//keep fis open for byte buffers setup
			json = new String(jsonBytes, StandardCharsets.UTF_8);
			
		} else {
			fis.close();
			fis = new FileInputStream(f);
			var jsonBytes = new byte[(int) f.length()];
			fis.read(jsonBytes);
			fis.close();
			json = new String(jsonBytes, StandardCharsets.UTF_8);
		}
		
	    
		JSONObject glTF = new JSONObject(json);
		json = null;
		

        System.out.println("Header read in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
		System.out.println("Unpacking data...");

//		System.out.println(glTF.getJSONObject("asset").getString("generator"));
//		System.out.println(glTF.getJSONObject("asset").getString("version"));
		
		var scene = glTF.getJSONArray("scenes").getJSONObject(glTF.getInt("scene"));

		var nodes = glTF.getJSONArray("nodes");
		var materials = glTF.getJSONArray("materials");
		var meshes = glTF.getJSONArray("meshes");
		var accessors = glTF.getJSONArray("accessors");
		var bufferViews = glTF.getJSONArray("bufferViews");
		var JSONbuffers = glTF.getJSONArray("buffers");
		
		ByteBuffer[] buffers = new ByteBuffer[JSONbuffers.length()];
		for (int i=0; i<JSONbuffers.length(); i++) {
			byte[] bytes;
			try {
				var uri = JSONbuffers.getJSONObject(i).getString("uri");
				var fi = new File(f.getParent()+File.separator+uri);
				bytes = new byte[(int) fi.length()];
				var fis2 = new FileInputStream(fi);
				fis2.read(bytes);
				fis2.close();
			} catch (@SuppressWarnings("unused") JSONException e) {
				var binLength = getNextInt(fis);
				getNextInt(fis); //BIN  magic
				
				bytes = new byte[binLength];
				fis.read(bytes);
			}
			buffers[i] = ByteBuffer.wrap(bytes);
			buffers[i].order(ByteOrder.LITTLE_ENDIAN);
		}
		fis.close();
		fis = null;
		JSONbuffers = null;
		
		for (int i=0; i<scene.getJSONArray("nodes").length(); i++){
			var node = nodes.getJSONObject(scene.getJSONArray("nodes").getInt(i));
			if (!node.getString("name").startsWith("_") && !node.getString("name").endsWith(LOD_TRIANGLES_SUFFIX)) {
				Part curPart = new Part(geom, node.getString("name"));

				meshes.getJSONObject(node.getInt("mesh")).getJSONArray("primitives").forEach(prim -> {
					JSONObject primitive = (JSONObject)prim;
					Material curMat = null;
					for (var m : geom.materials) if (m.uniqueName.equals(materials.getJSONObject(primitive.getInt("material")).getString("name"))) {
        				curMat = new Material(m); //copies the material the mesh uses to make it part specific
        				curMat.createVerticesBlock(geom.platform);
        				curPart.mesh.materials.materials.add(curMat);
        			}
					if (curMat == null) {
	        			// if no material data is found give a warning and create a fallback material
	        			System.out.println("[glTFLoader] Warning : material "+materials.getJSONObject(primitive.getInt("material")).getString("name")+" not found in config !");
	        			curMat = Material.getFallbackMaterial(geom);
	    				curMat.createVerticesBlock(geom.platform);
	    				curPart.mesh.materials.materials.add(curMat);
					}
					
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "POSITION");
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "NORMAL");
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "TEXCOORD_0");
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "TEXCOORD_1");
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "TEXCOORD_2");
					accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "TEXCOORD_3");
					if (geom.IMPORT_importVertexColors) accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "COLOR_0");

					accessData(accessors, bufferViews, buffers, curMat, "indices", primitive.getInt("indices"));
				});
				
				for (int j=0; j<scene.getJSONArray("nodes").length(); j++) {
					var node2 = nodes.getJSONObject(scene.getJSONArray("nodes").getInt(j));
					if (node2.getString("name").equals(node.getString("name")+LOD_TRIANGLES_SUFFIX)) {
						System.out.println("Linking low LOD road triangles for part "+node.getString("name"));
						
						meshes.getJSONObject(node2.getInt("mesh")).getJSONArray("primitives").forEach(prim -> {
							JSONObject primitive = (JSONObject)prim;
							Material curMat = null;
							for (var m : curPart.mesh.materials.materials) if (m.uniqueName.equals(materials.getJSONObject(primitive.getInt("material")).getString("name"))) {
								
								System.out.println("Linking material "+m.generateName());
								
		        				curMat = new Material(m); //copies the material the mesh uses to make it part specific
		        				curMat.createVerticesBlock(geom.platform);
		        				
								accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "POSITION");
								accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "NORMAL");
								accessAttribute(accessors, bufferViews, buffers, primitive, curMat, "TEXCOORD_0");
								accessData(accessors, bufferViews, buffers, curMat, "indices", primitive.getInt("indices"));

								//loop on triangles and find the correct vertices
								for (var t : curMat.triangles) {
									int i0 = -1, i1 = -1, i2 = -1;
									for (var v : m.verticesBlock.vertices) {
										if (v.positionNormalEquals(curMat.verticesBlock.vertices.get(t.vert0))) {
											i0 = m.verticesBlock.vertices.indexOf(v);
											break;
										}
									}
									for (var v : m.verticesBlock.vertices) {
										if (v.positionNormalEquals(curMat.verticesBlock.vertices.get(t.vert1))) {
											i1 = m.verticesBlock.vertices.indexOf(v);
											break;
										}
									}
									for (var v : m.verticesBlock.vertices) {
										if (v.positionNormalEquals(curMat.verticesBlock.vertices.get(t.vert2))) {
											i2 = m.verticesBlock.vertices.indexOf(v);
											break;
										}
									}
									if (i1 == -1 || i2 == -1 || i0 == -1) System.out.println("problet");
									m.trianglesExtra.add(new Triangle(i0, i1, i2));
								}
		        			}
						});
					}
				}
				
				
				
			} else if (node.getString("name").startsWith("_")) {
				MPoint curMarker = null;
    			for (var mp : geom.mpointsAll) if (mp.uniqueName.equals(node.getString("name"))) {
    				curMarker = mp;
        		}
    			if (curMarker == null) {
        			System.out.println("[glTFLoader] Marker "+node.getString("name")+" not found in config !");
        			curMarker = new MPoint();
    			}
    			final var marker = curMarker;

    			meshes.getJSONObject(node.getInt("mesh")).getJSONArray("primitives").forEach(prim -> {
					JSONObject primitive = (JSONObject)prim;
					
					accessorID = primitive.getJSONObject("attributes").getInt("POSITION");
					bufferViewID = accessors.getJSONObject(accessorID).getInt("bufferView");

					var in = buffers[bufferViews.getJSONObject(bufferViewID).getInt("buffer")];
					var offset = bufferViews.getJSONObject(bufferViewID).getInt("byteOffset");
					var length = bufferViews.getJSONObject(bufferViewID).getInt("byteLength");
					
					in.position(offset);
					while (in.position() < offset + length) {
						var y = -in.getFloat();
						var z = in.getFloat();
						var x = -in.getFloat();
						VertexData3 v = new VertexData3(x, y, z);
//						if (!marker.verts.contains(v)) 
							marker.verts.add(v);
					}
				});
				
				
				
			}

		}
		
        System.out.println("glTF read in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
        
		geom.rebuild();
        
        System.out.println("Post-treatment done in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
	}



	private static int getNextInt(FileInputStream fis) throws IOException {
		return fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
	}



	@SuppressWarnings("hiding")
	private static void accessAttribute(JSONArray accessors, JSONArray bufferViews, ByteBuffer[] buffers,
			JSONObject primitive, Material curMat, String type) {
		try {
			var accessorID = primitive.getJSONObject("attributes").getInt(type);
			accessData(accessors, bufferViews, buffers, curMat, type, accessorID);
		} catch (@SuppressWarnings("unused") JSONException e) {
			//expected
		}
	}



	@SuppressWarnings("hiding")
	private static void accessData(JSONArray accessors, JSONArray bufferViews, ByteBuffer[] buffers, Material curMat,
			String type, int accessorID) {
		var bufferViewID = accessors.getJSONObject(accessorID).getInt("bufferView");
		var componentType = ComponentType.get(accessors.getJSONObject(accessorID).getInt("componentType"));
		
		var in = buffers[bufferViews.getJSONObject(bufferViewID).getInt("buffer")];
		var offset = bufferViews.getJSONObject(bufferViewID).getInt("byteOffset");
		var length = bufferViews.getJSONObject(bufferViewID).getInt("byteLength");
		
		in.position(offset);
		int vIndex=0;
		//TODO no auto welding here!
		switch(type) {
		case "POSITION":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = new Vertex();
				curMat.verticesBlock.vertices.add(v);
				v.pos[Y] = -in.getFloat();
				v.pos[Z] = in.getFloat();
				v.pos[X] = -in.getFloat();
			}
			break;
		case "NORMAL":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = curMat.verticesBlock.vertices.get(vIndex);
				v.norm[Y] = -in.getFloat();
				v.norm[Z] = in.getFloat();
				v.norm[X] = -in.getFloat();
				vIndex++;
			}
			break;
		case "TEXCOORD_0":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = curMat.verticesBlock.vertices.get(vIndex);
				v.tex[0][U] = in.getFloat();
				v.tex[0][V] = (1-in.getFloat()); //flip V
				vIndex++;
			}
			break;
		case "TEXCOORD_1":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = curMat.verticesBlock.vertices.get(vIndex);
				v.tex[1][U] = in.getFloat();
				v.tex[1][V] = (1-in.getFloat()); //flip V
				vIndex++;
			}
			break;
		case "TEXCOORD_2":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = curMat.verticesBlock.vertices.get(vIndex);
				v.tex[2][U] = in.getFloat();
				v.tex[2][V] = (1-in.getFloat()); //flip V
				vIndex++;
			}
			break;
		case "TEXCOORD_3":
			if (componentType == ComponentType.FLOAT) while (in.position() < offset + length) {
				Vertex v = curMat.verticesBlock.vertices.get(vIndex);
				v.tex[3][U] = in.getFloat();
				v.tex[3][V] = (1-in.getFloat()); //flip V
				vIndex++;
			}
			break;
		case "COLOR_0":
			if (componentType == ComponentType.UNSIGNED_BYTE) {
				while (in.position() < offset + length) {
					Vertex v = curMat.verticesBlock.vertices.get(vIndex);
					v.color[R] = in.get()/255.0f;
					v.color[G] = in.get()/255.0f;
					v.color[B] = in.get()/255.0f;
					v.color[A] = in.get()/255.0f;
					vIndex++;
				}				
			} else if (componentType == ComponentType.UNSIGNED_SHORT) {
				while (in.position() < offset + length) {
					Vertex v = curMat.verticesBlock.vertices.get(vIndex);
					v.color[R] = in.getShort()/65535.0f;
					v.color[G] = in.getShort()/65535.0f;
					v.color[B] = in.getShort()/65535.0f;
					v.color[A] = in.getShort()/65535.0f;
					vIndex++;
				}
			} else if (componentType == ComponentType.FLOAT) {
				while (in.position() < offset + length) {
					Vertex v = curMat.verticesBlock.vertices.get(vIndex);
					v.color[R] = in.getFloat();
					v.color[G] = in.getFloat();
					v.color[B] = in.getFloat();
					v.color[A] = in.getFloat();
					vIndex++;
				}
			}
			break;
			
		case "indices":
			if (componentType == ComponentType.UNSIGNED_SHORT) while (in.position() < offset + length) {
				curMat.triangles.add(new Triangle(
						Short.toUnsignedInt(in.getShort()),
						Short.toUnsignedInt(in.getShort()),
						Short.toUnsignedInt(in.getShort())
						));
			}

			break;
		}
	}

	static int accessorID;
	static int bufferViewID;
	

	public static void save(Geometry geom, String f, boolean exportGlb) throws IOException {
		
		//TODO glb export broken
		
		var glTF = new JSONObject();
		var asset = new JSONObject();
		asset.put("generator", "Undercover Geometry Tool v"+GeometryEditorCLI.programVersion+" - needeka 2026");
		asset.put("version", "2.0");
		glTF.put("asset", asset);

		glTF.append("extensionsUsed", "KHR_materials_specular");
//		glTF.append("extensionsUsed", "KHR_materials_ior");
		
		glTF.put("scene", 0);
		var scene = new JSONObject();
		scene.put("name", geom.carname);
		scene.append("nodes", null);
		scene.getJSONArray("nodes").clear();
//		for (int i=0; i<geom.parts.size() + geom.mpointsPositions.size(); i++) scene.append("nodes", i); //moved further
		glTF.append("scenes", scene);
				
//		for (var p : geom.parts) { //moved further
//			var node = new JSONObject();
//			node.put("mesh", geom.parts.indexOf(p));
//			node.put("name", p.name);
//			glTF.append("nodes", node);
//		}
//		
		var tempTexStorage = new ArrayList<Integer>();
		
		for (var m : geom.materials) {
			var material = new JSONObject();
			material.put("name", m.uniqueName);
			material.put("doubleSided", false);
			alphasearch: {
				for (var n : m.shaderUsage.possibleNames) if (n.contains("Alpha")) {
					material.put("alphaMode", "BLENDED");
					break alphasearch;
				}
				for (var t : m.textureUsages) if (t == TextureUsage.OPACITY) {
					material.put("alphaMode", "BLENDED");
					break alphasearch;
				}
			}
			var pbr = new JSONObject();
			JSONObject baseColorTexture;
			
			if (m.textureUsages.contains(TextureUsage.DIFFUSE)) {
				baseColorTexture = new JSONObject();
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.DIFFUSE))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.DIFFUSE)));
				baseColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.DIFFUSE))));
				pbr.put("baseColorTexture", baseColorTexture);
			}
			
			if (m.textureUsages.contains(TextureUsage.ROADELEMENTS)) {
				baseColorTexture = new JSONObject();
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADELEMENTS))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADELEMENTS)));
				baseColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADELEMENTS))));
				pbr.put("baseColorTexture", baseColorTexture);
			}
			
			if (m.textureUsages.contains(TextureUsage.ROADBASE)) {
				baseColorTexture = new JSONObject();
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADBASE))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADBASE)));
				baseColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ROADBASE))));
				baseColorTexture.put("texCoord", 1);
				pbr.put("baseColorTexture", baseColorTexture);
			}
			
			/*
			 * TODO
			 * roadelements uv map 0
			 * roadbase uv map 1
			 * ...
			 */
			
			
			pbr.put("metallicFactor", 0);
			pbr.put("roughnessFactor", 0);
			material.put("pbrMetallicRoughness",pbr);
			
			if (m.textureUsages.contains(TextureUsage.NORMALMAP)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMALMAP))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMALMAP)));
				var normalTexture = new JSONObject();
				normalTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMALMAP))));
				material.put("normalTexture",normalTexture);
			}
			if (m.textureUsages.contains(TextureUsage.NORMAL)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMAL))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMAL)));
				var normalTexture = new JSONObject();
				normalTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMAL))));
				material.put("normalTexture",normalTexture);
			}
			if (m.textureUsages.contains(TextureUsage.ELEMENTSNORMAL)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL)));
				var normalTexture = new JSONObject();
				normalTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ELEMENTSNORMAL))));
				material.put("normalTexture",normalTexture);
			}
			if (m.textureUsages.contains(TextureUsage.BASENORMAL)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASENORMAL))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASENORMAL)));
				var normalTexture = new JSONObject();
				normalTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASENORMAL))));
				normalTexture.put("texCoord", 1);
				material.put("normalTexture",normalTexture);
			}
			
			if (m.textureUsages.contains(TextureUsage.SELFILLUMINATION)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SELFILLUMINATION))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SELFILLUMINATION)));
				var emissiveTexture = new JSONObject();
				emissiveTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SELFILLUMINATION))));
				material.put("emissiveFactor", new int[]{1,1,1});
				material.put("emissiveTexture",emissiveTexture);
			}
			if (m.textureUsages.contains(TextureUsage.ILLUMINATE)) {
				if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ILLUMINATE))))
					tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ILLUMINATE)));
				var emissiveTexture = new JSONObject();
				emissiveTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.ILLUMINATE))));
				material.put("emissiveFactor", new int[]{1,1,1});
				material.put("emissiveTexture",emissiveTexture);
			}


			if (m.textureUsages.contains(TextureUsage.SPECULAR) || m.textureUsages.contains(TextureUsage.BASESPEC) || m.textureUsages.contains(TextureUsage.REFLECTION)) {
				var extensions = new JSONObject();
				var specular = new JSONObject();
				extensions.put("KHR_materials_specular", specular);
				
				if (m.textureUsages.contains(TextureUsage.SPECULAR)) {
					if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SPECULAR))))
						tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SPECULAR)));
					var specularColorTexture = new JSONObject();
					specularColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SPECULAR))));
					specular.put("specularColorTexture",specularColorTexture);
				}
				if (m.textureUsages.contains(TextureUsage.BASESPEC)) {
					if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASESPEC))))
						tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASESPEC)));
					var specularColorTexture = new JSONObject();
					specularColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.BASESPEC))));
					specularColorTexture.put("texCoord", 1);
					specular.put("specularColorTexture",specularColorTexture);
				}
				if (m.textureUsages.contains(TextureUsage.REFLECTION)) {
					if (!tempTexStorage.contains(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.REFLECTION))))
						tempTexStorage.add(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.REFLECTION)));
					var specularColorTexture = new JSONObject();
					specularColorTexture.put("index", tempTexStorage.indexOf(m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.REFLECTION))));
					specular.put("specularTexture",specularColorTexture);
				}
				material.put("extensions",extensions);
			}

			
			
			glTF.append("materials", material);
		}
		
		for (int i=0; i<tempTexStorage.size(); i++) {
			var texture = new JSONObject();
//			texture.put("sampler",0); //TODO sampler necessary ?
			texture.put("source",i);
			glTF.append("textures", texture);
			
			texture = new JSONObject();
			texture.put("uri", Hash.getBIN(tempTexStorage.get(i)).replace(geom.carname, "%")+".dds");
			glTF.append("images", texture);
			
		}
		
		accessorID = -1;
		bufferViewID = -1;
		
		var bos = new ByteArrayOutputStream();
		
		
		var partsWithExtraTriangles = new ArrayList<Part>();
		
		for (var p : geom.parts) {
			scene.append("nodes", scene.getJSONArray("nodes").length());
			
			var node = new JSONObject();
			node.put("mesh", geom.parts.indexOf(p));
			node.put("name", p.name);
			glTF.append("nodes", node);
			
			var mesh = new JSONObject();
			mesh.put("name", p.name);
			
			for	(var m : p.mesh.materials.materials) {
				if (m.trianglesExtra.size() != 0 && !partsWithExtraTriangles.contains(p)) partsWithExtraTriangles.add(p);
				var primitive = new JSONObject();
				var attributes = new JSONObject();
				
				exportPosition(glTF, bos, m, attributes);
				if (m.shaderUsage.vertexFormat_PC.hasNormals()) exportNormals(glTF, bos, m, attributes);
				for (int i=0; i<m.shaderUsage.vertexFormat_PC.getNumTexChannels(); i++) exportTexcoords(glTF, bos, m, attributes, i);				
				if (m.shaderUsage.vertexFormat_PC.hasColor()) exportColors(glTF, bos, m, attributes);
				primitive.put("attributes", attributes);
				
				exportTriangles(glTF, bos, m.triangles, primitive);

				primitive.put("material", geom.materials.indexOf(m));
				mesh.append("primitives", primitive);
			}
			glTF.append("meshes", mesh);
		}
		
		
//		int i=0;
		for (var mpc : geom.mpointsPositions) {
			scene.append("nodes", scene.getJSONArray("nodes").length());
        	final float s = 0.05f; //cube half size 
        	
        	var mp = mpc.mpoints.get(0);
        	
			var node = new JSONObject();
//			node.put("mesh", geom.parts.size() + i);
			node.put("mesh", geom.parts.size() + geom.mpointsPositions.indexOf(mpc));
			node.put("name", mp.uniqueName);
//			i++;
			glTF.append("nodes", node);
			
			var mesh = new JSONObject();
			mesh.put("name", mp.uniqueName);
			
			var primitive = new JSONObject();
			var attributes = new JSONObject();
			
			makeAccessor(glTF, bos.size(), 8*3*4, Target.ARRAY_BUFFER, ComponentType.FLOAT, 8, "VEC3");
			attributes.put("POSITION", accessorID);
			bos.write(makeMPointCubePositions(s, mp));
			primitive.put("attributes", attributes);
			
			makeAccessor(glTF, bos.size(), 12*2*3, Target.ELEMENT_ARRAY_BUFFER, ComponentType.UNSIGNED_SHORT, 12*3, "SCALAR");
			bos.write(makeCubeIndices());

			primitive.put("indices", accessorID);
			mesh.append("primitives", primitive);
			glTF.append("meshes", mesh);
        }

		
		

		for (var p : partsWithExtraTriangles) {
			scene.append("nodes", scene.getJSONArray("nodes").length());
			
			var node = new JSONObject();
			node.put("mesh", geom.parts.size() + geom.mpointsPositions.size() + partsWithExtraTriangles.indexOf(p));
			node.put("name", p.name+LOD_TRIANGLES_SUFFIX);
			glTF.append("nodes", node);

			var mesh = new JSONObject();
			mesh.put("name", p.name+LOD_TRIANGLES_SUFFIX);
			
			for	(var m : p.mesh.materials.materials) if (m.trianglesExtra.size() != 0) {
				var primitive = new JSONObject();
//				var attributes = new JSONObject();
				
//				exportPosition(glTF, bos, m, attributes);
//				if (m.shaderUsage.vertexFormat_PC.hasNormals()) exportNormals(glTF, bos, m, attributes);
//				for (int i=0; i<m.shaderUsage.vertexFormat_PC.getNumTexChannels(); i++) exportTexcoords(glTF, bos, m, attributes, i);				
//				if (m.shaderUsage.vertexFormat_PC.hasColor()) exportColors(glTF, bos, m, attributes);
				
//				attributes.put("POSITION", );
				
//				primitive.put("attributes", attributes);
				primitive.put("attributes", glTF.getJSONArray("meshes").getJSONObject(geom.parts.indexOf(p)).getJSONArray("primitives").getJSONObject(p.mesh.materials.materials.indexOf(m)).getJSONObject("attributes"));
				
				exportTriangles(glTF, bos, m.trianglesExtra, primitive);

				primitive.put("material", geom.materials.indexOf(m));
				mesh.append("primitives", primitive);
			}
			glTF.append("meshes", mesh);
		}
		
		
		
		
		
		
		
		

		var buffer = new JSONObject();
		buffer.put("byteLength", bos.size());
		if (!exportGlb) buffer.put("uri", new File(f).getName().replace("."+ new File(f).getName().split("\\.")[new File(f).getName().split("\\.").length-1], "-data.bin"));
		glTF.append("buffers", buffer);
		
		if (!exportGlb) {
			var bw = new BufferedWriter(new FileWriter(new File(f)));
			bw.write(glTF.toString());
			bw.close();
			
			var fos = new FileOutputStream(new File(new File(f).getParent() + File.separator + new File(f).getName().replace("."+ new File(f).getName().split("\\.")[new File(f).getName().split("\\.").length-1], "-data.bin")));
			bos.writeTo(fos);
			fos.close();
			
		} else {
			var json = glTF.toString().getBytes(StandardCharsets.UTF_8);
			int suppl = Block.findAlignment(json.length, 4);
			
			var bb = ByteBuffer.allocate(20);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(glTF_BINARY);
			bb.putInt(2);
			bb.putInt(20 + json.length + suppl + 8 + bos.size()); //total file length
			bb.putInt(json.length + suppl);
			bb.putInt(JSON);
			
			var fos = new FileOutputStream(new File(f));
			fos.write(bb.array());
			fos.write(json);
			for (int i=0; i<suppl; i++) fos.write(0x20);
			bb = ByteBuffer.allocate(8);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(BIN);
			bb.putInt(bos.size());
			fos.write(bb.array());
			bos.writeTo(fos);
			fos.close();

			
		}

		
	}



	private static byte[] makeCubeIndices() {
		var bb = ByteBuffer.allocate(12*2*3);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		bb.putShort((short) 0);	bb.putShort((short) 1);	bb.putShort((short) 2);
		bb.putShort((short) 1);	bb.putShort((short) 2);	bb.putShort((short) 3);
		
		bb.putShort((short) 4);	bb.putShort((short) 5);	bb.putShort((short) 6);
		bb.putShort((short) 5);	bb.putShort((short) 6);	bb.putShort((short) 7);

		bb.putShort((short) 0);	bb.putShort((short) 1);	bb.putShort((short) 4);
		bb.putShort((short) 1);	bb.putShort((short) 4);	bb.putShort((short) 5);
		
		bb.putShort((short) 2);	bb.putShort((short) 3);	bb.putShort((short) 6);
		bb.putShort((short) 3);	bb.putShort((short) 6);	bb.putShort((short) 7);

		bb.putShort((short) 0);	bb.putShort((short) 2);	bb.putShort((short) 4);
		bb.putShort((short) 2);	bb.putShort((short) 4);	bb.putShort((short) 6);

		bb.putShort((short) 1);	bb.putShort((short) 3);	bb.putShort((short) 5);
		bb.putShort((short) 3);	bb.putShort((short) 5);	bb.putShort((short) 7);
		return bb.array();
	}



	private static byte[] makeMPointCubePositions(final float s, MPoint mp) {
		var bb = ByteBuffer.allocate(8*3*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		bb.putFloat(-mp.positionY-s);	bb.putFloat(mp.positionZ+s);	bb.putFloat(-mp.positionX+s);
		bb.putFloat(-mp.positionY-s);	bb.putFloat(mp.positionZ+s);	bb.putFloat(-mp.positionX-s);
		bb.putFloat(-mp.positionY-s);	bb.putFloat(mp.positionZ-s);	bb.putFloat(-mp.positionX+s);
		bb.putFloat(-mp.positionY-s);	bb.putFloat(mp.positionZ-s);	bb.putFloat(-mp.positionX-s);
		bb.putFloat(-mp.positionY+s);	bb.putFloat(mp.positionZ+s);	bb.putFloat(-mp.positionX+s);
		bb.putFloat(-mp.positionY+s);	bb.putFloat(mp.positionZ+s);	bb.putFloat(-mp.positionX-s);
		bb.putFloat(-mp.positionY+s);	bb.putFloat(mp.positionZ-s);	bb.putFloat(-mp.positionX+s);
		bb.putFloat(-mp.positionY+s);	bb.putFloat(mp.positionZ-s);	bb.putFloat(-mp.positionX-s);
		return bb.array();
	}



	private static void exportTriangles(JSONObject glTF, ByteArrayOutputStream bos, List<Triangle> triangles, JSONObject primitive) throws IOException {
		var bb = ByteBuffer.allocate(triangles.size()*2*3);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (var t : triangles) {
			bb.putShort((short) t.vert0);
			bb.putShort((short) t.vert1);
			bb.putShort((short) t.vert2);
		}
		makeAccessor(glTF, bos.size(), triangles.size()*2*3, Target.ELEMENT_ARRAY_BUFFER, ComponentType.UNSIGNED_SHORT, triangles.size()*3, "SCALAR");
		primitive.put("indices", accessorID);
		bos.write(bb.array());
	}



	private static void exportColors(JSONObject glTF, ByteArrayOutputStream bos, Material m, JSONObject attributes)
			throws IOException {
		var bb = ByteBuffer.allocate(m.verticesBlock.vertices.size()*4*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (var v : m.verticesBlock.vertices) {
			bb.putFloat(v.color[R]);
			bb.putFloat(v.color[G]);
			bb.putFloat(v.color[B]);
			bb.putFloat(v.color[A]);
		}
		makeAccessor(glTF, bos.size(), m.verticesBlock.vertices.size()*4*4, Target.ARRAY_BUFFER, ComponentType.FLOAT, m.verticesBlock.vertices.size(), "VEC4");
		attributes.put("COLOR_0", accessorID);
		bos.write(bb.array());
	}



	private static void exportTexcoords(JSONObject glTF, ByteArrayOutputStream bos, Material m, JSONObject attributes,
			int i) throws IOException {
		var bb = ByteBuffer.allocate(m.verticesBlock.vertices.size()*2*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (var v : m.verticesBlock.vertices) {
			bb.putFloat(v.tex[i][U]);
			bb.putFloat(1-v.tex[i][V]);
		}
		makeAccessor(glTF, bos.size(), m.verticesBlock.vertices.size()*2*4, Target.ARRAY_BUFFER, ComponentType.FLOAT, m.verticesBlock.vertices.size(), "VEC2");
		attributes.put("TEXCOORD_"+i, accessorID);
		bos.write(bb.array());
	}



	private static void exportNormals(JSONObject glTF, ByteArrayOutputStream bos, Material m, JSONObject attributes)
			throws IOException {
		var bb = ByteBuffer.allocate(m.verticesBlock.vertices.size()*3*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (var v : m.verticesBlock.vertices) {
			bb.putFloat(-v.norm[Y]);
			bb.putFloat(v.norm[Z]);
			bb.putFloat(-v.norm[X]);
		}
		makeAccessor(glTF, bos.size(), m.verticesBlock.vertices.size()*3*4, Target.ARRAY_BUFFER, ComponentType.FLOAT, m.verticesBlock.vertices.size(), "VEC3");
		attributes.put("NORMAL", accessorID);
		bos.write(bb.array());
	}



	private static void exportPosition(JSONObject glTF, ByteArrayOutputStream bos, Material m, JSONObject attributes)
			throws IOException {
		var bb = ByteBuffer.allocate(m.verticesBlock.vertices.size()*3*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		for (var v : m.verticesBlock.vertices) {
			bb.putFloat(-v.pos[Y]);
			bb.putFloat(v.pos[Z]);
			bb.putFloat(-v.pos[X]);
		}
		makeAccessor(glTF, bos.size(), m.verticesBlock.vertices.size()*3*4, Target.ARRAY_BUFFER, ComponentType.FLOAT, m.verticesBlock.vertices.size(), "VEC3");
		attributes.put("POSITION", accessorID);
		bos.write(bb.array());
	}



	private static void makeAccessor(JSONObject glTF, int offset, int size, Target target, ComponentType componentType,
			int count, String type) {
		var bufferView = new JSONObject();
		bufferViewID++;
		bufferView.put("buffer", 0);
		bufferView.put("byteLength", size);
		bufferView.put("byteOffset", offset);
		bufferView.put("target", target.value);
		glTF.append("bufferViews", bufferView);
		
		
		var accessor = new JSONObject();
		accessorID++;
		accessor.put("bufferView", bufferViewID);
		accessor.put("componentType", componentType.value);
		accessor.put("count", count);
		accessor.put("type", type);
		glTF.append("accessors", accessor);
	}
}

enum ComponentType{
	UNSIGNED_BYTE(5121),
	UNSIGNED_SHORT(5123),
	FLOAT(5126),
	INVALID(-1);
	
	int value;

	ComponentType(int i) {
		this.value = i;
	}
	
	static ComponentType get(int i) {
		for (var v : values()) if (v.value == i) return v;
		return INVALID;
	}
}

enum Target{
	ARRAY_BUFFER(34962),
	ELEMENT_ARRAY_BUFFER(34963),
	INVALID(-1);
	
	int value;

	Target(int i) {
		this.value = i;
	}
	
	static Target get(int i) {
		for (var v : values()) if (v.value == i) return v;
		return INVALID;
	}
}