package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Geom_Unknown extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Geom_UNKNOWN;}
	
	public Geom_Unknown(ByteBuffer in) {
		var length = in.getInt();
		var blockStart = in.position();
		data = new byte[length];
		in.get(data); //raw data if there's any

		if (length > 0) System.out.println("Found Geom>Unknown block with length="+length);
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
