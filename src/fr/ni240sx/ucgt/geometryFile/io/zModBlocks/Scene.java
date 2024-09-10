package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Part;
import fr.ni240sx.ucgt.geometryFile.io.VertexData3;
import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;
import fr.ni240sx.ucgt.geometryFile.part.MPoint;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Triangle;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
//import fr.ni240sx.ucgt.geometryFile.part.mesh.Material;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;

public class Scene extends ZModBlock {
	
	public simpleNode rootNode;
	
	public Scene(int ID) {
		super(ID);
	}

	public Scene() {
	}

	@Override
	public void readData(ByteBuffer in) {
		rootNode = new simpleNode(in);
	}
	
	@Override
	public String getName() {
		return "ZModeler::Scene";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * something : 4B
		 * blank : 4B
		 * nameLength+5B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(64);
		block.putInt(0);
		ZModBlock.putString(block, getName());

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * then per child node
		 * UID : 4B
		 * numChildren : 4B
		 */
		//precompute length
		final var length = 16 + rootNode.getSavingLengthRecursively();
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		rootNode.saveRecursively(block);
		
		fos.write(block.array());
	}
	
	public class simpleNode{
		@SuppressWarnings("hiding")
		int UID;
		public ArrayList<simpleNode> childNodes = new ArrayList<>();
		
		public ZModBlock node; //only used at saving
		
		public simpleNode(ByteBuffer in) {
			UID = in.getInt();
			var numChildren = in.getInt();
			for (int i=0; i<numChildren; i++) {
				childNodes.add(new simpleNode(in));
			}
		}

		public simpleNode(ZModBlock node) {
			UID = node.UID;
			this.node = node;
		}
		
