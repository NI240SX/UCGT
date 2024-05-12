package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class UnknownBlock extends Block {

	public GeomBlock getBlockID() {return GeomBlock.INVALID;}
	
	public int ID;
	
	public UnknownBlock(ByteBuffer in, int ID) {
		System.out.println("Unknown block, ID="+Integer.toHexString(Integer.reverseBytes(ID)));
		this.ID = ID;
		var length = in.getInt();
		data = new byte[length];
		in.get(data); //raw data if there's any
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [data.length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(ID); 
		buf.putInt(data.length); //length for later
		buf.put(data);
		
		return buf.array();
	}

}
