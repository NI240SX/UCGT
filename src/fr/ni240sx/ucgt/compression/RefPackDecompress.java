package fr.ni240sx.ucgt.compression;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Original code by Rick Gibbed https://github.com/gibbed/Gibbed.RefPack/tree/master
// Ported to Java by NI240SX
public class RefPackDecompress {

	public static boolean stop = true;
	
	//decompression stuff
	public int[] m_prefix = new int[4];
	public ByteBuffer in;
	public ByteBuffer out;
	
	public byte[] decompress(byte[] in) {
		return decompress(ByteBuffer.wrap(in));
	}
	

	public byte[] decompress(ByteBuffer in) {
		return decompress(in, true);
	}

	public byte[] decompress(ByteBuffer in, boolean chunkData) {
		byte[] header = new byte[2];
//		int flags;
		int compressedSize = 0;
		int decompressedSize = 0;

//		System.out.println("RefPackDecompress");
		
		this.in = in;
		
		if(chunkData) {
			//this method is called right after detecting the compressiontype (RFPK magic)
			//these are still part of the chunk's data, before the archive itself, but i guess we can use them ?
			in.order(ByteOrder.LITTLE_ENDIAN);
			/*flags =*/ in.getInt(); //flags or whatever ?
			decompressedSize = in.getInt();
			compressedSize = in.getInt();
		}
		
		// BEGINNING OF REFPACK ARCHIVE
		
		in.order(ByteOrder.BIG_ENDIAN);
		header[0] = in.get();
		header[1] = in.get();
		if ((header[0] & 0x3E)!=16 || header[1]!=-5) { //0x10FB
			System.out.println("[RFPKDecomp] Error : Input is not compressed."); 
			return null;
		} else {
			boolean isLong  = ((header[0] & 0x80) != 0);
			boolean isDoubled = ((header[0] & 0x01) != 0);
			if (isDoubled) System.out.println("[RFPKDecomp] Warning : archive's isDoubled property set to true, this shouldn't happen"); 

			byte[] buf;
			int length;
			if (isLong) {
				in.get(buf = new byte[4]);
				length = (Byte.toUnsignedInt(buf[0]) << 24) | 
						(Byte.toUnsignedInt(buf[1]) << 16) |
						(Byte.toUnsignedInt(buf[2]) << 8) |
						(Byte.toUnsignedInt(buf[3]) << 0);
			} else {
				in.get(buf = new byte[3]);
				length = (Byte.toUnsignedInt(buf[0]) << 16) |
						(Byte.toUnsignedInt(buf[1]) << 8) |
						(Byte.toUnsignedInt(buf[2]) << 0);
			}
			
			if (length != decompressedSize) System.out.println("[RFPKDecomp] Warning : archive size ("+length+") doesn't match announced size ("+decompressedSize+") !");
			
			out = ByteBuffer.allocate(length);
			
			stop = false;
			while (!stop ) {
				decompressionStep();
			}
			if (in.position() != (compressedSize+16)) System.out.println("[RFPKDecomp] Warning : archive compressed size ("+in.position()+") doesn't match announced size ("+(compressedSize+16)+") !");
			
			return out.array();
		}
	}
	
	public void decompressionStep() {
		/* read one byte from compressed stream */
		m_prefix[0] = Byte.toUnsignedInt(in.get()); 
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

	private void copyShort() {
		/* read one more byte from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 3..0x0A */
		/* dst_offset    ~ 1..0x400 */
		int plainSize = m_prefix[0] & 3;
		int copySize = ((m_prefix[0] & 0x1C) >>> 2) + 3;
//		int num_dst_bytes = ((m_prefix[0] >>> 2) & 0x07) + 3;
		int copyOffset = (((m_prefix[0] & 0x60) << 3) | m_prefix[1]) + 1;

//		System.out.println("copyShort | num_src_bytes="+plainSize+", num_dst_bytes="+copySize+", dst_offset="+copyOffset);
		
		pump(plainSize);
		copy(copyOffset, copySize);
	}

	private void copyMedium() {
		/* read two more bytes from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());
		m_prefix[2] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 4..0x43 */
		/* dst_offset    ~ 1..0x4000 */
		int plainSize = m_prefix[1] >>> 6;
		int copySize = (m_prefix[0] & 0x3F) + 4;
		int copyOffset = (((m_prefix[1] & 0x3F) << 8) | m_prefix[2]) + 1;

//		System.out.println("copyMedium | num_src_bytes="+plainSize+", num_dst_bytes="+copySize+", dst_offset="+copyOffset);
		
		pump(plainSize);
		copy(copyOffset, copySize);
	}

	private void copyLong() {
		/* read three more bytes from compressed stream */
		m_prefix[1] = Byte.toUnsignedInt(in.get());
		m_prefix[2] = Byte.toUnsignedInt(in.get());
		m_prefix[3] = Byte.toUnsignedInt(in.get());

		/* num_src_bytes ~ 0..3 */
		/* num_dst_bytes ~ 5..0x404 */
		/* dst_offset    ~ 1..0x20000 */
		int plainSize = m_prefix[0] & 3;
		int copySize = (((m_prefix[0] & 0x0C) << 6) | m_prefix[3]) + 5;
		int copyOffset = (((((m_prefix[0] & 0x10) << 4) | m_prefix[1]) << 8) | m_prefix[2]) + 1;
//		int copyOffset = ((m_prefix[0] & 0x10) << 12) + (m_prefix[1] << 8) + m_prefix[2] + 1;

//		System.out.println("copyLong | num_src_bytes="+plainSize+", num_dst_bytes="+copySize+", dst_offset="+copyOffset);
		
		pump(plainSize);
		copy(copyOffset, copySize);
	}

	private void immediateBytesLong() {
		/* num_src_bytes ~ 4..0x70 step 4 */
		int plainSize = ((m_prefix[0] & 0x1F) + 1) * 4;

//		System.out.println("immediateBytesLong | num_src_bytes="+plainSize);
		
		pump(plainSize);
	}

	private void immediateBytesAndFinish() {
		/* num_src_bytes ~ 0..3 and finish */
		int plainSize = m_prefix[0] & 3;

//		System.out.println("immediateBytesAndFinish | num_src_bytes="+plainSize);

		pump(plainSize);
		stop = true;

		assert(out.position() == out.capacity());
	}
	
	private void copy(int offset, int length) {

		int positionOut = out.position();
		
		for (int i = 0; i < length; i++)
        {
			out.put(positionOut + i, out.get((positionOut - offset) + i));
        }
		out.position(positionOut + length);
		
	}
	
	private void pump(int length) {

		for (int i=0; i<length; i++) {
			out.put(in.get());
		}
	}
}
