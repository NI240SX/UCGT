package fr.ni240sx.ucgt.streamEditor.streamBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunkScenery_Instances implements Block {

	public ArrayList<Position> positions = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunkScenery_Instances;}

	public ChunkScenery_Instances(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			positions.add(new Position(in));
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
		Block.makeAlignment(out, Block.findAlignment(currentPosition+out.size(), 16), (byte)0x11);

		for (var z : positions) out.write(z.save());
		
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

	
	public static class Position{
		public short objectIndex, objectID, v1, v2, v3, v4, v5, v6, v7, v8; 
		public short groupID, instanceID;
		//int hash;
		public float[] pos = new float[3];
		public float[] boundsMin = new float[3];
		public float[] boundsMax = new float[3];
		public float[][] matrix = new float[3][3];

		Position(ByteBuffer in){
			boundsMin[X] = in.getFloat();
			boundsMin[Y] = in.getFloat();
			boundsMin[Z] = in.getFloat();

			v1 = in.getShort();
			objectID = in.getShort();

			boundsMax[X] = in.getFloat();
			boundsMax[Y] = in.getFloat();
			boundsMax[Z] = in.getFloat();
			v2 = in.getShort();
			objectIndex = in.getShort();
			
			pos[X] = in.getFloat();
			pos[Y] = in.getFloat();
			pos[Z] = in.getFloat();
			instanceID = in.getShort();
			groupID = in.getShort();
//			hash = in.getInt();
			
			matrix[0][0] = in.getFloat();
			matrix[1][0] = in.getFloat();
			matrix[2][0] = in.getFloat();
			v3 = in.getShort();
			v4 = in.getShort();
			
			matrix[0][1] = in.getFloat();
			matrix[1][1] = in.getFloat();
			matrix[2][1] = in.getFloat();
			v5 = in.getShort();
			v6 = in.getShort();
			
			matrix[0][2] = in.getFloat();
			matrix[1][2] = in.getFloat();
			matrix[2][2] = in.getFloat();
			v7 = in.getShort();
			v8 = in.getShort();
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putFloat(boundsMin[X]);
			bb.putFloat(boundsMin[Y]);
			bb.putFloat(boundsMin[Z]);
			bb.putShort(v1);
			bb.putShort(objectID);

			bb.putFloat(boundsMax[X]);
			bb.putFloat(boundsMax[Y]);
			bb.putFloat(boundsMax[Z]);
			bb.putShort(v2);
			bb.putShort(objectIndex);
			
			bb.putFloat(pos[X]);
			bb.putFloat(pos[Y]);
			bb.putFloat(pos[Z]);
			bb.putShort(instanceID);
			bb.putShort(groupID);

			bb.putFloat(matrix[0][0]);
			bb.putFloat(matrix[1][0]);
			bb.putFloat(matrix[2][0]);
			bb.putShort(v3);
			bb.putShort(v4);

			bb.putFloat(matrix[0][1]);
			bb.putFloat(matrix[1][1]);
			bb.putFloat(matrix[2][1]);
			bb.putShort(v5);
			bb.putShort(v6);

			bb.putFloat(matrix[0][2]);
			bb.putFloat(matrix[1][2]);
			bb.putFloat(matrix[2][2]);
			bb.putShort(v7);
			bb.putShort(v8);

			return bb.array();
		}
	}
}
