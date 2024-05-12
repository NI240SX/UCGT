package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Padding extends Block {
	
	public GeomBlock getBlockID() {return GeomBlock.Padding;}
	public static final int PaddingModulo = 128;
	
	public int length = 0;
	
	public Padding(ByteBuffer in) {
		length = in.getInt();
		var blockStart = in.position();
		in.position(blockStart+length);
	}

	//TODO will need another save() method to create proper padding with proper saving
	
	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(length); //length for later

		return buf.array();
	}

	public static void makePadding(ByteArrayOutputStream out) throws IOException {
		int paddingLength = 128 - ((out.size() + 8) % PaddingModulo);
		
		var buf = ByteBuffer.wrap(new byte[paddingLength + 8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(GeomBlock.Padding.getKey());
		buf.putInt(paddingLength);

		out.write(buf.array());
	}
}
