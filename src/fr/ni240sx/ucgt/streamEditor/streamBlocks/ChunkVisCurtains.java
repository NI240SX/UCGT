package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunkVisCurtains extends ArrayList<fr.ni240sx.ucgt.streamEditor.streamBlocks.ChunkVisCurtains.VisCurtain> implements Block {

/**
	 * 
	 */
	private static final long serialVersionUID = 6204170888488720681L;


//	public ArrayList<VisCurtain> curtains = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunkVisCurtains;}

	public ChunkVisCurtains(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			this.add(new VisCurtain(in));
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
		Utils.makeAlignment(out, Utils.findAlignment(currentPosition+out.size(), 128), (byte)0x11);
		
		for (var z : this) out.write(z.save());
		
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

	
	public static class VisCurtain{
		public float[] from = new float[3];
		public float[] to = new float[3];
		public short chunkID, type, v3, v4;
		public float[] vector = new float[3];
		
		VisCurtain(ByteBuffer in){
			try {
			from[X] = in.getFloat();
			from[Y] = in.getFloat();
			from[Z] = in.getFloat();
			chunkID = in.getShort(); //i think
			type = in.getShort(); //2, 4
			to[X] = in.getFloat();
			to[Y] = in.getFloat();
			to[Z] = in.getFloat();
			v3 = in.getShort();
			v4 = in.getShort(); //0xAEE1 most of the times
			vector[X] = in.getFloat();
			vector[Y] = in.getFloat();
			vector[Z] = in.getFloat();
			in.getInt(); //AAAAAAAA
			} catch (Exception e) {
				//X360
				e.printStackTrace();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48);
			bb.order(ByteOrder.LITTLE_ENDIAN);

			bb.putFloat(from[X]);
			bb.putFloat(from[Y]);
			bb.putFloat(from[Z]);
			bb.putShort(chunkID);
			bb.putShort(type);
			bb.putFloat(to[X]);
			bb.putFloat(to[Y]);
			bb.putFloat(to[Z]);
			bb.putShort(v3);
			bb.putShort(v4);
			bb.putFloat(vector[X]);
			bb.putFloat(vector[Y]);
			bb.putFloat(vector[Z]);
			bb.putInt(0xAAAAAAAA);
			return bb.array();
		}
	}
}
