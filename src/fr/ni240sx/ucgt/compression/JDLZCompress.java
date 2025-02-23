package fr.ni240sx.ucgt.compression;

import java.util.Arrays;

// original code on encode.su, https://encode.su/threads/2417-Creating-A-Compressor-for-JDLZ
// quick Java port by NI240SX

public class JDLZCompress {
	
	public static final int HEADER_SIZE = 16;
	
	//alternative compressor
	public static byte[] compress(byte[] input)
	{
	    if (input == null) return null;

	    final int HeaderSize = 16;
	    final int MinMatchLength = 3;
	    final int MaxSearchDepth = 512;

	    int inputBytes = input.length;
	    byte[] output = new byte[inputBytes + ((inputBytes + 7) / 8 ) + HeaderSize + 1];
	    int[] hashPos = new int[0x2000];
	    int[] hashChain = new int[inputBytes];

	    int outPos = 0;
	    int inPos = 0;
	    byte flags1bit = 1;
	    byte flags2bit = 1;
	    byte flags1 = 0;
	    byte flags2 = 0;

	    output[outPos++] = 0x4A; // 'J'
	    output[outPos++] = 0x44; // 'D'
	    output[outPos++] = 0x4C; // 'L'
	    output[outPos++] = 0x5A; // 'Z'
	    output[outPos++] = 0x02;
	    output[outPos++] = 0x10;
	    output[outPos++] = 0x00;
	    output[outPos++] = 0x00;
	    output[outPos++] = (byte)inputBytes;
	    output[outPos++] = (byte)(inputBytes >>> 8 );
	    output[outPos++] = (byte)(inputBytes >>> 16);
	    output[outPos++] = (byte)(inputBytes >>> 24);
	    outPos += 4;

	    int flags1Pos = outPos++;
	    int flags2Pos = outPos++;

	    flags1bit <<= 1;
	    output[outPos++] = input[inPos++];
	    inputBytes--;

	    while (inputBytes > 0)
	    {
	        int bestMachLength = MinMatchLength - 1;
	        int bestMatchDist = 0;

	        if (inputBytes >= MinMatchLength)
	        {
	            int hash = (-0x1A1 * ((input[inPos]&0xff) ^ (((input[inPos + 1]&0xff) ^ ((input[inPos + 2] << 4)&0xff)) << 4))) & 0x1FFF;
	            int matchPos = hashPos[hash];
	            hashPos[hash] = inPos;
	            hashChain[inPos] = matchPos;
	            int prevMatchPos = inPos;

	            for (int i = 0; i < MaxSearchDepth; i++)
	            {
	                int matchDist = inPos - matchPos;
	                if (matchDist > 2064 || matchPos >= prevMatchPos) break;

	                int matchLengthLimit = matchDist <= 16 ? 4098 : 34;
	                int maxMatchLength = inputBytes;
	                if (maxMatchLength > matchLengthLimit) maxMatchLength = matchLengthLimit;
	                if (bestMachLength >= maxMatchLength) break;

	                int matchLength = 0;
	                while (matchLength < maxMatchLength && input[inPos + matchLength] == input[matchPos + matchLength])
	                    matchLength++;

	                if (matchLength > bestMachLength)
	                {
	                    bestMachLength = matchLength;
	                    bestMatchDist = matchDist;
	                }

	                prevMatchPos = matchPos;
	                matchPos = hashChain[matchPos];
	            }
	        }

	        if (bestMachLength >= MinMatchLength)
	        {
	            flags1 |= flags1bit;
	            inPos += bestMachLength;
	            inputBytes -= bestMachLength;
	            bestMachLength -= MinMatchLength;

	            if (bestMatchDist < 17)
	            {
	                flags2 |= flags2bit;
	                output[outPos++] = (byte)(((bestMatchDist - 1) &0xff) | ((bestMachLength >> 4) & 0xf0));
	                output[outPos++] = (byte)bestMachLength;
	            }
	            else
	            {
	                bestMatchDist -= 17;
	                output[outPos++] = (byte)(bestMachLength | ((bestMatchDist >> 3) & 0xe0));
	                output[outPos++] = (byte)bestMatchDist;
	            }

	            flags2bit <<= 1;
	        }
	        else
	        {
	            output[outPos++] = input[inPos++];
	            inputBytes--;
	        }

	        flags1bit <<= 1;

	        if (flags1bit == 0)
	        {
	            output[flags1Pos] = flags1;
	            flags1 = 0;
	            flags1Pos = outPos++;
	            flags1bit = 1;
	        }

	        if (flags2bit == 0)
	        {
	            output[flags2Pos] = flags2;
	            flags2 = 0;
	            flags2Pos = outPos++;
	            flags2bit = 1;
	        }
	    }

	    if (flags2bit > 1 || flags2bit < 0)
	        output[flags2Pos] = flags2;
	    else if (flags2Pos == outPos - 1)
	        outPos = flags2Pos;

	    if (flags1bit > 1 || flags1bit < 0)
	        output[flags1Pos] = flags1;
	    else if (flags1Pos == outPos - 1)
	        outPos = flags1Pos;

	    output[12] = (byte)outPos;
	    output[13] = (byte)(outPos >>> 8 );
	    output[14] = (byte)(outPos >>> 16);
	    output[15] = (byte)(outPos >>> 24);

	    return Arrays.copyOf(output, outPos);
	}
	
//	public static void main(String[] args) throws Exception {
//		File f = new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\M3 E92 KIT00_BASE_A");
//		var fis = new FileInputStream(f);
//		var arr = new byte[(int) f.length()];
//		fis.read(arr);
//		fis.close();
//
//		File outFile = new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\M3 E92 KIT00_BASE_A.jdlz");
//		var fos = new FileOutputStream(outFile);
//		fos.write(Compression.compress(arr, CompressionType.JDLZ));
//		fos.close();
//		
//
//		File compFile = new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\M3 E92 KIT00_BASE_A.jdlz");
//		var fis2 = new FileInputStream(compFile);
//		var comp = new byte[(int) compFile.length()];
//		fis2.read(comp);
//		fis2.close();
//
//		File redecompFile = new File("C:\\Users\\NI240SX\\Documents\\NFS\\a MUCP\\UCGT\\M3 E92 KIT00_BASE_A-redecomp");
//		var fos2 = new FileOutputStream(redecompFile);
//		fos2.write(Compression.decompress(comp));
//		fos2.close();
//	}
}
