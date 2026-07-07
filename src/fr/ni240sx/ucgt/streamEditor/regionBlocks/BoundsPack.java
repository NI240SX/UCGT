package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.collisionsEditor.Collisions;
import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class BoundsPack implements Block {

	@Override
	public BlockType getBlockID() {return BlockType.BoundsPack;}
	
	public ArrayList<Collisions> subBlocks = new ArrayList<>();
	
	public BoundsPack(ByteBuffer in) throws Exception {
		var blockLength = in.getInt();
		var blockStart = in.position();
		Block block;
		
		if (Block.debug) System.out.println("\\/- Sub-blocks in BoundsPack -\\/");
		while(in.position() < blockStart+blockLength) {
			if ((block = Block.read(in)) != null) subBlocks.add((Collisions) block);
		}
		if (Block.debug) System.out.println("/\\- Sub-blocks in BoundsPack -/\\");
		
		// if there's more than one block only the last one is taken into account
		for (var b : subBlocks) {
			switch (b.getBlockID()) {
			default:
				break;
			}
		}
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
		
		for (var b : subBlocks) {
			out.write(b.save(currentPosition + out.size()));
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
