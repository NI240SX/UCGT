package fr.ni240sx.ucgt.geometryFile.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.MPoints;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;
import fr.ni240sx.ucgt.geometryFile.settings.SettingsImport_Tangents;
import fr.ni240sx.ucgt.geometryFile.sorters.MaterialsSorterName;
import javafx.util.Pair;

public class WavefrontOBJ {
	
	public static void load(Geometry geom, File f) throws IOException {
//		boolean verticesPerPart = false;
		
		BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean nextIsMarker = false;
        Part curPart = null;
        MPoint curMarker = null;
        Material curMat = Material.getFallbackMaterial();
        ArrayList<VertexData3> verts = new ArrayList<VertexData3>(); //WARNING obj index = list index + 1
        ArrayList<VertexData3> normals = new ArrayList<VertexData3>(); //WARNING obj index = list index + 1
        ArrayList<TexcoordData2> tex = new ArrayList<TexcoordData2>(); //WARNING obj index = list index + 1
        
		long time = System.currentTimeMillis();
		
        while ((line = br.readLine()) != null) {
        	lineheader: if (line.startsWith("o ") || line.startsWith("g ")) {

        		if (!line.substring(2).startsWith("_")) {
        			// part
        			nextIsMarker = false;
	        		for (var p : geom.parts) if ((p.kit+"_"+p.part+"_"+p.lod).equals(line.substring(2))) {
	        			curPart = p;
	        			break lineheader;
	        		}
	        		
	        		// initialize a part that's not already in the geometry
	        		curPart = new Part(geom.carname, line.substring(2));
	        		geom.parts.add(curPart);
	
	        		for (var mp : geom.mpointsAll) if (mp.tempPartNames.contains(line.substring(2))) { // binding mpoints read from config, if existing
	    				if (curPart.mpoints == null) curPart.mpoints = new MPoints();
	    				curPart.mpoints.mpoints.add(mp);
//	    				mp.part = curPart;
//	    				mp.parts.add(curPart);
	        		}
	        		
	        		for (var asl : geom.asLinking) if (asl.tempPartName.equals(line.substring(2))) { // binding autosculpt linking data read from ini
	        			curPart.asLinking = asl;
	        			break;
	        		}
        		} else {
        			// marker
        			nextIsMarker = true;
        			for (var mp : geom.mpointsAll) if (mp.uniqueName.equals(line.substring(2))) {
	    				curMarker = mp;
	    				break lineheader;
	        		}
        			System.out.println("[OBJLoader] Marker "+line.substring(2)+" not found in config !");
        			curMarker = new MPoint();
        		}
        	}
        	else if (line.startsWith("v ")) {
        		// vertex
        		if (line.split(" ").length < 4) System.out.println("[OBJLoader] Wrong vertex data");
        		
        		if (Geometry.IMPORT_importVertexColors && line.split(" ").length >= 7) {
        			// vertex with vertex colors
        			verts.add(new VertexData6(line.split(" ")[1], line.split(" ")[2], line.split(" ")[3], line.split(" ")[4], line.split(" ")[5], line.split(" ")[6]));
        		} else {
        			// vertex without vertex colors
        			verts.add(new VertexData3(line.split(" ")[1], line.split(" ")[2], line.split(" ")[3]));	
        		}
        		
        		//assuming vertices are declared before vertex normals and uv
        		
        	} else if (line.startsWith("vn ")) {
        		// normal
        		if (line.split(" ").length < 4) System.out.println("[OBJLoader] Wrong normal data");
        		normals.add(new VertexData3(line.split(" ")[1], line.split(" ")[2], line.split(" ")[3]));
        		        		
        	} else if (line.startsWith("vt")) {
        		// texcoord
        		if (line.split(" ").length < 3) System.out.println("[OBJLoader] Wrong texcoord data");
        		tex.add(new TexcoordData2(line.split(" ")[1], line.split(" ")[2]));
        		
        	} else if (line.startsWith("usemtl")) {
        		if (!nextIsMarker) {
//        			if (curMat != null) vertoffset += curMat.verticesBlock.vertices.size(); //offsets vertex ids in the next materials
        			
        			for (var m : geom.materials) if (m.uniqueName.equals(line.substring(7))) {
        				curMat = new Material(m); //copies the material the mesh uses to make it part specific
        				curMat.verticesBlock = new Vertices();
        				curPart.mesh.materials.materials.add(curMat);
        				break lineheader;
        			}
        			// if no material data is found give a warning and create a fallback material
        			System.out.println("[OBJLoader] Warning : material "+line.substring(7)+" not found in config !");
        			curMat = Material.getFallbackMaterial();
    				curMat.verticesBlock = new Vertices();
    				curPart.mesh.materials.materials.add(curMat);
        			
        		} //no material data to process for markers
        	}
        	else if (line.startsWith("f ")) {
        		// face
	        		
        		if (!nextIsMarker) {
        			//part
        			Vertex[] triVerts = new Vertex[3];
        			for (int i=1; i<4; i++) {
	        			String vertdata = line.split(" ")[i];
	        			Vertex v = new Vertex();
	        			v.posX = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).x;
	        			v.posY = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).y;
	        			v.posZ = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).z;
	        			
	        			if (verts.get(Integer.parseInt(vertdata.split("/")[0])-1).getClass() == VertexData6.class) {
	        				v.colorR = (byte)(int)(((VertexData6)(verts.get(Integer.parseInt(vertdata.split("/")[0])-1))).a*255);
	        				v.colorG = (byte)(int)(((VertexData6)(verts.get(Integer.parseInt(vertdata.split("/")[0])-1))).b*255);
	        				v.colorB = (byte)(int)(((VertexData6)(verts.get(Integer.parseInt(vertdata.split("/")[0])-1))).c*255);
	        			}
	
	        			v.texU = tex.get(Integer.parseInt(vertdata.split("/")[1])-1).u;
	        			if (Geometry.IMPORT_flipV) v.texV = -tex.get(Integer.parseInt(vertdata.split("/")[1])-1).v;
	        			else v.texV = tex.get(Integer.parseInt(vertdata.split("/")[1])-1).v;
	        			
	        			v.normX = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).x;
	        			v.normY = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).y;
	        			v.normZ = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).z;
	        			

	        			if (Geometry.IMPORT_calculateVertexColors) {
	        				// -1 to 1 -> 20 to 255
	        				int color;
	        				if (!curPart.part.contains("WHEEL") && !curPart.part.contains("BRAKE"))
	        					color = Math.max(0, Math.min(255, (int)((v.normZ+0.8)*150)));
	        				else color = Math.max(20, Math.min(255, (int)((-v.normY+0.8)*150)));
	        				v.colorR = (byte) color;
	        				v.colorG = (byte) color;
	        				v.colorB = (byte) color;
	        			}
	        			
	        			if (!curMat.verticesBlock.vertices.contains(v)) 
	        				curMat.verticesBlock.vertices.add(v);
	        			triVerts[i-1] = v;
	        		}
        			Triangle t = new Triangle(	(short)(curMat.verticesBlock.vertices.indexOf(triVerts[0])), 
        										(short)(curMat.verticesBlock.vertices.indexOf(triVerts[1])),
        										(short)(curMat.verticesBlock.vertices.indexOf(triVerts[2])) );
