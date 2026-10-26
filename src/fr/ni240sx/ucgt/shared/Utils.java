package fr.ni240sx.ucgt.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils {

	public static String readStringAligned(ByteBuffer bb) {
		var s = Utils.readString(bb);
		while (bb.position()%4 != 0) bb.get();
		return s;
	}

	public static String readString(ByteBuffer bb) {
		byte[] stringBytesOversize = new byte[128];
		byte b;
		int i = 0;
		while ((b=bb.get())!=0) {
			stringBytesOversize[i] = b;
			i++;
		}
		return new String(Arrays.copyOf(stringBytesOversize, i));
	}

	public static void putString(ByteBuffer bb, String s) {
		Utils.putString(bb, s, Integer.MAX_VALUE);
	}

	public static void putString(ByteArrayOutputStream bb, String s) throws IOException {
		Utils.putString(bb, s, Integer.MAX_VALUE);
	}

	/**
	 * @param bb - ByteBuffer to use
	 * @param s - String to add
	 * @param maxSize - Maximum string length
	 */
	public static void putString(ByteBuffer bb, String s, int maxSize) {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize-2) {
				break;
			}
			bb.put((byte)s.charAt(i));
		}
		bb.put((byte)0);
		//4-byte alignment
		if (bb.position()%4 != 0) bb.put(new byte[4 - bb.position()%4]);
	}

	public static void putString(ByteArrayOutputStream bb, String s, int maxSize) throws IOException {
		
		for (int i=0; i<s.length(); i++) {
			if (i>maxSize-1) {
				System.out.println("[WARN] String too long !");
				break;
			}
			bb.write((byte)s.charAt(i));
		}
		bb.write((byte)0);
		//4-byte alignment
		if (bb.size()%4 != 0) bb.write(new byte[4 - bb.size()%4]);
	}

	public static int stringLengthAligned(String s) {
		if ((s.length()+1)%4 == 0) return s.length()+1;
		return s.length() + 5 - (s.length()+1)%4;
	}

	public static void makeAlignment(ByteBuffer bb, int length, byte pattern) {
		for (int i=0; i<length; i++) {
			bb.put(pattern);
		}
	}

	public static void skipAlignment(ByteBuffer bb, int alignment) {
		while (bb.position()%alignment != 0) {
			bb.get();
		}
	}

	public static void skipAlignment(ByteArrayOutputStream bos, int modulo, int pattern) {
		while (bos.size()%modulo != 0) {
			bos.write(pattern);
		}
	}

	public static int findAlignment(int position, int alignment) {
		if (position%alignment == 0) return 0;
		return alignment-position%alignment;
	}

	public static void makeAlignment(ByteArrayOutputStream out, int length, byte pattern) {
		for (int i=0; i<length; i++) {
		out.write(pattern);
		}
	}

}
