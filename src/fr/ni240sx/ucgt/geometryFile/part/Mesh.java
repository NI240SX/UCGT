package fr.ni240sx.ucgt.geometryFile.part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.geometryFile.Geometry;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.mesh.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.*;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PS3.*;

public class Mesh implements Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh;}
	
	public ArrayList<Block> subBlocks = new ArrayList<>();
	
	public Platform platform;
	public Mesh_Info info;
	public Materials materials;
	public Shaders shaders;
	public ArrayList<Vertices> verticesBlocks = new ArrayList<>();
	public Triangles triangles;
	
	public static Platform currentPlatform;
	
	public Mesh(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block = null;
		
		while(in.position() < blockStart+blockLength) {
			try {
				if ((block = Block.read(in)) != null) subBlocks.add(block);
				if (block != null && block.getClass() == Mesh_Info_PC.class) {
					platform = ((Mesh_Info_PC)block).platform;
					currentPlatform = platform; //TODO NOT IDEAL
//					if (platform == null) System.out.println("Error: unsupported mesh!");
				}
				if (block != null && block.getClass() == Mesh_Info_X360.class) platform = Platform.X360;
				if (block != null && block.getClass() == Mesh_Info_PS3.class) platform = Platform.PS3;
			} catch (Exception e) {
				System.out.println("Unable to read mesh sub-block: "+e.getMessage());
				if (block != null) System.out.println("Last successfully read mesh sub-block: "+block.getClass().getSimpleName());
//				e.printStackTrace();
				throw e; //handle in Part
			}
		}
		// SUB-BLOCKS PRE-TREATMENT TO REFERENCE THEM ALL
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case Part_Mesh_ShadersUsage:
				shaders = (Shaders) b;
				break;
			case Part_Mesh_VertsHeader:
				break;
			case Part_Mesh_Info_PC:
				info = (Mesh_Info_PC) b;
				break;
			case Part_Mesh_Materials_PC:
				materials = (Materials_PC) b;
				break;
			case Part_Mesh_Vertices_PC:
				verticesBlocks.add((Vertices_PC) b);
				break;
			case Part_Mesh_Triangles_PC:
				triangles = (Triangles_PC) b;
				break;
				
			case Part_Mesh_Info_X360:
//				platform = Platform.X360;
				info = (Mesh_Info_X360) b;
				break;
			case Part_Mesh_Materials_X360:
				materials = (Materials_X360) b;
				break;
			case Part_Mesh_Vertices_X360:
				verticesBlocks.add((Vertices_X360) b);
				break;
			case Part_Mesh_Triangles_X360:
				triangles = (Triangles_X360) b;
				break;

			case Part_Mesh_Info_PS3:
//				platform = Platform.PS3;
				info = (Mesh_Info_PS3) b;
				break;
			case Part_Mesh_Materials_PS3:
				materials = (Materials_PS3) b;
				break;
			case Part_Mesh_Vertices_PS3:
				verticesBlocks.add((Vertices_PS3) b);
				break;
			case Part_Mesh_Triangles_PS3:
				triangles = (Triangles_PS3) b;
				break;

			case Part_Mesh_LegacyMaterials:
				materials = (LegacyMaterials) b;
				break;
			case Part_Mesh_LegacyVertices:
				verticesBlocks.add((LegacyVertices) b);
				break;
			case Part_Mesh_LegacyTriangles:
				triangles = (LegacyTriangles) b;
				break;

			default:
				break;}
		}
		
