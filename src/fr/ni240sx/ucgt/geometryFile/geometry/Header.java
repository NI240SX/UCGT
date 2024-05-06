package fr.ni240sx.ucgt.geometryFile.geometry;

import java.nio.ByteBuffer;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomChunk;

public class Header extends Block {

	public static final GeomChunk BlockID = GeomChunk.Geom_Header;
	
	public Header(ByteBuffer in) {
		var blockLength = in.getInt();
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
