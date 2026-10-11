package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class ChunkScenery implements Block {

	@Override
	public BlockType getBlockID() {return BlockType.ChunkScenery;}

	public ArrayList<Block> subBlocks = new ArrayList<>();
	
	public ChunkScenery_Header header;
	public ChunkScenery_Instances instances;
	public ChunkScenery_Objects objects;
	
	public ChunkScenery(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		if (Block.debug) System.out.println("\\/- Sub-blocks in ChunksObjects -\\/");
		while(in.position() < blockStart+blockLength) {
			if ((block = Block.read(in)) != null) subBlocks.add(block);
		}
		in.position(blockStart+blockLength);
		
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			case ChunkScenery_Instances:
				instances = (ChunkScenery_Instances) b;
				break;
			case ChunkScenery_Objects:
				objects = (ChunkScenery_Objects) b;
				break;
			case ChunkScenery_Header:
				header = (ChunkScenery_Header) b;
			case ChunkScenery_Ads:
			case ChunkScenery_TreeNodes:
			//0x00034106: list of ID (short), other ID (short) starting from 0 -> hashmap???
			//scenery override hookups
			default:
				break;
			}
		}
		if (Block.debug) System.out.println("/\\- Sub-blocks in ChunksObjects -/\\");
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			out.write(b.save(out.size()));
		}

		buf = ByteBuffer.wrap(new byte[4]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(out.size()-8);

//		out.write(buf.array(), 4, 4); //write correct size
		
		var arr = out.toByteArray();
		arr[4] = buf.array()[0];
		arr[5] = buf.array()[1];
		arr[6] = buf.array()[2];
		arr[7] = buf.array()[3]; //writes the correct size
		return arr;	
	}
}
