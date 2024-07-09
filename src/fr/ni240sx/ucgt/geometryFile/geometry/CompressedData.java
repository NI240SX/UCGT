package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.GeomBlock;

public class CompressedData extends Block {

	@Override
	public GeomBlock getBlockID() {return GeomBlock.CompressedData;}

	public int decompressionOffset;
	public int decompressedLength;
	public int supplementaryChunkOffset;
	
	public CompressedData(ByteBuffer in) {
		decompressedLength = in.getInt();
		var length = in.getInt();
		decompressionOffset = in.getInt(); //probably something slightly different ?
		supplementaryChunkOffset = in.getInt(); // length of previous CompressedData blocks belonging to the same part aka supplementary offset from the PartOffset's one
		in.getInt(); //0
		data = new byte[length-24];
		in.get(data); //raw compressed data, ready to be used by the Compression class
	}
	
	public CompressedData(byte[] data, int decompressedLength, int decompressionOffset, int supplementaryChunkOffset) {
		this.data = data;
		this.decompressionOffset = decompressionOffset;
		this.decompressedLength = decompressedLength;
		this.supplementaryChunkOffset = supplementaryChunkOffset;
	}

	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [data.length+24];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(decompressedLength);
		buf.putInt(data.length+24); //length for later
		buf.putInt(decompressionOffset);
		buf.putInt(supplementaryChunkOffset);
		buf.putInt(0);
		buf.put(data);
		
		return buf.array();
	}

}
