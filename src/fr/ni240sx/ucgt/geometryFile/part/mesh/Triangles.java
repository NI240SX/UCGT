package fr.ni240sx.ucgt.geometryFile.part.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Triangles extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.Part_Mesh_Triangles;}
	
	public static final int triangleLength = 6;

	public ArrayList<Triangle> triangles = new ArrayList<>();
	
	public Triangles(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		while(in.getInt() == 0x11111111) {} // skip alignment
		in.position(in.position()-4);
		
		Triangle t;
		while (in.position() <= blockStart+blockLength-triangleLength) {
			triangles.add(t = new Triangle());
			t.vert0 = in.getShort();
			t.vert1 = in.getShort();
			t.vert2 = in.getShort();
		}
		in.position(blockStart+blockLength);
	}

	public Triangles() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {

		var alignment = Block.findAlignment(currentPosition+8, 32);
		var dataLength = triangleLength*triangles.size() + Block.findAlignment(triangleLength*triangles.size(), 4);
		var out = ByteBuffer.wrap(new byte[dataLength + 8 + alignment]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(dataLength + alignment);
		
		Block.makeAlignment(out, alignment, (byte) 0x11);
		
		for (var t : triangles) {
			out.putShort(t.vert0);
			out.putShort(t.vert1);
			out.putShort(t.vert2);
		}

		return out.array();	
	}
}