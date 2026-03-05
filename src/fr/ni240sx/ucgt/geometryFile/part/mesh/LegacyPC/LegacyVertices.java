package fr.ni240sx.ucgt.geometryFile.part.mesh.LegacyPC;

import java.io.IOException;
import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;
import fr.ni240sx.ucgt.geometryFile.part.mesh.PC.Vertices_PC;

public class LegacyVertices extends Vertices_PC {


	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_LegacyVertices;}
	
	public LegacyVertices(ByteBuffer in) {
		super(in);
	}

	@Override
	public void readVertices() {
		super.readVertices();
	}
	
	public LegacyVertices() {
		super();
	}

	public LegacyVertices(Vertices vertices) {
		super(vertices);
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		return super.save(currentPosition);
	}
}