//        			//check there isn't two identical vertices
//        			if (t.vert0 == t.vert1) {
//        				t.vert1 = (short) curMat.verticesBlock.vertices.size();
//        				curMat.verticesBlock.vertices.add(new Vertex(triVerts[0]));
//        			}
//        			if (t.vert0 == t.vert2) {
//        				t.vert2 = (short) curMat.verticesBlock.vertices.size();
//        				curMat.verticesBlock.vertices.add(new Vertex(triVerts[0]));
//        			}
//        			if (t.vert1 == t.vert2) {
//        				t.vert2 = (short) curMat.verticesBlock.vertices.size();
//        				curMat.verticesBlock.vertices.add(new Vertex(triVerts[1]));
//        			}
        			
        			curMat.triangles.add(t);
        			
//        			curMat.triangles.add( new Triangle(	(short)(curMat.verticesBlock.vertices.size()-3), 
//							(short)(curMat.verticesBlock.vertices.size()-2),
//							(short)(curMat.verticesBlock.vertices.size()-1) ) );
        			
        			if (	(Geometry.IMPORT_Tangents == SettingsImport_Tangents.LOW && curMat.needsTangentsLow()) || 
        					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.HIGH && curMat.needsTangentsHigh()) || 
        					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.MANUAL && curMat.useTangents == true) ||
        					Geometry.IMPORT_Tangents == SettingsImport_Tangents.ON ) {
        				addToTangents(triVerts);
        			}
	        			
        		} else {
        			//marker
        			//collect coords, average them later to get the mpoint x y z
        			for (int i=1; i<4; i++) {
	        			String vertdata = line.split(" ")[i];
	        			VertexData3 v = verts.get(Integer.parseInt(vertdata.split("/")[0])-1);
	        			if (!curMarker.verts.contains(v)) curMarker.verts.add(v);
	        		}
        			
        		}
        		
        	}
        }
        br.close();
        
        System.out.println("OBJ read in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
        
//        if (verticesPerPart) {
//    		// compute last part
//        	computePart(curPart, verts, vertoffset, normals, normoffset, tex, texoffset, tris);
//        }
        
        for (var p : geom.parts) {

        	//sort materials (same as in config file) and add the materials data back to the mesh
        	if (Geometry.SAVE_sortEverythingByName) p.mesh.materials.materials.sort(new MaterialsSorterName()); //TODO is this causing issues with autosculpt zones ?
    		p.mesh.verticesBlocks.clear();
    		p.mesh.triangles.triangles.clear();
        	
        	//make hashmaps for textures and shaders
        	
        	//    ID		TEX/SHAD        USAGE
        	HashMap<Integer, Pair<Integer, Integer>> texturesAndUsage = new HashMap<Integer, Pair<Integer, Integer>>();
        	HashMap<Pair<Integer, Integer>, Integer> texturesIDs = new HashMap<Pair<Integer, Integer>, Integer>();
//        	HashMap<Integer, Pair<Integer, Integer>> shadersAndUsage = new HashMap<Integer, Pair<Integer, Integer>>();
        	HashMap<Integer , Integer> shadersOnly = new HashMap<Integer, Integer>();
        	HashMap<Integer , Integer> shadersIDs = new HashMap<Integer, Integer>();

        	List<TextureUsage> allTextureUsages = new ArrayList<TextureUsage>();
        	List<ShaderUsage> allShaderUsages = new ArrayList<ShaderUsage>();
        	
        	int texi = 0;
        	int shai = 0;
        	for (var m : p.mesh.materials.materials) {
        		p.mesh.verticesBlocks.add(m.verticesBlock);
        		p.mesh.triangles.triangles.addAll(m.triangles);
        		
        		
        		if (	(Geometry.IMPORT_Tangents == SettingsImport_Tangents.LOW && m.needsTangentsLow()) || 
    					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.HIGH && m.needsTangentsHigh()) || 
    					(Geometry.IMPORT_Tangents == SettingsImport_Tangents.MANUAL && m.useTangents == true) ||
    					Geometry.IMPORT_Tangents == SettingsImport_Tangents.ON ) {
        			normalizeTangents(m.verticesBlock.vertices);
        		}
        		
//        		if (!shadersAndUsage.containsValue(new Pair<Integer,Integer>(m.ShaderHash.binHash, m.shaderUsage.getKey()))) {
//       			shadersAndUsage.put(shadersAndUsage.size(), new Pair<Integer,Integer>(m.ShaderHash.binHash, m.shaderUsage.getKey()));
//        		}
        		if (!shadersOnly.containsValue(m.ShaderHash.binHash)) {
        			shadersOnly.put(shai, m.ShaderHash.binHash);
        			shadersIDs.put(m.ShaderHash.binHash, shai);
        			shai++;
        		}
        		if (!allShaderUsages.contains(m.shaderUsage)) allShaderUsages.add(m.shaderUsage);

        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			if (!texturesAndUsage.containsValue(new Pair<Integer,Integer>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()))) {
	        				texturesAndUsage.put(texi, new Pair<Integer,Integer>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()));
	        				texturesIDs.put(new Pair<Integer,Integer>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()), texi);
	        				texi++;
            			}
        			if (!allTextureUsages.contains(m.textureUsages.get(i))) allTextureUsages.add(m.textureUsages.get(i));
        		}
        	}
        	
