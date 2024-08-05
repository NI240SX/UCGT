package fr.ni240sx.ucgt.compression;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Compression {

	public static byte[] decompress(byte[] arr){
		ByteBuffer in = ByteBuffer.wrap(arr);
		CompressionType compressionType = CompressionType.get(String.valueOf(new char[] {(char)(in.get()), (char)(in.get()), (char)(in.get()), (char)(in.get())}));
//		System.out.println("Compression type : "+compressionType);
		
		switch(compressionType) {
		case RefPack:
			return new RefPackDecompress().decompress(in);
		case JDLZ:
			return JDLZDecompress.decompress(in);
//		case HUFF:
//			return HUFFDecompress.decompress(in); 
		case RawDecompressed:
			// raw data
			return Arrays.copyOfRange(arr, 16, arr.length);
		default:
			System.out.println("Compression type not supported.");	
			return null;
		}
	}
	
	public static byte[] compress(byte[] arr, CompressionType compressionType){
		return compress(arr, compressionType, CompressionLevel.Ultra);
	}

	public static byte[] compress(byte[] arr, CompressionType compressionType, CompressionLevel compressionLevel){
//		System.out.println("Compression type : "+compressionType);
		
		switch(compressionType) {
		case RefPack:
			return new RefPackCompress().compress(ByteBuffer.wrap(arr), compressionLevel);
		case JDLZ:
			return JDLZCompress.compress(arr);
		case RawDecompressed:
			var bb = ByteBuffer.allocate(arr.length+16);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put((byte) 0x52); bb.put((byte) 0x41); bb.put((byte) 0x57); bb.put((byte) 0x57);
			bb.putInt(4097);
			bb.putInt(arr.length);
			bb.putInt(arr.length);
			bb.put(arr);
			return bb.array();
			
		default:
			System.out.println("Compression type not supported.");	
			return null;
		}
	}
}
