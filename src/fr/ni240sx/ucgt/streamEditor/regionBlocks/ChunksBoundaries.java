package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunksBoundaries implements Block {

	public ArrayList<ChunkBoundary> boundaries = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.VisibleSections_Boundaries;}

	public ChunksBoundaries(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			boundaries.add(new ChunkBoundary(in));
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

		for (var z : boundaries) out.write(z.save());
		
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

	
	public static class ChunkBoundary{
		public int type1, type2, elevationHash, unk3;
		public short ID;
		public short ID_over;
		public short unk2;
		public byte type;
		
		public float[] pos = new float[2];
		public float[] boundsMin = new float[2];
		public float[] boundsMax = new float[2];
		public float[][] points;
				
		ChunkBoundary(ByteBuffer in){
			type1 = in.getInt(); //11
			type2 = in.getInt(); //11
			ID = in.getShort(); //id ?
			var numPoints = in.get();
			type = in.get(); // 0=chunks, 1=panoramas
			ID_over = in.getShort();
			unk2 = in.getShort();
			elevationHash = in.getInt();
			unk3 = in.getInt();
				
			boundsMin[X] = in.getFloat();
			boundsMin[Y] = in.getFloat();
			boundsMax[X] = in.getFloat();
			boundsMax[Y] = in.getFloat();
			pos[X] = in.getFloat();
			pos[Y] = in.getFloat();

			points = new float[numPoints][2];
			for (int i=0; i<numPoints; i++) {
				points[i][X] = in.getFloat();
				points[i][Y] = in.getFloat();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48+numPoints()*8);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(type1);
			bb.putInt(type2);
			bb.putShort(ID);
			bb.put((byte)numPoints());
			bb.put(type);
			bb.putShort(ID_over);
			bb.putShort(unk2);
			bb.putInt(elevationHash);
			bb.putInt(unk3);
			bb.putFloat(boundsMin[X]);
			bb.putFloat(boundsMin[Y]);
			bb.putFloat(boundsMax[X]);
			bb.putFloat(boundsMax[Y]);
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
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
