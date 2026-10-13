package fr.ni240sx.ucgt.streamEditor.regionBlocks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.shared.Block;
import fr.ni240sx.ucgt.shared.BlockType;
import static fr.ni240sx.ucgt.geometryFile.part.mesh.Vertex.*;

public class TrackStreamingSections extends ArrayList<fr.ni240sx.ucgt.streamEditor.regionBlocks.TrackStreamingSections.TrackStreamingSection> implements Block {

	private static final long serialVersionUID = 554251508300413045L;
	public static final int infoLength = 92;
//	public ArrayList<TrackStreamingSection> trackStreamingSections = new ArrayList<>();
	
	@Override
	public BlockType getBlockID() {
		return BlockType.ChunksOffsets;
	}

	public TrackStreamingSections(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();
		while (in.position()<blockStart+blockLength) {
			this.add(new TrackStreamingSection(in));
		}
		in.position(blockStart+blockLength);
	}
	
	public void update(String blockID, int deltaLength) {
		int i = 0;
		for (i = 0; i < this.size(); i++) {
			if (this.get(i).SectionName.equals(blockID)) {
				this.get(i).Size += deltaLength;
				this.get(i).CompressedSize += deltaLength;
				break;
			}
		}
		for (int j = i+1; j < this.size(); j++) {
			this.get(j).FileOffset += deltaLength;
		}
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		var bb = ByteBuffer.wrap(new byte[infoLength * this.size() + 8]);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		bb.putInt(getBlockID().getKey());
		bb.putInt(infoLength * this.size());
		
		for (var i : this) {
			i.save(bb);
		}
		return bb.array();
	}
	/*
	 * each block data consists of :
	 * name					ID						offset	length		length	stuff										hash probably
	 * X0					23000							362032		362032	66696	
	 * 58300000 00000000 d8590000 00000000 01000000 00000000 30860500 30860500 88040100 01000000 00000000 00000000 00000000 04e7968a 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * Y0
	 * 59300000 00000000 c05d0000 00000000 01000000 00503300 b0fd1200 b0fd1200 08060100 03000000 00000000 00000000 00000000 d1744b64 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * Y112
	 * 59313132 00000000 305e0000 00000000 01000000 00102405 30ad0000 30ad0000 88010000 73000000 00000000 00000000 00000000 96f84124 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
	 * and we just need to adjust the length for a given block
	 * this can be kept in memory (list of UnknownBlock with byte array storage, except block 10410300 with objects)
	 * more blocks follow, apparently we don't need to edit them
	 */
	public static class TrackStreamingSection{
		
		public String SectionName; //eg X0, on 8 bytes
		public short SectionNumber; //23000 (25000 Z, 24000 Y, 23000 X, 22000 W, 20000 U, 0 A, 1000 B, 2000 C, 3000 D, 4000 E, 5000 F)
//		public int Status = 0; //0
		public int FileType = 1; //1
		public int FileOffset; //whatever
		public int oldOffset;
		public int Size;
		public int CompressedSize;
		public int PermSize;
		public int SectionPriority;
		public float[] Centre = new float[2];
		public float Radius = 0;
		public int Checksum;
		
		byte[] otherData = new byte[36]; //runtime stuff and pointers
		
		public TrackStreamingSection(ByteBuffer in) {
			var beginning = in.position();
			SectionName = Block.readString(in);
			in.position(beginning+8);
			SectionNumber = in.getShort();
			in.get(); //WasRendered
			in.get(); //CurrentlyVisible
			in.getInt(); //Status	UNLOADED	ALLOCATED	LOADING	LOADED	ACTIVATED
			FileType = in.getInt(); //FileType	PERMANENT	REGION
			FileOffset = in.getInt();
			oldOffset = FileOffset;
			Size = in.getInt();
			CompressedSize = in.getInt();
			PermSize = in.getInt();
			SectionPriority = in.getInt();
			Centre[X] = in.getFloat();
			Centre[Y] = in.getFloat();
			Radius = in.getFloat();
			Checksum = in.getInt();
			in.get(otherData);
		}

		public void save(ByteBuffer bb) {
			var beginning = bb.position();
			Block.putString(bb, SectionName, 8);
			bb.position(beginning+8);
			bb.putShort(SectionNumber);
			bb.put((byte)0);
			bb.put((byte)0);
			bb.putInt(0);
			bb.putInt(FileType);
			bb.putInt(FileOffset);
			bb.putInt(Size);
			bb.putInt(CompressedSize);
			bb.putInt(PermSize);
			bb.putInt(SectionPriority);
			bb.putFloat(Centre[X]);
			bb.putFloat(Centre[Y]);
			bb.putFloat(Radius);
			bb.putInt(Checksum);
			bb.put(otherData);
		}

		public boolean isScenery() {
			return SectionNumber < 20000;
		}
	}
}
