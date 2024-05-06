package fr.ni240sx.ucgt.compression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JDLZDecompress {
	public static byte[] decompress(ByteBuffer input )
	{
		int flags1 = 1, flags2 = 1;
		int t, length;
//		int inPos = 16, outPos = 0;

		System.out.println("JDLZDecompress");
		
		//called right after the header
//		if( input[0] != 'J' || input[1] != 'D' || input[2] != 'L' || input[3] != 'Z' || input[4] != 0x02 )
//		{
//			System.out.println("[JDLZDecomp] Error : Input is not compressed.");
////			throw new InvalidDataException( "Input not JDLZ!" );
//		}

		input.getInt(); //input[4] to input[7]

		input.order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer output = ByteBuffer.wrap(new byte[input.getInt()]);	//[( input[11] << 24 ) + ( input[10] << 16 ) + ( input[9] << 8 ) + input[8]];

		input.getInt();

		while (input.hasRemaining() && output.hasRemaining())
		{
			if( flags1 == 1 )
			{
				flags1 = Byte.toUnsignedInt(input.get()) | 0x00000100;
			}
			if( flags2 == 1 )
			{
				flags2 = Byte.toUnsignedInt(input.get()) | 0x00000100;
			}

			if( ( flags1 & 1 ) == 1 )
			{
				if( ( flags2 & 1 ) == 1 ) // 3 to 4098(?) iterations, backtracks 1 to 16(?) bytes
				{
					// length max is 4098(?) (0x1002), assuming input[inPos] and input[inPos + 1] are both 0xFF
					length = ( Byte.toUnsignedInt(input.get(input.position()+1)) | ( ( Byte.toUnsignedInt(input.get(input.position())) & Byte.toUnsignedInt((byte) 0xF0) ) << 4 ) ) + 3;
					// t max is 16(?) (0x10), assuming input[inPos] is 0xFF
					t = ( Byte.toUnsignedInt(input.get(input.position())) & Byte.toUnsignedInt((byte) 0x0F) ) + 1;
				}
				else // 3(?) to 34(?) iterations, backtracks 17(?) to 2064(?) bytes
				{
					// t max is 2064(?) (0x810), assuming input[inPos] and input[inPos + 1] are both 0xFF
					t = ( Byte.toUnsignedInt(input.get(input.position()+1)) | ( ( Byte.toUnsignedInt(input.get(input.position())) & Byte.toUnsignedInt((byte) 0xE0) ) << 3 ) ) + 17;
					// length max is 34(?) (0x22), assuming input[inPos] is 0xFF
					length = ( Byte.toUnsignedInt(input.get(input.position())) & Byte.toUnsignedInt((byte) 0x1F) ) + 3;
				}

				input.position(input.position()+2);

				
				for( int i = 0; i < length; ++i )
				{
					output.put(output.get(output.position()-t));
//					output[outPos + i] = output[outPos + i - t];
				}

//				outPos += length;
				flags2 >>>= 1;
			}
			else
			{
				if( output.position() < output.capacity())
				{
//					output[outPos++] = input[inPos++];
					output.put(input.get());
				}
			}
			flags1 >>>= 1;
					
					

			FileOutputStream fos;
			try {
				fos = new FileOutputStream(new File("C:\\Users\\gaupp\\OneDrive\\Documents\\quickbms\\files\\ctk_NIS_240_SX_89_KIT00_BRAKELIGHT_GLASS_RIGHT_A_decomp"));
				fos.write(output.array());	
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return output.array();
	}
}
