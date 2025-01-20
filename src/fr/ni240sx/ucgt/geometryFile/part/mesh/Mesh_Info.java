package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public abstract class Mesh_Info extends Block {

	public int version;

	//flags, may be linked to how simplified the part is : 0x8041A302 = full part, 80410000 = less detail (brake, exhaust_tip)
	public byte[] flags = new byte[4];
			
	public int numMaterials;

	public int numTriangles;
	public int numTrianglesExtra;
	
	public int numVertices;
	
	@Override
	public abstract BlockType getBlockID();

	@Override
	public abstract byte[] save(int currentPosition) throws IOException, InterruptedException;

}
