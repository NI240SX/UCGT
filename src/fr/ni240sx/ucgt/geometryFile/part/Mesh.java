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
	
	public Mesh(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		while(in.position() < blockStart+blockLength) {
			try {
				if ((block = Block.read(in)) != null) subBlocks.add(block);
			} catch (Exception e) {
				System.out.println("Unable to read mesh sub-block : "+e.getMessage());
				e.printStackTrace();
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
				platform = Platform.PC;
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
			default:
				break;}
		}
		
//		System.out.println(info.numMaterials+" materials, "+info.numTriangles+"/"+triangles.triangles.size()+" triangles, "+info.numVertices+" vertices");
		
		if (materials != null && verticesBlocks.size() == materials.materials.size() && triangles != null) for (int i=0; i<materials.materials.size(); i++) {
			verticesBlocks.get(i).material = materials.materials.get(i);
			switch (platform) {
			case PC:
				verticesBlocks.get(i).vertexFormat = materials.materials.get(i).shaderUsage.vertexFormat_PC;
				break;
			case X360:
				verticesBlocks.get(i).vertexFormat = materials.materials.get(i).shaderUsage.vertexFormat_X360;
				break;
			default:
				break;
			
			}
			verticesBlocks.get(i).readVertices();
			materials.materials.get(i).verticesBlock = verticesBlocks.get(i);
//			System.out.println("Requesting triangles from "+(materials.materials.get(i).fromVertID/3) + " to " + (materials.materials.get(i).toVertID/3));
			materials.materials.get(i).triangles = triangles.triangles.subList(materials.materials.get(i).fromTriVertID/3, (materials.materials.get(i).toTriVertID/3));
			materials.materials.get(i).trianglesExtra = triangles.triangles.subList(materials.materials.get(i).toTriVertID/3, (materials.materials.get(i).toTriVertID/3+materials.materials.get(i).numTriVerticesExtra/3));
		} else {
			throw new Exception ("Invalid/unsupported/corrupted mesh data!");
		}
	}

	public Mesh(Platform platform) {
		this.platform = platform;
		switch (platform) {
		case PC:
			this.info = new Mesh_Info_PC();
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
		default:
			break;
		}
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