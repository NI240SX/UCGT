package fr.ni240sx.ucgt.geometryFile.part.mesh.X360;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex;
import fr.ni240sx.ucgt.geometryFile.part.mesh.Vertices;

public class Vertices_X360 extends Vertices {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Mesh_Vertices_X360;}

	public Vertices_X360(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);

		verticesData = new byte[blockLength - (in.position()-blockStart)];
		in.get(verticesData);
		
		in.position(blockStart+blockLength);
	}
	
	@Override
	public void readVertices() {
		var bb = ByteBuffer.wrap(verticesData);
		bb.order(ByteOrder.LITTLE_ENDIAN);
//		System.out.println("Vertex format to use "+vertexFormat.getName());
		while (bb.position() < bb.capacity()) {
			vertices.add(new Vertex(bb, vertexFormat));
		}
		bb = null;
		verticesData = null; //remove the temporarily stored data from memory
	}
	
	public Vertices_X360() {
	}


	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 128);
		var out = ByteBuffer.wrap(new byte[vertexFormat.getLength()*vertices.size() + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(vertexFormat.getLength()*vertices.size() + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		for (var v : vertices) {
			v.save(out, vertexFormat);
		}

		return out.array();	
	}
}
