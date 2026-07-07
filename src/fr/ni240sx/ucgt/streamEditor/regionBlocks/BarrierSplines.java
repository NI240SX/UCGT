package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class BarrierSplines implements Block {

	public ArrayList<Spline> splines = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.TrackPathBarriers;}

	public BarrierSplines(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			splines.add(new Spline(in));
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

		for (var z : splines) out.write(z.save());
		
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

	
	public static class Spline{
		public int hash;
		public int priority;
		public float[] from = new float[2];
		public float[] to = new float[2];
		
		Spline(ByteBuffer in){
			from[X] = in.getFloat();
			from[Y] = in.getFloat();
			to[X] = in.getFloat();
			to[Y] = in.getFloat();
			priority = in.getInt();
			hash = in.getInt();
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(24);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(from[X]);
			bb.putFloat(from[Y]);
			bb.putFloat(to[X]);
			bb.putFloat(to[Y]);
			bb.putInt(hash);
			bb.putInt(priority);
			return bb.array();
		}
	}
}
