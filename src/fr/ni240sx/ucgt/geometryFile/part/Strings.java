package fr.ni240sx.ucgt.geometryFile.part;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class Strings extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part_Strings;}

	// texture binhash, usage type
	public ArrayList<String> strings = new ArrayList<String>();
	
	public Strings(ByteBuffer in) {
		var blockLength = in.getInt();
		var blockStart = in.position();

		var numStrings = in.getInt();
		var stringOffsets = new int[numStrings];
		for (int i=0; i< numStrings; i++) {
			stringOffsets[i] = in.getInt();
		}
		var stringsBeginning = in.position();
		for (int i=0; i< numStrings; i++) {
			in.position(stringsBeginning+stringOffsets[i]);
			strings.add(Block.readString(in));
		}
		in.position(blockStart+blockLength);
	}

	public Strings() {
	}

	@Override
	public byte[] save(int currentPosition) throws IOException, InterruptedException {
		
		//calculate size
		var blockLength = 4 + strings.size()*4;
		for (var s : strings) blockLength += Block.stringLengthAligned(s);

		var out = ByteBuffer.wrap(new byte[blockLength+8]);
		out.order(ByteOrder.LITTLE_ENDIAN);

		out.putInt(getBlockID().getKey());
		out.putInt(blockLength);

		var blockStart = out.position();
		out.putInt(strings.size());
		var stringOffsets = new int[strings.size()];
		out.position(out.position()+strings.size()*4); //skip offsets

		for (int i = 0; i<strings.size(); i++) {
			stringOffsets[i] = out.position() - blockStart - 4 - strings.size()*4;
			Block.putString(out, strings.get(i));
		}
		
		out.position(blockStart+4);
		for (int i = 0; i<strings.size(); i++) {
			out.putInt(stringOffsets[i]);
		}
		//TODO 3 bytes worth of data get erased in AUD_RS4_STK_08_KIT00_BASE_A (0xEE907C), are they important ?
		
		out.position(blockStart+blockLength);
		return out.array();	
	}
}