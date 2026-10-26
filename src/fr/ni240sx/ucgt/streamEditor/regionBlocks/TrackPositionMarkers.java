package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import fr.ni240sx.ucgt.shared.Utils;

import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class TrackPositionMarkers extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackPositionMarkers.TrackPositionMarker> implements Block {

//	public ArrayList<TrackPositionMarker> trackPositionMarkers = new ArrayList<>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7992420016088859679L;


	@Override
	public BlockType getBlockID() {return BlockType.ChunksPositionMarkers;}

	public TrackPositionMarkers(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			this.add(new TrackPositionMarker(in));
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
		Utils.makeAlignment(out, Utils.findAlignment(currentPosition+out.size(), 48), (byte)0x11);

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

	
	public static class TrackPositionMarker{
		public int NameHash, Param, TrackNumber;
		public float[] pos = new float[3];
		public byte[] SceneryGroupNumber = new byte[9];
		public byte NumSceneryGroups = 0;
		public short Angle;

		TrackPositionMarker(ByteBuffer in){
			in.getInt(); //11 -> bTNode<TrackPositionMarker>
			in.getInt(); //11
			NameHash = in.getInt(); //unknown, hash?
			Param = in.getInt(); //unknown, hash?
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
			TrackNumber = in.getInt(); //region number (8000 or 8999)
			in.get(SceneryGroupNumber); //AAAAAAAA AAAAAAAA AA
			NumSceneryGroups = in.get(); //00
			Angle = in.getShort(); //unknown, not a chunk id
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(11);
			bb.putInt(11);
			bb.putInt(NameHash);
			bb.putInt(Param);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(pos[Z]);
			bb.putInt(0);
			bb.putInt(TrackNumber);
			bb.put(SceneryGroupNumber);
			bb.put(NumSceneryGroups);
			bb.putShort(Angle);

			return bb.array();
		}
	}
}
