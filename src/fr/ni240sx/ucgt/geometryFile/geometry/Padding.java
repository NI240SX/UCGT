package fr.ni240sx.ucgt.geometryFile.geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.ni240sx.ucgt.binstuff.Block;
import fr.ni240sx.ucgt.geometryFile.BlockType;
import fr.ni240sx.ucgt.geometryFile.GeometryEditorCLI;

public class Padding extends Block {
	
	@Override
	public BlockType getBlockID() {return BlockType.Padding;}
	public static final int PaddingModulo = 128;
	
	public int length = 0;
	
	public Padding(ByteBuffer in) {
		length = in.getInt();
		var blockStart = in.position();
		in.position(blockStart+length);
	}
	
	@Override
	public byte[] save(int currentPosition) throws IOException {
		
		var arr = new byte [length+8];
		
		var buf = ByteBuffer.wrap(arr);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(getBlockID().getKey()); 
		buf.putInt(length); //length for later

		return buf.array();
	}

	public static void makePadding(ByteArrayOutputStream out) throws IOException {
		int paddingLength = (PaddingModulo - ((out.size() + 8) % PaddingModulo))%PaddingModulo;
		
		var buf = ByteBuffer.wrap(new byte[paddingLength + 8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(BlockType.Padding.getKey());
		buf.putInt(paddingLength);

		out.write(buf.array());
	}
	
	public static byte[] makePadding(long pos, int modulo) {
		int paddingLength = (int) ((modulo - ((pos + 8) % modulo))%modulo);
		
		var buf = ByteBuffer.wrap(new byte[paddingLength + 8]);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(BlockType.Padding.getKey());
		buf.putInt(paddingLength);
		if (paddingLength>36) Block.putString(buf, "Edited with UCGT v"+GeometryEditorCLI.programVersion+" | needeka", paddingLength);		
		return buf.array();
	}
}
