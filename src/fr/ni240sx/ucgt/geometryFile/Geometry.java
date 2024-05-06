package fr.ni240sx.ucgt.geometryFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;

public class Geometry extends Block {

	public static final GeomChunk BlockID = GeomChunk.Geometry;
	
	public Geometry(ByteBuffer in) {
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		var blockLength = in.getInt();
		in.getInt();
		in.getInt(); // skip header and common stuff
		var blockStart = in.position();
		
		while(in.position() < blockStart+blockLength) {
			subBlocks.add(Block.read(in));
		}
	}
	
	@Override
	public byte[] save() {
		// TODO Auto-generated method stub
		return null;
	}
}
