package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class ChunksElevationRules implements Block {

	public ArrayList<ElevationRuleTriangle> elevationRuleTriangles = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.ChunksElevationRules;}

	public ChunksElevationRules(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			elevationRuleTriangles.add(new ElevationRuleTriangle(in));
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

		for (var z : elevationRuleTriangles) out.write(z.save());
		
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

	
	public static class ElevationRuleTriangle{
		public int hash;

		public float[][] points = new float[3][3];
				
		ElevationRuleTriangle(ByteBuffer in){
			hash = in.getInt(); 
			in.getInt();
			in.getInt();
			in.getInt();
			for (int i=0; i<numPoints(); i++) {
				points[i][X] = in.getFloat();
				points[i][Y] = in.getFloat();
				points[i][Z] = in.getFloat();
				in.getInt();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(64);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(hash);
			bb.putInt(0);
			bb.putInt(0);
			bb.putInt(0);
			for (int i=0; i<numPoints(); i++) {
				bb.putFloat(points[i][X]);
				bb.putFloat(points[i][Y]);
				bb.putFloat(points[i][Z]);
				bb.putInt(0);
			}
			return bb.array();
		}
		
		public static int numPoints() {
			return 3;
		}
	}
}
