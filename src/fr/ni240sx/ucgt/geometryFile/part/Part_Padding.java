package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Part_Padding extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Padding;}
	
	public byte[] data = new byte[12];
	
	public Part_Padding(ByteBuffer in) {
		var length = in.getInt();
		var blockStart = in.position();
		data = new byte[length];
		in.get(data); //raw data if there's any

//		if (!(data == new byte[12])) System.out.println("Found Part>Padding? block with data!=0");
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
