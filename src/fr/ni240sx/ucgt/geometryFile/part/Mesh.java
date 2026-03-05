package fr.ni240sx.ucgt.geometryFile.part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.Platform;
import fr.ni240sx.ucgt.geometryFile.part.mesh.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.*;
import fr.ni240sx.ucgt.geometryFile.part.mesh.X360.*;

public class Mesh extends Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh;}
	
	public Platform platform;
	public Mesh_Info info;
	public Materials materials;
	public ShadersUsage shadersUsage;
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
				shadersUsage = (ShadersUsage) b;
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
				platform = Platform.X360;
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

		ShaderUsage previousSU = null;
		int j=0;
		if (materials != null && triangles != null) for (int i=0; i<materials.materials.size(); i++) {
			switch (platform) {
			case PC:
			case Prostreet_PC:
			case Prostreet_X360: //PS X360
			case Carbon_PC: //carbono
				verticesBlocks.get(j).vertexFormat = materials.materials.get(i).shaderUsage.vertexFormat_PC;
				break;
			case X360:
				verticesBlocks.get(j).vertexFormat = materials.materials.get(i).shaderUsage.vertexFormat_X360;
				break;
			}
			verticesBlocks.get(j).readVertices();

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
						if (verticesBlocks.get(k).vertexFormat == null) verticesBlocks.get(k).vertexFormat = materials.materials.get(i).shaderUsage.vertexFormat_PC;
						verticesBlocks.get(k).readVertices();
						materials.materials.get(i).verticesBlock = verticesBlocks.get(k);
					}
				}
			}

			//added because stupid carbon
			if (platform == Platform.Carbon_PC) {
				if (previousSU != null && !previousSU.equals(materials.materials.get(i).shaderUsage)) j++;
				previousSU = materials.materials.get(i).shaderUsage;
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
			this.shadersUsage = new ShadersUsage();
			this.triangles = new Triangles_PC();
			break;
		case X360:
			this.info = new Mesh_Info_X360();
			this.materials = new Materials_X360();
			this.shadersUsage = new ShadersUsage();
			this.triangles = new Triangles_X360();
			break;
		case Prostreet_PC:
		case Prostreet_X360:
		case Carbon_PC:
			this.info = new Mesh_Info_PC(platform);
			this.materials = new LegacyMaterials();
//			this.shadersUsage = new ShadersUsage(); //no shaders usage in proshit i think
			this.shadersUsage = null;
			this.triangles = new LegacyTriangles();
			break;
		}
	}
	
	public void rebuildSubBlocks() {
		subBlocks.clear();
		subBlocks.add(info);
		if (platform == Platform.PC || platform == Platform.X360) subBlocks.add(shadersUsage);
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
			this.shadersUsage = new ShadersUsage();
			this.triangles = new Triangles_PC(triangles);
			for (var m : materials.materials) {
//				System.out.println(m.shaderUsage.getName() + " is " + m.shaderUsage.getClass().getSimpleName());
				m.shaderUsage = ShaderUsage.getNGOrDefault(m.shaderUsage.getName(), ShaderUsage.get("Diffuse"));
//				System.out.println("new "+m.shaderUsage.getName() + " is " + m.shaderUsage.getClass().getSimpleName());
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new Vertices_PC(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shaderUsage.vertexFormat_PC;
			}
			break;
		case X360:
			this.info = new Mesh_Info_X360(info);
			this.materials = new Materials_X360(materials);
			this.shadersUsage = new ShadersUsage();
			this.triangles = new Triangles_X360(triangles);
			for (var m : materials.materials) {
				m.shaderUsage = ShaderUsage.getNGOrDefault(m.shaderUsage.getName(), ShaderUsage.get("Diffuse"));
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new Vertices_X360(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shaderUsage.vertexFormat_X360;
			}
			break;
		case Prostreet_PC:
		case Prostreet_X360:
		case Carbon_PC:
			this.info = new Mesh_Info_PC(info);
			((Mesh_Info_PC)info).updatePlatform(plat);
			this.materials = new LegacyMaterials(materials);
//			this.shadersUsage = new ShadersUsage(); //no shaders usage in proshit i think
			this.shadersUsage = null;
			this.triangles = new LegacyTriangles(triangles);
			for (var m : materials.materials) {
				m.shaderUsage = ShaderUsage.findLegacyUsage(m.shaderUsage.getName(), plat);
			}
			for (int i=0; i< verticesBlocks.size(); i++) {
				verticesBlocks.set(i, new LegacyVertices(verticesBlocks.get(i)));
				verticesBlocks.get(i).vertexFormat = verticesBlocks.get(i).material.shaderUsage.vertexFormat_PC;
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