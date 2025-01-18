package fr.ni240sx.ucgt.geometryFile.textures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;

public class TPK extends Block {

	@Override
	public BlockType getBlockID() {
		return BlockType.TPK;
	}
	
	public TPK(ByteBuffer in) {
		var length = in.getInt();
		data = new byte[length];
		in.get(data); //raw data if there's any
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [data.length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(data.length); //length for later
		buf.put(data);
		
		return buf.array();
	}

}