//		System.out.println(info.numMaterials+" materials, "+info.numTriangles+"/"+triangles.triangles.size()+" triangles, "+info.numVertices+" vertices");
//		System.out.println(materials.materials.size() + " materials, "+verticesBlocks.size() + " vertices blocks");

		Shader previousSU = null;
		int j=0;
		if (materials != null && triangles != null) for (int i=0; i<materials.materials.size(); i++) {
			if (verticesBlocks.size() == j) {
				System.out.println("Mesh : No more vertices block found ! (material idx %d / total materials %d / vertices block idx %d / total vertices blocks %d)".formatted(i, materials.materials.size(), j, verticesBlocks.size()));
				break;
			}
			switch (platform) {
			case PC:
			case Prostreet_PC:
			case Prostreet_X360: //PS X360
			case Carbon_PC: //carbono
				verticesBlocks.get(j).vertexFormat = materials.materials.get(i).shader.vertexFormat_PC;
				break;
			case X360:
				verticesBlocks.get(j).vertexFormat = materials.materials.get(i).shader.vertexFormat_X360;
				break;
			case PS3:
				verticesBlocks.get(j).vertexFormat = materials.materials.get(i).shader.vertexFormat_PS3;
				break;
			}
			
			if (materials.materials.get(i).singleVertexLength != 0 && materials.materials.get(i).singleVertexLength != verticesBlocks.get(j).vertexFormat.getLength()) {
				System.err.println("WARN: mismatching vertex format length: declared %d, expected %d (shader %s)".formatted(materials.materials.get(i).singleVertexLength, verticesBlocks.get(j).vertexFormat.getLength(), materials.materials.get(i).shader.getName()));
			}
			try {
				verticesBlocks.get(j).readVertices();
			} catch (Exception e) {
				verticesBlocks.get(j).verticesData = null;
				System.err.println("Unable to read all vertices (%d / %d successfully read) @ in=%d".formatted(verticesBlocks.get(j).vertices.size(), materials.materials.get(i).verticesDataLength / materials.materials.get(i).singleVertexLength, in.position()));
				e.printStackTrace();
			}
				
			verticesBlocks.get(j).material = materials.materials.get(i);
			materials.materials.get(i).verticesBlock = verticesBlocks.get(j);
			
//			System.out.println(materials.materials.get(i).generateName()+" requesting triangles from "+(materials.materials.get(i).fromTriIndex/3) + " to " + (materials.materials.get(i).toTriIndex/3));
			materials.materials.get(i).triangles = new ArrayList<>(triangles.triangles.subList(materials.materials.get(i).fromTriIndex/3, (materials.materials.get(i).toTriIndex/3)));
			materials.materials.get(i).trianglesExtra = new ArrayList<>(triangles.triangles.subList(materials.materials.get(i).toTriIndex/3, (materials.materials.get(i).toTriIndex/3+materials.materials.get(i).numTriIndicesExtra/3)));
			
			//workaround for carbon, doesnt fully work but might unfuck some meshes maybe
			//stupid game has several vertices blocks which sum each other 
			//and you have seemingly no way to know which material they will belong to (= which vertex format they have)
			if (platform == Platform.Carbon_PC) for (var t : materials.materials.get(i).triangles) {
				if (t.vert0 >= materials.materials.get(i).verticesBlock.vertices.size()) {
					int k = j;
					while (t.vert0 >= materials.materials.get(i).verticesBlock.vertices.size()	
							&& verticesBlocks.size() > k+1
							) {
						k++;
//						System.out.println("add vertices");
						if (verticesBlocks.get(k).vertexFormat == null) verticesBlocks.get(k).vertexFormat = materials.materials.get(i).shader.vertexFormat_PC;
						verticesBlocks.get(k).readVertices();
						materials.materials.get(i).verticesBlock = verticesBlocks.get(k);
					}
				}
			}

			//added because stupid carbon
			if (platform == Platform.Carbon_PC) {
				if (previousSU != null && !previousSU.equals(materials.materials.get(i).shader)) j++;
				previousSU = materials.materials.get(i).shader;
			} else 
				j++;
			

		} else {
//			throw new Exception ("Invalid/unsupported/corrupted mesh data!");
			System.out.println("Invalid/unsupported/corrupted mesh data!");
		}
		in.position(blockStart+blockLength);
	}

	public Mesh(Platform platform) {
		this.platform = platform;
		currentPlatform = platform;
		switch (platform) {
		case PC:
			this.info = new Mesh_Info_PC(platform);
			this.materials = new Materials_PC();
			this.shaders = new Shaders();
			this.triangles = new Triangles_PC();
			break;
		case X360:
			this.info = new Mesh_Info_X360();
			this.materials = new Materials_X360();
			this.shaders = new Shaders();
			this.triangles = new Triangles_X360();
			break;
		case PS3:
			this.info = new Mesh_Info_PS3();
			this.materials = new Materials_PS3();
			this.shaders = new Shaders();
			this.triangles = new Triangles_PS3();
			break;
		case Prostreet_PC:
		case Prostreet_X360:
		case Carbon_PC:
			this.info = new Mesh_Info_PC(platform);
			this.materials = new LegacyMaterials();
//			this.shadersUsage = new ShadersUsage(); //no shaders usage in proshit i think
			this.shaders = null;
			this.triangles = new LegacyTriangles();
			break;
		}
	}
	
	public void rebuildSubBlocks() {
		subBlocks.clear();
		subBlocks.add(info);
		if (Geometry.isUC(platform)) subBlocks.add(shaders);
		subBlocks.add(materials);
		for (var v : verticesBlocks) {
			subBlocks.add(new Mesh_VertsHeader());
			subBlocks.add(v);
		}
		subBlocks.add(triangles);
	}
	
	public void changePlatform(Platform plat) {
		platform = plat;
		currentPlatform = plat;
		
		switch (platform) {
		case PC:
			this.info = new Mesh_Info_PC(info);
			((Mesh_Info_PC)info).updatePlatform(plat);
			this.materials = new Materials_PC(materials);
			this.shaders = new Shaders();
			this.triangles = new Triangles_PC(triangles);
			for (var m : materials.materials) {
//				System.out.println(m.shaderUsage.getName() + " is " + m.shaderUsage.getClass().getSimpleName());
				m.shader = Shader.getNGOrDefault(m.shader.getName(), Shader.get("Diffuse"));
//				System.out.println("new "+m.shaderUsage.getName() + " is " + m.shaderUsage.getClass().getSimpleName());
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new Vertices_PC(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shader.vertexFormat_PC;
			}
			break;
		case X360:
			this.info = new Mesh_Info_X360(info);
			this.materials = new Materials_X360(materials);
			this.shaders = new Shaders();
			this.triangles = new Triangles_X360(triangles);
			for (var m : materials.materials) {
				m.shader = Shader.getNGOrDefault(m.shader.getName(), Shader.get("Diffuse"));
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new Vertices_X360(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shader.vertexFormat_X360;
			}
			break;
		case PS3:
			this.info = new Mesh_Info_PS3(info);
			this.materials = new Materials_PS3(materials);
			this.shaders = new Shaders();
			this.triangles = new Triangles_PS3(triangles);
			for (var m : materials.materials) {
				m.shader = Shader.getNGOrDefault(m.shader.getName(), Shader.get("Diffuse"));
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new Vertices_PS3(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shader.vertexFormat_PS3;
			}
			break;
		case Prostreet_PC:
		case Prostreet_X360:
		case Carbon_PC:
			this.info = new Mesh_Info_PC(info);
			((Mesh_Info_PC)info).updatePlatform(plat);
			this.materials = new LegacyMaterials(materials);
//			this.shadersUsage = new ShadersUsage(); //no shaders usage in proshit i think
			this.shaders = null;
			this.triangles = new LegacyTriangles(triangles);
			for (var m : materials.materials) {
				m.shader = Shader.findLegacyUsage(m.shader.getName(), plat);
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new LegacyVertices(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shader.vertexFormat_PC;
			}
			break;
		}

		this.rebuildSubBlocks();
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			if (b != null) out.write(b.save(currentPosition + out.size()));
		}

		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

//		out.write(buf.array(), 4, 4); //write correct size
		
		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		return arr;	
	}
}