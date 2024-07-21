package fr.ni240sx.ucgt.geometryFile.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.TextureUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.ShaderUsage;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;

public class WavefrontOBJ {
	
	public static void load(Geometry geom, File f) throws IOException {
//		boolean verticesPerPart = false;
		
		BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean nextIsMarker = false;
        Part curPart = null;
        MPoint curMarker = null;
        Material curMat = Material.getFallbackMaterial();
        ArrayList<VertexData3> verts = new ArrayList<>(); //WARNING obj index = list index + 1
        ArrayList<VertexData3> normals = new ArrayList<>(); //WARNING obj index = list index + 1
        ArrayList<TexcoordData2> tex = new ArrayList<>(); //WARNING obj index = list index + 1
        
		long time = System.currentTimeMillis();
		
        while ((line = br.readLine()) != null) {
        	lineheader: if (line.startsWith("o ") || line.startsWith("g ")) {

        		if (!line.substring(2).startsWith("_")) {
        			// part
        			nextIsMarker = false;
	        		for (var p : geom.parts) if ((p.name).equals(line.substring(2).toUpperCase())) {
	        			curPart = p;
	        			break lineheader;
	        		}
	        		
	        		// initialize a part that's not already in the geometry
	        		curPart = new Part(geom, line.substring(2).toUpperCase());

        		} else {
        			// marker
        			nextIsMarker = true;
        			for (var mp : geom.mpointsAll) if (mp.uniqueName.equals(line.substring(2).toUpperCase())) {
	    				curMarker = mp;
	    				break lineheader;
	        		}
        			System.out.println("[OBJLoader] Marker "+line.substring(2).toUpperCase()+" not found in config !");
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
        			assert (curPart != null);
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
	        			if (Geometry.IMPORT_flipV) v.texV = 1-tex.get(Integer.parseInt(vertdata.split("/")[1])-1).v;
	        			else v.texV = tex.get(Integer.parseInt(vertdata.split("/")[1])-1).v;
	        			
	        			v.normX = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).x;
	        			v.normY = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).y;
	        			v.normZ = normals.get(Integer.parseInt(vertdata.split("/")[2])-1).z;
	        			
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
	        			
        		} else {
        			//marker
        			//collect coords, average them later to get the mpoint x y z
        			assert (curMarker != null);
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
        
		geom.rebuild();
        
        System.out.println("Post-treatment done in "+(System.currentTimeMillis()-time)+" ms.");
		time = System.currentTimeMillis();
	}



	public static void save(Geometry geom, String f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f.replace(".obj", "")+".obj")));
		bw.write("#OBJ file exported with UCGT - NI240SX 2024\n"
				+ "mtllib "+f.split("\\\\")[f.split("\\\\").length-1].replace(".obj", "")+".mtl\n");
//		long iterator = 1;
		long vl = 1;
        for (var p : geom.parts) {
        	bw.write("\n"
        			+ "o "+p.name+"\n"
        			+ "g "+p.name+"\n");
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
        	bw.write("vn -0.5774 0.5773 -0.5773\n"
					+"vn -0.5774 -0.5773 0.5773\n"
					+"vn -0.5773 0.5774 0.5774\n"
					+"vn -0.5773 -0.5774 -0.5774\n"
					+"vn 0.5773 -0.5774 0.5774\n"
					+"vn 0.5774 -0.5773 -0.5773\n"
					+"vn 0.5774 0.5773 0.5773\n"
					+"vn 0.5773 0.5774 -0.5774\n");
        	bw.write("vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n"
					+"vt 0.000000 1.000000\n");
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
	}
	
}