//        	System.out.println(texturesAndUsage);
        	
        	//post-treatment
        	//--- header ---
        	//calculate triangle count
        	//calculate textures count
        	//calculate shaders count
        	//calculate bounds
        	p.header.trianglesCount = p.mesh.triangles.triangles.size();
        	p.header.texturesCount = (short) texturesAndUsage.size();
        	p.header.shadersCount = (short) shadersOnly.size();
        	p.computeBounds();
        	//--- texusage ---
        	//fill in the texusage pairs
        	for (int i=0; i<texturesAndUsage.size(); i++) {
        		p.texusage.texusage.add(texturesAndUsage.get(i));
        	}
        	//--- strings ---
        	//fill in the strings based on mesh shader usages
        	if (allTextureUsages.contains(TextureUsage.DIFFUSE)) p.strings.strings.add("DIFFUSE");
        	if (allTextureUsages.contains(TextureUsage.NORMAL)) p.strings.strings.add("NORMAL");
        	if (allTextureUsages.contains(TextureUsage.SWATCH)) p.strings.strings.add("AMBIENT");
        	if (allTextureUsages.contains(TextureUsage.ALPHA)) p.strings.strings.add("OPACITY");
        	if (allTextureUsages.contains(TextureUsage.SELFILLUMINATION)) p.strings.strings.add("SELFILLUMINATION");
        	//--- shaders ---
        	//fill in the shaders binhashes
        	//TODO does this still work when a shader is used in multiple materials with multiple usages ???
        	for (int i=0; i<shadersOnly.size(); i++) p.shaderlist.shaders.add(shadersOnly.get(i));

        	//=== mesh ===
        	//--- info ---
        	//numMaterials, numTriangles, numVertices
        	p.mesh.info.numMaterials = p.mesh.materials.materials.size();
        	p.mesh.info.numTriangles = p.mesh.triangles.triangles.size();

        	for (var vb : p.mesh.verticesBlocks) p.mesh.info.numVertices += vb.vertices.size();
        	
        	int triVertI = 0;
        	//--- materials ---
        	//for each material :
        	//numVertices, toVertID
        	//shaderID, textureIDs
        	//verticesDataLength
        	for (var m : p.mesh.materials.materials) {
        		m.fromTriVertID = triVertI; //actually concerns triangles
        		m.toTriVertID = m.fromTriVertID + m.triangles.size()*3;
        		m.numTriVertices = m.toTriVertID - m.fromTriVertID;
        		triVertI = m.toTriVertID;
        		int shaderid = shadersIDs.get(m.ShaderHash.binHash);
        		m.shaderID = (byte) shaderid;
        		for (int i=0; i<m.TextureHashes.size(); i++) {
        			int texid = texturesIDs.get(new Pair<Integer,Integer>(m.TextureHashes.get(i).binHash, m.textureUsages.get(i).getKey()));
        			m.textureIDs.add((byte) texid);
        		}
        		m.verticesDataLength = m.verticesBlock.vertices.size()*Vertices.vertexLength;
            }
        	
        	//--- shadersusage ---
        	//fill in the shaders usage
        	for (var s : allShaderUsages) p.mesh.shadersUsage.shadersUsage.add(s.getKey());
        	//--- vertices (several blocks) ---
        	//normally already filled in
        	//--- triangles (one block) ---
        	//normally already filled in
        	//--- autosculpt linking ---
        	//already filled in
        	//--- autosculpt zones ---
        	//recalculated at export time
        	
        	p.rebuildSubBlocks();
        }
        
        for (var mp : geom.mpointsAll) {

        	for (var mp2 : geom.mpointsAll) if (mp2 != mp && mp2.uniqueName.equals(mp.uniqueName) && mp2.verts.size()>0) {
        		mp.verts = mp2.verts;
        	}
        	
        	//calculate mpoint coords
        	VertexData3 avgPos = new VertexData3(0,0,0);
        	for (var v : mp.verts) {
        		avgPos.x += v.x;
        		avgPos.y += v.y;
        		avgPos.z += v.z;
        	}
        	mp.positionX = (float) (avgPos.x / mp.verts.size());
        	mp.positionY = (float) (avgPos.y / mp.verts.size());
        	mp.positionZ = (float) (avgPos.z / mp.verts.size());
        	
        	
        	if (Float.isNaN(mp.positionX)) System.out.println("NaN position for "+mp.uniqueName);
        }
