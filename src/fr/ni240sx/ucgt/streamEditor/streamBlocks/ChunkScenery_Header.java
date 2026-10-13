package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

//ScenerySectionHeader
public class ChunkScenery_Header implements Block {

	@Override
	public BlockType getBlockID() {return BlockType.ChunkScenery_Header;}

	public int chunkID;
	
	public ChunkScenery_Header(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		chunkID = in.getInt(blockStart+12);
		in.position(blockStart+blockLength);
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var buf = ByteBuffer.wrap(new byte[84+8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(84); //length for later

		buf.putInt(12+8, chunkID);
		
		return buf.array();	
	}
}
