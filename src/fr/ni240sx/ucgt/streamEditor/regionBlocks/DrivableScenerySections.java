package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;

public class DrivableScenerySections extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.DrivableScenerySections.DrivableScenerySection> implements Block {

//	public ArrayList<DrivableScenerySection> drivableScenerySection = new ArrayList<>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8472025785878957482L;


	@Override
	public BlockType getBlockID() {return BlockType.VisibleSections_Relations;}

	public DrivableScenerySections(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while(in.position() < blockStart+blockLength) {
			this.add(new DrivableScenerySection(in));
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

	
	public static class DrivableScenerySection{
		public short SectionNumber, MostVisibleSections;
		public ArrayList<Short> relatedChunkIDs = new ArrayList<>();
		
		DrivableScenerySection(ByteBuffer in){
			in.getInt(); //11 -> bTNode<DrivableScenerySection>
			in.getInt(); //11
			in.getInt(); //0 -> pBoundary
			SectionNumber = in.getShort();
			MostVisibleSections = in.getShort(); //dataCount
			var MaxVisibleSections = in.getShort();
			var NumVisibleSections = in.getShort();
			for (int i=0; i<NumVisibleSections; i++) {
				relatedChunkIDs.add(in.getShort()); //related chunk id
			}
			for (int i=NumVisibleSections; i<MaxVisibleSections; i++) in.getShort(); //filler
			in.getInt();
		}
		
		public byte[] save() {
			short NumVisibleSections = (short) relatedChunkIDs.size();
			short MaxVisibleSections = (short) (NumVisibleSections+(NumVisibleSections%2 == 0 ? 6 : 7));
			var bb = ByteBuffer.allocate(MaxVisibleSections*2+24);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(11);
			bb.putInt(11);
			bb.putInt(0);
			bb.putShort(SectionNumber);
			bb.putShort(MostVisibleSections);
			bb.putShort(MaxVisibleSections);
			bb.putShort(NumVisibleSections);
			for (int i=0; i<NumVisibleSections; i++) {
				bb.putShort(relatedChunkIDs.get(i));
			}
			for (int i=NumVisibleSections; i<MaxVisibleSections; i++) bb.putShort((short) 0); //filler
			bb.putInt(0);
			return bb.array();
		}
	}
}
