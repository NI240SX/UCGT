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
        ArrayList<BasicVertex> verts = new ArrayList<BasicVertex>(); //WARNING obj index = list index + 1
        ArrayList<BasicVertex> normals = new ArrayList<BasicVertex>(); //WARNING obj index = list index + 1
        ArrayList<BasicTexcoord> tex = new ArrayList<BasicTexcoord>(); //WARNING obj index = list index + 1
        
//        ArrayList<OBJTriangle> tris = new ArrayList<OBJTriangle>();
//
//        int vertoffset = 1;
//        int normoffset = 1;
//        int texoffset = 1;
        
        while ((line = br.readLine()) != null) {
        	lineheader: if (line.startsWith("o ") || line.startsWith("g ")) {
        		//object or group -> defines part name used by the next triangles

//        		if (verts.size() == 0) verticesPerPart = true; //first part declared before finding any vertices
//        		
//        		if (verticesPerPart && curPart != null) {
//            		// compute part
//            		computePart(curPart, verts, vertoffset, normals, normoffset, tex, texoffset, tris);
//            		vertoffset += verts.size();
//            		texoffset += tex.size();
//            		normoffset += tex.size();
//        			verts.clear();
//        			normals.clear();		//TODO is this really useful ?
//        			tex.clear();
//        			tris.clear();
//        		}

        		if (!line.substring(2).startsWith("_")) {
        			// part
        			nextIsMarker = false;
  //      			vertoffset = 0;
	        		for (var p : geom.parts) if ((p.kit+"_"+p.part+"_"+p.lod).equals(line.substring(2))) {
	        			curPart = p;
	        			break lineheader;
	        		}
	        		
	        		// initialize a part that's not already in the geometry
	        		curPart = new Part(geom.carname, line.substring(2));
	        		geom.parts.add(curPart);
	
	        		for (var mp : geom.mpoints)if (mp.tempPartName.equals(line.substring(2))) { // binding mpoints read from config, if existing
	    				if (curPart.mpoints == null) curPart.mpoints = new MPoints();
	    				curPart.mpoints.mpoints.add(mp);
	    				mp.parts.add(curPart);
	        		}
	        		
	        		for (var asl : geom.asLinking) if (asl.tempPartName.equals(line.substring(2))) { // binding autosculpt linking data read from ini
	        			curPart.asLinking = asl;
	        			break;
	        		}
        		} else {
        			// marker
        			nextIsMarker = true;
        			for (var mp : geom.mpoints) if (mp.uniqueName.equals(line.substring(2))) {
	    				curMarker = mp;
	    				break lineheader;
	        		}
        			System.out.println("[OBJLoader] Marker "+line.substring(3)+" not found in config !");
        		}
        	}
        	else if (line.startsWith("v ")) {
        		// vertex
        		if (line.split(" ").length < 4) System.out.println("[OBJLoader] Wrong vertex data");
        		verts.add(new BasicVertex(line.split(" ")[1], line.split(" ")[2], line.split(" ")[3]));
        		
        		//assuming vertices are declared before vertex normals and uv
        		
        	} else if (line.startsWith("vn ")) {
        		// normal
        		if (line.split(" ").length < 4) System.out.println("[OBJLoader] Wrong normal data");
        		normals.add(new BasicVertex(line.split(" ")[1], line.split(" ")[2], line.split(" ")[3]));
        		
        	} else if (line.startsWith("vt")) {
        		// texcoord
        		if (line.split(" ").length < 3) System.out.println("[OBJLoader] Wrong texcoord data");
        		tex.add(new BasicTexcoord(line.split(" ")[1], line.split(" ")[2]));
        		
        	} else if (line.startsWith("usemtl")) {
        		if (!nextIsMarker) {
//        			if (curMat != null) vertoffset += curMat.verticesBlock.vertices.size(); //offsets vertex ids in the next materials
        			
        			for (var m : geom.materials) if (m.uniqueName.equals(line.substring(7))) {
        				curMat = new Material(m); //copies the material the mesh uses to make it part specific
//        				curMat.fromTriVertID = vertoffset;
        				curMat.verticesBlock = new Vertices();
//        				curPart.mesh.verticesBlocks.add(curMat.verticesBlock); 
        				curPart.mesh.materials.materials.add(curMat);
        				break lineheader;
        			}
        			// if no material data is found give a warning and create a fallback material
        			System.out.println("[OBJLoader] Warning : material "+line.substring(7)+" not found in config !");
        			curMat = Material.getFallbackMaterial();
//    				curMat.fromTriVertID = vertoffset;
    				curMat.verticesBlock = new Vertices();
//    				curPart.mesh.verticesBlocks.add(curMat.verticesBlock); 
    				curPart.mesh.materials.materials.add(curMat);
        			
        		} //no material data to process for markers
        	}
        	else if (line.startsWith("f ")) {
        		// face, this one's gonna be funny
        		// first determine vertices based on face, coords, normal, texcoords, material
        		// then check they're not preexisting in the destination geometry : if yes take the existing vertex id, if not create one for the corresponding mat, 
        		// compute its index, finally link the 3 obtained vertices to the triangle
	        		
        		if (!nextIsMarker) {
        			//part
//        			if (!verticesPerPart) {
	        			Vertex[] triVerts = new Vertex[3];
	        			for (int i=1; i<4; i++) {
		        			String vertdata = line.split(" ")[i];
		        			Vertex v = new Vertex();
		        			v.posX = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).x;
		        			v.posY = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).y;
		        			v.posZ = verts.get(Integer.parseInt(vertdata.split("/")[0])-1).z;
		
		        			v.texU = tex.get(Integer.parseInt(vertdata.split("/")[1])-1).u;
		        			v.texV = tex.get(Integer.parseInt(vertdata.split("/")[1])-1).v;
		
		        			v.normX = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).x;
		        			v.normY = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).y;
		        			v.normZ = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).z;
		        			
		        			if (!curMat.verticesBlock.vertices.contains(v)) curMat.verticesBlock.vertices.add(v);
		        			triVerts[i-1] = v;
		        		}
	        			Triangle t = new Triangle(	(short)(curMat.verticesBlock.vertices.indexOf(triVerts[0])), //vertoffset+ breaks it, idk anymore
	        										(short)(curMat.verticesBlock.vertices.indexOf(triVerts[1])),
	        										(short)(curMat.verticesBlock.vertices.indexOf(triVerts[2])));
	//        			curPart.mesh.triangles.triangles.add(t);
	        			
	        			
