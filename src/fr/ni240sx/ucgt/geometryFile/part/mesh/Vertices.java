package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Vertices extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_Vertices;}
	
	public static final int vertexLength = 32;

	public ArrayList<Vertex> vertices = new ArrayList<Vertex>();

	public Material material;
	
	public Vertices(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		while (in.position() < blockStart+blockLength) {
			vertices.add(new Vertex(in));
		}
		in.position(blockStart+blockLength);
	}

	public Vertices() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[vertexLength*vertices.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(vertexLength*vertices.size() + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		for (var v : vertices) {
			v.save(out);
		}

		return out.array();	
	}
}