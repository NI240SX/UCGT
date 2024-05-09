package fr.ni240sx.ucgt.compression;

import java.util.Arrays;

// original code in OpenNFSTools, https://github.com/MWisBest/OpenNFSTools
// ported to Java by NI240SX

//TODO BROKEN, may have some unsigned/signed issues going on

public class JDLZCompress {
	
	public static final int HEADER_SIZE = 16;
	
	public static byte[] compress (byte[] input) {
		return compress(input, 0x2000, 1024); // not as effective as RefPack but much much faster
	}
	
	public static byte[] compress( byte[] input, int hashSize, int maxSearchDepth){

		final int MinMatchLength = 3;

		int inputBytes = input.length;
		byte[] output = new byte[inputBytes + ( ( inputBytes + 7 ) / 8 ) + HEADER_SIZE + 1];
		int[] hashPos = new int[hashSize];
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
		output[outPos++] = (byte)( inputBytes >>> 8 );
		output[outPos++] = (byte)( inputBytes >>> 16 );
		output[outPos++] = (byte)( inputBytes >>> 24 );
		outPos += 4;

		int flags1Pos = outPos++;
		int flags2Pos = outPos++;

		flags1bit <<= 1;
		output[outPos++] = input[inPos++];
		inputBytes--;

		while(inputBytes > 0)
		{
			int bestMatchLength = MinMatchLength - 1;
			int bestMatchDist = 0;

			if( inputBytes >= MinMatchLength )
			{
				int hash = ( -0x1A1 * ( input[inPos] ^ ( ( input[inPos + 1] ^ ( input[inPos + 2] << 4 ) ) << 4 ) ) ) & (hashSize - 1);
				int matchPos = hashPos[hash];
				hashPos[hash] = inPos;
				hashChain[inPos] = matchPos;
				int prevMatchPos = inPos;

				for( int i = 0; i < maxSearchDepth; i++ )
				{
					int matchDist = inPos - matchPos;

					if( matchDist > 2064 || matchPos >= prevMatchPos )
					{
						break;
					}

					int matchLengthLimit = matchDist <= 16 ? 4098 : 34;
					int maxMatchLength = inputBytes;

					if( maxMatchLength > matchLengthLimit )
					{
						maxMatchLength = matchLengthLimit;
					}
					if( bestMatchLength >= maxMatchLength )
					{
						break;
					}

					int matchLength = 0;
					while( ( matchLength < maxMatchLength ) && ( input[inPos + matchLength] == input[matchPos + matchLength] ) )
					{
						matchLength++;
					}

					if( matchLength > bestMatchLength )
					{
						bestMatchLength = matchLength;
						bestMatchDist = matchDist;
					}

					prevMatchPos = matchPos;
					matchPos = hashChain[matchPos];
				}
			}

			if( bestMatchLength >= MinMatchLength )
			{
				flags1 |= flags1bit;
				inPos += bestMatchLength;
				inputBytes -= bestMatchLength;
				bestMatchLength -= MinMatchLength;

				if( bestMatchDist < 17 )
				{
					flags2 |= flags2bit;
					output[outPos++] = (byte)( ( bestMatchDist - 1 ) | ( ( bestMatchLength >>> 4 ) & 0xF0 ) );
					output[outPos++] = (byte)bestMatchLength;
				}
				else
				{
					bestMatchDist -= 17;
					output[outPos++] = (byte)( bestMatchLength | ( ( bestMatchDist >>> 3 ) & 0xE0 ) );
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

			if( flags1bit == 0 )
			{
				output[flags1Pos] = flags1;
				flags1 = 0;
				flags1Pos = outPos++;
				flags1bit = 1;
			}

			if( flags2bit == 0 )
			{
				output[flags2Pos] = flags2;
				flags2 = 0;
				flags2Pos = outPos++;
				flags2bit = 1;
			}
		}

		if( flags2bit > 1 )
		{
			output[flags2Pos] = flags2;
		}
		else if( flags2Pos == outPos - 1 )
		{
			outPos = flags2Pos;
		}

		if( flags1bit > 1 )
		{
			output[flags1Pos] = flags1;
		}
		else if( flags1Pos == outPos - 1 )
		{
			outPos = flags1Pos;
		}

		output[12] = (byte)outPos;
		output[13] = (byte)( outPos >>> 8 );
		output[14] = (byte)( outPos >>> 16 );
		output[15] = (byte)( outPos >>> 24 );

		return Arrays.copyOf(output, outPos);
	}

}
