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

public class ChunkSmokeSpawners extends ArrayList<fr.ni240sx.ucgt.streamEditor.streamBlocks.ChunkSmokeSpawners.SmokeSpawner> implements Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6674048519507341434L;
	public int type1=11, type2=11;
//	public ArrayList<SmokeSpawner> spawners = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunkSmokeSpawners;}

	public ChunkSmokeSpawners(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);

		type1 = in.getInt(); //11
		type2 = in.getInt(); //11
//		var numStuff = 
				in.getShort(); //or block index?
		var numSpawners = 
				in.getShort();
		in.getInt();
		
//		while(in.position() < blockStart+blockLength) {
		for (int i=0; i<numSpawners;i++) {
			this.add(new SmokeSpawner(in));
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
		Utils.makeAlignment(out, Utils.findAlignment(currentPosition+out.size(), 16), (byte)0x11);

		buf = ByteBuffer.wrap(new byte[16]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(type1);
		buf.putInt(type2);
		buf.putShort((short) -1); //TODO
		buf.putShort((short) this.size());
		buf.putInt(0);
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

	
	public static class SmokeSpawner{
		public float f0;
		public float[] vector = new float[3];
		public float[] pos = new float[3];
		public int hash0, hash1, hash2, id0, id1, bytes;
		SmokeSpawner(ByteBuffer in){
			f0 = in.getFloat();
			vector[X] = in.getFloat();
			vector[Y] = in.getFloat();
			vector[Z] = in.getFloat();
			pos[X] = in.getFloat(); //NOT SAME AS WORLD OBJECTS! +Y = UP
			pos[Y] = in.getFloat();
			pos[Z] = in.getFloat();
			hash0 = in.getInt();
			hash1 = in.getInt();
			hash2 = in.getInt();
			id0 = in.getInt();
			id1 = in.getInt();
			bytes = in.getInt();
			in.position(in.position()+28);
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(80);
			bb.order(ByteOrder.LITTLE_ENDIAN);

			bb.putFloat(f0);
			bb.putFloat(vector[X]);
			bb.putFloat(vector[Y]);
			bb.putFloat(vector[Z]);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(pos[Z]);
			bb.putInt(hash0);
			bb.putInt(hash1);
			bb.putInt(hash2);
			bb.putInt(id0);
			bb.putInt(id1);
			bb.putInt(bytes);
			bb.position(bb.position()+28);
			return bb.array();
		}
	}
}
