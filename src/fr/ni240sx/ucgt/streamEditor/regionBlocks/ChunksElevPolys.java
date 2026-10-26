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

public class ChunksElevPolys extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.ChunksElevPolys.ElevPoly> implements Block {

//	public ArrayList<ElevPoly> elevPolies = new ArrayList<>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1100173830974207614L;


	@Override
	public BlockType getBlockID() {return BlockType.ChunksElevationRules;}

	public ChunksElevPolys(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.getInt() == 0x11111111) {}
		in.position(in.position()-4);
		while(in.position() < blockStart+blockLength) {
			this.add(new ElevPoly(in));
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
		Utils.makeAlignment(out, Utils.findAlignment(currentPosition+out.size(), 16), (byte)0x11);
		
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

	
	public static class ElevPoly{
		public int DepthName;

		public float[][] vertices = new float[3][3];
				
		ElevPoly(ByteBuffer in){
			DepthName = in.getInt(); 
			in.getInt();
			in.getInt();
			in.getInt();
			for (int i=0; i<numPoints(); i++) {
				vertices[i][X] = in.getFloat();
				vertices[i][Y] = in.getFloat();
				vertices[i][Z] = in.getFloat();
				in.getInt();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(64);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(DepthName);
			bb.putInt(0);
			bb.putInt(0);
			bb.putInt(0);
			for (int i=0; i<numPoints(); i++) {
				bb.putFloat(vertices[i][X]);
				bb.putFloat(vertices[i][Y]);
				bb.putFloat(vertices[i][Z]);
				bb.putInt(0);
			}
			return bb.array();
		}
		
		public static int numPoints() {
			return 3;
		}
	}
}
