package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.streamEditor.streamBlocks.CARP;

public class CarpWorldGrid implements Block {
	
	public int chunkID;
	public CARP CARP;
	
	@Override
	public BlockType getBlockID() {return BlockType.CarpWorldGrid;}

	public CarpWorldGrid(ByteBuffer in) { //bChunkCarpHeader
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {} //always 2x -> align 16?
		in.position(in.position()-4);
		
		//CARP block beginning
		CARP = new CARP(in);

		in.position(blockStart+blockLength);
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later
		
		out.write(buf.array());
		Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 16), (byte)0x11);

		buf = ByteBuffer.wrap(new byte[16]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		out.write(buf.array());
		
		out.write(CARP.save());		
		
		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		
		return arr;
	}
}
