package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class ChunksRelations implements Block {

	public ArrayList<ChunksRelated> chunksRelated = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.VisibleSections_Relations;}

	public ChunksRelations(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			chunksRelated.add(new ChunksRelated(in));
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());

		for (var z : chunksRelated) out.write(z.save());
		
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

	
	public static class ChunksRelated{
		public int type1, type2, type;
		public short ID;
		public ArrayList<Short> relatedChunkIDs = new ArrayList<>();
		
		ChunksRelated(ByteBuffer in){
			type1 = in.getInt(); //11
			type2 = in.getInt(); //11
			type = in.getInt(); //0
			ID = in.getShort();
			in.getShort(); //dataCount
			var dataCapacity = in.getShort();
			var dataCount = in.getShort();
			for (int i=0; i<dataCount; i++) {
				relatedChunkIDs.add(in.getShort()); //related chunk id
			}
			for (int i=dataCount; i<dataCapacity; i++) in.getShort(); //filler
			in.getInt();
		}
		
		public byte[] save() {
			short dataCount = (short) relatedChunkIDs.size();
			short dataCapacity = (short) (dataCount+(dataCount%2 == 0 ? 6 : 7));
			var bb = ByteBuffer.allocate(dataCapacity*2+24);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(type1);
			bb.putInt(type2);
			bb.putInt(type);
			bb.putShort(ID);
			bb.putShort(dataCount);
			bb.putShort(dataCapacity);
			bb.putShort(dataCount);
			for (int i=0; i<dataCount; i++) {
				bb.putShort(relatedChunkIDs.get(i));
			}
			for (int i=dataCount; i<dataCapacity; i++) bb.putShort((short) 0); //filler
			bb.putInt(0);
			return bb.array();
		}
	}
}
