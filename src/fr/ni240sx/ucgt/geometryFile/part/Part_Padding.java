package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class Part_Padding implements Block {

	@Override
	public BlockType getBlockID() {return BlockType.Part_Padding;}
	
	public byte[] data;
	
	public Part_Padding(ByteBuffer in) {
		var length = in.getInt();
//		var blockStart = in.position();
		data = new byte[length];
		in.get(data); //raw data if there's any

//		if (!(data == new byte[12])) System.out.println("Found Part>Padding? block with data!=0");
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException {
		if (data == null) data = new byte[12];
		var arr = new byte [data.length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(data.length); //length for later
		buf.put(data);
		
		return buf.array();
	}

}