//        geom.mpointsAll.clear();
//        for (var p : geom.parts) geom.globalizePartMarkers(p);
//        geom.computeMarkersList();
        Geometry.IMPORT_flipV = false; //reset
        
        System.out.println("Post-treatment done in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
	}

private static void addToTangents(Vertex[] v) {
    v[0].tanX += ((v[2].texV-v[0].texV) * (v[1].posX-v[0].posX) - (v[1].texV-v[0].texV) * (v[2].posX-v[0].posX)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[0].tanY += ((v[2].texV-v[0].texV) * (v[1].posY-v[0].posY) - (v[1].texV-v[0].texV) * (v[2].posY-v[0].posY)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[0].tanZ += ((v[2].texV-v[0].texV) * (v[1].posZ-v[0].posZ) - (v[1].texV-v[0].texV) * (v[2].posZ-v[0].posZ)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[1].tanX += ((v[2].texV-v[0].texV) * (v[1].posX-v[0].posX) - (v[1].texV-v[0].texV) * (v[2].posX-v[0].posX)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[1].tanY += ((v[2].texV-v[0].texV) * (v[1].posY-v[0].posY) - (v[1].texV-v[0].texV) * (v[2].posY-v[0].posY)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[1].tanZ += ((v[2].texV-v[0].texV) * (v[1].posZ-v[0].posZ) - (v[1].texV-v[0].texV) * (v[2].posZ-v[0].posZ)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[2].tanX += ((v[2].texV-v[0].texV) * (v[1].posX-v[0].posX) - (v[1].texV-v[0].texV) * (v[2].posX-v[0].posX)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[2].tanY += ((v[2].texV-v[0].texV) * (v[1].posY-v[0].posY) - (v[1].texV-v[0].texV) * (v[2].posY-v[0].posY)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
    v[2].tanZ += ((v[2].texV-v[0].texV) * (v[1].posZ-v[0].posZ) - (v[1].texV-v[0].texV) * (v[2].posZ-v[0].posZ)) / ((v[1].texU-v[0].texU) * (v[2].texV-v[0].texV) - (v[2].texU-v[0].texU) * (v[1].texV-v[0].texV));
}

private static void normalizeTangents(ArrayList<Vertex> vertices) {
	// Normalize tangents
    for (var v : vertices) {
        double nx = v.normX;
        double ny = v.normY;
        double nz = v.normZ;

        double tx = v.tanX;
        double ty = v.tanY;
        double tz = v.tanZ;

        // Gram-Schmidt orthogonalize
        double dot = nx * tx + ny * ty + nz * tz;
        tx -= nx * dot;
        ty -= ny * dot;
        tz -= nz * dot;

        // Normalize the tangent
        double length = Math.sqrt(tx * tx + ty * ty + tz * tz);
        v.tanX = tx / length;
        v.tanY = ty / length;
        v.tanZ = tz / length;
        v.tanW = 0x7FFF/32768.0;
    }
}

	public static void save(Geometry geom, String f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f.replace(".obj", "")+".obj")));
		bw.write("#OBJ file exported with UCGT - NI240SX 2024\n"
				+ "mtllib "+f.split("\\\\")[f.split("\\\\").length-1].replace(".obj", "")+".mtl\n");
//		long iterator = 1;
		long vl = 1;
        for (var p : geom.parts) {
        	bw.write("\n"
        			+ "o "+p.kit+"_"+p.part+"_"+p.lod+"\n"
        			+ "g "+p.kit+"_"+p.part+"_"+p.lod+"\n");
        	//vrite material data one after the other
        	for (var m : p.mesh.materials.materials) {
        		for (var v : m.verticesBlock.vertices) {
        			// vertex + vertex color
        			bw.write("v "+v.posX+" "+v.posY+" "+v.posZ+" "
        			+((double)Byte.toUnsignedInt(v.colorR)/255)+" "+((double)Byte.toUnsignedInt(v.colorG)/255)+" "+((double)Byte.toUnsignedInt(v.colorB)/255)+"\n");
        			//vertex texcoord
        			bw.write("vt "+v.texU+" "+v.texV+"\n");
        			//vertex normal
        			bw.write("vn "+v.normX+" "+v.normY+" "+v.normZ+"\n");
        		}
        		//material
        		bw.write("usemtl "+m.uniqueName+"\n");
        		for (var t : m.triangles) {
        			//triangles
        			bw.write("f "+(vl+t.vert0)+"/"+(vl+t.vert0)+"/"+(vl+t.vert0)+" "
        					+(vl+t.vert1)+"/"+(vl+t.vert1)+"/"+(vl+t.vert1)+" "
        					+(vl+t.vert2)+"/"+(vl+t.vert2)+"/"+(vl+t.vert2)+"\n");
        		}
        		vl += m.verticesBlock.vertices.size();
        	}
//        	//normals
//        	for (var m : p.mesh.materials.materials) {
//        		for (var v : m.verticesBlock.vertices) {
//        			// normal + tangent
//        			bw.write("vn "+v.normX+" "+v.normY+" "+v.normZ+"\n");
//        		}
//        	}
//        	//texcoord
//        	for (var m : p.mesh.materials.materials) {
//        		for (var v : m.verticesBlock.vertices) {
//        			bw.write("vt "+v.texU+" "+v.texV+"\n");
//        		}
//        	}
        	//separation idk
//        	bw.write("s "+iterator+"\n");
//        	iterator *= 2;
        	//materials
//        	for (var m : p.mesh.materials.materials) {
//        		bw.write("usemtl "+m.uniqueName+"\n");
//        		for (var t : m.triangles) {
//        			bw.write("f "+(vl+t.vert1)+"/"+(vl+t.vert1)+"/"+(vl+t.vert1)+" "
//        					+(vl+t.vert2)+"/"+(vl+t.vert2)+"/"+(vl+t.vert2)+" "
//        					+(vl+t.vert3)+"/"+(vl+t.vert3)+"/"+(vl+t.vert3)+"\n");
//        		}
//        		vl += m.verticesBlock.vertices.size();
//        	}
//        	vl += vertices;
        }
        for (var mpc : geom.mpointsPositions) {
        	final float s = 0.05f; //cube half size 
        	bw.write("\n"
        			+ "o "+mpc.mpoints.get(0).uniqueName+"\n"
        			+ "g "+mpc.mpoints.get(0).uniqueName+"\n");
        	bw.write("v "+(mpc.mpoints.get(0).positionX-s)+" "+(mpc.mpoints.get(0).positionY+s)+" "+(mpc.mpoints.get(0).positionZ+s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX-s)+" "+(mpc.mpoints.get(0).positionY+s)+" "+(mpc.mpoints.get(0).positionZ-s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX-s)+" "+(mpc.mpoints.get(0).positionY-s)+" "+(mpc.mpoints.get(0).positionZ+s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX-s)+" "+(mpc.mpoints.get(0).positionY-s)+" "+(mpc.mpoints.get(0).positionZ-s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX+s)+" "+(mpc.mpoints.get(0).positionY+s)+" "+(mpc.mpoints.get(0).positionZ+s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX+s)+" "+(mpc.mpoints.get(0).positionY+s)+" "+(mpc.mpoints.get(0).positionZ-s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX+s)+" "+(mpc.mpoints.get(0).positionY-s)+" "+(mpc.mpoints.get(0).positionZ+s)+"\r\n"
        			+ "v "+(mpc.mpoints.get(0).positionX+s)+" "+(mpc.mpoints.get(0).positionY-s)+" "+(mpc.mpoints.get(0).positionZ-s)+"\r\n");
        	bw.write("""
					vn -0.5774 0.5773 -0.5773
					vn -0.5774 -0.5773 0.5773
					vn -0.5773 0.5774 0.5774
					vn -0.5773 -0.5774 -0.5774
					vn 0.5773 -0.5774 0.5774
					vn 0.5774 -0.5773 -0.5773
					vn 0.5774 0.5773 0.5773
					vn 0.5773 0.5774 -0.5774
					""");
        	bw.write("""
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					vt 0.000000 1.000000
					""");
        	bw.write("usemtl MARKER\n");
        	bw.write("f "+(vl+1)+"/"+(vl+1)+"/"+(vl+1)+" "+(vl+2)+"/"+(vl+2)+"/"+(vl+2)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+"\r\n"
        			+ "f "+(vl+4)+"/"+(vl+4)+"/"+(vl+4)+" "+(vl+7)+"/"+(vl+7)+"/"+(vl+7)+" "+(vl+2)+"/"+(vl+2)+"/"+(vl+2)+"\r\n"
        			+ "f "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+" "+(vl+5)+"/"+(vl+5)+"/"+(vl+5)+" "+(vl+7)+"/"+(vl+7)+"/"+(vl+7)+"\r\n"
        			+ "f "+(vl+6)+"/"+(vl+6)+"/"+(vl+6)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+" "+(vl+5)+"/"+(vl+5)+"/"+(vl+5)+"\r\n"

        			+ "f "+(vl+7)+"/"+(vl+7)+"/"+(vl+7)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+" "+(vl+3)+"/"+(vl+3)+"/"+(vl+3)+"\r\n"
        			+ "f "+(vl+4)+"/"+(vl+4)+"/"+(vl+4)+" "+(vl+6)+"/"+(vl+6)+"/"+(vl+6)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+"\r\n"
        			+ "f "+(vl+1)+"/"+(vl+1)+"/"+(vl+1)+" "+(vl+4)+"/"+(vl+4)+"/"+(vl+4)+" "+(vl+3)+"/"+(vl+3)+"/"+(vl+3)+"\r\n"
        			+ "f "+(vl+4)+"/"+(vl+4)+"/"+(vl+4)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+" "+(vl+7)+"/"+(vl+7)+"/"+(vl+7)+"\r\n"
        			
        			+ "f "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+" "+(vl+6)+"/"+(vl+6)+"/"+(vl+6)+" "+(vl+5)+"/"+(vl+5)+"/"+(vl+5)+"\r\n"
        			+ "f "+(vl+6)+"/"+(vl+6)+"/"+(vl+6)+" "+(vl+1)+"/"+(vl+1)+"/"+(vl+1)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+"\r\n"
        			+ "f "+(vl+7)+"/"+(vl+7)+"/"+(vl+7)+" "+(vl+5)+"/"+(vl+5)+"/"+(vl+5)+" "+(vl+0)+"/"+(vl+0)+"/"+(vl+0)+"\r\n"
        			+ "f "+(vl+4)+"/"+(vl+4)+"/"+(vl+4)+" "+(vl+1)+"/"+(vl+1)+"/"+(vl+1)+" "+(vl+6)+"/"+(vl+6)+"/"+(vl+6)+"\r\n");
        	vl += 8;
        }
        bw.close();
        bw = new BufferedWriter(new FileWriter(new File(f.replace(".obj", "")+".mtl")));
        bw.write("#MTL file exported with UCGT - NI240SX 2024\n");
        for (var m : geom.materials) {
        	bw.write("\nnewmtl "+m.uniqueName+"\n"
        			+ "Ns 200.0\r\n"
        			+ "Ka 1.0 1.0 1.0\r\n"
        			+ "Ks 1.0 1.0 1.0\r\n"
        			+ "Ke 0.0 0.0 0.0\r\n"
        			+ "Ni 1.5 \r\n"
        			+ "d 1.0 \r\n"
        			+ "illum 2\r\n");
        	if (m.textureUsages.contains(TextureUsage.DIFFUSE)) bw.write("map_Kd "+m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.DIFFUSE)).label+".dds\r\n");
        	if (m.textureUsages.contains(TextureUsage.NORMAL)) {
        		bw.write("map_Kn "+m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMAL)).label+".dds\r\n");
        		bw.write("map_norm "+m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.NORMAL)).label+".dds\r\n");
        	}
        	if (m.shaderUsage == ShaderUsage.DiffuseAlpha || m.shaderUsage == ShaderUsage.DiffuseNormalAlpha|| m.shaderUsage == ShaderUsage.DiffuseGlowAlpha)
        		bw.write("map_d "+m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.DIFFUSE)).label+".dds\r\n");
        	if (m.textureUsages.contains(TextureUsage.SELFILLUMINATION)) bw.write("map_Ke "+m.TextureHashes.get(m.textureUsages.indexOf(TextureUsage.SELFILLUMINATION)).label+".dds\r\n");
        	
        }
        bw.close();
        geom.writeConfig(new File(f.replace(".obj", "")+".ini"));
	}
	
}