package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

public abstract class ZModBlock {
	
	public int UID;
	public int numDecl = 1;
	
	public ZModBlock(int ID) {
		this.UID = ID;
	}

	public ZModBlock() {
		this.UID = ZModelerZ3D.createUID();
	}
	
	public abstract String getName();
	
	public abstract void readData(ByteBuffer in) throws Exception;
	
	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[bb.getInt()];
		byte b;
		int i = 0;
		while ((b=bb.get())!=0) {
			stringBytesOversize[i] = b;
			i++;
		}
		return new String(Arrays.copyOf(stringBytesOversize, i));
	}

	public static void putString(ByteBuffer bb, String s) {
		bb.putInt(s.length()+1);
		bb.put(s.getBytes(StandardCharsets.ISO_8859_1));
		bb.put((byte) 0);
	}

	public static int getNumDecl(List<ZModBlock> list) {
		int total = 0;
		for (var b : list) total += b.numDecl;
		return total;
	}
	
	public abstract void writeDeclaration(OutputStream fos) throws IOException;

	public abstract void writeData(OutputStream fos) throws IOException;
}
