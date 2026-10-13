package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class TrackPathBarriers extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackPathBarriers.TrackPathBarrier> implements Block {

/**
	 * 
	 */
	private static final long serialVersionUID = -8210835100273328074L;


//	public ArrayList<TrackPathBarrier> trackPathBarriers = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.TrackPathBarriers;}

	public TrackPathBarriers(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			this.add(new TrackPathBarrier(in));
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

	
	public static class TrackPathBarrier{
		public int GroupHash;
//		public int priority;
		public byte Enabled, PlayerBarrier, LeftHanded;
		public float[] from = new float[2];
		public float[] to = new float[2];
		
		TrackPathBarrier(ByteBuffer in){
			from[X] = in.getFloat();
			from[Y] = in.getFloat();
			to[X] = in.getFloat();
			to[Y] = in.getFloat();
			Enabled = in.get();
			in.get();
			PlayerBarrier = in.get();
			LeftHanded = in.get();
			GroupHash = in.getInt();
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(24);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(from[X]);
			bb.putFloat(from[Y]);
			bb.putFloat(to[X]);
			bb.putFloat(to[Y]);
			bb.put(Enabled);
			bb.put((byte)0);
			bb.put(PlayerBarrier);
			bb.put(LeftHanded);
			bb.putInt(GroupHash);
			return bb.array();
		}
	}
}
