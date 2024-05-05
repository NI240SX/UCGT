package fr.ni240sx.ucgt.compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RefPackDecompress {

	//stream stuff
	public static boolean BUFFER = true;
	public static int cBufferLength = 2 * 0x20000;
	public static long m_pos = 0;
	public static byte m_buf[] = new byte[cBufferLength];

	public static boolean stop = true;
	
	//decompression stuff
	public static int[] m_prefix = new int[4];
	public static ByteBuffer in;
	public static ByteBuffer out;
	
	public static byte[] decompress(byte[] in) throws IOException, InterruptedException {
		return decompress(ByteBuffer.wrap(in));
	}
	
	public static byte[] decompress(ByteBuffer in) throws IOException, InterruptedException {
		byte[] header = new byte[2];
		int flags;
		int compressedSize;
		int decompressedSize;
		
		
		System.out.println("decompressing RefPack");
		
		RefPackDecompress.in = in;
		
		//this method is called right after detecting the compressiontype (RFPK magic)
		in.order(ByteOrder.LITTLE_ENDIAN);
		flags = in.getInt(); //flags
		decompressedSize = in.getInt();
		compressedSize = in.getInt();

		
		//REFPACK HEADER
		
		in.order(ByteOrder.BIG_ENDIAN);
		header[0] = in.get();
		header[1] = in.get();
		if ((header[0] & 0x3E)!=16 || header[1]!=-5) { //0x10FB
			System.out.println("Not compressed."); 
			return null;
		} else {
			boolean isLong  = ((header[0] & 0x80) != 0);
			boolean hasMore = ((header[0] & 0x01) != 0);
			
			int length;
//			byte[] buf = new byte[(isLong ? 4 : 3) * (hasMore ? 2 : 1)];			
//			in.get(buf);
//			length = (((buf[0] << 8) + buf[1]) << 8) + buf[2]; //doesnt work in Java
			
			in.get(); // size is stored as 24byte int but never reaches over 16bytes (blocks are 32768B long max) so this byte can be skipped
//			length = 0x0000FFFF & (int)in.getShort(); //for unsigned
			length = Short.toUnsignedInt(in.getShort());
			if (length != decompressedSize) System.out.println("[RFPKDecomp] Warning : archive size ("+length+") doesn't match announced size ("+decompressedSize+") !");
			
			out = ByteBuffer.allocate(decompressedSize);
			
			stop = false;
			while (!stop ) {
				decompressionStep();
				FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\AUD_RS4_STK_08_KIT00_BRAKELIGHT_GLASS_LEFT_A_decomp"));
				fos.write(out.array());
//				Thread.sleep(300);
			}
			
			return out.array();
		}
	}
	
	public static void decompressionStep() {
		/* read one byte from compressed stream */
		m_prefix[0] = Byte.toUnsignedInt(in.get()); //TODO if something breaks, might be a signed/unsigned stuff issue
		if (m_prefix[0] >= 0xC0) {
			if (m_prefix[0] >= 0xE0) {
				/* 0xE0..0xFF */
				if (m_prefix[0] >= 0xFC) {
					/* 0xFC..0xFF */
					immediateBytesAndFinish();
				} else {
					/* 0xE0..0xFB */
					immediateBytesLong();
				}
			} else {
				/* 0xC0..0xDF */
				copyLong();
			}
		} else {
			if (m_prefix[0] >= 0x80) {
				/* 0x80..0xBF */
				copyMedium();
			} else {
				/* 0x00..0x7F */
				copyShort();
			}
		}
	}

	private static void copyShort() {
		/* read one more byte from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 3..0x0A */
		/* dst_offset    ~ 1..0x400 */
		int num_src_bytes = m_prefix[0] & 3;
//		int num_dst_bytes = ((m_prefix[0] & 0x1C) >>> 2) + 3;
		int num_dst_bytes = ((m_prefix[0] >>> 2) & 0x07) + 3;
		int dst_offset = (((m_prefix[0] & 0x60) << 3) | m_prefix[1]) + 1;

		System.out.println("copyShort | num_src_bytes="+num_src_bytes+", num_dst_bytes="+num_dst_bytes+", dst_offset="+dst_offset);
		
		pump(num_src_bytes);
		copy(dst_offset, num_dst_bytes, num_src_bytes); //i have no clue how wrong this is
//		m_dst.Copy(*m_ei, dst_offset, num_dst_bytes);
	}

	private static void copyMedium() {
		/* read two more bytes from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());
		m_prefix[2] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 4..0x43 */
		/* dst_offset    ~ 1..0x4000 */
		int num_src_bytes = m_prefix[1] >>> 6;
		int num_dst_bytes = (m_prefix[0] & 0x3F) + 4;
		int dst_offset = (((m_prefix[1] & 0x3F) << 8) | m_prefix[2]) + 1;

		System.out.println("copyMedium | num_src_bytes="+num_src_bytes+", num_dst_bytes="+num_dst_bytes+", dst_offset="+dst_offset);
		
		pump(num_src_bytes);
		copy(dst_offset, num_dst_bytes, num_src_bytes);
//		m_dst.Pump(*m_ei, m_src, num_src_bytes);
//		m_dst.Copy(*m_ei, dst_offset, num_dst_bytes);
	}

	private static void copyLong() {
		/* read three more bytes from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());
		m_prefix[2] = Byte.toUnsignedInt(in.get());
		m_prefix[3] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 5..0x404 */
		/* dst_offset    ~ 1..0x20000 */
		int num_src_bytes = m_prefix[0] & 3;
		int num_dst_bytes = (((m_prefix[0] & 0x0C) << 6) | m_prefix[3]) + 5;
//		int dst_offset = (((((m_prefix[0] & 0x10) << 4) | m_prefix[1]) << 8) | m_prefix[2]) + 1;
		int dst_offset = ((m_prefix[0] & 0x10) << 12) + (m_prefix[1] << 8) + m_prefix[2] + 1;

		System.out.println("copyLong | num_src_bytes="+num_src_bytes+", num_dst_bytes="+num_dst_bytes+", dst_offset="+dst_offset);
		
		pump(num_src_bytes);
		copy(dst_offset, num_dst_bytes, num_src_bytes);
	}

	private static void immediateBytesLong() {
		/* num_src_bytes ~ 4..0x70 step 4 */
		int num_src_bytes = ((m_prefix[0] & 0x1F) + 1) * 4;

		System.out.println("immediateBytesLong | num_src_bytes="+num_src_bytes);
		
		pump(num_src_bytes);
	}

	private static void immediateBytesAndFinish() {
		/* num_src_bytes ~ 0..3 and finish */
		int num_src_bytes = m_prefix[0] & 3;

		System.out.println("immediateBytesAndFinish | num_src_bytes="+num_src_bytes);

		pump(num_src_bytes);
		stop = true;

		assert(out.position() == out.capacity());
	}
	
	private static void copy(int offset, int length, int num_src_bytes) {
		if(!BUFFER) {
			
		// WITHOUT BUFFER
		//					dst_offset, num_dst_bytes
		assert(offset < in.position());
		int positionIn = in.position();
		int positionOut = out.position();
		in.position(in.position() + offset); //what the fuck am i supposed to do
//		out.position(out.position() - offset + length);
//		in.position((int)(in.position() - offset) % in.capacity());
		for (int i=0; i<length; i++) {
			out.put(in.get(positionIn - offset));
//			in.get();
		}
		in.position(positionIn);
		out.position(positionOut + length);
		
		}else {
			
		// WITH BUFFER
		assert(offset < m_pos);
		int idx = (int)((m_pos - offset) % cBufferLength);
//		byte *from = m_buf + (m_pos - offset) % cBufferLength;
//		byte *to = m_buf + m_pos % cBufferLength;
//		for (uint32 i = 0; i < length; i++) {
//			*(to++) = *(from++);
//			if (from >= endof(m_buf)) from = m_buf;
//			if (to   >= endof(m_buf)) to   = m_buf;
//		}

		
		if (idx + length <= cBufferLength) {
//			super::Write(ei, m_buf + idx, length);
			out.put(m_buf, idx, length);

			
//			for (int i=0; i<length; i++) {
//				if (num_src_bytes != 0) out.put(m_buf, idx+(i%num_src_bytes), 1);
//				else out.put(m_buf, idx+i, 1);
//			}
			
			
			System.out.print("copy "+length+" | data=");
			try {System.out.print(new String(Arrays.copyOfRange(m_buf, idx, idx+length), "ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {}
			System.out.println();
		} else {
			int length_1 = cBufferLength - idx;
			int length_2 = length - length_1;
			out.put(m_buf, idx, length_1);
			out.put(m_buf, 0, length_2);

//			for (int i=0; i<length; i++) {
//				if (num_src_bytes != 0) out.put(m_buf, idx+(i%num_src_bytes), 1);
//				else out.put(m_buf, idx+i, 1);
//			}

			System.out.print("copy "+length+" | data=");
			try {System.out.print(new String(Arrays.copyOfRange(m_buf, idx, idx+length_1), "ISO-8859-1"));
			System.out.print(new String(Arrays.copyOfRange(m_buf, 0, length_2), "ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {}
			System.out.println();
//			super::Write(ei, m_buf + idx, length_1);
//			super::Write(ei, m_buf, length - length_1);
		}
		
		}
		m_pos += length;
	}
	
	private static void pump(int length) {
		if(!BUFFER) {
		// WITHOUT BUFFER
		for (int i=0; i<length; i++) {
			out.put(in.get());
		}
		
		} else {
			
		// WITH BUFFER
		int idx = (int)m_pos % cBufferLength;
//		byte *buf = m_buf + idx; //not sure i can do the same in java
		if (idx + length <= cBufferLength) {
			in.get(m_buf, idx, length);
			out.put(m_buf, idx, length); //i'm guessing that's it ?
//			src.Read(ei, buf, length);
//			super::Write(ei, buf, length);
			
			System.out.print("pump "+length+" | data=");
			try {System.out.print(new String(Arrays.copyOfRange(m_buf, idx, idx+length), "ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {}
			System.out.println();
		} else {
			int length_1 = cBufferLength - idx;
			int length_2 = length - length_1;
			in.get(m_buf, idx, length_1);
			out.put(m_buf, idx, length_1);
			in.get(m_buf, 0, length_2);
			out.put(m_buf, 0, length_2);

			System.out.print("pump "+length+" | data=");
			try {System.out.print(new String(Arrays.copyOfRange(m_buf, idx, idx+length_1), "ISO-8859-1"));
			System.out.print(new String(Arrays.copyOfRange(m_buf, 0, length_2), "ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {}
			System.out.println();
//			src.Read(ei, buf, length_1);
//			super::Write(ei, buf, length_1);
//			src.Read(ei, m_buf, length_2);
//			super::Write(ei, m_buf, length_2);
		}
		m_pos += length;

		
		}
	}
}