//	        			addToTangents(triVerts);
	        			
	        			
	        			curMat.triangles.add(t);
//        		} else {
//        			OBJTriangle t;
//        			tris.add(t = new OBJTriangle());
//        			for (int i=0; i<3; i++) {
//	        			String vertdata = line.split(" ")[i+1];
//	        			
//	        			t.verts[i] = verts.get(Integer.parseInt(vertdata.split("/")[0])-vertoffset);
//	        			t.texcoords[i] = tex.get(Integer.parseInt(vertdata.split("/")[1])-texoffset);
//	        			t.normals[i] = normals.get(Integer.parseInt(vertdata.split("/")[2])-normoffset);
//	        			t.mat = curMat;
//	        		}
//        		}
	        			
        		} else {
        			//marker
        			//collect coords, average them later to get the mpoint x y z
        			for (int i=1; i<4; i++) {
	        			String vertdata = line.split(" ")[i];
	        			BasicVertex v = verts.get(Integer.parseInt(vertdata.split("/")[0])-1);
	        			if (!curMarker.verts.contains(v)) curMarker.verts.add(v);
	        		}
        			
        		}
        		
        	}
        }
        br.close();
        
//        if (verticesPerPart) {
//    		// compute last part
//        	computePart(curPart, verts, vertoffset, normals, normoffset, tex, texoffset, tris);
//        }
        
        
        for (var p : geom.parts) {

        	//sort materials (same as in config file) and add the materials data back to the mesh
        	p.mesh.materials.materials.sort(new MaterialsSorterName());
        	for (var m : p.mesh.materials.materials) {
        		p.mesh.verticesBlocks.add(m.verticesBlock);
        		p.mesh.triangles.triangles.addAll(m.triangles);
//        		for (var vb : p.mesh.verticesBlocks) normalizeTangents(vb.vertices);
        	}
        	
//        	p.guessFlags(geom); // TODO guess flags for part header, hopefully this is what was causing the mesh not to be recognized clueless
        	
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
        	for (var m : p.mesh.materials.materials) {
//        		p.mesh.info.numVertices += m.verticesBlock.vertices.size();

        	//--- materials ---
        	//for each material :
        	//numVertices, toVertID
        	//shaderID, textureIDs
        	//verticesDataLength
        		m.fromTriVertID = triVertI; //actually concerns triangles
        		m.toTriVertID = m.fromTriVertID + m.triangles.size()*3;
        		m.numTriVertices = m.toTriVertID - m.fromTriVertID;
//        		m.toTriVertID = m.fromTriVertID + m.verticesBlock.vertices.size();
//        		m.numTriVertices = m.verticesBlock.vertices.size();
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
        
        for (var mp : geom.mpoints) {
        	//calculate mpoint coords
        	BasicVertex avgPos = new BasicVertex(0,0,0);
        	for (var v : mp.verts) {
        		avgPos.x += v.x;
        		avgPos.y += v.y;
        		avgPos.z += v.z;
        	}
        	mp.positionX = avgPos.x / mp.verts.size();
        	mp.positionY = avgPos.y / mp.verts.size();
        	mp.positionZ = avgPos.z / mp.verts.size();
        }
	}

@SuppressWarnings("unused")
private static void addToTangents(Vertex[] triVerts) {
	// yes i had to ask chatgpt please shut the fuck up

    // Get the vertex positions
    double x1 = triVerts[0].posX;
    double y1 = triVerts[0].posY;
    double z1 = triVerts[0].posZ;
    double x2 = triVerts[1].posX;
    double y2 = triVerts[1].posY;
    double z2 = triVerts[1].posZ;
    double x3 = triVerts[2].posX;
    double y3 = triVerts[2].posY;
    double z3 = triVerts[2].posZ;
    
    // Get the texture coordinates
    double u1 = triVerts[0].texU;
    double v1 = triVerts[0].texV;
    double u2 = triVerts[1].texU;
    double v2 = triVerts[1].texV;
    double u3 = triVerts[2].texU;
    double v3 = triVerts[2].texV;

    // Calculate the edges of the triangle
    double dx1 = x2 - x1;
    double dy1 = y2 - y1;
    double dz1 = z2 - z1;
    double dx2 = x3 - x1;
    double dy2 = y3 - y1;
    double dz2 = z3 - z1;

    // Calculate the delta UVs
    double du1 = u2 - u1;
    double dv1 = v2 - v1;
    double du2 = u3 - u1;
    double dv2 = v3 - v1;

    double f = 1.0 / (du1 * dv2 - du2 * dv1);

    // Calculate tangent and bitangent
    double tx = f * (dv2 * dx1 - dv1 * dx2);
    double ty = f * (dv2 * dy1 - dv1 * dy2);
    double tz = f * (dv2 * dz1 - dv1 * dz2);

    triVerts[0].tanX += tx;
    triVerts[0].tanY += ty;
    triVerts[0].tanZ += tz;
    triVerts[1].tanX += tx;
    triVerts[1].tanY += ty;
    triVerts[1].tanZ += tz;
    triVerts[2].tanX += tx;
    triVerts[2].tanY += ty;
    triVerts[2].tanZ += tz;			
}

@SuppressWarnings("unused")
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

//	private static void computePart(Part curPart, ArrayList<BasicVertex> verts, int vertoffset, ArrayList<BasicVertex> normals,
//			int normoffset, ArrayList<BasicTexcoord> tex, int texoffset, ArrayList<OBJTriangle> tris) {
//		// TODO Auto-generated method stub
//		
//		var vertMapping = new HashMap<BasicVertex, Pair<BasicTexcoord, BasicVertex>>();
//		
//		
//		
//	}

	public static void save(Geometry geom, String f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f.replace(".obj", "")+".obj")));
		bw.write("#OBJ file exported with UCGT - NI240SX 2024\n"
				+ "mtllib "+f.replace(".obj", "")+".mtl\n");
		long iterator = 1;
		long vl = 1;
        for (var p : geom.parts) {
        	bw.write("\n"
        			+ "o "+p.kit+"_"+p.part+"_"+p.lod+"\n"
        			+ "g "+p.kit+"_"+p.part+"_"+p.lod+"\n");
        	//vertices
        	for (var m : p.mesh.materials.materials) {
        		for (var v : m.verticesBlock.vertices) {
        			bw.write("v "+v.posX+" "+v.posY+" "+v.posZ+"\n");
        		}
        	}
        	//normals
        	for (var m : p.mesh.materials.materials) {
        		for (var v : m.verticesBlock.vertices) {
        			bw.write("vn "+v.normX+" "+v.normY+" "+v.normZ+"\n");
        		}
        	}
        	//texcoord
        	for (var m : p.mesh.materials.materials) {
        		for (var v : m.verticesBlock.vertices) {
        			bw.write("vt "+v.texU+" "+v.texV+"\n");
        		}
        	}
        	//separation idk
        	bw.write("s "+iterator+"\n");
        	iterator *= 2;
        	//materials
        	for (var m : p.mesh.materials.materials) {
        		bw.write("usemtl "+m.uniqueName+"\n");
        		for (var t : m.triangles) {
        			bw.write("f "+(vl+t.vert1)+"/"+(vl+t.vert1)+"/"+(vl+t.vert1)+" "
        					+(vl+t.vert2)+"/"+(vl+t.vert2)+"/"+(vl+t.vert2)+" "
        					+(vl+t.vert3)+"/"+(vl+t.vert3)+"/"+(vl+t.vert3)+"\n");
        		}
        		vl += m.verticesBlock.vertices.size();
        	}
//        	vl += vertices;
        }
        for (var mp : geom.mpoints) {
        	final float s = 0.05f; //cube half size 
        	bw.write("\n"
        			+ "o "+mp.uniqueName+"\n"
        			+ "g"+mp.uniqueName+"\n");
        	bw.write("v "+(mp.positionX-s)+" "+(mp.positionY+s)+" "+(mp.positionZ+s)+"\r\n"
        			+ "v "+(mp.positionX-s)+" "+(mp.positionY+s)+" "+(mp.positionZ-s)+"\r\n"
        			+ "v "+(mp.positionX-s)+" "+(mp.positionY-s)+" "+(mp.positionZ+s)+"\r\n"
        			+ "v "+(mp.positionX-s)+" "+(mp.positionY-s)+" "+(mp.positionZ-s)+"\r\n"
        			+ "v "+(mp.positionX+s)+" "+(mp.positionY+s)+" "+(mp.positionZ+s)+"\r\n"
        			+ "v "+(mp.positionX+s)+" "+(mp.positionY+s)+" "+(mp.positionZ-s)+"\r\n"
        			+ "v "+(mp.positionX+s)+" "+(mp.positionY-s)+" "+(mp.positionZ+s)+"\r\n"
        			+ "v "+(mp.positionX+s)+" "+(mp.positionY-s)+" "+(mp.positionZ-s)+"\r\n");
        	bw.write("vn -0.5774 0.5773 -0.5773\r\n"
        			+ "vn -0.5774 -0.5773 0.5773\r\n"
        			+ "vn -0.5773 0.5774 0.5774\r\n"
        			+ "vn -0.5773 -0.5774 -0.5774\r\n"
        			+ "vn 0.5773 -0.5774 0.5774\r\n"
        			+ "vn 0.5774 -0.5773 -0.5773\r\n"
        			+ "vn 0.5774 0.5773 0.5773\r\n"
        			+ "vn 0.5773 0.5774 -0.5774\r\n");
        	bw.write("vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n"
        			+ "vt 0.000000 1.000000\r\n");
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