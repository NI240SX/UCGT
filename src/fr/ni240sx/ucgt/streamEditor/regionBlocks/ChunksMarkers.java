package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunksMarkers implements Block {

	public ArrayList<Marker> markers = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunksPositionMarkers;}

	public ChunksMarkers(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			markers.add(new Marker(in));
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
		Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 48), (byte)0x11);

		for (var z : markers) out.write(z.save());
		
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

	
	public static class Marker{
		public int type1, type2, hash, hash2, region;
		public short sh;
		public float[] pos = new float[3];

		Marker(ByteBuffer in){
			type1 = in.getInt(); //11
			type2 = in.getInt(); //11
			hash = in.getInt(); //unknown, hash?
			hash2 = in.getInt(); //unknown, hash?
			/*
			 * B8560C7D = BustedAlley
			 * B803A89F = BustedCity
			 * A87C527E = BustedHwy
			 * EC90A22A = pursuit breaker
			 * others...
			 */
			pos[X] = in.getFloat();
			pos[Y] = in.getFloat();
			pos[Z] = in.getFloat();
			in.getInt();
			region = in.getInt(); //region number (8000 or 8999)
			in.getInt(); //AAAAAAAA
			in.getInt(); //AAAAAAAA
			in.get(); //AA
			in.get(); //00
			sh = in.getShort(); //unknown, not a chunk id
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(type1);
			bb.putInt(type2);
			bb.putInt(hash);
			bb.putInt(hash2);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(pos[Z]);
			bb.putInt(0);
			bb.putInt(region);
			bb.putInt(0xAAAAAAAA);
			bb.putInt(0xAAAAAAAA);
			bb.put((byte)0xAA);
			bb.put((byte)0x00);
			bb.putShort(sh);

			return bb.array();
		}
	}
}
