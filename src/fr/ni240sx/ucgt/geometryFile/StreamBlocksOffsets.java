package fr.ni240sx.ucgt.geometryFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;

public class StreamBlocksOffsets extends Block {

	public static final int infoLength = 92;
	public ArrayList<ChunkInfo> chunkInfos = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {
		// TODO Auto-generated method stub
		return BlockType.StreamBlocksOffsets;
	}

	public StreamBlocksOffsets(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.position()<blockStart+blockLength) {
			chunkInfos.add(new ChunkInfo(in));
		}
		in.position(blockStart+blockLength);
	}
	
	public void update(String blockID, int deltaLength) {
		int i = 0;
		for (i = 0; i < chunkInfos.size(); i++) {
			if (chunkInfos.get(i).name.equals(blockID)) {
				chunkInfos.get(i).length1 += deltaLength;
				chunkInfos.get(i).length2 += deltaLength;
				break;
			}
		}
		for (int j = i+1; j < chunkInfos.size(); j++) {
			chunkInfos.get(j).offset += deltaLength;
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var bb = ByteBuffer.wrap(new byte[infoLength * chunkInfos.size() + 8]);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		bb.putInt(getBlockID().getKey());
		bb.putInt(infoLength * chunkInfos.size());
		
		for (var i : chunkInfos) {
			i.save(bb);
		}
		return bb.array();
	}
	/*
	 * each block data consists of :
	 * name					ID						offset	length		length	stuff										hash probably
	 * X0					23000							362032		362032	66696	
	 * 58300000 00000000 d8590000 00000000 01000000 00000000 30860500 30860500 88040100 01000000 00000000 00000000 00000000 04e7968a 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * Y0
	 * 59300000 00000000 c05d0000 00000000 01000000 00503300 b0fd1200 b0fd1200 08060100 03000000 00000000 00000000 00000000 d1744b64 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * Y112
	 * 59313132 00000000 305e0000 00000000 01000000 00102405 30ad0000 30ad0000 88010000 73000000 00000000 00000000 00000000 96f84124 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * and we just need to adjust the length for a given block
	 * this can be kept in memory (list of UnknownBlock with byte array storage, except block 10410300 with objects)
	 * more blocks follow, apparently we don't need to edit them
	 */
	public class ChunkInfo{
		
		String name; //eg X0, on 8 bytes
		int ID; //23000
		int const01; //0
		int const02; //1
		int offset; //whatever
		int oldOffset;
		int length1;
		int length2;
		byte[] otherData = new byte[60];
		
		public ChunkInfo(ByteBuffer in) {
			var beginning = in.position();
			name = Block.readString(in);
			in.position(beginning+8);
			ID = in.getInt();
			const01 = in.getInt();
			const02 = in.getInt();
			offset = in.getInt();
			oldOffset = offset;
			length1 = in.getInt();
			length2 = in.getInt();
			in.get(otherData);
		}

		public void save(ByteBuffer bb) {
			var beginning = bb.position();
			Block.putString(bb, name, 8);
			bb.position(beginning+8);
			bb.putInt(ID);
			bb.putInt(const01);
			bb.putInt(const02);
			bb.putInt(offset);
			bb.putInt(length1);
			bb.putInt(length2);
			bb.put(otherData);
		}
	}
}
