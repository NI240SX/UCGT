package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class VisibleSectionBoundaries extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.VisibleSectionBoundaries.VisibleSectionBoundary> implements Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5147477644502458735L;
//	public ArrayList<VisibleSectionBoundary> boundaries = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {return BlockType.VisibleSections_Boundaries;}

	public VisibleSectionBoundaries(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			this.add(new VisibleSectionBoundary(in));
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

	
	public static class VisibleSectionBoundary{
//		public int elevationHash, unk3;
		public short SectionNumber;
//		public short ID_over;
//		public short unk2;
		public byte PanoramaBoundary;

		public short[] DepthSectionNumbers = new short[2];
		public int[] DepthNames = new int[2];
		
		public float[] Centre = new float[2];
		public float[] boundsMin = new float[2];
		public float[] boundsMax = new float[2];
		public float[][] points;
				
		VisibleSectionBoundary(ByteBuffer in){
			in.getInt(); //11 -> bTNode<VisibleSectionBoundary>
			in.getInt(); //11
			SectionNumber = in.getShort(); //id ?
			var numPoints = in.get();
			PanoramaBoundary = in.get(); // 0=chunks, 1=panoramas
//			ID_over = in.getShort();
//			unk2 = in.getShort();
//			elevationHash = in.getInt();
//			unk3 = in.getInt();
			DepthSectionNumbers[0] = in.getShort();
			DepthSectionNumbers[1] = in.getShort();
			DepthNames[0] = in.getInt();
			DepthNames[1] = in.getInt();
				
			boundsMin[X] = in.getFloat();
			boundsMin[Y] = in.getFloat();
			boundsMax[X] = in.getFloat();
			boundsMax[Y] = in.getFloat();
			Centre[X] = in.getFloat();
			Centre[Y] = in.getFloat();

			points = new float[numPoints][2];
			for (int i=0; i<numPoints; i++) {
				points[i][X] = in.getFloat();
				points[i][Y] = in.getFloat();
			}
		}
		
		public byte[] save() {
			var bb = ByteBuffer.allocate(48+numPoints()*8);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(11);
			bb.putInt(11);
			bb.putShort(SectionNumber);
			bb.put((byte)numPoints());
			bb.put(PanoramaBoundary);
			bb.putShort(DepthSectionNumbers[0]);
			bb.putShort(DepthSectionNumbers[1]);
			bb.putInt(DepthNames[0]);
			bb.putInt(DepthNames[1]);
			bb.putFloat(boundsMin[X]);
			bb.putFloat(boundsMin[Y]);
			bb.putFloat(boundsMax[X]);
			bb.putFloat(boundsMax[Y]);
			bb.putFloat(Centre[X]);
			bb.putFloat(Centre[Y]);
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
