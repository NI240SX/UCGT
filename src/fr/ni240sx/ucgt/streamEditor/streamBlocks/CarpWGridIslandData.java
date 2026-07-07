package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class CarpWGridIslandData implements Block {
	
	public int chunkID;
	public CARP CARP;
	
	@Override
	public BlockType getBlockID() {return BlockType.CarpWGridIslandData;}

	public CarpWGridIslandData(ByteBuffer in) { //bChunkCarpHeader
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {} //always 2x -> align 16?
		in.position(in.position()-4);

		//var CARPdataLength = 
				in.getInt(); //mCrpSize
		chunkID = in.getInt(); //mSectionNumber
		in.getInt(); //mFlags
		in.getInt(); //mLastAddress
		
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
		buf.putInt(4, chunkID);
		out.write(buf.array());
				
		var carpBegin = out.size();
		out.write(CARP.save());
		
		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-carpBegin);
		
		var buf2 = ByteBuffer.wrap(new byte[4]);
		buf2.order(ByteOrder.LITTLE_ENDIAN);
		buf2.putInt(out.size()-8);

		var arr = out.toByteArray();
		arr[4] = buf2.array()[0];
		arr[5] = buf2.array()[1];
		arr[6] = buf2.array()[2];
		arr[7] = buf2.array()[3]; //writes the correct size

		arr[carpBegin-16] = buf.array()[0];
		arr[carpBegin-15] = buf.array()[1];
		arr[carpBegin-14] = buf.array()[2];
		arr[carpBegin-13] = buf.array()[3]; //writes the correct CARP size
		return arr;
	}
}