		public void addToGeomRecursively(Geometry geom) {
			var b = ZModelerZ3D.blocks.get(this.UID);
			if (b.getClass() == MeshNode.class) {
				//add the part to the geometry
				var mn = (MeshNode)b;
				var mesh = (ZMesh)ZModelerZ3D.blocks.get(mn.meshUID);

				//transformation matrix
				var M = mn.embeddedNode.matrix;
				
				if (!mn.embeddedNode.name.startsWith("_")) { //check whether it's a part or a marker
					var curPart = new Part(geom, mn.embeddedNode.name);
					
					HashMap<Integer, Material> ZMMatsToBINMats = new HashMap<>();

					//materials are already per triangle i just need to separate the vertices into multiple blocks and rebuild the indices
					//if (mesh.polyFormat == 3) 
					for (var poly : mesh.polys) { //only process triangle meshes for now
						
						//detect the material something something
						//create a hashmap for the part (materialUID, BIN material) we don't wanna do this for each poly
	
						// if the material isn't mapped already find it
						findMaterial: if (!ZMMatsToBINMats.containsKey(poly.materialUID)) {
							Material curMat;
							for (var m : geom.materials) if (m.uniqueName.equals(((ZMaterial)ZModelerZ3D.blocks.get(poly.materialUID)).name.replace(" ", "_"))) {
		        				curMat = new Material(m); //copies the material the mesh uses to make it part specific
		        				curMat.verticesBlock = new Vertices();
		        				curPart.mesh.materials.materials.add(curMat);
		        				ZMMatsToBINMats.put(poly.materialUID, curMat);
		        				break findMaterial;
		        			}
		        			// if no material data is found give a warning and create a fallback material
		        			System.out.println("[Z3DLoader] Warning : material "+((ZMaterial)ZModelerZ3D.blocks.get(poly.materialUID)).name.replace(" ", "_")+" not found in config !");
		        			curMat = Material.getFallbackMaterial();
		    				curMat.verticesBlock = new Vertices();
		    				curPart.mesh.materials.materials.add(curMat);
	        				ZMMatsToBINMats.put(poly.materialUID, curMat);
						}
						

						//process the triangle's vertices, add them to the right vertices block and process their new ids
	        			Vertex[] triVerts = new Vertex[poly.vertIDs.length];
	        			for (int i=0; i<poly.vertIDs.length; i++) {
	        				triVerts[i] = new Vertex();
	        				
	        				var x = mesh.verts.get(poly.vertIDs[i]).x;
	        				var y = mesh.verts.get(poly.vertIDs[i]).y;
	        				var z = mesh.verts.get(poly.vertIDs[i]).z;

	        				var nx = mesh.verts.get(poly.vertIDs[i]).nx;
	        				var ny = mesh.verts.get(poly.vertIDs[i]).ny;
	        				var nz = mesh.verts.get(poly.vertIDs[i]).nz;
	        				
	        				//apply transforms
	        				triVerts[i].posX = -M[0][0]*x+		-M[1][0]*y+		-M[2][0]*z+		-M[3][0];
	        				triVerts[i].posY = -M[0][2]*x+		-M[1][2]*y+		-M[2][2]*z+		-M[3][2];
	        				triVerts[i].posZ = M[0][1]*x+		M[1][1]*y+		M[2][1]*z+		M[3][1];
	        				triVerts[i].normX = -M[0][0]*nx +	-M[1][0]*ny +	-M[2][0]*nz;
	        				triVerts[i].normY = -M[0][2]*nx +	-M[1][2]*ny +	-M[2][2]*nz;
	        				triVerts[i].normZ = M[0][1]*nx +	M[1][1]*ny +	M[2][1]*nz;
	        				
	        				triVerts[i].tex0U = mesh.verts.get(poly.vertIDs[i]).u0;
	        				triVerts[i].tex0V = 1-mesh.verts.get(poly.vertIDs[i]).v0;

	        				if (Geometry.IMPORT_importVertexColors) {
		        				triVerts[i].colorR = mesh.verts.get(poly.vertIDs[i]).r;
		        				triVerts[i].colorG = mesh.verts.get(poly.vertIDs[i]).g;
		        				triVerts[i].colorB = mesh.verts.get(poly.vertIDs[i]).b;
		        				triVerts[i].colorA = mesh.verts.get(poly.vertIDs[i]).a;
	        				}

	        				if (!ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.contains(triVerts[i])) 
	        					ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.add(triVerts[i]);
	        			}
	        			
	        			ZMMatsToBINMats.get(poly.materialUID).triangles.add( new Triangle(	//TRIANGLES ARE FLIPPED WITH THIS AXIS CONVENTION
	        					(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[2])), 
								(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[1])),
								(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[0])) 
								) );
						
	        			if (poly.vertIDs.length == 4) { //
	        				ZMMatsToBINMats.get(poly.materialUID).triangles.add( new Triangle(
		        					(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[3])), 
									(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[2])),
									(short)(ZMMatsToBINMats.get(poly.materialUID).verticesBlock.vertices.indexOf(triVerts[0])) 
									) );
	        			} if (poly.vertIDs.length > 4) { //
	        				System.out.println("Please triangulate the mesh for "+mn.embeddedNode.name);
	        			}
	        			
					} //else System.out.println("Please triangulate the mesh for "+mn.embeddedNode.name);
					
					
				} else {//marker
					//collect coords, average them later to get the mpoint x y z
					MPoint curMarker = null;
					for (var mp : geom.mpointsAll) if (mp.uniqueName.equals(mn.embeddedNode.name)) {
	    				curMarker = mp;
	        		}
					if (curMarker == null) {
						System.out.println("[Z3DLoader] Marker "+mn.embeddedNode.name+" not found in config !");
	        			curMarker = new MPoint();						
					}
					
					for (var poly : mesh.polys) for (int i=0; i<poly.vertIDs.length; i++) {	
        				
						var x = mesh.verts.get(poly.vertIDs[i]).x;
        				var y = mesh.verts.get(poly.vertIDs[i]).y;
        				var z = mesh.verts.get(poly.vertIDs[i]).z;
        				
						VertexData3 v = new VertexData3(
								-M[0][0]*x -M[1][0]*y - M[2][0]*z - M[3][0] ,
								-M[0][2]*x -M[1][2]*y - M[2][2]*z - M[3][2] ,
								M[0][1]*x + M[1][1]*y + M[2][1]*z + M[3][1] );
	        			if (!curMarker.verts.contains(v)) curMarker.verts.add(v);
					}
						
				}
				
				
				
			}
			
			for (var c : childNodes) c.addToGeomRecursively(geom);
		}

		public void addNodesRecursively(ArrayList<ZModBlock> blocks) {
			blocks.add(node);
			for (var c : childNodes) c.addNodesRecursively(blocks);
		}

		public void addMeshesRecursively(ArrayList<ZModBlock> blocks) {
			if (node.getClass() == MeshNode.class) {
				var mn = (MeshNode) node;
				blocks.add(mn.renderTechnique);
				blocks.add(mn.mesh);
			}
			for (var c : childNodes) c.addMeshesRecursively(blocks);
		}

		public void addExtensionsRecursively(ArrayList<ZModBlock> blocks) {
			if (node.getClass() == MeshNode.class) {
				var mn = (MeshNode) node;
				blocks.add(mn.renderTechnique.techniques);
			}
			for (var c : childNodes) c.addExtensionsRecursively(blocks);
		}
		
		public int getSavingLengthRecursively() {
			int length = 8;
			for (var c : childNodes) length += c.getSavingLengthRecursively();
			return length;
		}
		
		public void saveRecursively(ByteBuffer block) {
			block.putInt(node.UID);
			block.putInt(childNodes.size());
			for (var c : childNodes) c.saveRecursively(block);
		}
	}
}
