package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunksTrackPathZones implements Block {

	public ArrayList<Zone> zones = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunksTrackPathZones;}

	public ChunksTrackPathZones(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			zones.add(new Zone(in));
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

		for (var z : zones) out.write(z.save());
		
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

	
	public static class Zone{
		public int type;
		public float[] pos = new float[2];

		public float[] boundsMin = new float[2];
		public float[] boundsMax = new float[2];
		public float[][] points;
		
		public float[] unk_f = new float[2];
		public int[] unk_i = new int[3];
		
		public int[] chunkIDs = new int[4];
		
		Zone(ByteBuffer in){
			type = in.getInt(); 
			/*
			3 = tunnel, 
			4 = overpass, 
			6 = road names ???, 
			8 = hiding spots,
			9 = regions ? PH N/S/GCH/SH/Canyons/PC, 
			13 = forbidden locations for cops ?
			15 = highway names ???
			16 = interchanges, random parking lots and the whole gold coast highway ???
			
			50 = PC
			51 = PH
			52 = Canyons
			53 = SH
			 */
			
			pos[X] = in.getFloat();
			pos[Y] = in.getFloat();
			unk_f[0] = in.getFloat();
			unk_f[1] = in.getFloat();
			unk_i[0] = in.getInt();
			unk_i[1] = in.getInt();
			unk_i[2] = in.getInt();
	
			boundsMin[X] = in.getFloat();
			boundsMin[Y] = in.getFloat();
			boundsMax[X] = in.getFloat();
			boundsMax[Y] = in.getFloat();
	
			chunkIDs[0] = in.getInt();
			chunkIDs[1] = in.getInt();
			chunkIDs[2] = in.getInt();
			chunkIDs[3] = in.getInt();
	
			var numPoints = in.getShort();
			in.getShort(); //thisDataLength = 68+numPoints*8
			points = new float[numPoints][2];
			for (int i=0; i<numPoints; i++) {
				points[i][X] = in.getFloat();
				points[i][Y] = in.getFloat();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(68+numPoints()*8);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(type);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(unk_f[0]);
			bb.putFloat(unk_f[1]);
			bb.putInt(unk_i[0]);
			bb.putInt(unk_i[1]);
			bb.putInt(unk_i[2]);
			bb.putFloat(boundsMin[X]);
			bb.putFloat(boundsMin[Y]);
			bb.putFloat(boundsMax[X]);
			bb.putFloat(boundsMax[Y]);
			bb.putInt(chunkIDs[0]);
			bb.putInt(chunkIDs[1]);
			bb.putInt(chunkIDs[2]);
			bb.putInt(chunkIDs[3]);
			bb.putShort((short) numPoints());
			bb.putShort((short)(68+numPoints()*8));
			for (int i=0; i<numPoints(); i++) {
				bb.putFloat(points[i][X]);
				bb.putFloat(points[i][Y]);
			}
			return bb.array();
		}
		
		public int numPoints() {
			return points.length;
		}
	}
}
