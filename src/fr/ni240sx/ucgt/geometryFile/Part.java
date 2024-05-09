package fr.ni240sx.ucgt.geometryFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.binstuff.Hash;
import fr.ni240sx.ucgt.compression.Compression;
import fr.ni240sx.ucgt.compression.CompressionLevel;
import fr.ni240sx.ucgt.geometryFile.geometry.*;
import fr.ni240sx.ucgt.testing.GeomDump;

public class Part extends Block {

	public GeomBlock getBlockID() {return GeomBlock.Part;}
	
	public int partKey;
	
	public int decompressedLength;
	public int compressedLength;
	
	public byte[] compressedData;
	
	public Part(ByteBuffer in, int partKey) {
		this.partKey = partKey;		
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getInt(); //ID
		var blockLength = in.getInt();
		var blockStart = in.position();
		
		Block b;

		while((in.position() < blockStart+blockLength)) {
			subBlocks.add(b=Block.read(in));
		}
		
		//TODO read blocks properly and find partName
	}
	
	@Override
	public byte[] save() throws IOException {
		var out = new ByteArrayOutputStream();

		var buf = ByteBuffer.wrap(new byte[8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey());
		buf.putInt(-1); //length for later

		out.write(buf.array());
		
		for (var b : subBlocks) {
			if (b != null) out.write(b.save());
		}

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
	
	public void precompress() throws IOException {
		
		//TODO check partKey according to the partName
		
		var partBytes = this.save();
		decompressedLength = partBytes.length;
		
		compressedData  = Compression.compress(partBytes, "RFPK", CompressionLevel.Minimum);
		compressedLength = compressedData.length + 24;
	}
}
